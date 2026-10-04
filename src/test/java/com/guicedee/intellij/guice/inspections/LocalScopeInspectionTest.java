package com.guicedee.intellij.guice.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class LocalScopeInspectionTest extends LightJavaCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("""
      package com.google.inject;
      @java.lang.annotation.Target(java.lang.annotation.ElementType.ANNOTATION_TYPE)
      public @interface ScopeAnnotation {}
      """);
    myFixture.addClass("""
      package com.google.inject;
      @java.lang.annotation.Target({java.lang.annotation.ElementType.CONSTRUCTOR,
          java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD})
      public @interface Inject {}
      """);
    addScope("com.google.inject", "Singleton", "com.google.inject.ScopeAnnotation");
    addScope("com.google.inject.servlet", "RequestScoped", "com.google.inject.ScopeAnnotation");
    addScope("com.google.inject.servlet", "SessionScoped", "com.google.inject.ScopeAnnotation");
    addScope("example", "LocalScoped", "com.google.inject.ScopeAnnotation");
  }

  private void addScope(String packageName, String name, String marker) {
    myFixture.addClass("""
      package %s;
      @%s
      @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE,
          java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.PARAMETER,
          java.lang.annotation.ElementType.FIELD})
      public @interface %s {}
      """.formatted(packageName, marker, name));
  }

  private void useGuicedEE() {
    myFixture.addClass("package com.google.inject.gee; public interface BindScopeProvider {}");
  }

  private int inspect(BaseInspection inspection, String source) {
    PsiFile file = myFixture.configureByText("Consumer.java", """
      import com.google.inject.*;
      import com.google.inject.servlet.*;
      import example.LocalScoped;
      """ + source);
    ProblemsHolder holder = new ProblemsHolder(InspectionManager.getInstance(getProject()), file, false);
    PsiElementVisitor visitor = inspection.buildVisitor(holder, false);
    file.accept(new PsiRecursiveElementWalkingVisitor() {
      @Override public void visitElement(PsiElement element) {
        element.accept(visitor);
        super.visitElement(element);
      }
    });
    return holder.getResults().size();
  }

  public void testCustomScopeConflictsOnFieldsAndConstructorParameters() {
    useGuicedEE();
    assertEquals(4, inspect(new ConflictingAnnotationsInspection(), """
      class Dependency {}
      class Consumer {
        @Inject @Singleton @LocalScoped Dependency field;
        @Inject Consumer(@Singleton @LocalScoped Dependency value) {}
      }
      """));
  }

  public void testDifferentScopesAtSeparateInjectionPointsDoNotConflict() {
    useGuicedEE();
    assertEquals(0, inspect(new ConflictingAnnotationsInspection(), """
      @Singleton class Dependency {}
      class Consumer {
        @Inject @Singleton Dependency first;
        @Inject @LocalScoped Dependency second;
        @Inject Consumer(@Singleton Dependency first, @LocalScoped Dependency second) {}
      }
      """));
  }

  public void testLocalScopeAndExistingBindingScopeDoNotConflict() {
    useGuicedEE();
    assertEquals(0, inspect(new ConflictingAnnotationsInspection(), """
      @Singleton class Dependency {}
      class Consumer {
        @Inject @RequestScoped Dependency field;
        @Inject Consumer(@LocalScoped Dependency value) {}
      }
      """));
  }

  public void testJakartaScopeMarkerIsRecognised() {
    useGuicedEE();
    myFixture.addClass("package jakarta.inject; public @interface Scope {}");
    addScope("example", "JakartaScoped", "jakarta.inject.Scope");
    assertEquals(2, inspect(new ConflictingAnnotationsInspection(), """
      class Consumer {
        @Inject @LocalScoped @example.JakartaScoped Object field;
      }
      """));
  }

  public void testSingletonInspectsLocalRequestAndSessionScopes() {
    useGuicedEE();
    assertEquals(4, inspect(new SingletonInjectsScopedInspection(), """
      class Dependency {}
      @Singleton class Consumer {
        @Inject @RequestScoped Dependency request;
        @Inject @SessionScoped Dependency session;
        @Inject Consumer(@RequestScoped Dependency first, @SessionScoped Dependency second) {}
      }
      """));
  }

  public void testSessionInspectsLocalRequestScopes() {
    useGuicedEE();
    assertEquals(2, inspect(new SessionScopedInjectsRequestScopedInspection(), """
      class Dependency {}
      @SessionScoped class Consumer {
        @Inject @RequestScoped Dependency field;
        @Inject Consumer(@RequestScoped Dependency value) {}
      }
      """));
  }

  public void testMethodParametersDoNotHaveLocalScopes() {
    useGuicedEE();
    assertEquals(0, inspect(new ConflictingAnnotationsInspection(), """
      class Dependency {}
      class Consumer {
        @Inject void configure(@Singleton @LocalScoped Dependency value) {}
      }
      """));
    assertEquals(0, inspect(new SingletonInjectsScopedInspection(), """
      class Dependency {}
      @Singleton class Consumer {
        @Inject void configure(@RequestScoped Dependency value) {}
      }
      """));
    assertEquals(0, inspect(new SessionScopedInjectsRequestScopedInspection(), """
      class Dependency {}
      @SessionScoped class Consumer {
        @Inject void configure(@RequestScoped Dependency value) {}
      }
      """));
  }

  public void testUninjectedConstructorDoesNotHaveLocalScopes() {
    useGuicedEE();
    assertEquals(0, inspect(new SingletonInjectsScopedInspection(), """
      class Dependency {}
      @Singleton class Consumer {
        Consumer(@RequestScoped Dependency value) {}
      }
      """));
  }

  public void testPlainGuiceDoesNotEnableLocalScopeSemantics() {
    assertEquals(0, inspect(new SingletonInjectsScopedInspection(), """
      class Dependency {}
      @Singleton class Consumer {
        @Inject @RequestScoped Dependency field;
        @Inject Consumer(@RequestScoped Dependency value) {}
      }
      """));
  }

  public void testExistingClassScopeLifetimeWarningsArePreserved() {
    String source = """
      @RequestScoped class Dependency {}
      @Singleton class Consumer {
        @Inject Dependency field;
        @Inject Consumer(Dependency value) {}
      }
      """;
    assertEquals(2, inspect(new SingletonInjectsScopedInspection(), source));
    assertEquals(2, inspect(new SessionScopedInjectsRequestScopedInspection(),
        source.replace("@Singleton class Consumer", "@SessionScoped class Consumer")));
  }
}
