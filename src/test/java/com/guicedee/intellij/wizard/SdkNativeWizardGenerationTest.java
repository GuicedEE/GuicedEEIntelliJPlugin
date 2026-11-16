package com.guicedee.intellij.wizard;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;

public class SdkNativeWizardGenerationTest extends LightJavaCodeInsightFixtureTestCase {
    public void testPackagingChoicesAreExclusiveAndSdkControlsPersist() throws Exception {
        var data = new GuicedEEProjectWizardData();
        var step = new GuicedEEProjectWizardStep(null, data);
        var nativeChoice = checkbox(step, "myNativeImageCheckBox");
        var jlinkChoice = checkbox(step, "myJlinkPackagingCheckBox");
        jlinkChoice.doClick();
        nativeChoice.doClick();
        assertTrue(nativeChoice.isSelected());
        assertFalse(jlinkChoice.isSelected());
        jlinkChoice.doClick();
        assertFalse(nativeChoice.isSelected());
        assertTrue(jlinkChoice.isSelected());
        nativeChoice.doClick();
        checkbox(step, "myModuleCloudCheckBox").doClick();
        checkbox(step, "myFirebaseAdminCheckBox").doClick();
        checkbox(step, "myCommercetoolsCheckBox").doClick();
        step.updateDataModel();
        assertTrue(data.isNativeImagePackaging());
        assertFalse(data.isJlinkPackaging());
        assertTrue(data.getModules().get(0).isFirebaseAdmin());
        assertTrue(data.getModules().get(0).isCommercetools());
    }

    private javax.swing.JCheckBox checkbox(GuicedEEProjectWizardStep step, String name) throws Exception {
        var field = GuicedEEProjectWizardStep.class.getDeclaredField(name);
        field.setAccessible(true);
        return (javax.swing.JCheckBox) field.get(step);
    }

    public void testSdkSelectionsAreIndependentInMavenAndGradle() throws Exception {
        for (boolean gradle : new boolean[]{false, true}) {
            for (boolean firebase : new boolean[]{false, true}) {
                var builder = new GuicedEEProjectTemplateBuilder();
                var data = data(builder);
                data.setUseGradle(gradle);
                var module = data.addModule("Orders", "orders");
                module.setCloud(true);
                module.setFirebaseAdmin(firebase);
                module.setCommercetools(!firebase);
                Path root = generate(builder, "createSingleModuleProject");
                String build = Files.readString(root.resolve(gradle ? "build.gradle.kts" : "pom.xml"));
                String descriptor = Files.readString(root.resolve("src/main/java/module-info.java"));
                assertEquals(firebase, build.contains("firebase-admin"));
                assertEquals(!firebase, build.contains("commercetools"));
                assertEquals(firebase, descriptor.contains("requires firebase.admin;"));
                assertEquals(!firebase, descriptor.contains("requires commercetools.sdk.java.api;"));
                assertFalse(build.contains("com.guicedee:native-image"));
                assertFalse(Files.exists(root.resolve("NativeBuild.java")));
                if (!gradle) DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(root.resolve("pom.xml").toFile());
            }
        }
    }

    public void testNativeStarterForSingleAndMultiModuleMavenAndGradle() throws Exception {
        for (boolean gradle : new boolean[]{false, true}) {
            for (boolean multi : new boolean[]{false, true}) {
                var builder = new GuicedEEProjectTemplateBuilder();
                var data = data(builder);
                data.setUseGradle(gradle);
                data.setMultiModuleProject(multi);
                data.setNativeImagePackaging(true);
                var module = data.addModule("Orders", "orders");
                module.setCloud(true);
                module.setFirebaseAdmin(true);
                module.setCommercetools(true);
                Path project = generate(builder, multi ? "createMultiModuleProject" : "createSingleModuleProject");
                Path root = multi ? project.resolve("orders") : project;
                String descriptor = Files.readString(root.resolve("src/main/java/module-info.java"));
                assertTrue(descriptor.contains("requires com.guicedee.nativeimage;"));
                assertTrue(descriptor.contains("provides com.guicedee.client.services.IGuiceDiscovery"));
                assertTrue(descriptor.contains("provides com.guicedee.nativeimage.GuiceNativeConfiguration"));
                Path source = root.resolve("src/main/java/example/orders");
                String application = Files.readString(source.resolve("Application.java"));
                assertTrue(application.contains("System.setProperty(\"GUICEDEE_DISCOVERY_MODE\", \"spi\")"));
                assertFalse(application.contains("setServiceLoadWithClassPath(true)"));
                assertTrue(Files.readString(source.resolve("ApplicationDiscovery.java")).contains("example.orders.Application.class"));
                assertTrue(Files.readString(source.resolve("NativeConfiguration.java")).contains("registration.reflect("));
                assertTrue(Files.readString(source.resolve("NativeConfiguration.java")).contains("Integer.class"));
                assertTrue(Files.readString(source.resolve("NativeConfiguration.java")).contains("com.guicedee.client.scopes.CallScopeProperties"));
                assertTrue(Files.readString(root.resolve("src/main/resources/META-INF/native-image/example.orders/orders/reachability-metadata.json"))
                        .contains("com.guicedee.guicedinjection.injections.ContextBinderGuice"));
                for (String spi : new String[]{"com.guicedee.client.services.IGuiceDiscovery", "com.guicedee.nativeimage.GuiceNativeConfiguration"}) {
                    assertTrue(Files.exists(root.resolve("src/main/resources/META-INF/services/" + spi)));
                }
                assertFalse(Files.readString(root.resolve("NATIVE.md")).contains("@ARTIFACT@"));
                assertTrue(Files.readString(root.resolve("NATIVE.md")).contains("orders/example.orders.Application"));
                String build = Files.readString(root.resolve(gradle ? "build.gradle.kts" : "pom.xml"));
                assertTrue(build.contains("native-image"));
                assertTrue(build.contains("firebase-admin"));
                assertTrue(build.contains("commercetools"));
                if (gradle) assertTrue(build.contains("copyNativeDependencies"));
                else DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(root.resolve("pom.xml").toFile());
            }
        }
    }

    private GuicedEEProjectWizardData data(GuicedEEProjectTemplateBuilder builder) throws Exception {
        var field = GuicedEEProjectTemplateBuilder.class.getDeclaredField("myWizardData");
        field.setAccessible(true);
        var data = (GuicedEEProjectWizardData) field.get(builder);
        data.setGroupId("example.orders");
        data.setArtifactId("orders");
        return data;
    }

    private Path generate(GuicedEEProjectTemplateBuilder builder, String name) throws Exception {
        Path root = com.intellij.openapi.util.io.FileUtil.createTempDirectory("sdk-native-wizard", "test", true).toPath();
        var method = GuicedEEProjectTemplateBuilder.class.getDeclaredMethod(name, java.io.File.class);
        method.setAccessible(true);
        method.invoke(builder, root.toFile());
        return root;
    }
}
