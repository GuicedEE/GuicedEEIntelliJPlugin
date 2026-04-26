package com.guicedee.intellij.intentions;

public class AddLdapAuthOptionsIntention extends BasePackageInfoAnnotationIntention {
    @Override protected String getAnnotationName() { return "com.guicedee.vertx.auth.ldap.LdapAuthOptions"; }
    @Override protected String getAnnotationDisplayName() { return "LdapAuthOptions"; }
}

