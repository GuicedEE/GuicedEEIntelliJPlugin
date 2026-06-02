package com.guicedee.intellij.intentions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for configuring a @RegisteredService annotation with all available options.
 */
public class RegisteredServiceDialog extends DialogWrapper {

    private JBTextField nameField;
    private JBTextField urlField;
    private JBTextField healthPathField;
    private JBTextField aliasesField;
    private JBTextField externalUrlsField;
    private JBTextField kubernetesUrlField;

    public RegisteredServiceDialog(@Nullable Project project) {
        super(project);
        setTitle("Add Registered Service");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.weightx = 0;

        int row = 0;

        // Service Name (required)
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("Service Name *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JBTextField(20);
        nameField.getEmptyText().setText("e.g. jwebmp-website");
        panel.add(nameField, gbc);

        // URL (optional)
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("URL (optional):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        urlField = new JBTextField(30);
        urlField.getEmptyText().setText("Empty = auto-construct from DNS suffix. Supports ${ENV_VAR}");
        panel.add(urlField, gbc);

        // Health Path
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("Health Path:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        healthPathField = new JBTextField(20);
        healthPathField.getEmptyText().setText("Empty = use registry default (e.g. /health/ready)");
        panel.add(healthPathField, gbc);

        // Aliases
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("Aliases:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        aliasesField = new JBTextField(30);
        aliasesField.getEmptyText().setText("Comma-separated, e.g. jwebmp, jwebswing");
        panel.add(aliasesField, gbc);

        // External URLs
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("External URLs:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        externalUrlsField = new JBTextField(30);
        externalUrlsField.getEmptyText().setText("Comma-separated, e.g. https://jwebmp.com, https://jwebswing.com");
        panel.add(externalUrlsField, gbc);

        // Kubernetes URL
        row++;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JBLabel("Kubernetes URL:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        kubernetesUrlField = new JBTextField(30);
        kubernetesUrlField.getEmptyText().setText("e.g. http://service.namespace.svc.cluster.local");
        panel.add(kubernetesUrlField, gbc);

        panel.setPreferredSize(new Dimension(550, 220));
        return panel;
    }

    public String getServiceName() {
        return nameField.getText().trim();
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public String getHealthPath() {
        return healthPathField.getText().trim();
    }

    public String getAliases() {
        return aliasesField.getText().trim();
    }

    public String getExternalUrls() {
        return externalUrlsField.getText().trim();
    }

    public String getKubernetesUrl() {
        return kubernetesUrlField.getText().trim();
    }

    /**
     * Builds the full annotation text from the dialog values.
     */
    public String buildAnnotationText() {
        StringBuilder sb = new StringBuilder("@com.guicedee.service.registry.RegisteredService(name = \"");
        sb.append(getServiceName()).append("\"");

        if (!getUrl().isEmpty()) {
            sb.append(",\n    url = \"").append(getUrl()).append("\"");
        }
        if (!getHealthPath().isEmpty()) {
            sb.append(",\n    healthPath = \"").append(getHealthPath()).append("\"");
        }
        if (!getAliases().isEmpty()) {
            String[] parts = getAliases().split(",");
            sb.append(",\n    aliases = {");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(parts[i].trim()).append("\"");
            }
            sb.append("}");
        }
        if (!getExternalUrls().isEmpty()) {
            String[] parts = getExternalUrls().split(",");
            sb.append(",\n    externalUrls = {");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append("\"").append(parts[i].trim()).append("\"");
            }
            sb.append("}");
        }
        if (!getKubernetesUrl().isEmpty()) {
            sb.append(",\n    kubernetesUrl = \"").append(getKubernetesUrl()).append("\"");
        }

        sb.append(")");
        return sb.toString();
    }
}

