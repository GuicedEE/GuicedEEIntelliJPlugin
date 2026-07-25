package com.guicedee.intellij.actions;

import com.guicedee.intellij.GuicedIcons;
import com.intellij.ide.IdeView;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a new GuicedEE {@code Provider} implementation with a default {@code get()} method.
 * <p>
 * When the provided type is generic (e.g. {@code Farm<?>}), an explicit
 * {@code bind(new TypeLiteral<Farm<?>>(){}).toProvider(...)} binding is added to the
 * selected Guice module, since Guice cannot just-in-time bind a generic type.
 * <p>
 * When persistence is requested, the generated provider injects a
 * {@code Mutiny.SessionFactory} (and optionally a service), and guards {@code get()}
 * against the injector still building or a closed session factory.
 */
public class CreateProviderAction extends AnAction implements DumbAware {

    private static final Logger LOGGER = Logger.getInstance(CreateProviderAction.class);

    private static final String ABSTRACT_MODULE = "com.google.inject.AbstractModule";
    private static final String GUICEDEE_MODULE = "com.guicedee.client.services.lifecycle.IGuiceModule";

    public CreateProviderAction() {
        super("Provider", "Create a new Guice Provider with a default get()", GuicedIcons.Logo);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean enabled = project != null && getTargetDirectory(e) != null;
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiDirectory directory = getTargetDirectory(e);
        if (directory == null) {
            Messages.showErrorDialog(project, "Please select a target directory.", "Create Provider");
            return;
        }

        List<PsiClass> moduleCandidates = findModuleCandidates(project);
        ProviderOptionsDialog dialog = new ProviderOptionsDialog(project, moduleCandidates);
        if (!dialog.showAndGet()) {
            return;
        }

        final String providerName = dialog.getProviderName();
        final String providedType = dialog.getProvidedType();
        final boolean generic = dialog.isGeneric();
        final boolean usePersistence = dialog.isUsePersistence();
        final String serviceType = dialog.getServiceType();
        final boolean callScope = dialog.isCallScope();
        final PsiClass targetModule = dialog.getSelectedModule();

        final String packageName = getPackageName(directory);
        final String source = buildProviderSource(packageName, providerName, providedType,
                usePersistence, serviceType, callScope);

        WriteCommandAction.runWriteCommandAction(project, "Create Provider", null, () -> {
            try {
                PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
                PsiFile newFile = fileFactory.createFileFromText(providerName + ".java", JavaFileType.INSTANCE, source);
                PsiElement shortened = JavaCodeStyleManager.getInstance(project).shortenClassReferences(newFile);
                PsiElement reformatted = CodeStyleManager.getInstance(project).reformat(shortened);
                PsiElement added = directory.add(reformatted);

                // Add the generic binding to the chosen module
                if (generic && targetModule != null) {
                    String providerFqn = (packageName == null || packageName.isEmpty())
                            ? providerName : packageName + "." + providerName;
                    addGenericBinding(project, targetModule, providedType, providerFqn);
                }

                // Ensure the persistence dependency is wired up
                if (usePersistence) {
                    ensurePersistenceDependency(project, directory);
                }

                navigateTo(project, added);
            } catch (Exception ex) {
                LOGGER.error(ex);
                Messages.showErrorDialog(project, "Failed to create provider: " + ex.getMessage(), "Create Provider");
            }
        });
    }

    // ----- source generation -----

