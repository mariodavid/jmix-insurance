# Codex Architecture Review - jmix-insurance

Stand: 2026-06-06

Ziel dieser Review: Das Repository soll als gutes Beispiel fuer Agentic Engineering dienen. Der Massstab ist deshalb nicht nur "funktioniert die App?", sondern:

- Hat das Repo klare Architekturgrenzen, die ein autonomer Coding Agent versteht?
- Gibt es deterministische Guardrails, die falsche Aenderungen schnell blockieren?
- Sind Skills, Docs, Tests und Build-Kommandos so konkret, dass ein Agent nicht raten muss?
- Sind fachliche Cross-Module-Flows gut genug beobachtbar, testbar und erklaerbar?

## Kurzfazit

Die Basis ist stark. Das Repo zeigt bereits eine saubere modulare Jmix-Add-on-Struktur mit API/Core/UI/Starter-Schichten, zentralen ArchUnit-Regeln, Checkstyle, SpotBugs, Spotless, JaCoCo, Test-Fixtures und realistischen Flow-Tests fuer Quote -> Policy -> Account. Fuer eine Agentic-Engineering-Demo ist das ein sehr gutes Fundament.

Die wichtigsten Verbesserungen liegen nicht in einem kompletten Umbau, sondern in der Haertung des Harness:

1. CI ist aktuell als Guardrail nicht verlaesslich, weil `.github/workflows/ci.yml` `./run_all_tests.sh` aufruft, das im Repo nicht existiert.
2. Die dokumentierte Modulgrenze "inter-domain nur ueber API" wird in `partner-ui` durch direkte JPQL-Zugriffe auf `policy_Policy` und `account_Account` durchbrochen.
3. Einige fachliche Invarianten werden durch UI-Zustaende geschuetzt, aber nicht vollstaendig in Services erzwungen, besonders bei `QuoteService.accept()` / `reject()`.
4. Es gibt Schema-/Doc-/Skill-Drift: Java 17 vs 21, dead README-Link, alte Harness-Review, veralteter app-spezifischer Testing-Skill, fehlende Produkt-/Security-Module-READMEs.
5. Logging ist vorhanden, aber noch keine konsistente Observability-Story fuer Business-Flows, Korrelation und Agent-Diagnose.

## Verifikation

Ausgefuehrt:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

Ergebnis: erfolgreich in ca. 20s.

```shell
./gradlew :webapp:test --tests "com.insurance.app.account.AccountServiceTest" --tests "com.insurance.app.account.AccountBalanceWithPolicyTest"
```

Ergebnis: erfolgreich in ca. 15s.

```shell
./gradlew check
```

Ergebnis: erfolgreich in ca. 3m 4s, 380 actionable tasks.

Hinweis: Der Worktree war vor der Review bereits dirty. Ich habe nur dieses Plan-Dokument ergaenzt. Nach den lokalen Checks ist ausserdem ein untracked `graphify-out/` sichtbar; ich habe es nicht veraendert.

## Was Bereits Gut Ist

### Modularer Add-on-Schnitt

Die Domaenen `account`, `partner`, `policy`, `quote`, `product`, `security` sind als Composite Builds organisiert. Die meisten Domaenen folgen dem Muster:

- `<domain>-api`
- `<domain>-api-starter`
- `<domain>-core`
- `<domain>-core-starter`
- `<domain>-ui`
- `<domain>-ui-starter`

Das ist fuer Agentic Engineering wertvoll, weil ein Agent kleine, fachlich begrenzte Aenderungen machen kann und ueber die Schichtnamen bereits viel Kontext bekommt.

### Zentrale ArchUnit-Regeln

`test-support/src/main/java/com/insurance/common/test_support/ArchitectureRules.java` enthaelt bereits zentrale Regeln fuer:

- API-Module duerfen nicht von Core/UI/FlowUI abhaengen.
- Core-Module duerfen keine fremden Core/UI-Implementierungen nutzen.
- Core darf keine Flow UI APIs nutzen.
- Core-Services duerfen nicht von UI-Klassen abhaengen.
- Jmix-Entities duerfen nicht via Konstruktor instanziiert werden.
- Persistente Jmix-Entities duerfen kein Lombok nutzen.
- UI-Module duerfen keine fremden Core-Implementierungen nutzen.

`webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java` bindet diese Regeln global ein. Das ist ein guter Demonstrationspunkt: "Wir lassen den Agenten arbeiten, aber die Architektur hat Zaunpfosten."

