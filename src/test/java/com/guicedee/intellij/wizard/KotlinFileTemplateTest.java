package com.guicedee.intellij.wizard;

import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class KotlinFileTemplateTest extends LightJavaCodeInsightFixtureTestCase {
    public void testRegisteredKotlinTemplateCreatesKotlinFile() throws Exception {
        var dir = PsiManager.getInstance(getProject()).findDirectory(
                myFixture.getTempDirFixture().findOrCreateDir("src/main/kotlin/example/generated"));
        var template = FileTemplateManager.getInstance(getProject()).getJ2eeTemplate("GuicedEEModule.kt");
        var props = new java.util.Properties();
        props.setProperty("PACKAGE_NAME", "example.generated");
        props.setProperty("NAME", "ApplicationModule");
        WriteCommandAction.writeCommandAction(getProject()).run(() ->
                FileTemplateUtil.createFromTemplate(template, "ApplicationModule", props, dir));
        var file = dir.findFile("ApplicationModule.kt");
        assertNotNull(file);
        assertTrue(file.getText().contains("open class ApplicationModule : AbstractModule(), IGuiceModule<ApplicationModule>"));
        assertFalse(file.getText().contains("${"));
    }

    public void testKotlinSourceRootMatchesSiblingJavaModuleDescriptor() throws Exception {
        var directory = myFixture.getTempDirFixture().findOrCreateDir("orders/src/main/kotlin/example/orders");
        var action = Class.forName("com.guicedee.intellij.actions.GuicedEENewFileActionGroup$CreateGuicedEEFileAction");
        var constructor = action.getDeclaredConstructor(String.class, String.class, String.class);
        constructor.setAccessible(true);
        var instance = constructor.newInstance("Module", "Module", "GuicedEEModule.java");
        var finder = action.getDeclaredMethod("findSourceRoot", com.intellij.openapi.vfs.VirtualFile.class);
        finder.setAccessible(true);
        var root = (com.intellij.openapi.vfs.VirtualFile) finder.invoke(instance, directory);
        assertNotNull(root);
        assertTrue(root.getPath().endsWith("orders/src/main/kotlin"));
        assertEquals("main", root.getParent().getName());
    }
}