    private String buildProviderSource(String packageName, String providerName, String providedType,
                                       boolean usePersistence, String serviceType, boolean callScope) {
        StringBuilder out = new StringBuilder(1024);
        if (packageName != null && !packageName.isEmpty()) {
            out.append("package ").append(packageName).append(";\n\n");
        }

        out.append("/**\n");
        out.append(" * Guice Provider for {@code ").append(providedType).append("}.\n");
        out.append(" */\n");

        if (usePersistence) {
            out.append("@lombok.extern.log4j.Log4j2\n");
        }
        if (callScope) {
            out.append("@com.guicedee.client.scopes.CallScope\n");
        }
        out.append("public class ").append(providerName)
           .append(" implements com.google.inject.Provider<").append(providedType).append("> {\n\n");

        boolean hasService = usePersistence && serviceType != null && !serviceType.isEmpty();
        if (hasService) {
            out.append("    @com.google.inject.Inject\n");
            out.append("    private ").append(serviceType).append(' ').append(serviceFieldName(serviceType)).append(";\n\n");
        }
        if (usePersistence) {
            out.append("    @com.google.inject.Inject\n");
            out.append("    private org.hibernate.reactive.mutiny.Mutiny.SessionFactory sessionFactory;\n\n");
        }

        out.append("    @Override\n");
        out.append("    public ").append(providedType).append(" get() {\n");
        if (usePersistence) {
            out.append("        if (com.guicedee.client.IGuiceContext.instance().isBuildingInjector() || !sessionFactory.isOpen()) {\n");
            out.append("            return null;\n");
            out.append("        }\n");
            out.append("        // TODO: load and return the provided value using the injected ");
            out.append(hasService ? "service and sessionFactory" : "sessionFactory").append(", e.g.:\n");
            out.append("        // return sessionFactory.withSession(session -> ").append(hasService ? serviceFieldName(serviceType) + ".load(session)" : "io.smallrye.mutiny.Uni.createFrom().nullItem()").append(")\n");
            out.append("        //         .await().atMost(java.time.Duration.ofSeconds(5));\n");
            out.append("        return null;\n");
        } else {
            out.append("        // TODO: add provider logic here\n");
            out.append("        return null;\n");
        }
        out.append("    }\n");
        out.append("}\n");
        return out.toString();
    }

    private static String serviceFieldName(String serviceType) {
        String raw = serviceType;
        int lt = raw.indexOf('<');
        if (lt >= 0) {
            raw = raw.substring(0, lt);
        }
        int dot = raw.lastIndexOf('.');
        String simple = dot >= 0 ? raw.substring(dot + 1) : raw;
        // Strip a leading interface "I" prefix (e.g. IFarmService -> FarmService)
        if (simple.length() > 1 && simple.charAt(0) == 'I' && Character.isUpperCase(simple.charAt(1))) {
            simple = simple.substring(1);
        }
        if (simple.isEmpty()) {
            return "service";
        }
        return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }

    // ----- module binding -----

    private void addGenericBinding(Project project, PsiClass module, String providedType, String providerFqn) {
        try {
            PsiMethod configure = findConfigureMethod(module);
            if (configure == null || configure.getBody() == null) {
                Messages.showWarningDialog(project,
                        "Could not find a configure() method on module '" + module.getQualifiedName()
                                + "'. Add the binding manually:\n\n"
                                + "bind(new TypeLiteral<" + providedType + ">(){}).toProvider(" + providerFqn + ".class);",
                        "Create Provider");
                return;
            }

            String bindingText = "bind(new com.google.inject.TypeLiteral<" + providedType + ">(){})"
                    + ".toProvider(" + providerFqn + ".class);";

            // Skip if an equivalent binding already exists
            String bodyText = configure.getBody().getText();
            if (bodyText.contains(".toProvider(" + providerFqn + ".class)")) {
                return;
            }

            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiStatement statement = factory.createStatementFromText(bindingText, configure.getBody());
            PsiElement added = configure.getBody().add(statement);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(added);
            CodeStyleManager.getInstance(project).reformat(added);
        } catch (Exception ex) {
            LOGGER.warn("Failed to add generic binding to module", ex);
        }
    }

    private PsiMethod findConfigureMethod(PsiClass module) {
        for (PsiMethod method : module.findMethodsByName("configure", false)) {
            if (method.getParameterList().isEmpty()) {
                return method;
            }
        }
        return null;
    }

    // ----- module discovery -----

    private List<PsiClass> findModuleCandidates(Project project) {
        Set<PsiClass> result = new LinkedHashSet<>();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);

