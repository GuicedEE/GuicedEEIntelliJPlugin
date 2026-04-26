package com.guicedee.intellij.intentions;

public class AddOAuth2OptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.oauth2.OAuth2Options"; }
    @Override protected String getAnnotationDisplayName() { return "OAuth2Options"; }
}