### Realistische Cross-Module Tests

Die `webapp/src/test/java/com/insurance/app/...` Tests pruefen echte fachliche Flows, besonders:

- `QuoteAcceptanceFlowTest`: Quote -> Policy -> Account.
- `PolicyCreatedEventTest`: PolicyCreatedEvent -> Account/AccountDocuments.
- `PolicyRollbackTest`: Rollback bei Downstream-Fehlern.
- `AccountBalanceWithPolicyTest`: Balance inkl. Coverage-End-Regel.
- `UiStarterRegistrationTest`: UI-Starter registrieren Views.

Das ist fuer eine Agent-Demo besser als reine Unit-Tests, weil die gefaehrlichen Integrationsstellen wirklich abgedeckt werden.

### Test-Support Module

`test-support` und `test-support-ui` sind ein guter Anfang fuer deterministische Agentenarbeit:

- `EntityTestData` erzwingt `DataManager.create()` und validiert vor `save()`.
- Domain-spezifische `*DataProvider` und AssertJ Assertions reduzieren Test-Rauschen.
- `DatabaseCleanup` nutzt `MetadataTools.getDatabaseTable()` statt harte Tabellennamen.
- UI-Helpers wie `DataGridInteractions`, `FormInteractions`, `ViewInteractions` machen Jmix UI Tests schreibbar.

## Priorisierte Befunde

### P0 - CI ruft ein fehlendes Script auf

Datei: `.github/workflows/ci.yml`

Der CI-Testjob macht:

```shell
chmod +x ./gradlew ./run_all_tests.sh
./run_all_tests.sh
```

Im Repo gibt es aber kein `run_all_tests.sh`.

Warum das wichtig ist: Fuer eine Agentic-Engineering-Praesentation ist CI der sichtbarste deterministische Guardrail. Wenn CI wegen eines fehlenden Harness-Scripts scheitert, wirkt das Sicherheitsnetz instabil, auch wenn `./gradlew check` lokal gruen ist.

Empfehlung:

- Entweder `run_all_tests.sh` wieder einfuehren und als kanonische Test-Matrix pflegen.
- Oder CI direkt auf `./gradlew check` umstellen.
- Besser: `scripts/verify-agent-harness.sh` einfuehren und sowohl in CI als auch in AGENTS/Skills referenzieren.

Minimaler Zielinhalt:

```shell
#!/usr/bin/env bash
set -euo pipefail

./gradlew spotlessCheck
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
./gradlew check
```

Spaeter kann das Script smarte, modulbezogene Shortcuts bekommen.

### P0 - Java-Version ist inkonsistent

Gefunden:

- `README.md`: Java 17.
- `AGENTS.md` / `CLAUDE.md`: Java 17.
- `.github/workflows/ci.yml`: JDK 17.
- Gradle Toolchains in nahezu allen Builds: `JavaLanguageVersion.of(21)`.
- `docs/architecture.md`: Java 21.

Warum das wichtig ist: Ein Agent wird Build-Fehler, CI-Fehler oder falsche lokale Reproduktion erzeugen, wenn Docs und Build nicht dieselbe Java-Version nennen.

Empfehlung:

- Auf Java 21 standardisieren, wenn die Toolchain bewusst so gesetzt ist.
- CI auf `java-version: "21"` umstellen.
- README, AGENTS, CLAUDE, webapp README aktualisieren.
- Optional Guardrail: kleiner Doc-/Build-Sync-Test, der `JavaLanguageVersion.of(...)`, CI-JDK und README/AGENTS prueft.

### P1 - `partner-ui` durchbricht die Cross-Domain-API-Grenze

Dateien:

- `partner/partner-ui/src/main/java/com/insurance/partner/ui/view/partner/PartnerDetailView.java`
- `partner/partner-ui/src/main/resources/com/insurance/partner/ui/view/partner/partner-detail-view.xml`

Die View laedt fremde Domaenen direkt:

```java
dataManager.loadValues(
    "select a.accountNo, a.accountBalance from account_Account a, policy_Policy p "
        + "where a.accountNo = p.policyNo and p.partnerNo = :partnerNo")
```

Und im XML:

```xml
select e.policyNo, e.coverageEnd from policy_Policy e where e.partnerNo = :partnerNo
```

