# Task 21 - Core Entity Boundary Guardrails Vereinfachen

## Status

Analyse angelegt. Noch nicht umgesetzt.

## Ziel

Die Cross-Domain-JPA-Guardrails sollen einfacher erklaerbar werden:

- Persistente Domain-Modelle leben deterministisch in `com.insurance.<module>.core.entity..`.
- Persistente Domain-Modelle duerfen keine fremden `com.insurance.<other>.core.entity..` Typen kennen.

Damit wird die wichtigste Regel nicht mehr als Source-Import-Sonderlogik erklaert, sondern als
saubere Kombination aus Package-Konvention und ArchUnit-Package-Dependencies.

## Aktuelle Guardrails

Der globale Einstiegspunkt ist:

```text
webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java
```

Aktuell registrierte Regelgruppen:

| Regelklasse | Aktuelle Aufgabe | Bewertung fuer diesen Umbau |
| --- | --- | --- |
| `JavaPackageDependencyRules` | API darf nicht auf Core/UI zeigen; Core darf keine fremden Core/UI Packages nutzen; Core darf kein Flow UI nutzen; UI darf keine fremden Core Implementierungen nutzen | Inhaltlich schon nahe am Ziel, aber fuer die Praesentation zu breit und nicht entity-zentriert |
| `JmixEntityRules` | Keine Constructor-Instanziierung von Jmix-Entities; kein Lombok auf persistenten Entities; Entity-Namen mit Modulprefix; Entities und Embeddables unter `com.insurance.<module>.core.entity..` | Bleibt zentral; Package-Regeln sind die Voraussetzung fuer den vereinfachten Boundary-Check |
| `JmixDomainBoundaryRules` | Java/XML-Dateien duerfen keine fremden persistenten Jmix-Entity-Namen wie `policy_Policy` referenzieren | Bleibt wichtig, weil JPQL/XML/String-Zugriffe keine Java-Type-Dependency brauchen |
| `JmixDomainBuildRules` | Domain-Build-Roots muessen das gemeinsame Gradle-Konventionsscript verwenden | Bleibt, ist aber kein Cross-Domain-Entity-Guardrail |
| `JmixDomainReferenceRules` | Persistent Entities und Embeddables werden source-/import-basiert gescannt; fremde API-Enums sind erlaubt, andere fremde `com.insurance...` Imports verboten | Kandidat zum Ersetzen oder deutlichen Vereinfachen |
| `JmixUiDependencyRules` | UI-Module duerfen nicht auf fremde UI-Implementierungen zeigen; prueft Java-Dependencies und Gradle-Dateien | Bleibt fuer UI-Grenzen, ist aber kein Muster fuer diesen Entity-Boundary-Umbau |
| `CoreModuleFileRules` | Core-Module duerfen keine Flow-UI-Dependencies oder View-Resources enthalten | Bleibt |
| `JmixLiquibaseRules` / `JmixSecurityRoleRules` | Schema-/Security-spezifische Guardrails | Nicht Teil dieses Umbaus |

## Problem Mit Der Aktuellen Reference-Regel

`JmixDomainReferenceRules.persistentDomainModelsOnlyImportOwnDomainModelOrForeignApiEnums`
arbeitet bewusst auf Source-Imports. Das war als erster Schnitt gut, ist aber als langfristige
Hauptregel zu speziell:

- Sie erklaert die Architektur ueber Import-Statements statt ueber Modul-/Package-Grenzen.
- Sie braucht Sonderlogik fuer fremde API-Enums.
- Sie erwischt vollqualifizierte Typreferenzen ohne Import nicht.
- Sie dupliziert teilweise, was `JavaPackageDependencyRules` und `JmixEntityRules` schon
  strukturell absichern.

Die Regel sollte deshalb nicht mehr die tragende Wand sein.

## Zielbild

### 1. Persistent Model Structure Rules

Beibehalten und ggf. klarer benennen:

```text
JmixEntityRules.corePersistentEntitiesResideInModuleEntityPackages
JmixEntityRules.corePersistentEmbeddablesResideInModuleEntityPackages
```

Ziel:

```text
@Entity und @Embeddable duerfen nur unter
com.insurance.<module>.core.entity..
liegen.
```

Diese Regel macht das Package selbst zur Architekturinformation. Andere Regeln koennen dann aus
dem Package ableiten, welches Modul das Domain-Modell besitzt.

### 2. Persistent Domain Models Do Not Depend On Foreign Core Entities

Neue oder ersetzende ArchUnit-Regel:

```text
Persistent Entities und Embeddables aus Modul A duerfen nicht von
com.insurance.<module B>.core.entity..
abhaengen.
```

