// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice;

import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Marks genuine Guice / GuicedEE framework entry points as implicitly used.
 *
 * <p>The platform consults every {@link ImplicitUsageProvider} for every declaration while
 * running the "Unused declaration" inspection and the "usages" code-vision provider. Returning
 * {@code true} for an ordinary declaration silently removes both the unused warning and the
 * usages hint, so the element types accepted here must stay narrow.</p>
 *
 * <p>Injection points (fields, methods and parameters carrying {@code @Inject} and friends) are
 * deliberately <em>not</em> claimed by {@link #isImplicitUsage}. The platform's
 * {@code UnusedDeclarationInspectionBase.isEntryPoint(PsiElement)} is defined as
 * {@code isStrictEntryPoint(element) || RefUtil.isImplicitUsage(element)}, and
 * {@code JavaReferencesCodeVisionProvider.getVisionInfo()} returns {@code null} for entry points.
 * Claiming an injected field therefore makes its "N usages" hint disappear entirely, including
 * the {@code 0 usages} case, leaving no way to tell whether the field is actually referenced.
 * The unused warning for those injection points is suppressed separately by
 * {@link GuiceUnusedDeclarationSuppressor}, which does not go through the entry-point check.</p>
 *
 * <p>{@link #isImplicitRead} and {@link #isImplicitWrite} still cover injection points so that
 * the platform does not report the secondary "never read" / "never assigned" warnings for a
 * field whose value is supplied by Guice at runtime.</p>
 *
 * <p>Annotations are checked through {@link PsiAnnotationOwner#findAnnotation(String)} on the
 * element's own modifier list. This avoids the additional hierarchy, external and inferred
 * annotation lookups performed by {@code AnnotationUtil.isAnnotated} and makes it impossible for
 * an unrelated class-level annotation to mark a whole class (and, transitively, all of its
 * private members) as implicitly used.</p>
 */
public final class GuiceImplicitUsageProvider implements ImplicitUsageProvider {

  private static final Collection<String> GUICE_INJECTION_POINTS = GuiceAnnotations.INJECTS;
  private static final Collection<String> ENTRY_POINTS = GuiceAnnotations.ENTRY_POINTS;
  private static final Collection<String> PROVIDES = List.of(GuiceAnnotations.PROVIDES);

  /**
   * Only real framework entry points are claimed here. Injection points are intentionally
   * excluded so that the usages code-vision provider keeps rendering their reference counts;
   * their unused warning is handled by {@link GuiceUnusedDeclarationSuppressor} instead.
   */
  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    return isEntryPoint(element) || isProvidesMethod(element);
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return isInjectionPoint(element) || isProvidesMethod(element);
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return isInjectionPoint(element);
  }

  /** Only real injection sites: fields, methods/constructors and their parameters. */
  static boolean isInjectionPoint(@NotNull PsiElement element) {
    if (!(element instanceof PsiField || element instanceof PsiMethod || element instanceof PsiParameter)) {
      return false;
    }
    return hasDirectAnnotation(element, GUICE_INJECTION_POINTS);
  }

  /** Methods and classes annotated as framework entry points, e.g. {@code @VertxEventDefinition}. */
  private static boolean isEntryPoint(@NotNull PsiElement element) {
    if (!(element instanceof PsiMethod || element instanceof PsiClass)) {
      return false;
    }
    return hasDirectAnnotation(element, ENTRY_POINTS);
  }

  /** {@code @Provides} is a method annotation only. */
  private static boolean isProvidesMethod(@NotNull PsiElement element) {
    return element instanceof PsiMethod && hasDirectAnnotation(element, PROVIDES);
  }

  private static boolean hasDirectAnnotation(@NotNull PsiElement element,
                                             @NotNull Collection<String> qualifiedNames) {
    if (!(element instanceof PsiModifierListOwner owner)) {
      return false;
    }
    PsiAnnotationOwner annotations = owner.getModifierList();
    if (annotations == null) {
      return false;
    }
    for (String qualifiedName : qualifiedNames) {
      if (annotations.findAnnotation(qualifiedName) != null) {
        return true;
      }
    }
    return false;
  }
}
