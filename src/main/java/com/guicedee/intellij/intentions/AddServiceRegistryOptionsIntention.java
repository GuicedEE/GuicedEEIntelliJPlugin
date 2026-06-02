package com.guicedee.intellij.intentions;

/**
 * Provides quick assist for adding @ServiceRegistryOptions annotation to package-info files.
 */
public class AddServiceRegistryOptionsIntention extends BasePackageInfoAnnotationIntention {

    private static final String ANNOTATION_NAME = "com.guicedee.service.registry.ServiceRegistryOptions";
    private static final String DISPLAY_NAME = "ServiceRegistryOptions";

    @Override
    protected String getAnnotationName() {
        return ANNOTATION_NAME;
    }

    @Override
    protected String getAnnotationDisplayName() {
        return DISPLAY_NAME;
    }
}

