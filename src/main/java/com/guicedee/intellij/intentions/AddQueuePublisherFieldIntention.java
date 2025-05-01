package com.guicedee.intellij.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Provides quick assist functionality for adding a QueuePublisher field with @Inject and @Named annotations.
 */
public class AddQueuePublisherFieldIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (psiClass == null) {
            return;
        }

        // Show dialog to get queue name
        QueueNameDialog dialog = new QueueNameDialog(project);
        if (!dialog.showAndGet()) {
            return;
        }

        String queueName = dialog.getQueueName();
        if (queueName.isEmpty()) {
            Messages.showErrorDialog(project, "Queue name cannot be empty", "Error");
            return;
        }

        // Create the field with annotations
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

        // Add imports if needed
        PsiJavaFile file = (PsiJavaFile) psiClass.getContainingFile();
        addImportIfNeeded(file, "jakarta.inject.Inject");
        addImportIfNeeded(file, "jakarta.inject.Named");
        addImportIfNeeded(file, "com.guicedee.rabbit.QueuePublisher");

        // Create the field
        String fieldText = "@Inject\n" +
                           "    @Named(\"" + queueName + "\")\n" +
                           "    private QueuePublisher " + getFieldName(queueName) + ";";

        PsiField field = factory.createFieldFromText(fieldText, psiClass);

        // Add the field to the class
        PsiElement anchor = findFieldAnchor(psiClass);
        if (anchor != null) {
            psiClass.addAfter(field, anchor);
        } else {
            psiClass.add(field);
        }
    }

    private void addImportIfNeeded(PsiJavaFile file, String qualifiedName) {
        PsiImportList importList = file.getImportList();
        if (importList == null) {
            return;
        }

        // Check if import already exists
        for (PsiImportStatement importStatement : importList.getImportStatements()) {
            if (importStatement.getQualifiedName() != null && 
                importStatement.getQualifiedName().equals(qualifiedName)) {
                return; // Import already exists
            }
        }

        // Add the import
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(file.getProject());
        PsiImportStatement importStatement = factory.createImportStatement(
                JavaPsiFacade.getInstance(file.getProject()).findClass(
                        qualifiedName, GlobalSearchScope.allScope(file.getProject())));
        importList.add(importStatement);
    }

    private String getFieldName(String queueName) {
        // Convert queue name to a valid Java field name
        String fieldName = queueName.replaceAll("[^a-zA-Z0-9]", "");
        if (fieldName.isEmpty()) {
            fieldName = "queuePublisher";
        } else {
            // Convert to camelCase
            fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1) + "Publisher";
        }
        return fieldName;
    }

    private PsiElement findFieldAnchor(PsiClass psiClass) {
        PsiField[] fields = psiClass.getFields();
        if (fields.length > 0) {
            return fields[fields.length - 1];
        }

        // If no fields, try to find the first method or inner class
        PsiElement[] children = psiClass.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiMethod || child instanceof PsiClass) {
                return child.getPrevSibling();
            }
        }

        return null;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        // Check if element is in a Java class
        PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (psiClass == null || psiClass.isInterface() || psiClass.isEnum() || psiClass.isAnnotationType()) {
            return false;
        }

        // Check if RabbitMQ is enabled in the project
        return isRabbitMQEnabled(project);
    }

    private boolean isRabbitMQEnabled(Project project) {
        // Look for module-info.java files
        PsiFile[] moduleInfoFiles = FilenameIndex.getFilesByName(
                project, "module-info.java", GlobalSearchScope.projectScope(project));

        for (PsiFile file : moduleInfoFiles) {
            if (file instanceof PsiJavaFile) {
                String text = file.getText();
                // Check if the module-info.java file contains a reference to com.guicedee.rabbit
                if (text.contains("requires") && text.contains("com.guicedee.rabbit")) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String getText() {
        return "Add @Inject @Named QueuePublisher field";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "GuicedEE";
    }

    /**
     * Dialog for getting the queue name.
     */
    private static class QueueNameDialog extends DialogWrapper {
        private JBTextField queueNameField;

        public QueueNameDialog(Project project) {
            super(project);
            setTitle("Add QueuePublisher Field");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            queueNameField = new JBTextField();
            queueNameField.setPreferredSize(new Dimension(300, 30));

            JPanel fieldPanel = new JPanel(new BorderLayout());
            fieldPanel.add(new JLabel("Queue Name:"), BorderLayout.NORTH);
            fieldPanel.add(queueNameField, BorderLayout.CENTER);

            panel.add(fieldPanel, BorderLayout.CENTER);

            return panel;
        }

        public String getQueueName() {
            return queueNameField.getText().trim();
        }
    }
}
