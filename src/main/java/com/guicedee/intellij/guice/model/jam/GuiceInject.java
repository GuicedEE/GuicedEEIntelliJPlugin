// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.model.jam;

import com.guicedee.intellij.guice.model.GuiceInjectorManager;
import com.guicedee.intellij.guice.model.InjectionPointDescriptor;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class GuiceInject<T extends PsiMember> {
  private final T myPsiElement;

  protected GuiceInject(T psiElement) {
    myPsiElement = psiElement;
  }

  public T getPsiElement() {
    return myPsiElement;
  }

  public abstract List<InjectionPointDescriptor> getInjectionPoints();

  public @NotNull Set<PsiAnnotation> getBindingAnnotations() {
    return GuiceInjectorManager.getBindingAnnotations(getPsiElement());
  }

  public static class Field extends GuiceInject<PsiField> {
    public Field(PsiField field) {
      super(field);
    }

    public PsiType getType() {
      return getPsiElement().getType();
    }

    @Override
    public List<InjectionPointDescriptor> getInjectionPoints() {
      return Collections.singletonList(new InjectionPointDescriptor(getPsiElement()));
    }
  }

  public static class Method extends GuiceInject<PsiMethod> {
    public Method(PsiMethod method) {
      super(method);
    }

    @Override
    public List<InjectionPointDescriptor> getInjectionPoints() {
      List<InjectionPointDescriptor> descriptors = new ArrayList<>();
      for (PsiParameter parameter : getPsiElement().getParameterList().getParameters()) {
        descriptors.add(new InjectionPointDescriptor(parameter));
      }
      return descriptors;
    }
  }
}
