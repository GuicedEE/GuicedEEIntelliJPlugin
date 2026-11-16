# Kotlin generator validation

Validated locally on 2026-11-16 with GuicedEE 2.6.0 artifacts, JDK 25, Maven 4 and Gradle 9.4.1.
The IntelliJ plugin itself builds with its JBR 21 toolchain.

| Generated project | Maven verify | Gradle build |
| --- | --- | --- |
| Plugin REST starter | Pass | Pass |
| Plugin multi-module REST starter | Pass | Pass |
| Plugin persistence, RabbitMQ, Hazelcast, WebSockets, Firebase and Commercetools | Pass | Pass |
| Plugin native starter with Firebase and Commercetools | Pass (JVM build) | Pass (JVM build) |
| Website Kotlin starter | Pass | Pass |
| Website native starter with Firebase and Commercetools | Pass (JVM build) | Pass (JVM build) |

Each generated project executes its Kotlin JUnit test. The plugin REST starter additionally responds over HTTP
with an injected Kotlin service while running on the full application module path. All 56 Kotlin file templates
compile against the actual local framework APIs. Nine Java package-annotation templates remain Java intentionally.
Native starter checks compile the SPI/reachability scaffolding; they do not build a Kotlin native executable or
exercise cloud services. Database and broker feature checks compile the generated examples, without connecting
to external databases or brokers. Publication and deployment are separate actions.

Reproduce plugin generation and template checks:

```powershell
# From the plugin checkout, JBR 21:
.\gradlew.bat test --tests '*KotlinWizardGenerationTest' --tests '*KotlinFileTemplateTest' --tests '*SdkNativeWizardGenerationTest' buildPlugin

# JDK 25 and Maven 4:
python scripts/verify-kotlin-templates.py
mvn -f build/kotlin-template-fixture/pom.xml clean verify
mvn -f build/generated-kotlin/maven/pom.xml clean verify
mvn -f build/generated-kotlin/maven-multi/pom.xml clean verify
gradle -p build/generated-kotlin/gradle clean build
gradle -p build/generated-kotlin/gradle-multi clean build

mvn -f build/generated-kotlin/maven/pom.xml dependency:copy-dependencies -DoutputDirectory=target/runtime -DincludeScope=runtime
python scripts/verify-kotlin-runtime.py
```

Before release, Gradle needs access to the locally installed 2.6.0 artifacts. Supply an init script containing
`allprojects { repositories { mavenLocal() } }` using `--init-script <absolute-path>` during validation.
The distributed build files use Maven Central. Generated fixtures live under `build/generated-kotlin`.

The website's `tools/verify-kotlin-builder.cjs` exercises the actual language selector and downloads Maven/Gradle
ZIPs from the production Angular bundle, including the native SDK selections. Set `PLAYWRIGHT_MODULE` to the
installed Playwright module path. Build the resulting projects in `website/target/kotlin-generated`.

Kotlin Maven starters also include the Maven 4 wrapper and Mockito test dependency. The creator skill's
Java-only baseline checker passes the build/module checks but reports its Java bootstrap check as missing:
the entry point is intentionally in Kotlin. Generated-project tests verify its JVM static `main` method,
and the HTTP smoke test verifies that it starts the application.
