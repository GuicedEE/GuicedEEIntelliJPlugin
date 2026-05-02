plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.guicedee.intellij"
version = "2.0.1-SNAPSHOT"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
  intellijPlatform {
    create("IC", "2025.2.6.1")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

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
      <h3>2.0.1-SNAPSHOT</h3>
      <ul>
        <li>Merged JetBrains Guice plugin with GuicedEE framework support</li>
        <li>Full JIT (just-in-time) binding support — click-through navigation for concrete class injection</li>
        <li>Bidirectional gutter navigation between injection points and bound classes</li>
        <li>@Inject fields correctly marked as implicitly used, read, and written</li>
        <li>REST Service creation with optional service class and DB session support</li>
        <li>REST Client template with @Endpoint support</li>
        <li>Kafka Consumer template and Kafka wizard integration</li>
        <li>Mail Client template for GuicedEE mail module</li>
        <li>MicroProfile Health Check template</li>
        <li>Authentication and Authorization Provider templates</li>
        <li>Hazelcast caching support in project wizard</li>
        <li>MicroProfile Config, Health, Metrics, Telemetry, and OpenAPI wizard options</li>
        <li>REST Client checkbox in Web Reactive wizard options</li>
        <li>GuicedEE guice-core documentation and SPI extension points</li>
        <li>Support for @ImplementedBy and @ProvidedBy navigation</li>
        <li>Cassandra Module file template for Vert.x CassandraClient integration</li>
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