Das widerspricht dem dokumentierten Prinzip, dass Inter-Domain-Kommunikation ueber API-Interfaces und DTOs laufen soll. Es ist auch ein gutes Beispiel dafuer, wo ArchUnit allein nicht reicht, weil die Grenze in XML/JPQL und `loadValues()` umgangen wird.

Empfehlung:

- Einen API-Read-Service einfuehren, z.B. `PolicyService.findPoliciesByPartnerNo(String partnerNo)` mit DTO/Read-Model.
- Fuer Account-Anzeige entweder `AccountService.findAccountSummaryByPolicyNo(...)` oder einen dedizierten Partner-Overview-Service in einer API-Schicht.
- `partner-ui` sollte fremde Domaenen nur ueber API-Services sehen.
- Guardrail: XML/Java-Scan, der in `*-ui` fremde Jmix Entity-Namen (`policy_Policy`, `account_Account`, usw.) blockiert, ausser im eigenen Modul.

Moeglicher Check:

```shell
rg -n "from (account_|partner_|policy_|quote_)" */*-ui/src/main
```

Dann mit Modul-Kontext auswerten: `partner-ui` darf `partner_` nutzen, aber nicht `policy_` oder `account_`.

### P1 - Quote-Lifecycle-Invarianten gehoeren in den Service

Datei: `quote/quote-core/src/main/java/com/insurance/quote/core/service/QuoteServiceCore.java`

`QuoteListView` deaktiviert Accept/Reject-Buttons fuer nicht passende Zustaende. Der Service selbst erzwingt aber nicht sichtbar genug:

- `accept()` nur fuer `PENDING`.
- `reject()` nur fuer `PENDING`.
- `accept()` nur mit berechnetem Premium.
- `accept()` nur innerhalb `validUntil`.
- Kein erneutes Accept, das eine zweite Policy erzeugen koennte.

Warum das wichtig ist: Ein Agent, ein Test oder ein spaeterer REST/API-Entry-Point kann den Service direkt aufrufen. UI-Button-State ist kein Architektur-Guardrail.

Empfehlung:

- In `QuoteServiceCore` explizite Vorbedingungen einfuehren.
- Tests ergaenzen:
  - accepted quote cannot be accepted again.
  - rejected quote cannot be accepted.
  - accepted quote cannot be rejected.
  - expired quote cannot be accepted.
  - quote without calculated premium cannot be accepted.
- Das als app-spezifischen Skill `insurance-quote-lifecycle` festhalten.

### P1 - Liquibase driftet von Entity-Metadaten

Beispiele:

- `Partner.partnerNo` hat `@Column(... unique = true)`, aber `partner-core/liquibase/010-init-partner.xml` legt keinen Unique Constraint an.
- `Quote.quoteNo` hat `@Column(... unique = true)`, aber `quote-core/liquibase/010-init-quote.xml` legt keinen Unique Constraint an.
- `Policy.policyNo` ist korrekt eindeutig in Liquibase.

Warum das wichtig ist: Agents verlassen sich oft auf Entity-Annotationen. Wenn Liquibase nicht deckungsgleich ist, entstehen deterministisch reproduzierbare Bugs erst nach Startup oder Datenkonflikten.

Empfehlung:

- Liquibase Constraints fuer `PARTNER_NO` und `QUOTE_NO` nachziehen.
- Guardrail-Script `scripts/check-liquibase-entity-drift.sh` oder ein Test in `ArchitectureTest`:
  - `@Column(unique = true)` muss in Changelog vorkommen.
  - `nullable = false` plus `@NotNull` muss Changelog-Nullability matchen.
  - `precision/scale/length` muss matchen.
- Den bestehenden Skill `jmix-create-liquibase-changelog` app-spezifisch ergaenzen: "bei business keys immer Unique Constraint pruefen".

### P1 - App-spezifischer Skill ist vorhanden, aber veraltet

Datei: `.skills/insurance-testing/SKILL.md`

Es gibt bereits einen app-spezifischen Skill. Das ist gut. Er beschreibt aber teilweise Klassen und Muster, die ich im aktuellen Repo nicht finde:

- `PartnerFactory`, `QuoteFactory`, `PolicyFactory`
- `*Data` Records mit Builder

Das aktuelle Repo nutzt dagegen ueberwiegend:

- `EntityTestData`
- `*DataProvider`
- Test-Fixtures pro Core-Modul
- `DatabaseCleanup` im `webapp`

