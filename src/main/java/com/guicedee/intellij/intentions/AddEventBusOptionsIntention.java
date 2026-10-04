package com.guicedee.intellij.intentions;

/** Adds transport options independently of cluster activation. */
public class AddEventBusOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.spi.EventBusOptions"; }
    @Override protected String getAnnotationDisplayName() { return "EventBusOptions"; }
}
