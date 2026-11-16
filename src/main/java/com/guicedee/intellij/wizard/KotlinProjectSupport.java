package com.guicedee.intellij.wizard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Installs Kotlin templates after shared resource/JPMS scaffolding has been generated. */
final class KotlinProjectSupport {
    private KotlinProjectSupport() {}

    static void write(Path root, GuicedEEProjectWizardData data, GuicedEEProjectWizardData.ModuleData module) throws IOException {
        String pkg = data.getGroupId();
        String moduleName = module.getArtifactId().replace('-', '.');
        if (!Character.isJavaIdentifierStart(moduleName.charAt(0))) moduleName = "m." + moduleName;
        Path java = root.resolve("src/main/java");
        Path kotlin = root.resolve("src/main/kotlin");
        try (var paths = Files.walk(java)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString().replace(".java", "");
                if (name.equals("module-info") || name.equals("package-info")) continue;
                String original = Files.readString(file);
                var matcher = Pattern.compile("package ([\\w.]+);").matcher(original);
                if (!matcher.find()) throw new IOException("Missing package in " + file);
                String sourcePkg = matcher.group(1);
                Map<String, String> values = new HashMap<>();
                values.put("PACKAGE_NAME", sourcePkg);
                values.put("NAME", name);
                values.put("SERVICE_NAME", name);
                values.put("MODULE_NAME", moduleName);
                values.put("PATH", "resources");
                values.put("QUEUE_NAME", "example-queue");
                values.put("CONNECTION_NAME", "default");
                values.put("KEYSPACE", "application");
                values.put("DATABASE_NAME", "application");
                values.put("REDIS_HOST", "localhost");
                values.put("REDIS_PORT", "6379");
                values.put("REDIS_DATABASE", "0");
                values.put("REDIS_NAME", "default");
                values.put("MAX_POOL_SIZE", "8");
                String source;
                if (name.equals("Application") || name.equals("WebApplication")) {
                    source = """
                            package %s
                            import com.guicedee.client.IGuiceContext
                            import com.guicedee.guicedinjection.GuiceApplication
                            import com.guicedee.vertx.spi.VertX
                            import com.guicedee.vertx.spi.Verticle
                            @VertX(blockedThreadCheckInterval = 10000)
                            @Verticle("my-app")
                            class %s {
                                companion object {
                                    @JvmStatic
                                    fun main(args: Array<String>) {
                                        %s
                                        GuiceApplication.run {
                                            IGuiceContext.registerModule("%s")
                                            IGuiceContext.getContext().getConfig().setServiceLoadWithClassPath(%s)
                                        }
                                    }
                                }
                            }
                            """.formatted(sourcePkg, name,
                            (data.isNativeImagePackaging() ? "System.setProperty(\"GUICEDEE_DISCOVERY_MODE\", \"spi\")\n" : "") +
                            (module.isWebReactive() ? "System.setProperty(\"HTTP_ENABLED\", \"true\")\nSystem.setProperty(\"HTTPS_ENABLED\", \"false\")" : ""),
                            moduleName, !data.isNativeImagePackaging());
                } else if (name.equals("YourEntity")) {
                    source = "package " + sourcePkg + "\n\n@jakarta.persistence.Entity\nopen class YourEntity {\n    @field:jakarta.persistence.Id\n    open var id: String = \"\"\n}\n";
                } else if (name.equals("DiscoveryPackage")) {
                    source = "package " + sourcePkg + "\n\nopen class " + name + "\n";
                } else if (name.equals("ApplicationDiscovery") || name.equals("NativeConfiguration")) {
                    // Native registration remains Java interop glue alongside module-info.java.
                    continue;
                } else if (module.isDatabase() && !module.isDatabaseCassandra() && !module.isDatabaseMongoDB()
                        && (name.equals("RestResource") || name.equals("RestResourceService"))) {
                    source = resource("/kotlin-starter/" + name + ".kt.txt").replace("${BASE_PACKAGE}", pkg);
                } else {
                    String template;
                    if (name.equals("RestResource")) template = "RestService";
                    else if (name.equals("RestResourceService")) {
                        template = "RestServiceImpl";
                        values.put("PACKAGE_NAME", sourcePkg.substring(0, sourcePkg.lastIndexOf('.')));
                    } else if (name.equals("ExampleConsumer")) template = "RabbitMQConsumer";
                    else if (original.contains("IWebSocketMessageReceiver")) template = "WebSocketMessageReceiver";
                    else if (original.contains("IGuiceScanModuleInclusions")) template = "ScanModuleInclusions";
                    else if (name.endsWith("DBModule")) {
                        template = module.isDatabaseCassandra() ? "CassandraModule" : module.isDatabaseMongoDB() ? "MongoDBModule" : module.isDatabaseRedis() ? "RedisModule" : "PersistenceModule";
                        String unit = module.isDatabaseMySQL() ? "mysql" : module.isDatabaseOracle() ? "oracle" : module.isDatabaseDB2() ? "db2" : module.isDatabaseSqlServer() ? "sqlserver" : module.isDatabaseJDBC() ? "jdbc" : "postgres";
                        values.put("PERSISTENCE_UNIT", unit);
                        values.put("DATABASE_TYPE", unit.equals("postgres") ? "postgresql" : unit);
                    } else throw new IOException("No Kotlin template for " + file);
                    source = resource("/fileTemplates/j2ee/GuicedEE" + template + ".kt.ft");
                    var qualifier = Pattern.compile("(?m)^@com\\.guicedee\\.persistence\\.annotations\\.EntityManager\\([^\\n]+\\)").matcher(original);
                    if (qualifier.find()) source = source.replace("open class", qualifier.group() + "\nopen class");
                    for (var value : values.entrySet()) source = source.replace("${" + value.getKey() + "}", value.getValue());
                    if (source.contains("${")) throw new IOException("Unresolved Kotlin template in " + file);
                }
                Path destination = kotlin.resolve(java.relativize(file).toString().replace(".java", ".kt"));
                Files.createDirectories(destination.getParent());
                Files.writeString(destination, source);
                Files.delete(file);
            }
        }
        Path descriptor = java.resolve("module-info.java");
        String descriptorText = Files.readString(descriptor).replace("requires static lombok;", "").replace("requires lombok;", "");
        descriptorText = descriptorText.replaceFirst("\\{", "{\n    requires com.guicedee.kotlin;\n");
        // Every Kotlin package can contain injected classes or DTOs.
        try (var sources = Files.walk(kotlin)) {
            for (String packageName : sources.filter(p -> p.toString().endsWith(".kt"))
                    .map(p -> kotlin.relativize(p.getParent()).toString().replace('\\', '.').replace('/', '.')).distinct().toList()) {
                if (!descriptorText.contains("opens " + packageName + " ") && !descriptorText.contains("opens " + packageName + ";"))
                    descriptorText = descriptorText.replace("\n}", "\n    opens " + packageName + " to com.google.guice, com.guicedee.vertx, com.guicedee.rest, tools.jackson.databind, tools.jackson.module.kotlin, kotlin.reflect;\n}");
            }
        }
        if (module.isDatabase() && descriptorText.contains("opens " + pkg + ".db.entity to "))
            descriptorText = descriptorText.replace("opens " + pkg + ".db.entity to ", "opens " + pkg + ".db.entity to org.hibernate.orm.core, ");
        Files.writeString(descriptor, descriptorText);
        Path pom = root.resolve("pom.xml");
        if (Files.exists(pom)) {
            writeMavenWrapper(root);
            String text = Files.readString(pom).replaceAll("(?s)<dependency>\\s*<groupId>org.projectlombok</groupId>.*?</dependency>", "");
            int dependencies = text.indexOf("<dependencies>", text.indexOf("</dependencyManagement>") + 1);
            if (dependencies < 0) dependencies = text.indexOf("<dependencies>");
            text = text.substring(0, dependencies) + text.substring(dependencies).replaceFirst("<dependencies>", "<dependencies>\n" +
                    "<dependency><groupId>com.guicedee</groupId><artifactId>kotlin</artifactId><version>2.6.0</version></dependency>\n" +
                    "<dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><version>5.24.0</version><scope>test</scope></dependency>\n" +
                    "<dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>6.0.3</version><scope>test</scope></dependency>");
            String plugins = resource("/kotlin-starter/maven-plugins.xml.txt");
            if (text.contains("<plugins>")) text = text.replace("<plugins>", "<plugins>\n" + plugins);
            else text = text.replace("</project>", "<build><plugins>" + plugins + "</plugins></build></project>");
            Files.writeString(pom, text);
        } else {
            Path gradle = root.resolve("build.gradle.kts");
            String text = Files.readString(gradle).replaceAll("(?m)^.*org.projectlombok.*\\R?", "");
            String plugin = "    kotlin(\"jvm\") version \"2.4.21\"\n";
            if (text.contains("plugins {")) text = text.replace("plugins {", "plugins {\n" + plugin);
            else text = "plugins {\n" + plugin + "}\n" + text;
            text = text.replace("dependencies {", "dependencies {\n    implementation(\"com.guicedee:kotlin:2.6.0\")\n" +
                    "    testImplementation(\"org.junit.jupiter:junit-jupiter:6.0.3\")\n    testRuntimeOnly(\"org.junit.platform:junit-platform-launcher:6.0.3\")\n");
            Files.writeString(gradle, text + "\n" + resource("/kotlin-starter/gradle-tasks.txt").replace("@MODULE@", moduleName));
        }
        Path test = root.resolve("src/test/kotlin/" + pkg.replace('.', '/') + "/test/GeneratedProjectTest.kt");
        Files.createDirectories(test.getParent());
        Files.writeString(test, """
                package %s.test
                import org.junit.jupiter.api.Assertions.assertNotNull
                import org.junit.jupiter.api.Test
                class GeneratedProjectTest {
                    @Test fun applicationHasJvmEntryPoint() {
                        assertNotNull(%s.%s::class.java.getMethod("main", Array<String>::class.java))
                    }
                }
                """.formatted(pkg, pkg, module.isWebReactive() ? "WebApplication" : "Application"));
        Path testDescriptor = root.resolve("src/test/java/module-info.java");
        Files.writeString(testDescriptor, Files.readString(testDescriptor).replaceFirst("\\{", "{\n    requires kotlin.stdlib;\n    opens " + pkg + ".test to org.junit.platform.commons;\n"));
    }

    static void writeMavenWrapper(Path root) throws IOException {
        Files.createDirectories(root.resolve(".mvn/wrapper"));
        Files.writeString(root.resolve(".mvn/wrapper/maven-wrapper.properties"), resource("/kotlin-starter/maven-wrapper.properties"));
        Files.writeString(root.resolve("mvnw"), resource("/kotlin-starter/mvnw.txt"));
        root.resolve("mvnw").toFile().setExecutable(true, false);
        Files.writeString(root.resolve("mvnw.cmd"), resource("/kotlin-starter/mvnw.cmd.txt"));
    }

    private static String resource(String path) throws IOException {
        try (var input = KotlinProjectSupport.class.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing Kotlin template: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
