package com.guicedee.intellij.wizard;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClusteringWizardGenerationTest extends LightJavaCodeInsightFixtureTestCase {
    public void testMavenAndGradleGenerateHazelcastDependencyAndModuleConfiguration() throws Exception {
        var builder = new GuicedEEProjectTemplateBuilder();
        var field = GuicedEEProjectTemplateBuilder.class.getDeclaredField("myWizardData");
        field.setAccessible(true);
        var data = (GuicedEEProjectWizardData) field.get(builder);
        data.setGroupId("example.orders");
        data.setArtifactId("orders");
        var module = new GuicedEEProjectWizardData.ModuleData("Orders", "orders");
        module.setVertxClustering(true);
        Path root = com.intellij.openapi.util.io.FileUtil.createTempDirectory("clustering-wizard", "test", true).toPath();
        Path source = Files.createDirectories(root.resolve("src/main/java"));
        invoke(builder, "createModuleInfo", source, module);
        assertTrue(Files.readString(source.resolve("module-info.java")).contains("requires transitive com.guicedee.guicedhazelcast"));
        assertTrue(Files.exists(source.resolve("example/orders/cluster/package-info.java")));
        assertTrue(Files.exists(root.resolve("cluster-a.env.example")));
        invoke(builder, "createPomXml", root, module);
        String pom = Files.readString(root.resolve("pom.xml"));
        assertTrue(pom.contains("<groupId>com.guicedee</groupId>"));
        assertTrue(pom.contains("<artifactId>hazelcast</artifactId>"));
        invoke(builder, "createGradleBuildFile", root, module);
        assertTrue(Files.readString(root.resolve("build.gradle.kts")).contains("implementation(\"com.guicedee:hazelcast\")"));
    }

    private void invoke(GuicedEEProjectTemplateBuilder builder, String name, Path directory,
                        GuicedEEProjectWizardData.ModuleData module) throws Exception {
        var method = GuicedEEProjectTemplateBuilder.class.getDeclaredMethod(name, java.io.File.class,
                GuicedEEProjectWizardData.ModuleData.class);
        method.setAccessible(true);
        method.invoke(builder, directory.toFile(), module);
    }
}
