package com.guicedee.intellij.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Provides quick assist functionality for adding GuicedEE annotations to package-info files.
 */
public class PackageInfoAnnotationIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiJavaFile file = (PsiJavaFile) element.getContainingFile();
        if (!isPackageInfoFile(file)) {
            return;
        }

        PsiPackageStatement packageStatement = file.getPackageStatement();

        if (packageStatement == null) {
            return;
        }

        // Create a dialog to select which annotation to add
        AddAnnotationDialog dialog = new AddAnnotationDialog(project);
        if (dialog.showAndGet()) {
            String selectedAnnotation = dialog.getSelectedAnnotation();
            addAnnotationIfMissing(project, file, packageStatement, selectedAnnotation);
        }
    }

    private void addAnnotationIfMissing(Project project, PsiJavaFile file, PsiPackageStatement packageStatement, String annotationName) {
        PsiModifierList modifierList = packageStatement.getAnnotationList();
        if (modifierList == null) {
            return;
        }

        // Check if the annotation is already present
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (annotation.getQualifiedName() != null && annotation.getQualifiedName().equals(annotationName)) {
                return; // Annotation already exists
            }
        }

        // Add the annotation
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiAnnotation annotation = factory.createAnnotationFromText("@" + annotationName, packageStatement);
        modifierList.add(annotation);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file instanceof PsiJavaFile && isPackageInfoFile((PsiJavaFile) file);
    }

    private boolean isPackageInfoFile(PsiJavaFile file) {
        return file.getName().equals("package-info.java");
    }

    @NotNull
    @Override
    public String getText() {
        return "Add GuicedEE annotations to package-info";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "GuicedEE";
    }
}
