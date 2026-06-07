# Security Module — jmix-insurance

The **Security Module** owns the application user entity, database user repository, full-access
role, and User management Flow UI.

## Module Structure

```
security/
├── security-api/          ← Security role contracts and repository-facing API support
├── security-api-starter/  ← Spring Boot auto-configuration for the API
├── security-core/         ← User entity, repository, Liquibase, core role policies
├── security-core-starter/ ← Spring Boot auto-configuration for the Core
├── security-ui/           ← User list/detail views and UI role policies
└── security-ui-starter/   ← Spring Boot auto-configuration for the UI
```

## Database Model

- **Jmix entity name**: `security_User`
- **Table**: `APP_USER`
- **Key attributes**: `username`, encoded `password`, `firstName`, `lastName`, `email`, `active`,
  `timeZoneId`

The table name is historical and can remain `APP_USER`; the entity name follows the module-prefix
architecture rule.

## Roles

- `system-full-access` grants unrestricted access for system/admin use.
- `security-core-manage` grants entity and attribute management for `User`.
- `security-ui-manage` grants `security_User.list`, `security_User.detail`, and the concrete menu
  item.

Application personas are composed in `webapp`:

- `insurance-agent`: business agent access, not user administration.
- `insurance-backoffice`: business backoffice access, not user administration by default.

## Rules

- Use `DataManager.create()` or `EntityTestData`, never `new User()`.
- Passwords must be encoded through `PasswordEncoder` before persistence.
- User view ids, menu ids, role policies, and starter registration tests must move together.
