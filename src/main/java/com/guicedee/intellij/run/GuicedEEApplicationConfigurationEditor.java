package com.guicedee.intellij.run;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Editor for Guiced EE application run configurations.
 */
public class GuicedEEApplicationConfigurationEditor extends SettingsEditor<GuicedEEApplicationConfiguration> {

    private final Project myProject;
    private JBPanel<?> myMainPanel;

    public GuicedEEApplicationConfigurationEditor(Project project) {
        myProject = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull GuicedEEApplicationConfiguration configuration) {
        // Reset the editor from the configuration
    }

    @Override
    protected void applyEditorTo(@NotNull GuicedEEApplicationConfiguration configuration) {
        // Apply the editor settings to the configuration
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        myMainPanel = new JBPanel<>();
        // Create the editor UI
        return myMainPanel;
    }
}