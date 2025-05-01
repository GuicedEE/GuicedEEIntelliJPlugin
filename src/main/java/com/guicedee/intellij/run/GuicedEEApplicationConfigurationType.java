package com.guicedee.intellij.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration type for Guiced EE applications.
 */
public class GuicedEEApplicationConfigurationType extends ConfigurationTypeBase {

    public static final String ID = "GuicedEEApplicationConfiguration";

    protected GuicedEEApplicationConfigurationType() {
        super(ID, "G Application", "Run G Application", AllIcons.RunConfigurations.Application);
        
        addFactory(new ConfigurationFactory(this) {
            @Override
            public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new GuicedEEApplicationConfiguration(project, this, "Guiced EE Application");
            }
            
            @Override
            public @NotNull String getId() {
                return "GuicedEEApplicationConfigurationFactory";
            }
        });
    }
    
    public static GuicedEEApplicationConfigurationType getInstance() {
        return ConfigurationType.CONFIGURATION_TYPE_EP.findExtensionOrFail(GuicedEEApplicationConfigurationType.class);
    }
}