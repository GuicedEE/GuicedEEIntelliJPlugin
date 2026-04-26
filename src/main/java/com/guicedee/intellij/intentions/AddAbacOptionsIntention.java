package com.guicedee.intellij.intentions;

public class AddAbacOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.abac.AbacOptions"; }
    @Override protected String getAnnotationDisplayName() { return "AbacOptions"; }
}

