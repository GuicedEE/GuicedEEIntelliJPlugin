// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.inspections;

import com.intellij.codeInspection.inheritance.ImplicitSubclassProvider;
import com.guicedee.intellij.guice.GuiceBundle;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.guicedee.intellij.guice.utils.GuiceUtils;
import com.intellij.java.analysis.JavaAnalysisBundle;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// see: https://github.com/google/guice/wiki/Transactions
// these methods must be on objects that were created by Guice and they must not be private.
final class GuiceImplicitSubclassProvider extends ImplicitSubclassProvider {

  @Override
  public boolean isApplicableTo(@NotNull PsiClass psiClass) {
    return GuiceUtils.isInstantiable(psiClass);
  }

  @Override
  public @Nullable SubclassingInfo getSubclassingInfo(@NotNull PsiClass psiClass) {
    Map<PsiMethod, OverridingInfo> methodsToOverride = new HashMap<>();
    for (PsiMethod method : psiClass.getMethods()) {
      if (method.hasAnnotation(GuiceAnnotations.TRANSACTIONAL)) {
        methodsToOverride.put(method,
          new OverridingInfo(GuiceBundle.message("ImplicitSubclassInspection.display.forMethod.annotated")));
      }
    }
    if (!methodsToOverride.isEmpty()) {
      return new SubclassingInfo(
        JavaAnalysisBundle.message("inspection.implicit.subclass.display.forClass", psiClass.getName()),
        methodsToOverride);
    }
    return null;
  }
}