Wichtig: Diese Regel sollte mit ArchUnit-Klassenabhaengigkeiten arbeiten, nicht mit Source-Imports.

Skizze:

```java
@ArchTest
public static void persistentDomainModelsDoNotDependOnForeignCoreEntities(JavaClasses classes) {
  for (String module : ArchitectureProject.entityBoundaryModules()) {
    noClasses()
        .that(persistentDomainModelIn(module))
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(ArchitectureProject.foreignEntityPackagesOf(module))
        .allowEmptyShould(true)
        .check(classes);
  }
}
```

Quell-Predicate:

```text
Klasse ist @jakarta.persistence.Entity oder @jakarta.persistence.Embeddable
und liegt unter com.insurance.<module>.core.entity..
```

Ziel-Package:

```text
com.insurance.<foreign>.core.entity..
```

Damit sind direkte JPA-Assoziationen ueber Modulgrenzen verboten:

```java
@ManyToOne
private Policy policy;
```

Ebenso verboten sind vollqualifizierte Typreferenzen auf fremde Entities, selbst wenn kein Import
existiert.

Erlaubt bleiben:

- eigene Entities und Embeddables,
- lokale consumer-owned References wie `AccountPolicyReference`,
- fremde API-Enums wie `PaymentFrequency`, weil sie nicht in `core.entity` liegen,
- normale Java/Jmix/JPA Infrastrukturtypen.

### 3. Foreign Entity Name String Rule Behalten

`JmixDomainBoundaryRules.domainModulesDoNotReferenceForeignPersistentEntities` bleibt notwendig.

Grund:

```java
dataManager.loadValues("select p.policyNo from policy_Policy p")
```

oder XML/JPQL kann fremde Entities per String referenzieren, ohne dass Java eine fremde Klasse
typisiert referenziert. Diese Regel deckt genau diese Jmix-spezifische Luecke ab.

Optional spaeter besserer Name:

```text
JmixPersistentEntityNameRules
```

## Umbauvorschlag

### P1

- `JmixEntityRules` als Strukturanker behalten.
- In `ArchitectureProject` Helper fuer Entity-Packages ergaenzen:

```java
entityPackageOf(module) -> "com.insurance.<module>.core.entity.."
foreignEntityPackagesOf(module)
```

- `JmixDomainReferenceRules` ersetzen durch eine ArchUnit-basierte Regel auf
  `core.entity`-Package-Dependencies.
- Die alte import-basierte Regel entfernen oder auf einen sehr kleinen Spezialcheck reduzieren,
  falls spaeter wirklich noetig.
- `ArchitectureTest` weiter gruen halten.

### P2

- Regelklassennamen schaerfen, damit sie die Praesentation tragen:

```text
PersistentModelStructureRules
PersistentModelDependencyRules
JmixPersistentEntityNameRules
```

Das kann in einem zweiten Schnitt passieren, falls die Umbenennung zu viel Diff erzeugt.

## Akzeptanzkriterien

- Es gibt eine zentrale ArchUnit-Regel, die persistente Entities und Embeddables gegen fremde
  `core.entity` Packages absichert.
- Fremde API-Enums bleiben ohne Sonderlogik erlaubt.
- Eine direkte JPA-Assoziation von `account-core` nach `policy-core` wuerde rot werden.
- Eine vollqualifizierte fremde Entity-Typreferenz ohne Import wuerde rot werden.
- Die alte importbasierte Reference-Regel ist entfernt oder klar als nicht-zentrale Zusatzregel
  begruendet.
- Die Entity-Name-String-Regel fuer Java/XML bleibt erhalten.
- `ArchitectureTest` bleibt der zentrale Einstiegspunkt.

## Validierung

```shell
./gradlew spotlessApply
./gradlew :test-support:compileJava
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

Negativproben:

1. In `Account` temporaer ein Feld `private com.insurance.policy.core.entity.Policy policy;`
   einfuegen. Erwartung: neue Persistent-Model-Dependency-Regel wird rot.
2. In einem Account-JPQL/XML temporaer `policy_Policy` referenzieren.
   Erwartung: Entity-Name-String-Regel bleibt rot.

## Entscheidung

Task 21 soll das Regelwerk vereinfachen, nicht weiter verkomplizieren.

Die neue Leitlinie lautet:

```text
Package-Struktur definiert Ownership.
ArchUnit prueft Typabhaengigkeiten zwischen Entity-Packages.
Jmix-String-Entity-Namen bleiben ein separater Jmix-spezifischer Guardrail.
```
