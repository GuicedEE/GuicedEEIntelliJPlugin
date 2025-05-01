package com.guicedee.intellij.run;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiMethodUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides run line markers for Guiced EE applications.
 * A Guiced EE Application is identified by having GuiceContext.inject() inside a public static void main method.
 */
public class GuicedEERunLineMarkerContributor extends RunLineMarkerContributor {

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PsiIdentifier && 
            element.getParent() instanceof PsiMethod) {

            PsiMethod method = (PsiMethod) element.getParent();

            // Check if it's a main method
            if (PsiMethodUtil.isMainMethod(method)) {
                // Check if the method contains GuiceContext.inject() call
                if (containsGuiceContextInject(method)) {
                    // Using deprecated constructor with suppressed warning
                    // This is the best approach available until a better API is provided
                    @SuppressWarnings("deprecation")
                    Info info = new Info(
                        AllIcons.RunConfigurations.Application, 
                        psiElement -> "Run Guiced EE Application"
                    );
                    return info;
                }
            }
        }
        return null;
    }

    private boolean containsGuiceContextInject(PsiMethod method) {
        final boolean[] containsInject = {false};

        method.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                String methodCall = expression.getMethodExpression().getText();
                if (methodCall.contains("GuiceContext") && methodCall.contains("inject")) {
                    containsInject[0] = true;
                }
                if (methodCall.contains("IGuiceContext") && methodCall.contains("registerModule")) {
                    containsInject[0] = true;
                }
            }
        });

        return containsInject[0];
    }
}
