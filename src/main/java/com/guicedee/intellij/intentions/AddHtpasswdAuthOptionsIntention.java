package com.guicedee.intellij.intentions;

public class AddHtpasswdAuthOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.htpasswd.HtpasswdAuthOptions"; }
    @Override protected String getAnnotationDisplayName() { return "HtpasswdAuthOptions"; }
}

