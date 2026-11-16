package com.guicedee.intellij.wizard;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import java.nio.file.*;

public class KotlinWizardGenerationTest extends LightJavaCodeInsightFixtureTestCase {
    public void testKotlinFeatureTemplates() throws Exception {
        for (boolean gradle : new boolean[]{false, true}) {
            for (boolean nativeImage : new boolean[]{false, true}) {
                var builder = new GuicedEEProjectTemplateBuilder();
                var field = GuicedEEProjectTemplateBuilder.class.getDeclaredField("myWizardData");
                field.setAccessible(true);
                var data = (GuicedEEProjectWizardData) field.get(builder);
                data.setGroupId("example.features");
                data.setArtifactId("features");
                data.setKotlin(true);
                data.setUseGradle(gradle);
                data.setNativeImagePackaging(nativeImage);
                var module = data.addModule("Features", "features");
                module.setWebReactive(true);
                module.setWebReactiveRest(true);
                module.setWebReactiveWebSockets(true);
                module.setCloud(true);
                module.setFirebaseAdmin(true);
                module.setCommercetools(true);
                if (!nativeImage) {
                    module.setDatabase(true);
                    module.setDatabasePersistence(true);
                    module.setDatabasePostgreSQL(true);
                    module.setMessaging(true);
                    module.setMessagingRabbitMQ(true);
                    module.setCaching(true);
                    module.setCachingHazelcast(true);
                }
                Path root = Path.of("build/generated-kotlin/" + (gradle ? "gradle" : "maven") + (nativeImage ? "-native-sdk" : "-features")).toAbsolutePath();
                Files.createDirectories(root);
                var method = GuicedEEProjectTemplateBuilder.class.getDeclaredMethod("createSingleModuleProject", java.io.File.class);
                method.setAccessible(true);
                method.invoke(builder, root.toFile());
                try (var sources = Files.walk(root.resolve("src/main/kotlin"))) {
                    for (Path source : sources.filter(p -> p.toString().endsWith(".kt")).toList()) {
                        assertFalse("Unresolved template in " + source, Files.readString(source).contains("${"));
                    }
                }
                if (nativeImage) assertTrue(Files.readString(root.resolve("src/main/java/example/features/ApplicationDiscovery.java")).contains("example.features.WebApplication.class"));
                else assertTrue(Files.exists(root.resolve("src/main/kotlin/example/features/db/PostgresDBModule.kt")));
            }
        }
    }

    public void testKotlinMavenGradleSingleAndMultiModule() throws Exception {
        for (boolean gradle : new boolean[]{false, true}) {
            for (boolean multi : new boolean[]{false, true}) {
                var builder = new GuicedEEProjectTemplateBuilder();
                var field = GuicedEEProjectTemplateBuilder.class.getDeclaredField("myWizardData");
                field.setAccessible(true);
                var data = (GuicedEEProjectWizardData) field.get(builder);
                data.setGroupId("example.kotlin");
                data.setArtifactId("kotlin-starter");
                data.setKotlin(true);
                data.setUseGradle(gradle);
                data.setMultiModuleProject(multi);
                var module = data.addModule("Application", "kotlin-app");
                module.setWebReactive(true);
                module.setWebReactiveRest(true);
                Path root = Path.of("build/generated-kotlin/" + (gradle ? "gradle" : "maven") + (multi ? "-multi" : ""))
                        .toAbsolutePath();
                Files.createDirectories(root);
                var method = GuicedEEProjectTemplateBuilder.class.getDeclaredMethod(multi ? "createMultiModuleProject" : "createSingleModuleProject", java.io.File.class);
                method.setAccessible(true);
                method.invoke(builder, root.toFile());
                Path app = multi ? root.resolve("kotlin-app") : root;
                assertTrue(Files.exists(app.resolve("src/main/kotlin/example/kotlin/WebApplication.kt")));
                assertFalse(Files.exists(app.resolve("src/main/java/example/kotlin/WebApplication.java")));
                assertTrue(Files.readString(app.resolve("src/main/java/module-info.java")).contains("requires com.guicedee.kotlin;"));
                assertTrue(Files.readString(app.resolve(gradle ? "build.gradle.kts" : "pom.xml")).contains("2.4.21"));
                assertTrue(Files.exists(app.resolve("src/test/kotlin/example/kotlin/test/GeneratedProjectTest.kt")));
            }
        }
    }
}