Warum das wichtig ist: Ein falscher Skill ist fuer einen Agenten schlechter als kein Skill. Er klingt autoritativ und fuehrt dann zu erfundenen Klassen oder falschen Testmustern.

Empfehlung:

- `.skills/insurance-testing/SKILL.md` auf den Ist-Zustand aktualisieren.
- Alternativ die im Skill beschriebenen Factories wirklich einfuehren. Dann aber bewusst als neue Test-Harness-Architektur.
- Den Skill kleiner machen: nur aktuelle Patterns, konkrete Dateipfade, konkrete Kommandos.

### P1 - Guardrails pruefen Java-Abhaengigkeiten gut, aber nicht XML, Message Keys, Rollen und Changelogs

ArchUnit deckt Java-Package-Abhaengigkeiten ab. Die typischen Jmix-Agentenfehler passieren aber oft in anderen Artefakten:

- XML-Descriptor referenziert falsche `msg://` Keys.
- XML-Descriptor nutzt fremde Entity-Namen in JPQL.
- `@MenuPolicy` passt nicht zum echten `menu.xml`.
- `@ViewPolicy` fehlt fuer Detail-Views.
- Liquibase spiegelt Entity-Metadaten nicht.
- UI-Tests suchen harte Labels statt Component IDs.

Empfehlung:

Neue deterministische Checks:

1. `check-jmix-message-keys`
   - Alle `msg://...` Referenzen in XML finden.
   - Gegen lokale und voll qualifizierte `messages.properties` / `messages_en.properties` pruefen.

2. `check-jmix-xml-boundaries`
   - JPQL in `*-ui` nur gegen eigene Core-Entity-Namen erlauben.
   - Cross-domain nur ueber API DTO `metaClass` oder Services.

3. `check-resource-roles`
   - Alle `@MenuPolicy(menuIds = ...)` gegen echte `menu.xml` `view`/`id` Werte pruefen.
   - Alle `@ViewController` IDs gegen Rollen abgleichen.

4. `check-liquibase-entity-drift`
   - Entity-Annotationen gegen Changelogs pruefen.

Diese Checks koennen erstmal Shell/Java/JUnit-basiert sein. Wichtig ist weniger die perfekte Implementierung, sondern dass ein Agent sie jedes Mal ausfuehren kann.

### P2 - Gradle-Konventionen sind stark dupliziert

Die `build.gradle` Dateien in `account`, `partner`, `quote`, `product`, `policy`, `security` enthalten sehr aehnliche Blocks fuer:

- Jmix plugin setup.
- Repositories.
- Toolchain.
- Publishing.
- Checkstyle/SpotBugs.
- Test/Jacoco.

Es gibt bereits Drift:

- `policy/build.gradle` hat im Gegensatz zu vielen anderen Domain-Builds kein lokales `apply plugin: 'jacoco'` / `jacocoTestReport` Setup im Subproject-Block.
- Root `jacocoRootReport` laeuft trotzdem, aber die Konvention ist nicht einheitlich.

Warum das wichtig ist: Ein Agent wird eher ein Modul patchen als sechs. Kopierte Konventionen laden zu partieller Aenderung ein.

Empfehlung:

- Eine gemeinsame Gradle Convention auslagern, z.B. `gradle/jmix-addon-conventions.gradle`.
- Domain-Builds nur noch konfigurieren:
  - `group`
  - `projectId`
  - included subprojects
- Fuer die Praesentation ist das ein starker Punkt: "Wir reduzieren Agenten-Fehler, indem wir Wiederholung aus dem Repo nehmen."

### P2 - API-Module sind nicht so leichtgewichtig, wie die Doku behauptet

Die API-Konfigurationen haengen an `EclipselinkConfiguration`, und API-Gradle-Dateien nutzen `io.jmix.data:jmix-eclipselink-starter`.

Beispiele:

- `partner-api/src/main/java/.../PartnerApiConfiguration.java`
- `quote-api/src/main/java/.../QuoteApiConfiguration.java`
- `product-api/src/main/java/.../ProductApiConfiguration.java`

Das kann wegen Jmix DTO Enhancement bewusst sein. Trotzdem steht die Doku auf "API = keine Persistence". Fuer Agenten ist die Grenze dadurch unklar.

Empfehlung:

- Entscheiden und dokumentieren:
  - API-Module duerfen Jmix DTO-Metadaten und Eclipselink Enhancement nutzen, aber keine JPA Entities, Repositories, Liquibase und EntityManager.
  - Oder API-Module weiter verschlanken, wenn technisch moeglich.
