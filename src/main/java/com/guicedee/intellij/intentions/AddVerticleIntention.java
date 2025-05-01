package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding Verticle annotation to package-info files.
 */
public class AddVerticleIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.vertx.spi.Verticle";
    private static final String DISPLAY_NAME = "Verticle";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}
