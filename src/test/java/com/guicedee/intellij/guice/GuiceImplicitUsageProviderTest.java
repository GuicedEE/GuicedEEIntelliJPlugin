package com.guicedee.intellij.guice;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

/**
 * Regression coverage for issue #1 "No suggestions for unused fields".
 *
 * The plugin must only claim real Guice/GuicedEE injection points. If an ordinary private field
 * (or a whole class) is reported as implicitly used, IntelliJ stops showing both the
 * "Unused declaration" warning and the "N usages" code-vision hint.
 */
public class GuiceImplicitUsageProviderTest extends LightJavaCodeInsightFixtureTestCase {

  private final ImplicitUsageProvider provider = new GuiceImplicitUsageProvider();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("""
      package com.google.inject;
      @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE,
          java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD,
          java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.PARAMETER})
      public @interface Inject {}
      """);
    myFixture.addClass("""
      package com.google.inject;
      @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE,
          java.lang.annotation.ElementType.METHOD})
      public @interface Provides {}
      """);
    myFixture.addClass("""
      package com.guicedee.vertx;
      @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE,
          java.lang.annotation.ElementType.METHOD})
      public @interface VertxEventDefinition {}
      """);
  }

  public void testPlainPrivateFieldsAreNotClaimed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Plain.java",
      "class Plain { private int neverUsed; private String alsoNeverUsed; }");
    for (PsiField field : file.getClasses()[0].getFields()) {
      assertFalse(field.getName(), provider.isImplicitUsage(field));
      assertFalse(field.getName(), provider.isImplicitRead(field));
      assertFalse(field.getName(), provider.isImplicitWrite(field));
    }
  }

  public void testPlainClassIsNotClaimed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Plain.java",
      "class Plain { private int neverUsed; }");
    PsiClass aClass = file.getClasses()[0];
    assertFalse(provider.isImplicitUsage(aClass));
    assertFalse(provider.isImplicitRead(aClass));
    assertFalse(provider.isImplicitWrite(aClass));
  }

  public void testInjectionAnnotationOnClassDoesNotClaimWholeClass() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Odd.java", """
      import com.google.inject.Inject;
      @Inject class Odd { private int neverUsed; }
      """);
    PsiClass aClass = file.getClasses()[0];
    assertFalse(provider.isImplicitUsage(aClass));
    assertFalse(provider.isImplicitRead(aClass));
    assertFalse(provider.isImplicitWrite(aClass));
    assertFalse(provider.isImplicitUsage(aClass.getFields()[0]));
  }

  public void testProvidesAnnotationOnClassDoesNotClaimWholeClass() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Odd.java", """
      import com.google.inject.Provides;
      @Provides class Odd { private int neverUsed; }
      """);
    PsiClass aClass = file.getClasses()[0];
    assertFalse(provider.isImplicitUsage(aClass));
    assertFalse(provider.isImplicitRead(aClass));
  }

  public void testInjectedFieldIsClaimed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject private Object service; }
      """);
    PsiField field = file.getClasses()[0].getFields()[0];
    assertTrue(provider.isImplicitUsage(field));
    assertTrue(provider.isImplicitRead(field));
    assertTrue(provider.isImplicitWrite(field));
  }

  public void testProvidesMethodIsClaimedAsReadButNotWritten() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Module.java", """
      import com.google.inject.Provides;
      class Module { @Provides Object provideService() { return new Object(); } }
      """);
    PsiMethod method = file.getClasses()[0].getMethods()[0];
    assertTrue(provider.isImplicitUsage(method));
    assertTrue(provider.isImplicitRead(method));
    assertFalse(provider.isImplicitWrite(method));
  }

  public void testFrameworkEntryPointMethodIsClaimed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.guicedee.vertx.VertxEventDefinition;
      class Consumer { @VertxEventDefinition void onMessage(Object message) { } }
      """);
    PsiMethod method = file.getClasses()[0].getMethods()[0];
    assertTrue(provider.isImplicitUsage(method));
    assertFalse(provider.isImplicitWrite(method));
  }

}
