# Native image starter

Use GraalVM for JDK 25 with Native Image and the platform's native compiler toolchain.
The generated application uses SPI discovery on both the JVM and native runtime.
Keep ApplicationDiscovery and NativeConfiguration up to date as you add classes.
Both providers are registered in module-info.java and META-INF/services.

Build with Maven 4 from this application module (install sibling modules first in a multi-module project):

```sh
mvn package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/native-libs
java NativeBuild.java target/@ARTIFACT@-@VERSION@.jar target/native-libs @MODULE@/@MAIN@
native-image @target/native-image.args
```

Or with Gradle:

```sh
gradle jar copyNativeDependencies
java NativeBuild.java build/libs/@ARTIFACT@-@VERSION@.jar build/native-libs @MODULE@/@MAIN@
native-image @target/native-image.args
```

On PowerShell, quote the final argument: native-image '@target/native-image.args'.
Run target/native/application (application.exe on Windows). GUICEDEE_DISCOVERY_MODE=spi
is set by the generated entry point. JVM JAR builds remain available.

NativeBuild enumerates the full application module path, including every dependency JAR,
so embedded library metadata can be discovered. It prepares arguments; it does not run
Native Image or start your application during generation.

This is an opt-in starter, not a claim that every selected integration is native-ready.
NativeConfiguration registers generated application types for reflection. Add explicit
interceptor selections with registration.intercept(...) for the methods your bindings intercept.
Add workload-specific resource, proxy and reflection metadata under META-INF/native-image
and validate actual startup and requests. Firebase and Commercetools integrations need
metadata for the SDK operations you use; the benchmark's metadata is workload-specific.
Keep metadata providers free of credentials, network clients and application initialization.

See https://github.com/GuicedEE/benchmarks for measured application recipes and
examples of workload-specific metadata.