- ArchUnit-Regel ergaenzen: API-Pakete duerfen keine `jakarta.persistence.*`, `io.jmix.data.Sequences`, Repositories oder Changelogs enthalten.
- Wenn `jmix-eclipselink-starter` notwendig ist, explizit in `docs/architecture.md` erklaeren.

### P2 - Webapp-Abhaengigkeiten sind redundant

`webapp/build.gradle` bindet pro Domaene API-, Core- und UI-Starter ein. Da `*-ui-starter` ueber `*-ui` und `*-core` oft schon transitiv viel mitbringt, ist die Liste fuer einen Agenten schwer zu interpretieren.

Empfehlung:

- Zielbild dokumentieren: Welche Starter muss die App explizit importieren?
- Falls alle drei Starter absichtlich importiert werden, warum?
- Falls nicht, vereinfachen:
  - UI-Starter fuer UI-Domaenen.
  - Core/API-Starter nur fuer headless/Backend-Domaenen.
- Guardrail: `webapp` sollte keine zufaelligen direkten Core-Artefakte brauchen, ausser bewusst im Compose Root.

### P2 - Test-Harness hat alte und neue Muster nebeneinander

Im `webapp` gibt es gute neue Patterns:

- `BaseIntegrationTest`
- `DatabaseCleanup`
- `InsuranceAssertions`

Daneben gibt es weiterhin Tests mit:

- manuellem `cleanupIds` in `@AfterEach`
- direkten `@SpringBootTest` Annotationen statt Base Class
- harten Tabellenloeschungen in einigen Modul-Tests
- Testnamen wie `test_createUser`

Das ist nicht schlimm, aber fuer Agenten uneindeutig.

Empfehlung:

- Entscheiden, ob alle Integrationstests langfristig in `webapp/src/test` leben sollen.
- App-spezifischen Testing-Skill auf Ist-Zustand bringen.
- Optional Migration:
  - `UserTest`, `UserUiTest`, `PartnerUiTest` auf die gleichen Given/When/Then Patterns bringen.
  - Harte Tabellenloeschungen in Modul-Tests durch `DatabaseCleanup` oder moduleigenes MetadataCleanup ersetzen.

### P2 - Logging ist punktuell, aber nicht operationalisiert

Vorhanden:

- `PolicyServiceCore`, `QuoteServiceCore`, `AccountServiceCore`, `PolicyCreatedEventListener`, `LoginView`, `PolicyDetailView` loggen.
- `webapp/src/main/resources/application.properties` setzt einige Framework-Loglevel.

Luecken:

- Keine Correlation ID / MDC fuer einen Quote-Accept-Flow.
- Keine einheitlichen Business-Event-Namen.
- Kein strukturierter Log-Kontext (`quoteNo`, `policyNo`, `partnerNo`) ueber alle Services hinweg.
- Keine Trennung "technical debug" vs "business audit".
- Keine Tests, die kritische Rollback-/Event-Logs absichern.

Empfehlung:

- Kleine `BusinessLog`-Konvention oder einfach klare Logger-Standards:
  - `quote.accept.started`
  - `quote.accept.policy-created`
  - `policy.created.event-published`
  - `account.created`
  - `quote.accept.completed`
  - `quote.accept.failed`
- MDC-Felder:
  - `quoteNo`
  - `policyNo`
  - `partnerNo`
  - `correlationId`
- Im Service-Einstieg `QuoteService.accept()` Correlation ID setzen, falls noch keine existiert.
- Fuer Demo reicht ein einfacher Servlet/Service-MDC Filter plus konsistente log messages.
- `application.properties` um app-spezifische logger levels ergaenzen:

```properties
logging.level.com.insurance=info
logging.level.com.insurance.quote.core.service=debug
logging.level.com.insurance.policy.core.service=debug
logging.level.com.insurance.account.core.service=debug
```

### P2 - Security-Rollen sind gut komponiert, aber nicht getestet

Positiv:

- Domain-Rollen sind getrennt in Core und UI.
- `InsuranceAgentRole` und `InsuranceBackOfficeRole` komponieren Domain-Rollen.
- `UiMinimalRole` kapselt Login/MainView.

Luecken:

