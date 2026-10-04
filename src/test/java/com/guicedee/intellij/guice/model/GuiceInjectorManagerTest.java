package com.guicedee.intellij.guice.model;

import com.guicedee.intellij.guice.constants.GuiceAnnotations;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.Set;

public class GuiceInjectorManagerTest extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("""
      package com.google.inject;

      @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
      @java.lang.annotation.Target(java.lang.annotation.ElementType.ANNOTATION_TYPE)
      public @interface BindingAnnotation {}
      """);
    myFixture.addClass("""
      package com.google.inject;

      @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
      @java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD,
                                    java.lang.annotation.ElementType.CONSTRUCTOR})
      public @interface Inject {}
      """);
    myFixture.addClass("""
      package com.google.inject.name;

      @com.google.inject.BindingAnnotation
      @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
      @java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER,
                                    java.lang.annotation.ElementType.METHOD})
      public @interface Named {
        String value();
      }
      """);
  }

  public void testImportedNamedIsRetainedAsBindingAnnotation() {
    PsiField field = configureField("""
      import com.google.inject.Inject;
      import com.google.inject.name.Named;

      class PublisherOwner {
        @Inject
        @Named("FarmUpdate")
        private Object farmUpdatePublisher;
      }
      """);

    assertNamedBinding(field, "FarmUpdate");
  }

  public void testFullyQualifiedNamedIsRetainedAsBindingAnnotation() {
    PsiField field = configureField("""
      class PublisherOwner {
        @com.google.inject.Inject
        @com.google.inject.name.Named("StaffUpdate")
        private Object staffUpdatePublisher;
      }
      """);

    assertNamedBinding(field, "StaffUpdate");
  }

  private PsiField configureField(String source) {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("PublisherOwner.java", source);
    return file.getClasses()[0].getFields()[0];
  }

  private static void assertNamedBinding(PsiField field, String expectedValue) {
    Set<PsiAnnotation> annotations = GuiceInjectorManager.getBindingAnnotations(field);
    assertEquals(1, annotations.size());

    PsiAnnotation annotation = annotations.iterator().next();
    assertEquals(GuiceAnnotations.NAMED, annotation.getQualifiedName());
    assertEquals(expectedValue, annotation.findAttributeValue("value").getText().replace("\"", ""));
  }
}
