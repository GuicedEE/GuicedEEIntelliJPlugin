package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding RabbitConnectionOptions annotation to package-info files.
 */
public class AddRabbitConnectionOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.rabbit.RabbitConnectionOptions";
    private static final String DISPLAY_NAME = "RabbitConnectionOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}