- Keine sichtbaren Tests, die pruefen:
  - Agent darf Partner/Quote managen.
  - Agent darf Policy/Account lesen, aber nicht aendern.
  - Backoffice darf alles in den Domaenen.
  - Menu-/View-Policies passen zu echten Views.

Empfehlung:

- `webapp/src/test/java/com/insurance/app/security/RolePolicyTest.java` ergaenzen.
- Ressource-Rollen in einem app-spezifischen Skill dokumentieren.
- Guardrail gegen fehlende `@ViewPolicy` fuer Detail-Views.

### P2 - Doku ist teils wertvoll, teils veraltet

Beispiele:

- `README.md` verlinkt auf `ARCHITECTURE.md`, aber vorhanden ist `docs/architecture.md`.
- `README.md` nennt ein `common/` Modul, tatsaechlich gibt es `test-support/` und `test-support-ui/`.
- `docs/agent_harness_review.md` sagt, es gebe keine Modul-READMEs und kein Spotless; beides ist inzwischen teilweise/faktisch ueberholt.
- `product/README.md` und `security/README.md` fehlen.

Empfehlung:

- Alte Review entweder als Historie markieren oder entfernen/ersetzen.
- Root README korrigieren.
- `docs/architecture.md` als kanonische Architekturquelle markieren.
- Modul-README fuer `product` und `security` ergaenzen.
- AGENTS/CLAUDE aus derselben Quelle generieren oder mindestens mit einem Sync-Check pruefen.

### P3 - Deprecated Jmix/Vaadin API im Test-Support

Beim Testlauf:

```text
ViewNavigators.view(Class<V>) has been deprecated and marked for removal
```

Datei:

- `test-support-ui/src/main/java/com/insurance/common/test_support_ui/ViewInteractions.java`

Empfehlung:

- Aktuelle Jmix/Vaadin Navigation API pruefen und Helper aktualisieren.
- Als Upgrade-Guardrail `./gradlew check --warning-mode all` gelegentlich laufen lassen.
- Fuer Agenten: Skill/Guideline "Bei Jmix API deprecation erst Projektbeispiel suchen, dann Context7/offizielle Doku".

## Empfohlene Neue App-Spezifische Skills

Die vorhandenen generischen Jmix-Skills sind gut fuer "wie baue ich eine Jmix Entity/List/Detail". Fuer die Praesentation wuerde ich bewusst kleinere, fachliche Skills zeigen. Gute Skills sollten nicht nur erklaeren, sondern deterministische Artefakte erzwingen.

### 1. `insurance-testing`

Status: existiert, aber aktualisieren.

Soll enthalten:

- Aktuelle Testpfade.
- `BaseIntegrationTest` nur fuer webapp-Integrationstests.
- `EntityTestData` + `*DataProvider` statt nicht existierender Factories.
- `DatabaseCleanup` als Default fuer webapp DB-Tests.
- `InsuranceAssertions` als Standardimport.
- Kommandos:
  - `./gradlew :webapp:test --tests "..."`
  - `./gradlew :<module>:check`
  - `./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"`

Forbidden:

- `new Entity()`.
- harte Tabellenloeschung, wenn `DatabaseCleanup` verfuegbar ist.
- `@Transactional` auf Testklassen.
- stale entity assertions nach Service-Aufrufen.

### 2. `insurance-quote-lifecycle`

Use when:

- Quote erstellen, berechnen, akzeptieren, ablehnen.
- Quote -> Policy -> Account Flow aendern.

Muss erzwingen:

- Quote status machine: `PENDING -> ACCEPTED` oder `PENDING -> REJECTED`.
- Kein zweites Accept.
- Kein Reject nach Accept.
- Premium muss berechnet sein.
- Validity window pruefen.
- Nach Accept: Policy und Account existieren.
- Rollback-Test, wenn Policy/Account Creation fehlschlaegt.

### 3. `insurance-module-boundaries`

Use when:

- Ein Modul soll Daten aus einem anderen Modul anzeigen oder verwenden.

Muss erzwingen:

- Cross-domain nur ueber `*-api` Service/DTO/Event.
- Kein fremdes `*-core` in Java.
- Kein fremder Jmix Entity-Name in UI XML/JPQL.
- Keine JPA Relation ueber Domain-Grenzen.
- Falls bewusst value reference: business key/UUID dokumentieren.

### 4. `insurance-security-roles`

Use when:

- Views, Menus, Entities oder Rollen geaendert werden.

Muss erzwingen:

