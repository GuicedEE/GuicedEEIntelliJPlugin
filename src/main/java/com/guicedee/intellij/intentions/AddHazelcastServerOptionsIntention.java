package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding HazelcastServerOptions annotation to package-info files.
 */
public class AddHazelcastServerOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.guicedhazelcast.HazelcastServerOptions";
    private static final String DISPLAY_NAME = "HazelcastServerOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

