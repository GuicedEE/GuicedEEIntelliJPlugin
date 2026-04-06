package com.guicedee.intellij;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Central icon definitions for the Guiced plugin.
 */
public final class GuicedIcons {
    /** 16x16 GuicedEE logo icon (used in menus, action groups, and templates). */
    public static final @NotNull Icon Logo = IconLoader.getIcon("/icons/guicedee-logo.svg", GuicedIcons.class);

    private GuicedIcons() {}
}


