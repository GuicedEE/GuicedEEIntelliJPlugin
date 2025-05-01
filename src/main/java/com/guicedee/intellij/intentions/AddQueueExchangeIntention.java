package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding QueueExchange annotation to package-info files.
 */
public class AddQueueExchangeIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.rabbit.QueueExchange";
    private static final String DISPLAY_NAME = "QueueExchange";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}
