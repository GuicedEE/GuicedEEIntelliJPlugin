package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding @AuthOptions annotation to package-info files.
 */
public class AddAuthOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.vertx.auth.AuthOptions";
    private static final String DISPLAY_NAME = "AuthOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

