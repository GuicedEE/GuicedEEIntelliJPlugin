// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.model;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.guicedee.intellij.guice.GuiceBundle;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.guicedee.intellij.guice.model.beans.BindDescriptor;
import com.guicedee.intellij.guice.model.jam.GuiceProvides;
import com.guicedee.intellij.guice.GuiceIcons;
import com.guicedee.intellij.guice.utils.GuiceUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class GuiceInjectionsClassAnnotator extends RelatedItemLineMarkerProvider {
  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement psiElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
    if (psiElement instanceof PsiClass psiClass) {
      final Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
      if (module == null) return;

      annotateClassBindings(result, module, psiClass);
    }
    if (psiElement instanceof PsiMethod) {
      final Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
      if (module == null) return;

      if ("configure".equals(((PsiMethod)psiElement).getName())) {
          annotateInjectionPoints(result, module, (PsiMethod)psiElement);
      }
      if (AnnotationUtil.isAnnotated((PsiMethod)psiElement, GuiceAnnotations.PROVIDES, 0)) {
        annotateProvidesInjectionPoints(result, module, (PsiMethod)psiElement);

      }
    }
  }

  private static void annotateClassBindings(Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                            Module module,
                                            PsiClass psiClass) {
    final Set<InjectionPointDescriptor> points = GuiceInjectionUtil.getInjectionPoints(psiClass, false);

    final PsiFile containingFile = psiClass.getContainingFile();
    if (!points.isEmpty()) {
      final Set<BindDescriptor> allDescriptors = GuiceInjectorManager.getBindingDescriptors(module);
      final List<GuiceProvides> allProvides = GuiceInjectionUtil.getProvides(module.getProject(), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
      for (InjectionPointDescriptor ip : points) {
        final PsiModifierListOwner owner = ip.getOwner();
        if (containingFile.equals(owner.getContainingFile())) {
          final Set<BindDescriptor> descriptors = GuiceInjectorManager.getInjectBindingDescriptors(ip, allDescriptors);
          Set<GuiceProvides<?>> providesSet = GuiceInjectorManager.getInjectProvidesDescriptors(ip, allProvides);

          if (owner instanceof PsiNameIdentifierOwner) {
            PsiElement identifier = ((PsiNameIdentifierOwner)owner).getNameIdentifier();
            if (identifier != null) {
              addInjectedGutterIcon(result, descriptors, providesSet, identifier, ip, module);
            }
          }
        }
      }
    }
  }

  private static void annotateInjectionPoints(Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                              @NotNull Module module,
                                              @NotNull PsiMethod scope) {
    final Set<BindDescriptor> descriptors = GuiceInjectorManager.getBindingDescriptors(scope);
    if (!descriptors.isEmpty()) {
      final Set<InjectionPointDescriptor> allInjectionPoints = GuiceInjectionUtil.getInjectionPoints(module.getProject(),
                                                                                                     GlobalSearchScope
                                                                                                       .moduleWithDependenciesScope(
                                                                                                         module));
      if (!allInjectionPoints.isEmpty()) {
        for (BindDescriptor descriptor : descriptors) {
          addInjectionPointsGutterIcon(result, GuiceInjectionUtil.getInjectionPoints(descriptor, allInjectionPoints),
                                       PsiTreeUtil.getDeepestFirst(descriptor.getBindExpression()));
        }
      }
    }
  }

  private static void annotateProvidesInjectionPoints(Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                                      @NotNull Module module,
                                                      @NotNull PsiMethod psiMethod) {
    GuiceProvides<?> provides = AnnotationUtil.isAnnotated(psiMethod, GuiceAnnotations.PROVIDES, 0)
                               ? new GuiceProvides.Method(psiMethod)
                               : null;
    if (provides != null) {
      Set<InjectionPointDescriptor> allInjectionPoints = GuiceInjectionUtil.getInjectionPoints(module.getProject(),
                                                                                               GlobalSearchScope.moduleWithDependenciesScope(
                                                                                                 module));
      Set<InjectionPointDescriptor> injectionPoints = GuiceInjectionUtil.getInjectionPoints(provides, allInjectionPoints);
      if (!injectionPoints.isEmpty()) {
        addInjectionPointsGutterIcon(result, injectionPoints, psiMethod.getNameIdentifier());
      }
    }
  }

  private static void addInjectedGutterIcon(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                            @NotNull Set<? extends BindDescriptor> descriptors,
                                            @NotNull Set<? extends GuiceProvides<?>> provides,
                                            @NotNull PsiElement identifier,
                                            @NotNull InjectionPointDescriptor ip,
                                            @NotNull Module module) {
    List<PsiElement> allTargets = new ArrayList<>();
    for (BindDescriptor descriptor : descriptors) {
      allTargets.add(descriptor.getBindExpression());
    }
    for (GuiceProvides<?> provide : provides) {
      allTargets.add(provide.getPsiElement());
    }

    // If no explicit bind() or @Provides found, fall back to JIT binding resolution
    if (allTargets.isEmpty()) {
      PsiType type = ip.getType();
      if (type instanceof PsiClassType) {
        PsiClass injectedClass = ((PsiClassType) type).resolve();
        if (injectedClass != null) {
          // Check for @ImplementedBy / @ProvidedBy annotations on the injected type
          PsiClass implementedByTarget = getImplementedByTarget(injectedClass);
          if (implementedByTarget != null) {
            allTargets.add(implementedByTarget);
          }
          PsiClass providedByTarget = getProvidedByTarget(injectedClass);
          if (providedByTarget != null) {
            allTargets.add(providedByTarget);
          }

          if (allTargets.isEmpty()) {
            if (GuiceUtils.isInstantiable(injectedClass)) {
              // Concrete, instantiable class — Guice can JIT-bind it directly
              allTargets.add(injectedClass);
            } else if (injectedClass.isInterface() || injectedClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
              // Interface or abstract class — find concrete inheritors in module scope
              GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
              Collection<PsiClass> inheritors = ClassInheritorsSearch.search(injectedClass, scope, true).findAll();
              for (PsiClass inheritor : inheritors) {
                if (GuiceUtils.isInstantiable(inheritor)) {
                  allTargets.add(inheritor);
                }
              }
            }
          }
        }
      }
    }

    if (!allTargets.isEmpty()) {
      NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(GuiceIcons.GoogleSmall, GuiceBundle.GUICE)
          .setPopupTitle(GuiceBundle.message("GuiceClassAnnotator.popup.title"))
          .setTooltipText(GuiceBundle.message("GuiceClassAnnotator.popup.tooltip.text"))
          .setTargets(allTargets);

      result.add(builder.createLineMarkerInfo(identifier));
    }
  }

  /**
   * Resolves the target class from an @ImplementedBy annotation on the given class.
   */
  private static @Nullable PsiClass getImplementedByTarget(@NotNull PsiClass psiClass) {
    PsiAnnotation annotation = AnnotationUtil.findAnnotation(psiClass, GuiceAnnotations.IMPLEMENTED_BY);
    if (annotation != null) {
      PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
      if (value instanceof PsiClassObjectAccessExpression) {
        PsiType classType = ((PsiClassObjectAccessExpression) value).getOperand().getType();
        if (classType instanceof PsiClassType) {
          return ((PsiClassType) classType).resolve();
        }
      }
    }
    return null;
  }

  /**
   * Resolves the provider class from a @ProvidedBy annotation on the given class.
   */
  private static @Nullable PsiClass getProvidedByTarget(@NotNull PsiClass psiClass) {
    PsiAnnotation annotation = AnnotationUtil.findAnnotation(psiClass, GuiceAnnotations.PROVIDED_BY);
    if (annotation != null) {
      PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
      if (value instanceof PsiClassObjectAccessExpression) {
        PsiType classType = ((PsiClassObjectAccessExpression) value).getOperand().getType();
        if (classType instanceof PsiClassType) {
          return ((PsiClassType) classType).resolve();
        }
      }
    }
    return null;
  }

  private static void addInjectionPointsGutterIcon(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                                   @NotNull Set<? extends InjectionPointDescriptor> ips,
                                                   @NotNull PsiElement owner) {
    if (!ips.isEmpty()) {
      List<PsiModifierListOwner> members = ContainerUtil.map(ips, descriptor -> descriptor.getOwner());
      NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(GuiceIcons.GoogleSmall, GuiceBundle.GUICE)
          .setTargets(members)
          .setPopupTitle(GuiceBundle.message("gutter.choose.injected.point"))
          .setTooltipText(GuiceBundle.message("gutter.navigate.to.injection.point"));

      result.add(builder.createLineMarkerInfo(owner));
    }
  }
}