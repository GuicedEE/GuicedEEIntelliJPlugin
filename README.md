# Guiced — IntelliJ IDEA Plugin

Full **Google Guice** and **GuicedEE 2.3.0** framework support for IntelliJ IDEA 2025.2+.

## Features

### GuicedEE Framework Support

- **Project creation** — scaffold new GuicedEE projects with Maven or Gradle BOMs at 2.3.0 and `module-info.java`
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
- **Entry-point recognition** — methods (and classes) annotated with `@VertxEventDefinition` are treated as framework entry points, so event-bus consumers invoked reflectively by the runtime are never reported as unused
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
| `ScopeAnnotationProvider` | Supply scope meta-annotations that identify concrete scope annotations |
| `BindingAnnotationProvider` | Supply custom binding annotations |
| `NamedAnnotationProvider` | Map custom naming annotations to `@Named` |

- Multibindings built-in (`MapBinder`, `Multibinder`, `OptionalBinder`)
- Drop-in replacement — all public Guice APIs unchanged

### Local dependency scopes

GuicedEE supports scope annotations on injected constructor parameters and fields:

```java
@Inject Handler(@RequestScoped RequestState state) { ... }
@Inject @RequestScoped RequestState fieldState;
```

Providers are shared within the consuming constructor or field group by scope annotation and
dependency key. Different consumers and different scope annotations remain isolated. The local
scope wraps any existing binding scope; direct injection retains the original binding. Method
injection and provider-method parameters retain their existing behavior.

The scope annotation template includes `TYPE`, `METHOD`, `PARAMETER`, and `FIELD` targets.
Declare runtime retention and `@ScopeAnnotation`, then bind the concrete annotation with
`Binder.bindScope()` or a `BindScopeProvider`. Register providers through JPMS `provides`, or
`META-INF/services` on the classpath. If using a different scope meta-annotation, return its
marker type from `ScopeAnnotationProvider`; the concrete annotation still needs a binding.
Unregistered scopes fail injector creation. Applications own request boundaries and asynchronous
context propagation.

The plugin detects conflicting standard and custom scopes marked with `@ScopeAnnotation`,
`javax.inject.Scope`, or `jakarta.inject.Scope`. In GuicedEE projects, the existing lifetime
inspections also check local request/session scopes on injected fields and constructor parameters.

## Requirements

- IntelliJ IDEA 2025.2+ (Community or Ultimate; since build 252)
- Java, Maven, Gradle, and Properties plugins (bundled)

## License

See [LICENSE](LICENSE).
