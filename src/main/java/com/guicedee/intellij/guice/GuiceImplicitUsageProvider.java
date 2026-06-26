// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public final class GuiceImplicitUsageProvider implements ImplicitUsageProvider {
  private static final Collection<String> GUICE_INJECTION_POINT = GuiceAnnotations.INJECTS;
  private static final Collection<String> ENTRY_POINTS = GuiceAnnotations.ENTRY_POINTS;

  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    if (element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, GUICE_INJECTION_POINT, 0)) {
      return true;
    }
    // Methods (and classes) annotated as framework entry points — e.g. @VertxEventDefinition
    // event-bus consumers — are invoked reflectively by the runtime, so treat them as used.
    if (element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, ENTRY_POINTS, 0)) {
      return true;
    }
    return isImplicitRead(element);
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    if (element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, GUICE_INJECTION_POINT, 0)) {
      return true;
    }
    return element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, GuiceAnnotations.PROVIDES, 0);
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, GUICE_INJECTION_POINT, 0);
  }
}