        for (String baseName : new String[]{ABSTRACT_MODULE, GUICEDEE_MODULE}) {
            PsiClass base = facade.findClass(baseName, GlobalSearchScope.allScope(project));
            if (base == null) {
                continue;
            }
            for (PsiClass candidate : ClassInheritorsSearch.search(base, scope, true).findAll()) {
                if (candidate.isInterface() || candidate.hasModifierProperty(PsiModifier.ABSTRACT)) {
                    continue;
                }
                if (candidate.getQualifiedName() == null) {
                    continue;
                }
                result.add(candidate);
            }
        }
        return new ArrayList<>(result);
    }

    // ----- persistence dependency wiring -----

    private void ensurePersistenceDependency(Project project, PsiDirectory dir) {
        VirtualFile pom = findPomXml(dir);
        if (pom != null) {
            try {
                String content = new String(pom.contentsToByteArray());
                if (!content.contains("<artifactId>persistence</artifactId>")) {
                    String depXml = "\n        <dependency>\n"
                            + "            <groupId>com.guicedee</groupId>\n"
                            + "            <artifactId>persistence</artifactId>\n"
                            + "        </dependency>";
                    int idx = content.lastIndexOf("</dependencies>");
                    if (idx != -1) {
                        String updated = content.substring(0, idx) + depXml + "\n    " + content.substring(idx);
                        pom.setBinaryContent(updated.getBytes());
                    }
                }
            } catch (IOException ignore) {
                // best effort
            }
        }
        addRequiresToModuleInfo(project, "com.guicedee.persistence", dir);
    }

    private void addRequiresToModuleInfo(Project project, String moduleName, PsiDirectory dir) {
        PsiJavaFile moduleInfo = findModuleInfo(dir);
        if (moduleInfo == null) {
            return;
        }
        String text = moduleInfo.getText();
        if (text.contains("requires " + moduleName + ";")
                || text.contains("requires transitive " + moduleName + ";")) {
            return;
        }
        int insertIndex;
        int lastRequires = text.lastIndexOf("requires ");
        if (lastRequires != -1) {
            int semi = text.indexOf(';', lastRequires);
            insertIndex = semi != -1 ? semi + 1 : text.indexOf('{') + 1;
        } else {
            insertIndex = text.indexOf('{') + 1;
        }
        if (insertIndex <= 0) {
            return;
        }
        String updated = text.substring(0, insertIndex)
                + "\n    requires transitive " + moduleName + ";"
                + text.substring(insertIndex);
        var document = moduleInfo.getViewProvider().getDocument();
        if (document != null) {
            document.setText(updated);
        }
    }

    private PsiJavaFile findModuleInfo(PsiDirectory dir) {
        VirtualFile current = dir.getVirtualFile();
        VirtualFile sourceRoot = null;
        while (current != null) {
            if ("java".equals(current.getName())) {
                VirtualFile parent = current.getParent();
                if (parent != null && "main".equals(parent.getName())) {
                    sourceRoot = current;
                    break;
                }
            }
            current = current.getParent();
        }
        if (sourceRoot == null) {
            return null;
        }
        VirtualFile moduleInfoVf = sourceRoot.findChild("module-info.java");
        if (moduleInfoVf == null) {
            return null;
        }
        PsiFile file = PsiManager.getInstance(dir.getProject()).findFile(moduleInfoVf);
        return file instanceof PsiJavaFile ? (PsiJavaFile) file : null;
    }

    private VirtualFile findPomXml(PsiDirectory dir) {
        VirtualFile current = dir.getVirtualFile();
        while (current != null) {
            VirtualFile pom = current.findChild("pom.xml");
            if (pom != null) {
                return pom;
            }
            current = current.getParent();
        }
        return null;
    }

    // ----- helpers -----

    private String getPackageName(PsiDirectory dir) {
        PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(dir);
        return psiPackage != null ? psiPackage.getQualifiedName() : "";
    }

    private PsiDirectory getTargetDirectory(AnActionEvent e) {
        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        if (element instanceof PsiDirectory) {
            return (PsiDirectory) element;
        }
        IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        if (view != null) {
            PsiDirectory[] dirs = view.getDirectories();
            if (dirs.length > 0) {
                return dirs[0];
            }
        }
        Module module = e.getData(PlatformCoreDataKeys.MODULE);
        if (module != null && element != null) {
            PsiFile containing = element.getContainingFile();
            if (containing != null) {
                return containing.getContainingDirectory();
            }
        }
        return null;
    }

    private void navigateTo(Project project, PsiElement element) {
        if (element == null) {
            return;
        }
        PsiFile file = element instanceof PsiFile ? (PsiFile) element : element.getContainingFile();
        if (file == null) {
            file = PsiTreeUtil.getParentOfType(element, PsiFile.class);
        }
        if (file != null && file.getVirtualFile() != null) {
            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                    .openFile(file.getVirtualFile(), true);
        }
    }
}


