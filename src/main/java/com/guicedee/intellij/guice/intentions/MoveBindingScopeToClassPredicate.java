// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.intentions;

import com.intellij.codeInsight.AnnotationUtil;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.guicedee.intellij.guice.utils.GuiceUtils;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;

import static com.intellij.codeInsight.AnnotationUtil.CHECK_HIERARCHY;

public class MoveBindingScopeToClassPredicate implements PsiElementPredicate {
  @Override
  public boolean satisfiedBy(PsiElement element) {
    if (!GuiceUtils.isBinding(element)) {
      return false;
    }
    final PsiMethodCallExpression call = (PsiMethodCallExpression)element;
    if (GuiceUtils.findScopeForBinding(call) == null) {
      return false;
    }
    final PsiMethodCallExpression scopeCall = GuiceUtils.findScopeCallForBinding(call);
    final PsiExpression arg = scopeCall.getArgumentList().getExpressions()[0];
    final String scopeAnnotation = GuiceUtils.getScopeAnnotationForScopeExpression(arg);
    if (scopeAnnotation == null) {
      return false;
    }
    final PsiClass implementingClass = GuiceUtils.findImplementingClassForBinding(call);
    if (implementingClass == null) {
      return false;
    }
    if (AnnotationUtil.isAnnotated(implementingClass, GuiceAnnotations.SINGLETON, CHECK_HIERARCHY) ||
        AnnotationUtil.isAnnotated(implementingClass, GuiceAnnotations.REQUEST_SCOPED, 0) ||
        AnnotationUtil.isAnnotated(implementingClass, GuiceAnnotations.SESSION_SCOPED, 0)) {
      return false;
    }
    return true;
  }
}
