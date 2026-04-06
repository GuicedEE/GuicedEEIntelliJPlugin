// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.model;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.guicedee.intellij.guice.GuiceBundle;
import com.guicedee.intellij.guice.GuiceIcons;
import com.guicedee.intellij.guice.model.beans.BindDescriptor;
import com.guicedee.intellij.guice.model.beans.BindToProviderDescriptor;
import com.guicedee.intellij.guice.model.renderers.GuiceBindingClassPsiElementListCellRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class GuiceBindingClassAnnotator extends RelatedItemLineMarkerProvider {

  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement psiElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {

    if (psiElement instanceof PsiClass psiClass) {
      final Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
      if (module == null) return;

      final Set<BindDescriptor> descriptors = GuiceInjectorManager.getBindingDescriptors(module);

      Set<BindDescriptor> bindingDescriptors = new HashSet<>();

      for (BindDescriptor descriptor : descriptors) {
        final PsiClass boundClass = descriptor.getBoundClass();
        if (psiElement.equals(boundClass)) {
          bindingDescriptors.add(descriptor);
          continue;
        }

        if (psiElement.equals(getBindingBaseClass(descriptor.getBindingClass()))) {
          bindingDescriptors.add(descriptor);
          continue;
        }

        if (descriptor instanceof BindToProviderDescriptor) {
          final PsiClass providerClass = ((BindToProviderDescriptor)descriptor).getProviderClass();
          if (psiElement.equals(getBindingBaseClass(providerClass))) bindingDescriptors.add(descriptor);
        }
      }

      if (!bindingDescriptors.isEmpty()) {
        final NavigationGutterIconBuilder<BindDescriptor> builder =
          NavigationGutterIconBuilder.create(GuiceIcons.GoogleSmall, DEFAULT_CONVERTOR).
            setPopupTitle(GuiceBundle.message("GuiceClassAnnotator.popup.title")).
            setTooltipText(GuiceBundle.message("GuiceClassAnnotator.popup.tooltip.text")).
            setCellRenderer(GuiceBindingClassPsiElementListCellRenderer::new).
            setTargets(bindingDescriptors);

        final PsiIdentifier identifier = psiClass.getNameIdentifier();
        if (identifier != null) {
          result.add(builder.createLineMarkerInfo(identifier));
        }
      }
      else {
        // No explicit bindings found — check for JIT injection points that reference this class
        addJitInjectionPointMarkers(result, module, psiClass);
      }
    }
  }

  /**
   * When a class has no explicit bind() calls but is referenced by @Inject fields/parameters elsewhere,
   * show gutter icons on the class that navigate to the injection points.
   * This covers Guice JIT (just-in-time) bindings for concrete classes and inheritors of interfaces/abstract classes.
   */
  private static void addJitInjectionPointMarkers(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                                   @NotNull Module module,
                                                   @NotNull PsiClass psiClass) {
    Set<InjectionPointDescriptor> allInjectionPoints = GuiceInjectionUtil.getInjectionPoints(
      module.getProject(), GlobalSearchScope.moduleWithDependenciesScope(module));
    if (allInjectionPoints.isEmpty()) return;

    List<PsiElement> injectionOwners = new ArrayList<>();
    for (InjectionPointDescriptor ip : allInjectionPoints) {
      PsiType type = ip.getType();
      if (type instanceof PsiClassType) {
        PsiClass injectedClass = ((PsiClassType) type).resolve();
        if (injectedClass != null) {
          // Direct match: @Inject private ThisClass field;
          if (psiClass.equals(injectedClass)) {
            injectionOwners.add(ip.getOwner());
          }
          // Supertype match: @Inject private SomeInterface field; where this class implements SomeInterface
          else if (injectedClass.isInterface() || injectedClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
            if (psiClass.isInheritor(injectedClass, true)) {
              injectionOwners.add(ip.getOwner());
            }
          }
        }
      }
    }

    if (!injectionOwners.isEmpty()) {
      NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(GuiceIcons.GoogleSmall, GuiceBundle.GUICE)
          .setTargets(injectionOwners)
          .setPopupTitle(GuiceBundle.message("gutter.choose.injected.point"))
          .setTooltipText(GuiceBundle.message("gutter.navigate.to.injection.point"));

      PsiIdentifier identifier = psiClass.getNameIdentifier();
      if (identifier != null) {
        result.add(builder.createLineMarkerInfo(identifier));
      }
    }
  }

  private static @Nullable PsiClass getBindingBaseClass(@Nullable PsiClass bindingClass) {
    if (bindingClass instanceof PsiAnonymousClass) {
      return ((PsiAnonymousClass)bindingClass).getBaseClassType().resolve();
    }
    return bindingClass;
  }

  public static final NotNullFunction<BindDescriptor, Collection<? extends PsiElement>> DEFAULT_CONVERTOR =
    o -> ContainerUtil.createMaybeSingletonList(o.getBindExpression());
}
