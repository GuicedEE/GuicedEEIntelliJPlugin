package com.guicedee.intellij.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for package-info annotation intentions.
 */
public abstract class BasePackageInfoAnnotationIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    /**
     * Returns the fully qualified name of the annotation to add.
     */
    protected abstract String getAnnotationName();

    /**
     * Returns the display name of the annotation (without the @ symbol).
     */
    protected abstract String getAnnotationDisplayName();

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

        addAnnotationIfMissing(project, packageStatement);
    }

    private void addAnnotationIfMissing(Project project, PsiPackageStatement packageStatement) {
        PsiModifierList modifierList = packageStatement.getAnnotationList();
        if (modifierList == null) {
            return;
        }

        // Check if the annotation is already present
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (annotation.getQualifiedName() != null && annotation.getQualifiedName().equals(getAnnotationName())) {
                return; // Annotation already exists
            }
        }

        // Add the annotation
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiAnnotation annotation = factory.createAnnotationFromText("@" + getAnnotationName(), packageStatement);
        modifierList.add(annotation);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (!(file instanceof PsiJavaFile) || !isPackageInfoFile((PsiJavaFile) file)) {
            return false;
        }

        // Check if the annotation is already present
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiPackageStatement packageStatement = javaFile.getPackageStatement();
        if (packageStatement == null) {
            return false;
        }

        PsiModifierList modifierList = packageStatement.getAnnotationList();
        if (modifierList == null) {
            return false;
        }

        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (annotation.getQualifiedName() != null && annotation.getQualifiedName().equals(getAnnotationName())) {
                return false; // Annotation already exists
            }
        }

        return true;
    }

    private boolean isPackageInfoFile(PsiJavaFile file) {
        return file.getName().equals("package-info.java");
    }

    @NotNull
    @Override
    public String getText() {
        return "Add @" + getAnnotationDisplayName() + " to package-info";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "GuicedEE";
    }

}
