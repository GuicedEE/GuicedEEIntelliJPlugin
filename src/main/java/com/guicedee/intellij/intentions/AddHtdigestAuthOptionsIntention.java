package com.guicedee.intellij.intentions;

public class AddHtdigestAuthOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.htdigest.HtdigestAuthOptions"; }
    @Override protected String getAnnotationDisplayName() { return "HtdigestAuthOptions"; }
}

