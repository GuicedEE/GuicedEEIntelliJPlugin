package com.guicedee.intellij.guice;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;

/**
 * Platform-level check: the unused highlighting for injected fields must be suppressed by the
 * plugin's InspectionSuppressor, while ordinary unused private fields must still be reported.
 */
public class GuiceUnusedHighlightingTest extends LightJavaCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("""
      package com.google.inject;
      @java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD,
          java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR,
          java.lang.annotation.ElementType.PARAMETER})
      public @interface Inject {}
      """);
  }

  public void testPlainPrivateFieldIsStillReportedUnused() {
    assertTrue(hasUnusedHighlight("Plain.java", "class Plain { private int neverUsed; }"));
  }

  public void testInjectedFieldIsNotReportedUnused() {
    assertFalse(hasUnusedHighlight("Consumer.java", """
      import com.google.inject.Inject;
      class Consumer { @Inject private Object service; }
      """));
  }

  private boolean hasUnusedHighlight(String name, String text) {
    myFixture.enableInspections(new UnusedSymbolLocalInspection());
    myFixture.configureByText(name, text);
    List<HighlightInfo> highlights = myFixture.doHighlighting();
    return highlights.stream().anyMatch(info -> {
      String description = info == null ? null : info.getDescription();
      return description != null && description.toLowerCase().contains("never used");
    });
  }
}
