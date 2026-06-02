package com.guicedee.intellij.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Dialog-based intention for adding @RegisteredService to package-info files.
 * Shows a form to configure name, url, healthPath, aliases, externalUrls, kubernetesUrl.
 */
public class AddRegisteredServiceIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiJavaFile file = (PsiJavaFile) element.getContainingFile();
        if (!file.getName().equals("package-info.java")) return;

        PsiPackageStatement packageStatement = file.getPackageStatement();
        if (packageStatement == null) return;

        // Show dialog
        RegisteredServiceDialog dialog = new RegisteredServiceDialog(project);
        if (!dialog.showAndGet()) return;

        String serviceName = dialog.getServiceName();
        if (serviceName.isEmpty()) return;

        String annotationText = dialog.buildAnnotationText();

        // Add annotation
        ApplicationManager.getApplication().runWriteAction(() -> {
            PsiModifierList modifierList = packageStatement.getAnnotationList();
            if (modifierList == null) return;

            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
            PsiAnnotation annotation = factory.createAnnotationFromText(annotationText, packageStatement);
            modifierList.add(annotation);
        });
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (!(file instanceof PsiJavaFile javaFile)) return false;
        if (!javaFile.getName().equals("package-info.java")) return false;
        return javaFile.getPackageStatement() != null;
    }

    @NotNull
    @Override
    public String getText() {
        return "Add @RegisteredService to package-info";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "GuicedEE";
    }
}

