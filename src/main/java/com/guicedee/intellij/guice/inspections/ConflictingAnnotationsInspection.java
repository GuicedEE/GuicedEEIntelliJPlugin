// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.inspections;

import com.intellij.codeInsight.AnnotationUtil;
import com.guicedee.intellij.guice.GuiceBundle;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.guicedee.intellij.guice.utils.AnnotationUtils;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInsight.AnnotationUtil.CHECK_HIERARCHY;

public final class ConflictingAnnotationsInspection extends BaseInspection {
  @Override
  protected @NotNull String buildErrorString(Object... infos) {
    return GuiceBundle.message("conflicting.annotations.problem.descriptor");
  }

  @Override
  public BaseInspectionVisitor buildVisitor() {
    return new Visitor();
  }

  private static class Visitor extends BaseInspectionVisitor {
    @Override
    public void visitAnnotation(@NotNull PsiAnnotation annotation) {
      super.visitAnnotation(annotation);
      final PsiElement grandParent = annotation.getParent().getParent();
      if (!(grandParent instanceof PsiModifierListOwner owner)) {
        return;
      }
      final String qualifiedName = annotation.getQualifiedName();
      if (GuiceAnnotations.IMPLEMENTED_BY.equals(qualifiedName)) {
        if (AnnotationUtil.isAnnotated(owner, GuiceAnnotations.PROVIDED_BY, CHECK_HIERARCHY)) {
          registerError(annotation);
        }
        return;
      }
      if (GuiceAnnotations.PROVIDED_BY.equals(qualifiedName)) {
        if (AnnotationUtil.isAnnotated(owner, GuiceAnnotations.IMPLEMENTED_BY, CHECK_HIERARCHY)) {
          registerError(annotation);
        }
        return;
      }
      boolean includeCustomScopes = owner instanceof PsiClass ||
        (owner instanceof PsiMethod && AnnotationUtil.isAnnotated(owner, GuiceAnnotations.PROVIDES, 0)) ||
        AnnotationUtils.isLocalScopeInjectionPoint(owner);
      if (isScope(annotation, includeCustomScopes) && owner.getModifierList() != null) {
        for (PsiAnnotation other : owner.getModifierList().getAnnotations()) {
          if (!other.equals(annotation) && isScope(other, includeCustomScopes)) {
            registerError(annotation);
            return;
          }
        }
      }
    }

    private boolean isScope(PsiAnnotation annotation, boolean includeCustomScopes) {
      String name = annotation.getQualifiedName();
      return includeCustomScopes ? AnnotationUtils.isScopeAnnotation(annotation) :
        name != null && GuiceAnnotations.STANDARD_SCOPES.contains(name);
    }
  }
}
