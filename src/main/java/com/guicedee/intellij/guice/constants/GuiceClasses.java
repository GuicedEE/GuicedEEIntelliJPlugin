// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.guicedee.intellij.guice.constants;

public final class GuiceClasses {

  private GuiceClasses() {
  }

  public static final String ABSTRACT_MODULE = "com.google.inject.AbstractModule";

  public static final String PROVIDER = "com.google.inject.Provider";
  public static final String SCOPED_BINDING_BUILDER = "com.google.inject.binder.ScopedBindingBuilder";
  public static final String LINKED_BINDING_BUILDER = "com.google.inject.binder.LinkedBindingBuilder";

  // GuicedEE IGuiceModule SPI — implementations extend AbstractModule and implement this interface
  public static final String GUICEDEE_ABSTRACT_MODULE = "com.guicedee.client.services.lifecycle.IGuiceModule";

  public static final String MATCHERS = "com.google.inject.matcher.Matchers";
  public static final String SCOPES = "com.google.inject.Scopes";
  public static final String SERVLET_SCOPES = "com.google.inject.servlet.ServletScopes";
}
