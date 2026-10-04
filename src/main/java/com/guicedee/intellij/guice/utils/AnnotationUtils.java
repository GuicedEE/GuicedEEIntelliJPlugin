// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.utils;

import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class AnnotationUtils {
  public static @Nullable PsiElement findDefaultValue(PsiAnnotation annotation) {
    return PsiImplUtil.findDeclaredAttributeValue(annotation, PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);
  }

  public static boolean isScopeAnnotation(PsiAnnotation annotation) {
    String name = annotation.getQualifiedName();
    if (name != null && GuiceAnnotations.STANDARD_SCOPES.contains(name)) {
      return true;
    }
    PsiClass type = annotation.resolveAnnotationType();
    return type != null && AnnotationUtil.isAnnotated(
      type, GuiceAnnotations.SCOPE_META_ANNOTATIONS, 0);
  }

  /** Local dependency scopes belong to the GuicedEE fork, and only apply to fields and constructors. */
  public static boolean isLocalScopeInjectionPoint(PsiModifierListOwner owner) {
    if (JavaPsiFacade.getInstance(owner.getProject()).findClass(
        GuiceAnnotations.BIND_SCOPE_PROVIDER, owner.getResolveScope()) == null) {
      return false;
    }
    if (owner instanceof PsiField) {
      return AnnotationUtil.isAnnotated(owner, GuiceAnnotations.INJECTS, 0);
    }
    if (owner instanceof PsiParameter parameter &&
        parameter.getDeclarationScope() instanceof PsiMethod method && method.isConstructor()) {
      return AnnotationUtil.isAnnotated(method, GuiceAnnotations.INJECTS, 0);
    }
    return false;
  }

  public static boolean hasLocalScope(PsiModifierListOwner owner, Collection<String> scopes) {
    return isLocalScopeInjectionPoint(owner) && AnnotationUtil.isAnnotated(owner, scopes, 0);
  }
}
