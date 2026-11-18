// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Suppresses the "unused declaration" warning on Guice / GuicedEE injection points without
 * declaring them implicitly used.
 *
 * <p>{@code ImplicitUsageProvider.isImplicitUsage} cannot be used for this purpose: the platform
 * also treats every implicitly used element as an entry point, and
 * {@code JavaReferencesCodeVisionProvider} skips entry points entirely, which removes the
 * "N usages" hint. Suppressing the inspection instead leaves the code-vision path intact, so an
 * injected field still shows {@code 0 usages} when nothing references it and {@code N usages}
 * when it is actually used.</p>
 *
 * <p>Only the unused tool is affected; all other inspections, and all declarations that are not
 * injection points, keep their normal behaviour.</p>
 */
public final class GuiceUnusedDeclarationSuppressor implements InspectionSuppressor {

  /** {@code HighlightInfoType.UNUSED_SYMBOL_SHORT_NAME} is "unused"; the alias covers older builds. */
  private static final Set<String> UNUSED_TOOL_IDS = Set.of("unused", "UnusedDeclaration");

  @Override
  public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
    if (!UNUSED_TOOL_IDS.contains(toolId)) {
      return false;
    }
    PsiModifierListOwner owner = element instanceof PsiModifierListOwner modifierListOwner
      ? modifierListOwner
      : PsiTreeUtil.getParentOfType(element, PsiModifierListOwner.class, false);
    return owner != null && GuiceImplicitUsageProvider.isInjectionPoint(owner);
  }

  @Override
  public SuppressQuickFix[] getSuppressActions(@NotNull PsiElement element, @NotNull String toolId) {
    return new SuppressQuickFix[0];
  }
}
