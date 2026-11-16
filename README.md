# Guiced — IntelliJ IDEA Plugin

Full **Google Guice** and **GuicedEE 2.6.0** framework support for IntelliJ IDEA 2025.2+.

## Features

### GuicedEE Framework Support

- **Project creation** — scaffold Java or Kotlin projects targeting JDK 25 with Maven or Gradle BOMs at 2.6.0 and `module-info.java`.
- **Cloud SDKs** — independently select Firebase Admin (Auth, Messaging, Firestore) and Commercetools (commerce API and GraphQL client). Maven and Gradle use the BOM-managed `com.guicedee.modules.services` wrappers and preserve `firebase.admin` and `commercetools.sdk.java.api` in JPMS descriptors.
- **Native image starter** — an opt-in choice beside jlink, for single-module and multi-module Maven/Gradle projects. Generates explicit discovery, reflection metadata providers, dual JPMS/service-file registration, a modular argument-file helper and `NATIVE.md`. The entry point uses SPI discovery; add workload-specific resource/proxy/interceptor metadata and validate the selected integrations with GraalVM for JDK 25.
- **Application startup** — basic and web entry points use `GuiceApplication.run(Runnable)`, register their JPMS module and enable scanning before injection. The runner awaits asynchronous startup and cleans up failed startup before an unsuccessful process exit.
- **Java 25 compilation** — generated Maven module POMs pin Compiler Plugin 3.15.0; JPMS descriptors require `com.guicedee.guicedinjection` for the application runner.
- **Explicit clustering** — separate Hazelcast cache-only and Vert.x cluster choices; generate TCP membership, advertised event-bus endpoints and local node examples
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

### Clustering and WebSocket continuity

Selecting Hazelcast creates an owned embedded cache member with `clustered=false` and `joinType=NONE`.
Selecting **Vert.x clustering (Hazelcast)** adds intentional TCP configuration in a dedicated `cluster/package-info.java`,
along with `cluster-a.env.example`, `cluster-b.env.example`, `single-instance.env.example`, and `CLUSTERING.md`.
Maven and Gradle use `com.guicedee:hazelcast` and JPMS requires `com.guicedee.guicedhazelcast`.
Example environment files must be loaded into separate processes explicitly; they are not automatically read.
The single-instance example disables both cluster and client activation. Configure only one server annotation per assembled application.

The Hazelcast server annotation intention starts with cache-only defaults. The annotation picker and dedicated
`@EventBusOptions` intention also support transport configuration. `VertxConfigurator` templates customize shared
`VertxOptions` before builder hooks, and destroy-hook templates use `shutdownSortOrder()`.
Server configuration SPIs are intentional configuration; do not create extra members or replace the built-in cluster manager.

Public broadcasts retain cluster fan-out; socket commands and private replies/storage remain owner-local.
Protected destinations require verified identity and atomic single-use session-bound capability handling.
On reconnect, acquire fresh capabilities, restore subscriptions and fetch authoritative state. Raw/STOMP sockets
bound slow peers and release consumers, groups and connection state. See the website's
[continuity guide](https://guicedee.com/capabilities#clustering-websocket-continuity) and
[activation reference](https://guicedee.com/environment-variables#hazelcast-clustering).

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

## Compatibility verification

Plugin 2.4.1 replaces the `StdModuleTypes` class removed in IntelliJ IDEA 2026.3,
uses public PSI APIs, and avoids the scheduled-for-removal `Query.iterator()` API.
Generated projects use GuicedEE 2.5.0.

Run the tests, build the ZIP and verify against the 2025.2 baseline and
2026.3 EAP build 263.6800.24 with JDK 21:

```powershell
$env:JAVA_HOME = 'C:/Software/jdk/21'
./gradlew.bat test buildPlugin verifyPlugin --console=plain
```

Compatibility problems, deprecated API usage and scheduled-for-removal API usage
fail verification. Reports are in `build/reports/pluginVerifier`; the upload ZIP
is `build/distributions/GEEIntelliJPlugin-2.5.0.zip`.

Verified on 14 October 2026 with Plugin Verifier 1.410: both IC-252.28539.33 and
IU-263.6800.24 report `Compatible`, with no deprecated or scheduled-for-removal
API usages. All 20 tests passed.

## Requirements

- IntelliJ IDEA 2025.2+ (Community or Ultimate; since build 252)
- Java, Maven, Gradle, and Properties plugins (bundled)

## License

See [LICENSE](LICENSE).

## Kotlin project and file templates

Choose **Kotlin** beside Maven/Gradle in the project wizard. Single-module and multi-module projects include
Kotlin 2.4.21, `com.guicedee:kotlin:2.6.0`, Kotlin source/test roots, compiler configuration, and JUnit tests.
The New GuicedEE File dialog offers Kotlin variants for all 56 class/interface/annotation templates.
The nine package-annotation templates remain Java `package-info.java` files; JPMS descriptors and native
registration/build helpers also remain Java. Kotlin and Java compile into the same named application module.
Classes and service methods are open where Guice interception may be used.

Generated Gradle tests assemble Java descriptors and Kotlin classes into a test module JAR before execution.
Maven tests use separate application/test module descriptors. Keep Kotlin sources under `src/main/kotlin`,
tests under `src/test/kotlin`, and descriptors under the corresponding Java roots.
For suspending work, inject `GuicedCoroutines` and return `coroutines.future { ... }` from ordinary REST methods.

Validation commands and coverage: [Kotlin generator validation](docs/kotlin-generation.md).
