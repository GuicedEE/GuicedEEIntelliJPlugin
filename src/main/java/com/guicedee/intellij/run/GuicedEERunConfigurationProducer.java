package com.guicedee.intellij.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Produces run configurations for Guiced EE applications.
 */
public class GuicedEERunConfigurationProducer extends LazyRunConfigurationProducer<GuicedEEApplicationConfiguration> {

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return GuicedEEApplicationConfigurationType.getInstance().getConfigurationFactories()[0];
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull GuicedEEApplicationConfiguration configuration,
                                                   @NotNull ConfigurationContext context,
                                                   @NotNull Ref<PsiElement> sourceElement) {
        PsiElement element = context.getPsiLocation();
        if (element == null) {
            return false;
        }

        PsiMethod method = findMainMethod(element);
        if (method == null) {
            return false;
        }

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }

        // Check if the method contains GuiceContext.inject() call
        if (!containsGuiceContextInject(method)) {
            return false;
        }

        // Set up the configuration
        configuration.setName(containingClass.getName());
        // Additional configuration setup would go here

        sourceElement.set(method);
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull GuicedEEApplicationConfiguration configuration,
                                             @NotNull ConfigurationContext context) {
        PsiElement element = context.getPsiLocation();
        if (element == null) {
            return false;
        }

        PsiMethod method = findMainMethod(element);
        if (method == null) {
            return false;
        }

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }

        // Check if the configuration matches the context
        return containsGuiceContextInject(method) && 
               configuration.getName().equals(containingClass.getName());
    }

    private PsiMethod findMainMethod(PsiElement element) {
        PsiMethod method = null;
        
        if (element instanceof PsiMethod) {
            method = (PsiMethod) element;
        } else if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            method = findMainMethodInClass(psiClass);
        } else {
            PsiClass containingClass = getContainingClass(element);
            if (containingClass != null) {
                method = findMainMethodInClass(containingClass);
            }
        }
        
        return method != null && isMainMethod(method) ? method : null;
    }

    private PsiMethod findMainMethodInClass(PsiClass psiClass) {
        for (PsiMethod method : psiClass.getMethods()) {
            if (isMainMethod(method)) {
                return method;
            }
        }
        return null;
    }

    private boolean isMainMethod(PsiMethod method) {
        return method.getName().equals("main") &&
               method.hasModifierProperty(PsiModifier.PUBLIC) &&
               method.hasModifierProperty(PsiModifier.STATIC) &&
               method.getParameterList().getParametersCount() == 1 &&
               method.getReturnType() != null &&
               method.getReturnType().equalsToText("void");
    }

    private PsiClass getContainingClass(PsiElement element) {
        while (element != null) {
            if (element instanceof PsiClass) {
                return (PsiClass) element;
            }
            element = element.getParent();
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
            }
        });
        
        return containsInject[0];
    }
}