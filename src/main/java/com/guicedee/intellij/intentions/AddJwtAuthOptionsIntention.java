package com.guicedee.intellij.intentions;

public class AddJwtAuthOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.jwt.JwtAuthOptions"; }
    @Override protected String getAnnotationDisplayName() { return "JwtAuthOptions"; }
}

