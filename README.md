# Guiced — IntelliJ IDEA Plugin

Full **Google Guice** and **GuicedEE** framework support for IntelliJ IDEA 2024.2+.

## Features

### GuicedEE Framework Support

- **Project creation** — scaffold new GuicedEE projects with proper Maven dependencies and `module-info.java`
- **File templates** for common GuicedEE components:
  - Guice Modules
  - Lifecycle Hooks (Pre-Startup, Post-Startup, Pre-Destroy)
  - REST Services (with optional service class and DB session)
  - REST Clients with `@Endpoint`
  - Router Configurations
  - Persistence / Database Modules
  - Cassandra Modules
  - WebSocket Channels, Message Receivers, and Hooks
  - RabbitMQ Consumers
  - Kafka Consumers
  - Vert.x Startup and Configurators
  - Authentication Providers (`IGuicedAuthenticationProvider`)
  - Authorization Providers (`IGuicedAuthorizationProvider`)
  - Classpath Scanner SPIs
- **Intention actions** for annotation-driven configuration:
  - `@RabbitConnectionOptions` / `@QueueExchange` / `@QueuePublisher`
  - `@AuthOptions`, `@OAuth2Options`, `@JwtAuthOptions`, `@AbacOptions`
  - `@OtpAuthOptions`, `@PropertyFileAuthOptions`, `@LdapAuthOptions`
  - `@HtpasswdAuthOptions`, `@HtdigestAuthOptions`
  - `@KafkaConnectionOptions`
  - `@Verticle` and `package-info.java` annotations
- **Run configurations** — detect and run GuicedEE applications directly from the gutter

### Google Guice Support

- **Binding navigation** — click-through between injection points and `bind()` statements
- **JIT binding support** — gutter navigation for concrete-class injection without explicit bindings
- **`@ImplementedBy` / `@ProvidedBy` navigation**
- **SPI injection point recognition** — `@Endpoint` (REST Client) and `@ConfigProperty` (MicroProfile Config) fields treated as injection points with implicit usage detection
- **`@Inject` fields** correctly marked as implicitly used, read, and written
- **18 inspections** for common Guice mistakes (conflicting annotations, uninstantiable bindings, redundant bindings, scope issues, etc.)
- **Intention actions** to move bindings and scopes to class annotations
- **File templates** for Guice modules, providers, binding annotations, scope annotations, and method interceptors

### GuicedEE guice-core

The plugin documents the **GuicedEE guice-core** module — an enhanced, JPMS-ready fork of Google Guice:

- Full `module-info.java` (`module com.google.guice`) with explicit exports, requires, and uses
- JDK 25+ compatibility without `--add-opens` hacks
- Jakarta namespace (`jakarta.inject`, `jakarta.annotation`)
- Six `ServiceLoader`-based SPI extension points (`com.google.inject.gee`):

| SPI | Purpose |
|-----|---------|
| `InjectionPointProvider` | Register custom annotations that mark injection points |
| `InjectorAnnotationsProvider` | Declare additional injector annotations |
| `BindScopeProvider` | Programmatically bind custom scope annotations |
| `ScopeAnnotationProvider` | Supply custom scope annotations |
| `BindingAnnotationProvider` | Supply custom binding annotations |
| `NamedAnnotationProvider` | Map custom naming annotations to `@Named` |

- Multibindings built-in (`MapBinder`, `Multibinder`, `OptionalBinder`)
- Drop-in replacement — all public Guice APIs unchanged

## Requirements

- IntelliJ IDEA 2024.2+ (Community or Ultimate)
- Java, Maven, Gradle, and Properties plugins (bundled)

## License

See [LICENSE](LICENSE).