- EntityPolicy + EntityAttributePolicy.
- ViewPolicy fuer List und Detail.
- MenuPolicy nur fuer echte menu item ids/view ids.
- App-Rollen (`InsuranceAgentRole`, `InsuranceBackOfficeRole`) nachziehen.
- Mindestens ein Role Policy Test, wenn Berechtigungen fachlich relevant sind.

### 5. `insurance-liquibase-drift`

Use when:

- Entity-Felder, Enum-Felder, Business Keys, Constraints geaendert werden.

Muss erzwingen:

- Java Annotationen gegen Liquibase abgleichen.
- Unique Constraints fuer Business Keys.
- Nullability/precision/scale/length.
- Changelog ist vom webapp master changelog erreichbar.
- Test-Changelogs fuer Modul-Tests aktualisieren, falls noetig.

### 6. `insurance-observability`

Use when:

- Services, Events oder Fehlerpfade in fachlichen Flows geaendert werden.

Muss erzwingen:

- Konsistente Business-Log-Events.
- `quoteNo`, `policyNo`, `partnerNo` in Logs.
- Error logs am Rand des Rollbacks.
- Keine personenbezogenen/Passwort-Daten im Log.
- Tests oder manuelle Log-Pruefung fuer kritische Flows.

### 7. `insurance-ui-contract`

Use when:

- Jmix XML Views, MainView, Menus, ModuleTheme, CSS oder UI Tests geaendert werden.

Muss erzwingen:

- `msg://` fuer sichtbare Texte.
- Component IDs stabil.
- UI Tests bevorzugen IDs/Actions statt sichtbare Labels, wo moeglich.
- View registriert via Starter.
- Menu item hat Rolle und Theme-Konvention.

## Neue Guardrail-Matrix

### Schnelle lokale Checks

| Situation | Kommando |
|---|---|
| Nur Java Format | `./gradlew spotlessApply` |
| Nur Architekturgrenzen | `./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"` |
| Ein Core-Layer kompiliert | `./gradlew :partner:partner-core:compileJava` |
| Ein Modul komplett | `./gradlew :partner:check` |
| Webapp-Flow-Test | `./gradlew :webapp:test --tests "com.insurance.app.quote.QuoteAcceptanceFlowTest"` |
| Alles lokal | `./gradlew check` |

### Zusaetzliche Checks, die noch fehlen

| Check | Zweck | Prioritaet |
|---|---|---|
| `check-ci-harness` | CI referenziert nur existierende Scripts und richtige Java-Version | P0 |
| `check-jmix-xml-boundaries` | Keine fremden Entity-Namen in UI XML/JPQL | P1 |
| `check-quote-state-machine` | Quote-Lifecycle Invarianten | P1 |
| `check-liquibase-drift` | Entity Annotationen vs Changelog | P1 |
| `check-message-keys` | Alle `msg://` Keys resolvbar | P1 |
| `check-role-surface` | View/Menu/Entity policies vollstaendig | P2 |
| `check-doc-sync` | README/AGENTS/CI/Gradle Java-Version und Links konsistent | P2 |

## Roadmap Fuer Die Praesentation

### Phase 0 - Harness wieder glaubwuerdig machen

Ziel: Bevor Skills praesentiert werden, darf der sichtbare Build nicht wackeln.

Tasks:

- `run_all_tests.sh` entweder wiederherstellen oder CI auf `./gradlew check` umstellen.
- JDK auf 21 vereinheitlichen.
- Root README Link auf `docs/architecture.md` korrigieren.
- `docs/agent_harness_review.md` als veraltet markieren oder durch diese Review ersetzen.
- `.gitignore` um `graphify-out/` pruefen, falls das ein lokales Tool-Artefakt ist.

### Phase 1 - Architekturgrenze haerten

Ziel: Cross-domain nur ueber API.

Tasks:

- `partner-ui` direkte Queries auf `policy_Policy` / `account_Account` entfernen.
- API Read Services/DTOs fuer Partner Overview einfuehren.
- `check-jmix-xml-boundaries` bauen.
- ArchUnit/File-System-Check um XML/JPQL Scans ergaenzen.

### Phase 2 - Fachliche Invarianten in Services ziehen

Ziel: UI ist Komfort, Service ist Wahrheit.

Tasks:

