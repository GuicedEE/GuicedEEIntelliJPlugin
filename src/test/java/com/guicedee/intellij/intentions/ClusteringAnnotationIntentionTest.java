package com.guicedee.intellij.intentions;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class ClusteringAnnotationIntentionTest extends LightJavaCodeInsightFixtureTestCase {
    public void testHazelcastIntentionAddsCacheOnlyDefaultsAndIsIdempotent() {
        myFixture.addClass("""
                package com.guicedee.guicedhazelcast;
                @java.lang.annotation.Target(java.lang.annotation.ElementType.PACKAGE)
                public @interface HazelcastServerOptions {
                    boolean clustered() default true;
                    boolean startLocal() default false;
                    JoinType joinType() default JoinType.MULTICAST;
                    enum JoinType { NONE, MULTICAST }
                }
                """);
        var file = myFixture.configureByText("package-info.java", "package example.orders;");
        var intention = new AddHazelcastServerOptionsIntention();
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(getProject(),
                () -> intention.invoke(getProject(), myFixture.getEditor(), file));
        assertTrue(file.getText().contains("clustered = false"));
        assertTrue(file.getText().contains("startLocal = true"));
        assertTrue(file.getText().contains("JoinType.NONE"));
        assertFalse(intention.isAvailable(getProject(), myFixture.getEditor(), file));
    }

    public void testEventBusIntentionDoesNotEnableHazelcast() {
        myFixture.addClass("""
                package com.guicedee.vertx.spi;
                @java.lang.annotation.Target(java.lang.annotation.ElementType.PACKAGE)
                public @interface EventBusOptions {}
                """);
        var file = myFixture.configureByText("package-info.java", "package example.orders;");
        var intention = new AddEventBusOptionsIntention();
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(getProject(),
                () -> intention.invoke(getProject(), myFixture.getEditor(), file));
        assertTrue(file.getText().contains("@com.guicedee.vertx.spi.EventBusOptions"));
        assertFalse(file.getText().contains("Hazelcast"));
    }
}
