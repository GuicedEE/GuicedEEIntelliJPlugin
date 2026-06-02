package com.guicedee.intellij.intentions;

/**
 * Provides quick assist for adding @AzureContainerApps annotation to package-info files.
 */
public class AddAzureContainerAppsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.runtime.autoconfigure.AzureContainerApps";
    private static final String DISPLAY_NAME = "AzureContainerApps";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

