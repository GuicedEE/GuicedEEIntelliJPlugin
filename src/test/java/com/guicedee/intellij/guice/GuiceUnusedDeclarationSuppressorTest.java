package com.guicedee.intellij.guice;

import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

/**
 * The suppressor must be narrow: only the unused tool, only on real injection points.
 * Anything else must keep the platform's normal behaviour.
 */
public class GuiceUnusedDeclarationSuppressorTest extends LightJavaCodeInsightFixtureTestCase {

  private final GuiceUnusedDeclarationSuppressor suppressor = new GuiceUnusedDeclarationSuppressor();

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
      @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
      public @interface Provides {}
      """);
  }

  public void testInjectedFieldIsSuppressedForUnused() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject private Object service; }
      """);
    assertTrue(suppressor.isSuppressedFor(file.getClasses()[0].getFields()[0], "unused"));
  }

  public void testNameIdentifierOfInjectedFieldIsSuppressed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject private Object service; }
      """);
    assertTrue(suppressor.isSuppressedFor(file.getClasses()[0].getFields()[0].getNameIdentifier(), "unused"));
  }

  public void testPlainFieldIsNotSuppressed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Plain.java",
      "class Plain { private int neverUsed; }");
    assertFalse(suppressor.isSuppressedFor(file.getClasses()[0].getFields()[0], "unused"));
  }

  public void testOtherInspectionsAreNotSuppressed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject private Object service; }
      """);
    assertFalse(suppressor.isSuppressedFor(file.getClasses()[0].getFields()[0], "SpellCheckingInspection"));
  }

  public void testInjectedConstructorIsSuppressed() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject Consumer(Object service) {} }
      """);
    PsiMethod constructor = file.getClasses()[0].getConstructors()[0];
    assertTrue(suppressor.isSuppressedFor(constructor, "unused"));
  }

  public void testProvidesMethodIsHandledByImplicitUsageNotSuppressor() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Module.java", """
      import com.google.inject.Provides;
      class Module { @Provides Object provideService() { return new Object(); } }
      """);
    assertFalse(suppressor.isSuppressedFor(file.getClasses()[0].getMethods()[0], "unused"));
  }

  public void testSuppressorOffersNoQuickFixes() {
    PsiJavaFile file = (PsiJavaFile)myFixture.configureByText("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject private Object service; }
      """);
    assertEquals(0, suppressor.getSuppressActions(file.getClasses()[0].getFields()[0], "unused").length);
  }
}
