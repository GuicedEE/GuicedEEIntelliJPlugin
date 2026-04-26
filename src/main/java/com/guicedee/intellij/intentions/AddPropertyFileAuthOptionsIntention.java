package com.guicedee.intellij.intentions;

public class AddPropertyFileAuthOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.properties.PropertyFileAuthOptions"; }
    @Override protected String getAnnotationDisplayName() { return "PropertyFileAuthOptions"; }
}

