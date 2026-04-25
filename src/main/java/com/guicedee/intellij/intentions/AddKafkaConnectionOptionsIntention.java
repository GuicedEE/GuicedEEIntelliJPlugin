package com.guicedee.intellij.intentions;

/**
 * Provides quick assist functionality for adding KafkaConnectionOptions annotation to package-info files.
 */
public class AddKafkaConnectionOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.kafka.KafkaConnectionOptions";
    private static final String DISPLAY_NAME = "KafkaConnectionOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

