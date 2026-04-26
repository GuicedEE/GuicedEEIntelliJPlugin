package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding IBMMQConnectionOptions annotation to package-info files.
 */
public class AddIBMMQConnectionOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.ibmmq.IBMMQConnectionOptions";
    private static final String DISPLAY_NAME = "IBMMQConnectionOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

