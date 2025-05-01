package com.guicedee.intellij.run;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Run configuration for Guiced EE applications.
 */
public class GuicedEEApplicationConfiguration extends RunConfigurationBase<RunConfigurationOptions> {

    protected GuicedEEApplicationConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new GuicedEEApplicationConfigurationEditor(getProject());
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new JavaCommandLineState(environment) {
            @Override
            protected JavaParameters createJavaParameters() {
                JavaParameters params = new JavaParameters();
                // Configure the Java parameters for running the application
                // This would typically include setting the main class, classpath, JVM args, etc.
                return params;
            }
        };
    }
}
