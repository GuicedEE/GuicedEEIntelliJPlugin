package com.guicedee.intellij.intentions;

public class AddOtpAuthOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.otp.OtpAuthOptions"; }
    @Override protected String getAnnotationDisplayName() { return "OtpAuthOptions"; }
}

