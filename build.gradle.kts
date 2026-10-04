plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.guicedee.intellij"
version = "2.3.0"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
  testImplementation("junit:junit:4.13.2")

  intellijPlatform {
    create("IC", "2025.2.6.1")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Plugin.Java)

    // Add necessary plugin dependencies for compilation here
    bundledPlugin("com.intellij.java")
    bundledPlugin("org.jetbrains.plugins.gradle")
    bundledPlugin("org.jetbrains.idea.maven")
    bundledPlugin("com.intellij.properties")
  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "252"
    }

    changeNotes = """
      <h3>2.3.0</h3>
      <ul>
        <li>Explicit Vert.x clustering wizard option with TCP membership, advertised event-bus endpoints and isolated node examples</li>
        <li>Hazelcast cache-only scaffolding and annotation intentions remain unclustered; event-bus options have a dedicated intention</li>
        <li>Vert.x configurator templates compose shared options; destroy hooks use shutdownSortOrder</li>
        <li>Generated Maven and Gradle projects use GuicedEE and test BOMs 2.3.0</li>
        <li>Local dependency scope support for injected constructor parameters and fields</li>
        <li>Scope conflict inspections recognise custom scope annotations; lifetime inspections include local request and session scopes</li>
        <li>Scope annotation templates include type, method, parameter, and field targets</li>
        <li>Guice documentation explains consumer-local caching, wrapped binding scopes, and scope SPI registration</li>
      </ul>
      <h3>2.2.0</h3>
      <ul>
        <li>GuicedEE 2.2.0 baseline — generated projects now reference com.guicedee BOMs 2.2.0 (Vert.x 5.1.5, Jackson 3.2.1, Hibernate ORM 7.4.3)</li>
        <li>GraphQL instrumentation fix — Vert.x future and JSON object adapters now combine through a single ChainedInstrumentation</li>
        <li>GraphQL dependency hygiene — GraphQL-Java's shaded Guava copy stripped and rewired to canonical com.google.common (clean JPMS graph, tracks Guava CVE fixes)</li>
        <li>Telemetry — module now supports 3 new endpoints, with its underlying dependency upgraded to the latest version</li>
        <li>Hibernate ORM 7.4.3 + Hibernate Reactive 4.5.1 — version-locked pair; hibernate-maven-plugin and hibernate-processor pinned to the same ORM build</li>
        <li>Security updates — pgJDBC 42.7.13, SCRAM 3.4, Jackson 3.2.1 and Spring 6.2.19 close four advisories; Bouncy Castle 1.85</li>
        <li>Deterministic BOM resolution — GuicedEE BOMs no longer emit Maven 4 "Ignored POM import" model problems</li>
      </ul>
      <h3>2.1.0</h3>
      <ul>
        <li>Cloud support — Service Registry, Consul, Consul Service Resolver, and Azure Container Apps wizard options</li>
        <li>Cloud intention actions — quick-add @ServiceRegistryOptions, @RegisteredService, and @AzureContainerApps annotations to package-info</li>
        <li>IBM MQ intention action — quick-add @IBMMQConnectionOptions annotation</li>
        <li>Verticle intention action — quick-add verticle scaffolding</li>
        <li>Queue Publisher intention action — inject QueuePublisher fields</li>
        <li>Annotation dialog — unified "Add Annotation" dialog for all package-info annotations</li>
        <li>IntelliJ Platform 2025.2.6.1 baseline (sinceBuild 252)</li>
        <li>Platform Gradle Plugin updated to 2.5.0</li>
      </ul>
      <h3>2.0.1</h3>
      <ul>
        <li>Added Gradle project generation support (Gradle 9.0+ with JDK 25 toolchain)</li>
        <li>Build tool selection (Maven / Gradle) in project wizard</li>
        <li>Kafka Consumer file template and wizard integration</li>
        <li>IBM MQ Consumer file template and wizard integration</li>
        <li>Cassandra Module file template for Vert.x Cassandra client</li>
        <li>MongoDB Module file template for Vert.x Mongo client</li>
        <li>Redis Module file template for Vert.x Redis integration</li>
        <li>HTTP Proxy Module file template for Vert.x HttpProxy reverse proxy</li>
        <li>Persistence template updated to use ConnectionBaseInfoFactory</li>
        <li>Updated GuicedEE brand colour in SVG icons</li>
        <li>Removed deprecated Query.forEach(Consumer) usage</li>
      </ul>
      <h3>2.0.0</h3>
      <ul>
        <li>Merged JetBrains Guice plugin with GuicedEE framework support</li>
        <li>Full JIT (just-in-time) binding support — click-through navigation for concrete class injection</li>
        <li>Bidirectional gutter navigation between injection points and bound classes</li>
        <li>@Inject fields correctly marked as implicitly used, read, and written</li>
        <li>REST Service creation with optional service class and DB session support</li>
        <li>REST Client template with @Endpoint support</li>
        <li>Mail Client template for GuicedEE mail module</li>
        <li>MicroProfile Health Check template</li>
        <li>Authentication and Authorization Provider templates</li>
        <li>Hazelcast caching support in project wizard</li>
        <li>MicroProfile Config, Health, Metrics, Telemetry, and OpenAPI wizard options</li>
        <li>REST Client checkbox in Web Reactive wizard options</li>
        <li>GuicedEE guice-core documentation and SPI extension points</li>
        <li>Support for @ImplementedBy and @ProvidedBy navigation</li>
      </ul>
    """.trimIndent()
  }
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
  }
}
