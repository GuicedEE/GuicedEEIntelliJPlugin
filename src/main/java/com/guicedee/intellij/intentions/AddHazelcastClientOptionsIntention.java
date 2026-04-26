package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding HazelcastClientOptions annotation to package-info files.
 */
public class AddHazelcastClientOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.guicedhazelcast.HazelcastClientOptions";
    private static final String DISPLAY_NAME = "HazelcastClientOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