- Quote state machine in `QuoteServiceCore` erzwingen.
- Tests fuer illegale Statusuebergaenge.
- Policy request validation expliziter machen.
- Optional: Premium-Berechnung aus UI in fachlichen Service verschieben oder bewusst dokumentieren.

### Phase 3 - Schema- und Rollen-Drift blockieren

Ziel: Ein Agent kann keine halbe Entity/View/Rolle liefern.

Tasks:

- Liquibase Unique Constraints nachziehen.
- Drift-Check fuer Entity vs Liquibase.
- Role surface tests fuer `InsuranceAgentRole` / `InsuranceBackOfficeRole`.
- Menu/View policy check.

### Phase 4 - Skills als deterministische Arbeitsanweisungen

Ziel: In der Demo zeigen, dass Skills nicht "mehr Prompt" sind, sondern wiederholbare Engineering-Prozesse.

Tasks:

- `insurance-testing` aktualisieren.
- `insurance-module-boundaries` erstellen.
- `insurance-quote-lifecycle` erstellen.
- `insurance-liquibase-drift` erstellen.
- `insurance-security-roles` erstellen.
- Jeden Skill mit "Trigger", "Forbidden", "Required artifacts", "Validation command" aufbauen.

### Phase 5 - Observability polish

Ziel: Nach einer Agent-Aenderung kann man fachliche Flows im Log nachvollziehen.

Tasks:

- Business log vocabulary festlegen.
- MDC/correlation fuer Quote Accept.
- App-specific logger levels.
- Rollback-/Failure-Pfade konsistent loggen.

## Konkrete "Best Next Pull Requests"

Wenn ich daraus eine Sequenz fuer eine Demo bauen wuerde:

1. **PR: Fix CI and docs baseline**
   - JDK 21 in CI/Docs.
   - `run_all_tests.sh` ersetzen oder einfuehren.
   - README Link fixen.

2. **PR: Add XML boundary guardrail**
   - Kleiner Test/Shell-Check, der `partner-ui` aktuell rot macht.
   - Dann Partner Overview ueber API Service refactoren.
   - Check wird gruen.

3. **PR: Harden Quote lifecycle**
   - Neue Tests fuer illegale Transitions.
   - Service-Invarianten implementieren.

4. **PR: Add app-specific skills**
   - `insurance-testing` aktualisieren.
   - `insurance-module-boundaries`, `insurance-quote-lifecycle`, `insurance-liquibase-drift`.

5. **PR: Liquibase drift guardrail**
   - Unique Constraints fuer Partner/Quote.
   - Drift-Test.

6. **PR: Observability for Quote acceptance**
   - Business logs + MDC.
   - Dokumentierter Demo-Flow: Accept Quote und Logs zeigen.

## Praesentationswinkel

Die Story, die dieses Repo gut tragen kann:

> "Wir lassen den Agenten nicht frei im luftleeren Raum. Wir geben ihm eine modulare Architektur, lokale Skills mit Projektwissen, schnelle Feedback-Kommandos und Guardrails, die XML, Java, DB-Schema, Rollen und fachliche Flows pruefen."

Die besten Demo-Momente:

- Einen absichtlichen Cross-domain JPQL Zugriff zeigen und durch Guardrail blockieren.
- Einen Skill aktivieren, der dem Agenten sagt, welche Artefakte bei Quote-Lifecycle-Aenderungen zwingend sind.
- Einen illegalen Quote State Transition Test zuerst rot, dann gruen machen.
- Einen Liquibase Drift Check zeigen, der `@Column(unique = true)` ohne DB-Constraint blockiert.
- Nach Quote Accept die Business Logs mit `quoteNo`, `policyNo`, `partnerNo` zeigen.

## Schlussbewertung

Architekturell ist das Repo schon deutlich ueber "Demo-App" Niveau. Die groesste Hebelwirkung entsteht jetzt durch das Schliessen der Luecken zwischen Architekturabsicht und deterministischer Durchsetzung:

- CI muss exakt dasselbe Sicherheitsnetz fahren wie lokal.
- Modulgrenzen muessen auch in XML/JPQL gelten.
- Skills muessen den echten Projektzustand abbilden.
- Fachliche Invarianten gehoeren in Services und Tests.
- Logging sollte Cross-Module-Flows erklaerbar machen.

Wenn diese Punkte umgesetzt sind, ist `jmix-insurance` eine sehr starke Basis, um Agentic Engineering nicht nur zu behaupten, sondern live und reproduzierbar zu zeigen.
