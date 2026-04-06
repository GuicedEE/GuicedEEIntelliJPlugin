// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.inspections;

import com.intellij.codeInsight.AnnotationUtil;
import com.guicedee.intellij.guice.GuiceBundle;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.guicedee.intellij.guice.utils.AnnotationUtils;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeInsight.AnnotationUtil.CHECK_HIERARCHY;

public final class SingletonInjectsScopedInspection extends BaseInspection {
  @Override
  protected @NotNull String buildErrorString(Object... infos) {
    return GuiceBundle.message("singleton.injects.scoped.problem.descriptor");
  }

  @Override
  public BaseInspectionVisitor buildVisitor() {
    return new Visitor();
  }

  private static class Visitor extends BaseInspectionVisitor {
    @Override
    public void visitAnnotation(@NotNull PsiAnnotation annotation) {
      super.visitAnnotation(annotation);
      final String qualifiedName = annotation.getQualifiedName();
      if (qualifiedName == null || !GuiceAnnotations.INJECTS.contains(qualifiedName)) {
        return;
      }
      final PsiClass containingClass = PsiTreeUtil.getParentOfType(annotation, PsiClass.class);
      if (containingClass == null) {
        return;
      }
      if (!AnnotationUtil.isAnnotated(containingClass, GuiceAnnotations.SINGLETON, CHECK_HIERARCHY)) {
        return;
      }
      final PsiElement owner = annotation.getParent().getParent();
      if (owner instanceof PsiField field) {
        checkForScopedInjection(field.getTypeElement());
      }
      else if (owner instanceof PsiMethod method) {
        final PsiParameter[] parameters = method.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
          checkForScopedInjection(parameter.getTypeElement());
        }
      }
    }

    private void checkForScopedInjection(@Nullable PsiTypeElement typeElement) {
      if (typeElement == null) return;
      final PsiType type = typeElement.getType();
      if (!(type instanceof PsiClassType classType)) {
        return;
      }
      final PsiClass referencedClass = classType.resolve();
      if (referencedClass == null) {
        return;
      }
      if (AnnotationUtil.isAnnotated(referencedClass, GuiceAnnotations.SESSION_SCOPED, CHECK_HIERARCHY) ||
          AnnotationUtil.isAnnotated(referencedClass, GuiceAnnotations.REQUEST_SCOPED, CHECK_HIERARCHY)) {
        registerError(typeElement);
        return;
      }
      PsiModifierList modifierList = referencedClass.getModifierList();
      if (modifierList == null) {
        return;
      }
      final PsiAnnotation implementedByAnnotation = modifierList.findAnnotation(GuiceAnnotations.IMPLEMENTED_BY);
      if (implementedByAnnotation == null) {
        return;
      }
      final PsiElement defaultValue = AnnotationUtils.findDefaultValue(implementedByAnnotation);
      if (defaultValue == null) {
        return;
      }
      if (!(defaultValue instanceof PsiClassObjectAccessExpression)) {
        return;
      }
      final PsiTypeElement classTypeElement = ((PsiClassObjectAccessExpression)defaultValue).getOperand();
      final PsiType implementedByClassType = classTypeElement.getType();
      if (!(implementedByClassType instanceof PsiClassType)) {
        return;
      }
      final PsiClass implementedByClass = ((PsiClassType)implementedByClassType).resolve();
      if (implementedByClass == null) {
        return;
      }
      if (AnnotationUtil.isAnnotated(implementedByClass, GuiceAnnotations.SESSION_SCOPED, CHECK_HIERARCHY) ||
          AnnotationUtil.isAnnotated(implementedByClass, GuiceAnnotations.REQUEST_SCOPED, CHECK_HIERARCHY)) {
        registerError(typeElement);
      }
    }
  }
}