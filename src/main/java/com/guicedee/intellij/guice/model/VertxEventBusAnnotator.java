// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.model;

import com.guicedee.intellij.guice.GuiceBundle;
import com.guicedee.intellij.guice.GuiceIcons;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides gutter navigation between GuicedEE Vert.x event-bus publishers and consumers.
 *
 * <ul>
 *   <li>On a {@code @Named("address") VertxEventPublisher} injection point, the GuicedEE logo
 *       navigates to the {@code @VertxEventDefinition("address")} consumer(s).</li>
 *   <li>On a {@code @VertxEventDefinition("address")} consumer method/class, the GuicedEE logo
 *       navigates to the publisher injection point(s) for that address.</li>
 * </ul>
 *
 * The logo in the gutter indicates that the element participates in event-bus routing and can be
 * navigated.
 */
public final class VertxEventBusAnnotator extends RelatedItemLineMarkerProvider {

  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement element,
                                          @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
    // The line-marker framework requires markers to be anchored on leaf elements (the name
    // identifier). Attaching them to composite elements (PsiMethod/PsiClass/PsiField/PsiParameter)
    // causes overlapping ranges to be merged, so only the first marker survives. Therefore we only
    // act on the PsiIdentifier leaf and resolve its declaring owner from the parent.
    if (!(element instanceof PsiIdentifier)) {
      return;
    }

    final PsiElement owner = element.getParent();

    // Consumer side: @VertxEventDefinition on a method or class.
    if ((owner instanceof PsiMethod || owner instanceof PsiClass)
        && getNameIdentifier(owner) == element) {
      final String address = getAnnotationStringValue((PsiModifierListOwner) owner,
                                                       GuiceAnnotations.VERTX_EVENT_DEFINITION);
      if (address != null) {
        final Module module = ModuleUtilCore.findModuleForPsiElement(owner);
        if (module != null) {
          addConsumerMarker(result, module, address, element);
        }
      }
      return;
    }

    // Publisher side: @Named("address") VertxEventPublisher<T> field or parameter.
    if ((owner instanceof PsiField || owner instanceof PsiParameter)
        && getNameIdentifier(owner) == element) {
      final PsiVariable variable = (PsiVariable) owner;
      if (isPublisherVariable(variable)) {
        final String address = getAnnotationStringValue(variable, GuiceAnnotations.NAMED);
        if (address != null) {
          final Module module = ModuleUtilCore.findModuleForPsiElement(owner);
          if (module != null) {
            addPublisherMarker(result, module, address, element);
          }
        }
      }
    }
  }

  private static void addConsumerMarker(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                        @NotNull Module module,
                                        @NotNull String address,
                                        @NotNull PsiElement identifier) {
    final List<PsiElement> publishers = findPublishers(module, address);
    if (!publishers.isEmpty()) {
      final NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(GuiceIcons.GuicedeeLogo)
          .setTargets(publishers)
          .setPopupTitle(GuiceBundle.message("gutter.vertx.choose.publisher"))
          .setTooltipText(GuiceBundle.message("gutter.vertx.navigate.to.publisher", address));
      result.add(builder.createLineMarkerInfo(identifier));
    }
  }

  private static void addPublisherMarker(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                         @NotNull Module module,
                                         @NotNull String address,
                                         @NotNull PsiElement identifier) {
    final List<PsiElement> consumers = findConsumers(module, address);
    if (!consumers.isEmpty()) {
      final NavigationGutterIconBuilder<PsiElement> builder =
        NavigationGutterIconBuilder.create(GuiceIcons.GuicedeeLogo)
          .setTargets(consumers)
          .setPopupTitle(GuiceBundle.message("gutter.vertx.choose.consumer"))
          .setTooltipText(GuiceBundle.message("gutter.vertx.navigate.to.consumer", address));
      result.add(builder.createLineMarkerInfo(identifier));
    }
  }

  /**
   * Finds all consumers ({@code @VertxEventDefinition}) declared for the given address within the
   * project. Publishers and consumers commonly live in sibling application modules that have no
   * compile-time dependency on each other.
   */
  private static @NotNull List<PsiElement> findConsumers(@NotNull Module module, @NotNull String address) {
    final List<PsiElement> targets = new ArrayList<>();
    final GlobalSearchScope scope = GlobalSearchScope.projectScope(module.getProject());
    final PsiClass annotationClass = JavaPsiFacade.getInstance(module.getProject())
      .findClass(GuiceAnnotations.VERTX_EVENT_DEFINITION, GlobalSearchScope.allScope(module.getProject()));
    if (annotationClass == null) {
      return targets;
    }

    final Collection<PsiModifierListOwner> annotated =
      AnnotatedElementsSearch.<PsiModifierListOwner>searchElements(annotationClass, scope, PsiMethod.class, PsiClass.class).findAll();
    for (PsiModifierListOwner owner : annotated) {
      final String candidate = getAnnotationStringValue(owner, GuiceAnnotations.VERTX_EVENT_DEFINITION);
      if (address.equals(candidate)) {
        final PsiElement identifier = getNameIdentifier(owner);
        targets.add(identifier != null ? identifier : owner);
      }
    }
    return targets;
  }

  /**
   * Finds all publisher injection points ({@code @Named("address") VertxEventPublisher}) for the
   * given address within the project. Event-bus addresses deliberately cross module dependency
   * boundaries.
   */
  private static @NotNull List<PsiElement> findPublishers(@NotNull Module module, @NotNull String address) {
    final List<PsiElement> targets = new ArrayList<>();
    final GlobalSearchScope scope = GlobalSearchScope.projectScope(module.getProject());
    final PsiClass publisherClass = JavaPsiFacade.getInstance(module.getProject())
      .findClass(GuiceAnnotations.VERTX_EVENT_PUBLISHER, GlobalSearchScope.allScope(module.getProject()));
    if (publisherClass == null) {
      return targets;
    }

    for (PsiReference reference : ReferencesSearch.search(publisherClass, scope)) {
      final PsiVariable variable = PsiTreeUtil.getParentOfType(reference.getElement(), PsiVariable.class);
      if (variable == null || !isPublisherVariable(variable)) {
        continue;
      }
      if (address.equals(getAnnotationStringValue(variable, GuiceAnnotations.NAMED))) {
        final PsiElement identifier = getNameIdentifier(variable);
        final PsiElement target = identifier != null ? identifier : variable;
        if (!targets.contains(target)) {
          targets.add(target);
        }
      }
    }
    return targets;
  }

  private static boolean isPublisherVariable(@NotNull PsiVariable variable) {
    final PsiType type = variable.getType();
    if (!(type instanceof PsiClassType)) {
      return false;
    }
    final PsiClass publisherClass = ((PsiClassType) type).resolve();
    return publisherClass != null
           && GuiceAnnotations.VERTX_EVENT_PUBLISHER.equals(publisherClass.getQualifiedName())
           && AnnotationUtil.isAnnotated(variable, GuiceAnnotations.NAMED, 0);
  }

  private static @Nullable String getAnnotationStringValue(@NotNull PsiModifierListOwner owner,
                                                           @NotNull String annotationFqn) {
    final PsiAnnotation annotation = AnnotationUtil.findAnnotation(owner, annotationFqn);
    if (annotation == null) {
      return null;
    }
    return AnnotationUtil.getStringAttributeValue(annotation, "value");
  }

  private static @Nullable PsiElement getNameIdentifier(@NotNull PsiElement element) {
    if (element instanceof PsiNameIdentifierOwner) {
      return ((PsiNameIdentifierOwner) element).getNameIdentifier();
    }
    return null;
  }
}



