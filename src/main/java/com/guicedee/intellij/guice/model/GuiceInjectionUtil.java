// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.model;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.dataFlow.StringExpressionHelper;
import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.guicedee.intellij.guice.model.beans.BindDescriptor;
import com.guicedee.intellij.guice.model.jam.GuiceInject;
import com.guicedee.intellij.guice.model.jam.GuiceProvides;
import com.guicedee.intellij.guice.utils.GuiceUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GuiceInjectionUtil {
  public static Set<InjectionPointDescriptor> getInjectionPoints(@NotNull Project project, @NotNull GlobalSearchScope scope) {
    Set<InjectionPointDescriptor> ips = new HashSet<>();

    for (String injectAnno : GuiceAnnotations.INJECTS) {
      PsiClass annoClass = JavaPsiFacade.getInstance(project).findClass(injectAnno, GlobalSearchScope.allScope(project));
      if (annoClass == null) continue;

      for (PsiField field : AnnotatedElementsSearch.searchPsiFields(annoClass, scope).findAll()) {
        GuiceInject.Field inject = new GuiceInject.Field(field);
        ips.addAll(inject.getInjectionPoints());
      }
      for (PsiMethod method : AnnotatedElementsSearch.searchPsiMethods(annoClass, scope).findAll()) {
        GuiceInject.Method inject = new GuiceInject.Method(method);
        ips.addAll(inject.getInjectionPoints());
      }
    }
    return ips;
  }

  public static @NotNull Set<InjectionPointDescriptor> getInjectionPoints(@NotNull PsiClass psiClass) {
    return getInjectionPoints(psiClass, true);
  }

  public static @NotNull Set<InjectionPointDescriptor> getInjectionPoints(@NotNull PsiClass psiClass, boolean checkDeep) {
    Set<InjectionPointDescriptor> ips = new HashSet<>();
    if (!psiClass.isValid()) return ips;

    PsiField[] fields = checkDeep ? psiClass.getAllFields() : psiClass.getFields();
    for (PsiField field : fields) {
      if (AnnotationUtil.isAnnotated(field, GuiceAnnotations.INJECTS, 0)) {
        GuiceInject.Field inject = new GuiceInject.Field(field);
        ips.addAll(inject.getInjectionPoints());
      }
    }

    PsiMethod[] methods = checkDeep ? psiClass.getAllMethods() : psiClass.getMethods();
    for (PsiMethod method : methods) {
      if (AnnotationUtil.isAnnotated(method, GuiceAnnotations.INJECTS, 0)) {
        GuiceInject.Method inject = new GuiceInject.Method(method);
        ips.addAll(inject.getInjectionPoints());
      }
    }

    return ips;
  }

  public static @NotNull List<GuiceProvides> getProvides(@NotNull Project project, @NotNull GlobalSearchScope scope) {
    List<GuiceProvides> result = new ArrayList<>();
    PsiClass annoClass = JavaPsiFacade.getInstance(project).findClass(GuiceAnnotations.PROVIDES, GlobalSearchScope.allScope(project));
    if (annoClass != null) {
      for (PsiMethod method : AnnotatedElementsSearch.searchPsiMethods(annoClass, scope).findAll()) {
        result.add(new GuiceProvides.Method(method));
      }
    }
    return result;
  }

  public static @NotNull Set<InjectionPointDescriptor> getInjectionPoints(@NotNull BindDescriptor descriptor,
                                                                          @NotNull Set<? extends InjectionPointDescriptor> allInjectionPointDescriptors) {
    Set<InjectionPointDescriptor> ips = new HashSet<>();
    for (InjectionPointDescriptor ip : allInjectionPointDescriptors) {
      final PsiType type = ip.getType();

      if (type instanceof PsiClassType) {
        final PsiClass psiClass = ((PsiClassType)type).resolve();
        if (psiClass != null && psiClass.equals(descriptor.getBoundClass()) && checkBindingAnnotations(ip, descriptor)) {
          ips.add(ip);
        }
      }
    }

    return ips;
  }

  public static @NotNull Set<InjectionPointDescriptor> getInjectionPoints(@NotNull GuiceProvides provides,
                                                                          @NotNull Set<? extends InjectionPointDescriptor> allInjectionPointDescriptors) {

    Set<InjectionPointDescriptor> ips = new HashSet<>();

    final PsiType productType = provides.getProductType();
    if (productType != null) {
      for (InjectionPointDescriptor ip : allInjectionPointDescriptors) {
        final PsiType type = ip.getType();

        if (type != null && productType.isAssignableFrom(type)) {
          if (checkBindingAnnotations(ip.getBindingAnnotations(), provides.getBindingAnnotations())) {
            ips.add(ip);
          }
        }
      }
    }

    return ips;
  }

  public static boolean checkBindingAnnotations(@NotNull Set<? extends PsiAnnotation> annotations, @NotNull Set<? extends PsiAnnotation> candidateAnnotations) {
    if (annotations.size() != candidateAnnotations.size()) return false;

    for (PsiAnnotation psiAnnotation : annotations) {
      boolean hasAnno = false;
      for (PsiAnnotation annotation : candidateAnnotations) {
        if (compareAnnotations(psiAnnotation, annotation)) {
          hasAnno = true; break;
        }
      }
      if (!hasAnno) return false;
    }
    return true;
  }

  private static boolean compareAnnotations(@NotNull PsiAnnotation anno, @NotNull PsiAnnotation candidateAnno) {
    String fqn1 = anno.getQualifiedName();
    if (fqn1 != null && fqn1.equals(candidateAnno.getQualifiedName())) {
         return  compareAnnotationAttributes(getAnnotationAttributeValues(anno), getAnnotationAttributeValues(candidateAnno));
    }

    return false;
  }

  private static boolean compareAnnotationAttributes(@NotNull Set<Pair<String, PsiAnnotationMemberValue>> attrs,
                                                     @NotNull Set<Pair<String, PsiAnnotationMemberValue>> candidateAttrs) {
    if (attrs.isEmpty() && candidateAttrs.isEmpty()) return true;
    for (Pair<String, PsiAnnotationMemberValue> valuePair_1 : attrs) {
      for (Pair<String, PsiAnnotationMemberValue> valuePair_2 : candidateAttrs) {
        if (valuePair_1.first.equals(valuePair_2.first)) {
          final PsiAnnotationMemberValue value_1 = valuePair_1.getSecond();
          final PsiAnnotationMemberValue value_2 = valuePair_2.getSecond();

          if (value_1 instanceof PsiReference) {
            if (value_2 instanceof PsiReference) {
              PsiElement element1 = ((PsiReference)value_1).resolve();
              PsiElement element2 = ((PsiReference)value_2).resolve();
              return element1 != null && element2 != null && element1.equals(element2);
            }
          } else {
            Object attrValue = computeConstantValue(value_1);
            Object candidateAttrValue = computeConstantValue(value_2);

            return attrValue != null && candidateAttrValue != null && attrValue.equals(candidateAttrValue);
          }
          return false;
        }
      }
    }

    return false;
  }

  private static @Nullable Object computeConstantValue(@NotNull PsiAnnotationMemberValue value) {
    if (value instanceof PsiLiteralExpression) {
      return ((PsiLiteralExpression) value).getValue();
    }
    Project project = value.getProject();
    return JavaPsiFacade.getInstance(project).getConstantEvaluationHelper().computeConstantExpression(value);
  }

  private static @NotNull Set<Pair<String, PsiAnnotationMemberValue>> getAnnotationAttributeValues(@NotNull PsiAnnotation annotation) {
    Set<Pair<String, PsiAnnotationMemberValue>> pairs = new HashSet<>();

    final PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
    if (referenceElement != null) {
      PsiElement resolved = referenceElement.resolve();
      if (resolved != null) {
        PsiMethod[] methods = ((PsiClass)resolved).getMethods();
        for (PsiMethod psiMethod : methods) {
          if (PsiUtil.isAnnotationMethod(psiMethod)) {
            String attributeName = psiMethod.getName();
            pairs.add(Pair.create(attributeName, annotation.findAttributeValue(attributeName)));
          }
        }
      }
    }
    return pairs;
  }

  public static boolean checkBindingAnnotations(InjectionPointDescriptor ip, @NotNull BindDescriptor descriptor) {
    final Set<PsiAnnotation> bindingAnnotations = ip.getBindingAnnotations();
    if (bindingAnnotations.size() > 1) return false;

    final PsiMethodCallExpression expression = descriptor.getBindExpression();
    final PsiClass annotatedWith = getCallExpressionType(expression, "annotatedWith");
    if (annotatedWith == null && bindingAnnotations.isEmpty()) return true;

    if (annotatedWith != null && bindingAnnotations.size() == 1) {
      final PsiAnnotation bindingAnno = bindingAnnotations.iterator().next();
      if (GuiceAnnotations.NAMED.equals(annotatedWith.getQualifiedName())) {
        if (GuiceAnnotations.NAMED.equals(bindingAnno.getQualifiedName())) {
          final PsiExpression annotatedWithExpression = GuiceUtils.getArgumentOfCallInChain(expression, "annotatedWith");
          if (annotatedWithExpression != null) {
            final String nameValue = getNameValue(annotatedWithExpression);
            if (nameValue == null) return true; // impossible to define name

            return nameValue.equals(AnnotationUtil.getStringAttributeValue(bindingAnno, "value"));
          }
        }
        return false;
      }
      final String bindingAnnoQualifiedName = bindingAnno.getQualifiedName();
      if (bindingAnnoQualifiedName != null) {
        return bindingAnnoQualifiedName.equals(annotatedWith.getQualifiedName());
      }
    }

    return false;
  }

  private static @Nullable String getNameValue(@NotNull PsiExpression annotatedWithExpression) {
    final PsiExpression namedExpression = findNamedExpression(annotatedWithExpression);
    if (namedExpression != null) {
      final Pair<PsiElement, String> pair = StringExpressionHelper.evaluateExpression(namedExpression);
      if (pair != null) {
        return pair.getSecond();
      }
    }
    return null;
  }

  public static @Nullable PsiExpression findNamedExpression(final PsiExpression annotatedWithExpression) {
    if (annotatedWithExpression instanceof PsiMethodCallExpression expression) {
      final PsiMethod method = expression.resolveMethod();
      if (method != null) {
        if ("named".equals(method.getName())) {
          final PsiExpression[] expressions = expression.getArgumentList().getExpressions();
          return expressions.length > 0 ? expressions[0] : null;
        }
        else {
          PsiCodeBlock body = method.getBody();
          if (body != null) {
            final Set<PsiExpression> returns = new HashSet<>();

            body.accept(new JavaRecursiveElementVisitor() {
              @Override
              public void visitClass(@NotNull PsiClass aClass) {
              }

              @Override
              public void visitLambdaExpression(@NotNull PsiLambdaExpression expression) {
              }

              @Override
              public void visitReturnStatement(@NotNull PsiReturnStatement statement) {
                PsiExpression returnValue = statement.getReturnValue();
                if (returnValue != null) {
                  returns.add(returnValue);
                }
              }
            });

            for (PsiExpression psiExpression : returns) {
              final PsiExpression namedExpression = findNamedExpression(psiExpression);
              if (namedExpression != null) return namedExpression;
            }
          }
        }
      }
      final PsiExpression named = GuiceUtils.getArgumentOfCallInChain(expression, "named");
      if (named != null) {
        return named;
      }
    }
    if (annotatedWithExpression instanceof PsiReferenceExpression) {
      final PsiElement resolve = ((PsiReferenceExpression)annotatedWithExpression).resolve();
      if (resolve instanceof PsiVariable) {
        PsiExpression initializer = ((PsiVariable)resolve).getInitializer();
        if (initializer != null) {
          return findNamedExpression(initializer);
        }
      }
    }
    return null;
  }

  public static @Nullable PsiClass getCallExpressionType(@NotNull PsiMethodCallExpression expression, final String name) {
    PsiClass aClass = null;
    final PsiExpression psiExpression = GuiceUtils.getArgumentOfCallInChain(expression, name);
    if (psiExpression != null) {
      if (psiExpression instanceof PsiClassObjectAccessExpression) {
        final PsiType classType = ((PsiClassObjectAccessExpression)psiExpression).getOperand().getType();
        if (classType instanceof PsiClassType) {
          aClass = ((PsiClassType)classType).resolve();
        }
      }
      else {
        final PsiType psiType = psiExpression.getType();
        if (psiType instanceof PsiClassType) {
          aClass = ((PsiClassType)psiType).resolve();
        }
      }
    }

    return aClass;
  }
}
