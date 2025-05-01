package com.guicedee.intellij.wizard;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GuicedEEProjectWizardStep extends ModuleWizardStep {
    private final WizardContext myWizardContext;
    private final GuicedEEProjectWizardData myWizardData;

    private JPanel myMainPanel;
    private JBTextField myGroupIdField;
    private JBTextField myArtifactIdField;
    private JBTextField myVersionField;
    private JBTextField myDescriptionField;
    private JBTextField myUrlField;
    private JBTextField myScmConnectionField;
    private JBTextField myScmDeveloperConnectionField;
    private JBTextField myScmUrlField;
    private JBTextField myDeveloperNameField;
    private JBTextField myDeveloperEmailField;
    private JBTextField myDeveloperOrganizationField;
    private JBTextField myDeveloperOrganizationUrlField;
    private JCheckBox myMultiModuleCheckBox;
    private JCheckBox myJmodPackagingCheckBox;
    private JCheckBox myJlinkPackagingCheckBox;
    private JPanel myOptionalPanel;

    // Module-specific UI components
    private JPanel myModulesPanel;
    private JBList<GuicedEEProjectWizardData.ModuleData> myModulesList;
    private CollectionListModel<GuicedEEProjectWizardData.ModuleData> myModulesListModel;
    private JButton myAddModuleButton;
    private JButton myRemoveModuleButton;
    private JPanel myModuleFeaturesPanel;
    private JBTextField myModuleNameField;
    private JBTextField myModuleArtifactIdField;

    // Main feature groups
    private JCheckBox myModuleWebReactiveCheckBox;
    private JCheckBox myModuleDatabaseCheckBox;
    private JCheckBox myModuleMessagingCheckBox;
    private JCheckBox myModuleCachingCheckBox;
    private JCheckBox myModuleMicroProfileCheckBox;

    // Web Reactive sub-options
    private JPanel myModuleWebReactiveOptionsPanel;
    private JCheckBox myModuleWebReactiveRestCheckBox;
    private JCheckBox myModuleWebReactiveWebSocketsCheckBox;
    private JCheckBox myModuleWebReactiveSwaggerCheckBox;

    // Database sub-options
    private JPanel myModuleDatabaseOptionsPanel;
    private JCheckBox myModuleDatabasePersistenceCheckBox;
    private JCheckBox myModuleDatabasePostgreSQLCheckBox;
    private JCheckBox myModuleDatabaseMySQLCheckBox;
    private JCheckBox myModuleDatabaseOracleCheckBox;
    private JCheckBox myModuleDatabaseDB2CheckBox;
    private JCheckBox myModuleDatabaseSqlServerCheckBox;
    private JCheckBox myModuleDatabaseCassandraCheckBox;
    private JCheckBox myModuleDatabaseMongoDBCheckBox;
    private JCheckBox myModuleDatabaseJDBCCheckBox;

    // Messaging sub-options
    private JPanel myModuleMessagingOptionsPanel;
    private JCheckBox myModuleMessagingRabbitMQCheckBox;
    private JCheckBox myModuleMessagingKafkaCheckBox;
    private JCheckBox myModuleMessagingAMQPCheckBox;
    private JCheckBox myModuleMessagingMQTTCheckBox;

    // Caching sub-options
    private JPanel myModuleCachingOptionsPanel;
    private JCheckBox myModuleCachingHazelcastCheckBox;
    private JCheckBox myModuleCachingVertxHazelcastCheckBox;

    // MicroProfile sub-options
    private JPanel myModuleMicroProfileOptionsPanel;
    private JCheckBox myModuleMicroProfileHealthCheckBox;
    private JCheckBox myModuleMicroProfileTelemetryCheckBox;
    private JCheckBox myModuleMicroProfileOpenAPICheckBox;
    private JCheckBox myModuleMicroProfileLoggingCheckBox;
    private JCheckBox myModuleMicroProfileZipkinCheckBox;

    public GuicedEEProjectWizardStep(WizardContext wizardContext, GuicedEEProjectWizardData wizardData) {
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep constructor called");
        myWizardContext = wizardContext;
        myWizardData = wizardData;
        initUI();
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep constructor completed");
    }

    private void initUI() {
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep.initUI() called");
        myGroupIdField = new JBTextField("com.example");
        myVersionField = new JBTextField("1.0-SNAPSHOT");
        myMultiModuleCheckBox = new JCheckBox("Multi-Module Project");
        myJmodPackagingCheckBox = new JCheckBox("JMOD Packaging");
        myJlinkPackagingCheckBox = new JCheckBox("JLink Packaging");

        // Add document listener to group ID field to update module names when group ID changes
        myGroupIdField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateModuleNames();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateModuleNames();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateModuleNames();
            }

            private void updateModuleNames() {
                // Only update module names if the module name field is visible and has focus
                if (myModuleNameField != null && myModuleNameField.isVisible() && !myModuleNameField.hasFocus()) {
                    GuicedEEProjectWizardData.ModuleData selectedModule = myModulesList.getSelectedValue();
                    if (selectedModule != null) {
                        String artifactId = myModuleArtifactIdField.getText().trim();
                        if (!artifactId.isEmpty()) {
                            String newModuleName = generateModuleNameFromGroupIdAndArtifactId(myGroupIdField.getText(), artifactId);
                            myModuleNameField.setText(newModuleName);
                            updateSelectedModule();
                        }
                    }
                }
            }
        });

        JBLabel featuresLabel = new JBLabel("Application Features:");
        featuresLabel.setFont(featuresLabel.getFont().deriveFont(Font.BOLD));

        JBLabel packagingLabel = new JBLabel("Project Structure:");
        packagingLabel.setFont(packagingLabel.getFont().deriveFont(Font.BOLD));

        // Create optional panel
        myOptionalPanel = createOptionalPanel();

        // Create expandable section for optional information
        JBLabel optionalLabel = new JBLabel("Optional Information (click to expand)");
        optionalLabel.setFont(optionalLabel.getFont().deriveFont(Font.BOLD));

        // Initially hide the optional panel
        myOptionalPanel.setVisible(false);

        // Add click listener to toggle optional panel visibility
        optionalLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        optionalLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                myOptionalPanel.setVisible(!myOptionalPanel.isVisible());
                // Refresh the panel
                myMainPanel.revalidate();
                myMainPanel.repaint();
            }
        });

        // Create modules panel
        myModulesPanel = createModulesPanel();

        // Initialize modules list with a default module for single-module projects
        if (myWizardData.getModules().isEmpty()) {
            String artifactId = "guicedee-app";
            String moduleName = generateModuleNameFromGroupIdAndArtifactId(myGroupIdField.getText(), artifactId);
            myWizardData.addModule(moduleName, artifactId);
            updateModulesList();
        }

        // Add listener to multi-module checkbox to show/hide add/remove module buttons
        myMultiModuleCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isMultiModule = myMultiModuleCheckBox.isSelected();

                // Show/hide add/remove module buttons based on multi-module selection
                myAddModuleButton.setVisible(isMultiModule);
                myRemoveModuleButton.setVisible(isMultiModule);

                // Handle switching between single and multi-module mode
                if (isMultiModule) {
                    // If multi-module is enabled, initialize modules if empty
                    if (myWizardData.getModules().isEmpty()) {
                        myWizardData.initializeModules();
                    }
                } else {
                    // If single-module is enabled, ensure only one module is shown
                    if (myWizardData.getModules().size() > 1) {
                        // Keep only the first module
                        GuicedEEProjectWizardData.ModuleData firstModule = myWizardData.getModules().get(0);
                        myWizardData.getModules().clear();
                        myWizardData.getModules().add(firstModule);
                    } else if (myWizardData.getModules().isEmpty()) {
                        // If no modules, add a default one
                        String artifactId = "guicedee-app";
                        String moduleName = generateModuleNameFromGroupIdAndArtifactId(myGroupIdField.getText(), artifactId);
                        myWizardData.addModule(moduleName, artifactId);
                    }
                }

                // Update the modules list
                updateModulesList();

                // Update the module features panel
                updateModuleFeaturesPanel();

                // Refresh the panel
                myMainPanel.revalidate();
                myMainPanel.repaint();
            }
        });

        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Group ID:"), myGroupIdField)
                .addLabeledComponent(new JBLabel("Version:"), myVersionField)
                .addVerticalGap(10)
                .addComponent(packagingLabel)
                .addComponent(myMultiModuleCheckBox)
                .addComponent(myJmodPackagingCheckBox)
                .addComponent(myJlinkPackagingCheckBox)
                .addVerticalGap(10)
                .addComponent(new JBLabel("Project Configuration:"))
                .addComponent(myModulesPanel)
                .addVerticalGap(10)
                .addComponent(optionalLabel)
                .addComponent(myOptionalPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // Initialize the module features panel
        updateModuleFeaturesPanel();
    }

    @Override
    public JComponent getComponent() {
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep.getComponent() called");
        return myMainPanel;
    }

    @Override
    public void updateDataModel() {
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep.updateDataModel() called");
        // Required fields
        myWizardData.setGroupId(myGroupIdField.getText());
        myWizardData.setVersion(myVersionField.getText());

        // Application features are set at the module level in updateSelectedModule()
        myWizardData.setMultiModuleProject(myMultiModuleCheckBox.isSelected());
        myWizardData.setJmodPackaging(myJmodPackagingCheckBox.isSelected());
        myWizardData.setJlinkPackaging(myJlinkPackagingCheckBox.isSelected());

        // Get artifact ID from the selected module or module-specific field
        if (myWizardData.isMultiModuleProject()) {
            GuicedEEProjectWizardData.ModuleData selectedModule = myModulesList.getSelectedValue();
            if (selectedModule != null) {
                myWizardData.setArtifactId(selectedModule.getArtifactId());
            }
        } else {
            // For single-module projects, use the module artifact ID field
            if (myModuleArtifactIdField != null && !myModuleArtifactIdField.getText().isEmpty()) {
                myWizardData.setArtifactId(myModuleArtifactIdField.getText());
            }
        }

        // Optional fields
        myWizardData.setDescription(myDescriptionField.getText());
        myWizardData.setUrl(myUrlField.getText());
        myWizardData.setScmConnection(myScmConnectionField.getText());
        myWizardData.setScmDeveloperConnection(myScmDeveloperConnectionField.getText());
        myWizardData.setScmUrl(myScmUrlField.getText());
        myWizardData.setDeveloperName(myDeveloperNameField.getText());
        myWizardData.setDeveloperEmail(myDeveloperEmailField.getText());
        myWizardData.setDeveloperOrganization(myDeveloperOrganizationField.getText());
        myWizardData.setDeveloperOrganizationUrl(myDeveloperOrganizationUrlField.getText());

        // Save the currently selected module
        updateSelectedModule();

        // If this is a multi-module project, make sure we have at least one module
        if (myWizardData.isMultiModuleProject() && myWizardData.getModules().isEmpty()) {
            myWizardData.initializeModules();
        }

        // Set project-level flags based on module data
        updateProjectLevelFlags();
    }

    @Override
    public boolean validate() {
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep.validate() called");
        try {
            boolean superValidate = super.validate();
            System.out.println("[DEBUG_LOG] super.validate() returned: " + superValidate);
            ValidationInfo validationInfo = validateFields();
            System.out.println("[DEBUG_LOG] validateFields() returned: " + (validationInfo == null ? "null" : validationInfo.message));
            return superValidate && validationInfo == null;
        } catch (ConfigurationException e) {
            System.out.println("[DEBUG_LOG] ConfigurationException in validate(): " + e.getMessage());
            return false;
        }
    }

    @Nullable
    private ValidationInfo validateFields() {
        System.out.println("[DEBUG_LOG] GuicedEEProjectWizardStep.validateFields() called");
        if (myGroupIdField.getText().trim().isEmpty()) {
            return new ValidationInfo("Group ID cannot be empty", myGroupIdField);
        }
        if (myVersionField.getText().trim().isEmpty()) {
            return new ValidationInfo("Version cannot be empty", myVersionField);
        }

        // Validate artifact ID from the module-specific field
        if (myWizardData.isMultiModuleProject()) {
            GuicedEEProjectWizardData.ModuleData selectedModule = myModulesList.getSelectedValue();
            if (selectedModule != null && selectedModule.getArtifactId().trim().isEmpty()) {
                return new ValidationInfo("Artifact ID cannot be empty", myModuleArtifactIdField);
            }
        } else {
            if (myModuleArtifactIdField != null && myModuleArtifactIdField.getText().trim().isEmpty()) {
                return new ValidationInfo("Artifact ID cannot be empty", myModuleArtifactIdField);
            }
        }

        // Validate module name (always required, regardless of multi-module setting)
        if (myModuleNameField != null) {
            String moduleName = myModuleNameField.getText().trim();
            if (moduleName.isEmpty()) {
                return new ValidationInfo("Module name cannot be empty", myModuleNameField);
            }

            // Validate Java module name syntax
            if (!moduleName.matches("[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*")) {
                // Add a more descriptive error message
                return new ValidationInfo("Module name must follow Java module name syntax (e.g., 'com.example.module'). " +
                        "It will be automatically defaulted to package name + artifact name.", myModuleNameField);
            }
        }

        return null;
    }

    private JPanel createOptionalPanel() {
        // Initialize optional fields if not already done
        if (myDescriptionField == null) {
            myDescriptionField = new JBTextField();
            myUrlField = new JBTextField();
            myScmConnectionField = new JBTextField();
            myScmDeveloperConnectionField = new JBTextField();
            myScmUrlField = new JBTextField();
            myDeveloperNameField = new JBTextField();
            myDeveloperEmailField = new JBTextField();
            myDeveloperOrganizationField = new JBTextField();
            myDeveloperOrganizationUrlField = new JBTextField();
        }

        // Create a panel for optional fields
        JBLabel scmLabel = new JBLabel("SCM Information");
        scmLabel.setFont(scmLabel.getFont().deriveFont(Font.BOLD));

        JBLabel devLabel = new JBLabel("Developer Information");
        devLabel.setFont(devLabel.getFont().deriveFont(Font.BOLD));

        FormBuilder optionalBuilder = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Description:"), myDescriptionField)
                .addLabeledComponent(new JBLabel("URL:"), myUrlField)
                .addVerticalGap(10)
                .addComponent(scmLabel)
                .addLabeledComponent(new JBLabel("SCM Connection:"), myScmConnectionField)
                .addLabeledComponent(new JBLabel("SCM Developer Connection:"), myScmDeveloperConnectionField)
                .addLabeledComponent(new JBLabel("SCM URL:"), myScmUrlField)
                .addVerticalGap(10)
                .addComponent(devLabel)
                .addLabeledComponent(new JBLabel("Developer Name:"), myDeveloperNameField)
                .addLabeledComponent(new JBLabel("Developer Email:"), myDeveloperEmailField)
                .addLabeledComponent(new JBLabel("Developer Organization:"), myDeveloperOrganizationField)
                .addLabeledComponent(new JBLabel("Developer Organization URL:"), myDeveloperOrganizationUrlField);

        return optionalBuilder.getPanel();
    }

    /**
     * Creates the panel for managing modules in a multi-module project.
     */
    private JPanel createModulesPanel() {
        // Initialize the modules list
        myModulesListModel = new CollectionListModel<>();
        myModulesList = new JBList<>(myModulesListModel);
        myModulesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myModulesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GuicedEEProjectWizardData.ModuleData) {
                    GuicedEEProjectWizardData.ModuleData module = (GuicedEEProjectWizardData.ModuleData) value;
                    setText(module.getName() + " (" + module.getArtifactId() + ")");
                }
                return c;
            }
        });

        // Add selection listener to update the module features panel
        myModulesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateModuleFeaturesPanel();
                }
            }
        });

        // Create scroll pane for the modules list
        JBScrollPane scrollPane = new JBScrollPane(myModulesList);
        scrollPane.setPreferredSize(new Dimension(300, 150));

        // Create buttons for adding and removing modules
        myAddModuleButton = new JButton("Add Module");
        myRemoveModuleButton = new JButton("Remove Module");

        // Initially hide the add/remove module buttons (only visible in multi-module mode)
        myAddModuleButton.setVisible(false);
        myRemoveModuleButton.setVisible(false);

        // Add action listeners to buttons
        myAddModuleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addModule();
            }
        });

        myRemoveModuleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeModule();
            }
        });

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(myAddModuleButton);
        buttonPanel.add(myRemoveModuleButton);

        // Create module features panel
        myModuleFeaturesPanel = createModuleFeaturesPanel();

        // Create main panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.WEST);
        panel.add(myModuleFeaturesPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the panel for editing module features.
     */
    private JPanel createModuleFeaturesPanel() {
        // Initialize module feature fields
        myModuleNameField = new JBTextField();
        myModuleArtifactIdField = new JBTextField();

        // Initialize main feature groups
        myModuleWebReactiveCheckBox = new JCheckBox("Web Reactive");
        myModuleDatabaseCheckBox = new JCheckBox("Database");
        myModuleMessagingCheckBox = new JCheckBox("Messaging");
        myModuleCachingCheckBox = new JCheckBox("Caching");
        myModuleMicroProfileCheckBox = new JCheckBox("MicroProfile");

        // Initialize Web Reactive sub-options
        myModuleWebReactiveOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        myModuleWebReactiveRestCheckBox = new JCheckBox("Rest");
        myModuleWebReactiveWebSocketsCheckBox = new JCheckBox("Web Sockets");
        myModuleWebReactiveSwaggerCheckBox = new JCheckBox("Swagger");
        myModuleWebReactiveOptionsPanel.add(myModuleWebReactiveRestCheckBox);
        myModuleWebReactiveOptionsPanel.add(myModuleWebReactiveWebSocketsCheckBox);
        myModuleWebReactiveOptionsPanel.add(myModuleWebReactiveSwaggerCheckBox);
        myModuleWebReactiveOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        myModuleWebReactiveOptionsPanel.setVisible(false);

        // Initialize Database sub-options
        myModuleDatabaseOptionsPanel = new JPanel(new GridLayout(0, 3));
        myModuleDatabasePersistenceCheckBox = new JCheckBox("Persistence");
        myModuleDatabasePostgreSQLCheckBox = new JCheckBox("PostgreSQL");
        myModuleDatabaseMySQLCheckBox = new JCheckBox("MySQL");
        myModuleDatabaseOracleCheckBox = new JCheckBox("Oracle");
        myModuleDatabaseDB2CheckBox = new JCheckBox("DB2");
        myModuleDatabaseSqlServerCheckBox = new JCheckBox("SQL Server");
        myModuleDatabaseCassandraCheckBox = new JCheckBox("Cassandra");
        myModuleDatabaseMongoDBCheckBox = new JCheckBox("MongoDB");
        myModuleDatabaseJDBCCheckBox = new JCheckBox("JDBC");
        myModuleDatabaseOptionsPanel.add(myModuleDatabasePersistenceCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabasePostgreSQLCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseMySQLCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseOracleCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseDB2CheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseSqlServerCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseCassandraCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseMongoDBCheckBox);
        myModuleDatabaseOptionsPanel.add(myModuleDatabaseJDBCCheckBox);
        myModuleDatabaseOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        myModuleDatabaseOptionsPanel.setVisible(false);

        // Initialize Messaging sub-options
        myModuleMessagingOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        myModuleMessagingRabbitMQCheckBox = new JCheckBox("RabbitMQ");
        myModuleMessagingKafkaCheckBox = new JCheckBox("Kafka");
        myModuleMessagingAMQPCheckBox = new JCheckBox("AMQP");
        myModuleMessagingMQTTCheckBox = new JCheckBox("MQTT");
        myModuleMessagingOptionsPanel.add(myModuleMessagingRabbitMQCheckBox);
        myModuleMessagingOptionsPanel.add(myModuleMessagingKafkaCheckBox);
        myModuleMessagingOptionsPanel.add(myModuleMessagingAMQPCheckBox);
        myModuleMessagingOptionsPanel.add(myModuleMessagingMQTTCheckBox);
        myModuleMessagingOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        myModuleMessagingOptionsPanel.setVisible(false);

        // Initialize Caching sub-options
        myModuleCachingOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        myModuleCachingHazelcastCheckBox = new JCheckBox("Hazelcast");
        myModuleCachingVertxHazelcastCheckBox = new JCheckBox("VertxHazelcast");
        myModuleCachingOptionsPanel.add(myModuleCachingHazelcastCheckBox);
        myModuleCachingOptionsPanel.add(myModuleCachingVertxHazelcastCheckBox);
        myModuleCachingOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        myModuleCachingOptionsPanel.setVisible(false);

        // Initialize MicroProfile sub-options
        myModuleMicroProfileOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        myModuleMicroProfileHealthCheckBox = new JCheckBox("Health");
        myModuleMicroProfileTelemetryCheckBox = new JCheckBox("Telemetry");
        myModuleMicroProfileOpenAPICheckBox = new JCheckBox("OpenAPI");
        myModuleMicroProfileLoggingCheckBox = new JCheckBox("Logging");
        myModuleMicroProfileZipkinCheckBox = new JCheckBox("Zipkin");
        myModuleMicroProfileOptionsPanel.add(myModuleMicroProfileHealthCheckBox);
        myModuleMicroProfileOptionsPanel.add(myModuleMicroProfileTelemetryCheckBox);
        myModuleMicroProfileOptionsPanel.add(myModuleMicroProfileOpenAPICheckBox);
        myModuleMicroProfileOptionsPanel.add(myModuleMicroProfileLoggingCheckBox);
        myModuleMicroProfileOptionsPanel.add(myModuleMicroProfileZipkinCheckBox);
        myModuleMicroProfileOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        myModuleMicroProfileOptionsPanel.setVisible(false);

        // Add action listeners to update the module data when fields change
        myModuleNameField.addActionListener(e -> updateSelectedModule());
        myModuleArtifactIdField.addActionListener(e -> updateSelectedModule());

        // Add focus listener to module name field to automatically set it when it loses focus
        myModuleNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String moduleName = myModuleNameField.getText().trim();
                if (moduleName.isEmpty() || !moduleName.matches("[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*")) {
                    // If module name is empty or invalid, default it to package name + artifact name
                    String artifactId = myModuleArtifactIdField.getText().trim();
                    if (!artifactId.isEmpty()) {
                        String newModuleName = generateModuleNameFromGroupIdAndArtifactId(myGroupIdField.getText(), artifactId);
                        myModuleNameField.setText(newModuleName);
                        updateSelectedModule();
                    }
                }
            }
        });

        // Add document listener to artifact ID field to update module name when artifact ID changes
        myModuleArtifactIdField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateModuleName();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateModuleName();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateModuleName();
            }

            private void updateModuleName() {
                String moduleName = myModuleNameField.getText().trim();
                if (moduleName.isEmpty() || !moduleName.matches("[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*")) {
                    // If module name is empty or invalid, default it to package name + artifact name
                    String artifactId = myModuleArtifactIdField.getText().trim();
                    if (!artifactId.isEmpty()) {
                        String newModuleName = generateModuleNameFromGroupIdAndArtifactId(myGroupIdField.getText(), artifactId);
                        myModuleNameField.setText(newModuleName);
                        updateSelectedModule();
                    }
                }
            }
        });

        // Add action listeners for main feature groups
        myModuleWebReactiveCheckBox.addActionListener(e -> {
            myModuleWebReactiveOptionsPanel.setVisible(myModuleWebReactiveCheckBox.isSelected());
            updateSelectedModule();
        });

        myModuleDatabaseCheckBox.addActionListener(e -> {
            boolean isSelected = myModuleDatabaseCheckBox.isSelected();
            myModuleDatabaseOptionsPanel.setVisible(isSelected);

            // If database is selected, ensure persistence is also selected and disabled
            if (isSelected) {
                myModuleDatabasePersistenceCheckBox.setSelected(true);
                myModuleDatabasePersistenceCheckBox.setEnabled(false);
            } else {
                myModuleDatabasePersistenceCheckBox.setEnabled(true);
            }

            updateSelectedModule();
        });

        myModuleMessagingCheckBox.addActionListener(e -> {
            myModuleMessagingOptionsPanel.setVisible(myModuleMessagingCheckBox.isSelected());
            updateSelectedModule();
        });

        myModuleCachingCheckBox.addActionListener(e -> {
            myModuleCachingOptionsPanel.setVisible(myModuleCachingCheckBox.isSelected());
            updateSelectedModule();
        });

        myModuleMicroProfileCheckBox.addActionListener(e -> {
            myModuleMicroProfileOptionsPanel.setVisible(myModuleMicroProfileCheckBox.isSelected());
            updateSelectedModule();
        });

        // Add action listeners for sub-options
        myModuleWebReactiveRestCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleWebReactiveWebSocketsCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleWebReactiveSwaggerCheckBox.addActionListener(e -> updateSelectedModule());

        myModuleDatabasePersistenceCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabasePostgreSQLCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseMySQLCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseOracleCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseDB2CheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseSqlServerCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseCassandraCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseMongoDBCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleDatabaseJDBCCheckBox.addActionListener(e -> {
            if (myModuleDatabaseJDBCCheckBox.isSelected()) {
                // If JDBC is selected, deselect all other database options
                myModuleDatabasePostgreSQLCheckBox.setSelected(false);
                myModuleDatabaseMySQLCheckBox.setSelected(false);
                myModuleDatabaseOracleCheckBox.setSelected(false);
                myModuleDatabaseDB2CheckBox.setSelected(false);
                myModuleDatabaseSqlServerCheckBox.setSelected(false);
                myModuleDatabaseCassandraCheckBox.setSelected(false);
                myModuleDatabaseMongoDBCheckBox.setSelected(false);
            }
            updateSelectedModule();
        });

        myModuleMessagingRabbitMQCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMessagingKafkaCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMessagingAMQPCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMessagingMQTTCheckBox.addActionListener(e -> updateSelectedModule());

        myModuleCachingHazelcastCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleCachingVertxHazelcastCheckBox.addActionListener(e -> updateSelectedModule());

        myModuleMicroProfileHealthCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMicroProfileTelemetryCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMicroProfileOpenAPICheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMicroProfileLoggingCheckBox.addActionListener(e -> updateSelectedModule());
        myModuleMicroProfileZipkinCheckBox.addActionListener(e -> updateSelectedModule());

        // Create the panel
        JBLabel featuresLabel = new JBLabel("Module Features:");
        featuresLabel.setFont(featuresLabel.getFont().deriveFont(Font.BOLD));

        FormBuilder builder = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Artifact ID:"), myModuleArtifactIdField)
                .addLabeledComponent(new JBLabel("Module Name:"), myModuleNameField)
                .addVerticalGap(10)
                .addComponent(featuresLabel)
                .addComponent(myModuleWebReactiveCheckBox)
                .addComponent(myModuleWebReactiveOptionsPanel)
                .addComponent(myModuleDatabaseCheckBox)
                .addComponent(myModuleDatabaseOptionsPanel)
                .addComponent(myModuleMessagingCheckBox)
                .addComponent(myModuleMessagingOptionsPanel)
                .addComponent(myModuleCachingCheckBox)
                .addComponent(myModuleCachingOptionsPanel)
                .addComponent(myModuleMicroProfileCheckBox)
                .addComponent(myModuleMicroProfileOptionsPanel);

        return builder.getPanel();
    }

    /**
     * Updates the modules list from the wizard data.
     */
    private void updateModulesList() {
        myModulesListModel.removeAll();
        for (GuicedEEProjectWizardData.ModuleData module : myWizardData.getModules()) {
            myModulesListModel.add(module);
        }

        if (!myModulesListModel.isEmpty()) {
            myModulesList.setSelectedIndex(0);
        }
    }

    /**
     * Updates the module features panel with the selected module's data.
     */
    private void updateModuleFeaturesPanel() {
        // Make sure we have a default module if the list is empty
        if (myWizardData.getModules().isEmpty()) {
            String artifactId = "guicedee-app";
            String moduleName = generateModuleNameFromGroupIdAndArtifactId(myGroupIdField.getText(), artifactId);
            myWizardData.addModule(moduleName, artifactId);
            updateModulesList();
        }

        GuicedEEProjectWizardData.ModuleData selectedModule = myModulesList.getSelectedValue();
        if (selectedModule != null) {
            myModuleNameField.setText(selectedModule.getName());
            myModuleArtifactIdField.setText(selectedModule.getArtifactId());

            // Set main feature groups
            myModuleWebReactiveCheckBox.setSelected(selectedModule.isWebReactive());
            myModuleDatabaseCheckBox.setSelected(selectedModule.isDatabase());

            // If database is selected, ensure persistence is also selected
            if (selectedModule.isDatabase()) {
                selectedModule.setDatabasePersistence(true);
            }

            myModuleMessagingCheckBox.setSelected(selectedModule.isMessaging());
            myModuleCachingCheckBox.setSelected(selectedModule.isCaching());
            myModuleMicroProfileCheckBox.setSelected(selectedModule.isMicroProfile());

            // Set Web Reactive sub-options
            myModuleWebReactiveRestCheckBox.setSelected(selectedModule.isWebReactiveRest());
            myModuleWebReactiveWebSocketsCheckBox.setSelected(selectedModule.isWebReactiveWebSockets());
            myModuleWebReactiveSwaggerCheckBox.setSelected(selectedModule.isWebReactiveSwagger());
            myModuleWebReactiveOptionsPanel.setVisible(selectedModule.isWebReactive());

            // Set Database sub-options
            myModuleDatabasePersistenceCheckBox.setSelected(selectedModule.isDatabasePersistence());
            myModuleDatabasePostgreSQLCheckBox.setSelected(selectedModule.isDatabasePostgreSQL());
            myModuleDatabaseMySQLCheckBox.setSelected(selectedModule.isDatabaseMySQL());
            myModuleDatabaseOracleCheckBox.setSelected(selectedModule.isDatabaseOracle());
            myModuleDatabaseDB2CheckBox.setSelected(selectedModule.isDatabaseDB2());
            myModuleDatabaseSqlServerCheckBox.setSelected(selectedModule.isDatabaseSqlServer());
            myModuleDatabaseCassandraCheckBox.setSelected(selectedModule.isDatabaseCassandra());
            myModuleDatabaseMongoDBCheckBox.setSelected(selectedModule.isDatabaseMongoDB());
            myModuleDatabaseJDBCCheckBox.setSelected(selectedModule.isDatabaseJDBC());
            myModuleDatabaseOptionsPanel.setVisible(selectedModule.isDatabase());

            // If database is selected, ensure persistence checkbox is disabled to prevent deselection
            myModuleDatabasePersistenceCheckBox.setEnabled(!selectedModule.isDatabase());

            // Set Messaging sub-options
            myModuleMessagingRabbitMQCheckBox.setSelected(selectedModule.isMessagingRabbitMQ());
            myModuleMessagingKafkaCheckBox.setSelected(selectedModule.isMessagingKafka());
            myModuleMessagingAMQPCheckBox.setSelected(selectedModule.isMessagingAMQP());
            myModuleMessagingMQTTCheckBox.setSelected(selectedModule.isMessagingMQTT());
            myModuleMessagingOptionsPanel.setVisible(selectedModule.isMessaging());

            // Set Caching sub-options
            myModuleCachingHazelcastCheckBox.setSelected(selectedModule.isCachingHazelcast());
            myModuleCachingVertxHazelcastCheckBox.setSelected(selectedModule.isCachingVertxHazelcast());
            myModuleCachingOptionsPanel.setVisible(selectedModule.isCaching());

            // Set MicroProfile sub-options
            myModuleMicroProfileHealthCheckBox.setSelected(selectedModule.isMicroProfileHealth());
            myModuleMicroProfileTelemetryCheckBox.setSelected(selectedModule.isMicroProfileTelemetry());
            myModuleMicroProfileOpenAPICheckBox.setSelected(selectedModule.isMicroProfileOpenAPI());
            myModuleMicroProfileLoggingCheckBox.setSelected(selectedModule.isMicroProfileLogging());
            myModuleMicroProfileZipkinCheckBox.setSelected(selectedModule.isMicroProfileZipkin());
            myModuleMicroProfileOptionsPanel.setVisible(selectedModule.isMicroProfile());

            myModuleFeaturesPanel.setEnabled(true);

            // Module name is always available and refers to the Java module name syntax
            myModuleNameField.setEnabled(true);
            myModuleNameField.setToolTipText("Module name refers to the Java module name syntax");
        } else {
            myModuleNameField.setText("");
            myModuleArtifactIdField.setText("");

            // Clear main feature groups
            myModuleWebReactiveCheckBox.setSelected(false);
            myModuleDatabaseCheckBox.setSelected(false);
            myModuleMessagingCheckBox.setSelected(false);
            myModuleCachingCheckBox.setSelected(false);
            myModuleMicroProfileCheckBox.setSelected(false);

            // Clear Web Reactive sub-options
            myModuleWebReactiveRestCheckBox.setSelected(false);
            myModuleWebReactiveWebSocketsCheckBox.setSelected(false);
            myModuleWebReactiveSwaggerCheckBox.setSelected(false);
            myModuleWebReactiveOptionsPanel.setVisible(false);

            // Clear Database sub-options
            myModuleDatabasePersistenceCheckBox.setSelected(false);
            myModuleDatabasePostgreSQLCheckBox.setSelected(false);
            myModuleDatabaseMySQLCheckBox.setSelected(false);
            myModuleDatabaseOracleCheckBox.setSelected(false);
            myModuleDatabaseDB2CheckBox.setSelected(false);
            myModuleDatabaseSqlServerCheckBox.setSelected(false);
            myModuleDatabaseCassandraCheckBox.setSelected(false);
            myModuleDatabaseMongoDBCheckBox.setSelected(false);
            myModuleDatabaseJDBCCheckBox.setSelected(false);
            myModuleDatabaseOptionsPanel.setVisible(false);

            // Enable persistence checkbox when database is not selected
            myModuleDatabasePersistenceCheckBox.setEnabled(true);

            // Clear Messaging sub-options
            myModuleMessagingRabbitMQCheckBox.setSelected(false);
            myModuleMessagingKafkaCheckBox.setSelected(false);
            myModuleMessagingAMQPCheckBox.setSelected(false);
            myModuleMessagingMQTTCheckBox.setSelected(false);
            myModuleMessagingOptionsPanel.setVisible(false);

            // Clear Caching sub-options
            myModuleCachingHazelcastCheckBox.setSelected(false);
            myModuleCachingVertxHazelcastCheckBox.setSelected(false);
            myModuleCachingOptionsPanel.setVisible(false);

            // Clear MicroProfile sub-options
            myModuleMicroProfileHealthCheckBox.setSelected(false);
            myModuleMicroProfileTelemetryCheckBox.setSelected(false);
            myModuleMicroProfileOpenAPICheckBox.setSelected(false);
            myModuleMicroProfileLoggingCheckBox.setSelected(false);
            myModuleMicroProfileZipkinCheckBox.setSelected(false);
            myModuleMicroProfileOptionsPanel.setVisible(false);

            myModuleFeaturesPanel.setEnabled(false);
        }
    }

    /**
     * Updates the selected module with the values from the module features panel.
     */
    private void updateSelectedModule() {
        GuicedEEProjectWizardData.ModuleData selectedModule = myModulesList.getSelectedValue();
        if (selectedModule != null) {
            selectedModule.setName(myModuleNameField.getText());
            selectedModule.setArtifactId(myModuleArtifactIdField.getText());

            // Update main feature groups
            selectedModule.setWebReactive(myModuleWebReactiveCheckBox.isSelected());
            selectedModule.setDatabase(myModuleDatabaseCheckBox.isSelected());
            selectedModule.setMessaging(myModuleMessagingCheckBox.isSelected());
            selectedModule.setCaching(myModuleCachingCheckBox.isSelected());
            selectedModule.setMicroProfile(myModuleMicroProfileCheckBox.isSelected());

            // Update Web Reactive sub-options
            selectedModule.setWebReactiveRest(myModuleWebReactiveRestCheckBox.isSelected());
            selectedModule.setWebReactiveWebSockets(myModuleWebReactiveWebSocketsCheckBox.isSelected());
            selectedModule.setWebReactiveSwagger(myModuleWebReactiveSwaggerCheckBox.isSelected());

            // Update Database sub-options
            selectedModule.setDatabasePersistence(myModuleDatabasePersistenceCheckBox.isSelected());
            selectedModule.setDatabasePostgreSQL(myModuleDatabasePostgreSQLCheckBox.isSelected());
            selectedModule.setDatabaseMySQL(myModuleDatabaseMySQLCheckBox.isSelected());
            selectedModule.setDatabaseOracle(myModuleDatabaseOracleCheckBox.isSelected());
            selectedModule.setDatabaseDB2(myModuleDatabaseDB2CheckBox.isSelected());
            selectedModule.setDatabaseSqlServer(myModuleDatabaseSqlServerCheckBox.isSelected());
            selectedModule.setDatabaseCassandra(myModuleDatabaseCassandraCheckBox.isSelected());
            selectedModule.setDatabaseMongoDB(myModuleDatabaseMongoDBCheckBox.isSelected());
            selectedModule.setDatabaseJDBC(myModuleDatabaseJDBCCheckBox.isSelected());

            // Update Messaging sub-options
            selectedModule.setMessagingRabbitMQ(myModuleMessagingRabbitMQCheckBox.isSelected());
            selectedModule.setMessagingKafka(myModuleMessagingKafkaCheckBox.isSelected());
            selectedModule.setMessagingAMQP(myModuleMessagingAMQPCheckBox.isSelected());
            selectedModule.setMessagingMQTT(myModuleMessagingMQTTCheckBox.isSelected());

            // Update Caching sub-options
            selectedModule.setCachingHazelcast(myModuleCachingHazelcastCheckBox.isSelected());
            selectedModule.setCachingVertxHazelcast(myModuleCachingVertxHazelcastCheckBox.isSelected());

            // Update MicroProfile sub-options
            selectedModule.setMicroProfileHealth(myModuleMicroProfileHealthCheckBox.isSelected());
            selectedModule.setMicroProfileTelemetry(myModuleMicroProfileTelemetryCheckBox.isSelected());
            selectedModule.setMicroProfileOpenAPI(myModuleMicroProfileOpenAPICheckBox.isSelected());
            selectedModule.setMicroProfileLogging(myModuleMicroProfileLoggingCheckBox.isSelected());
            selectedModule.setMicroProfileZipkin(myModuleMicroProfileZipkinCheckBox.isSelected());

            // Update the list to reflect the name change
            int selectedIndex = myModulesList.getSelectedIndex();
            myModulesList.repaint();
            myModulesList.setSelectedIndex(selectedIndex);

            // Update project-level flags based on the updated module data
            updateProjectLevelFlags();
        }
    }

    /**
     * Adds a new module to the project.
     */
    private void addModule() {
        String moduleArtifactId = myWizardData.getArtifactId() + "-module" + (myWizardData.getModules().size() + 1);
        // Default module name to package name + artifact name (in Java module name format)
        String moduleName = generateModuleNameFromGroupIdAndArtifactId(myWizardData.getGroupId(), moduleArtifactId);

        GuicedEEProjectWizardData.ModuleData newModule = myWizardData.addModule(moduleName, moduleArtifactId);
        updateModulesList();
        myModulesList.setSelectedValue(newModule, true);
    }

    /**
     * Generates a module name from group ID and artifact ID in Java module name format.
     */
    private String generateModuleNameFromGroupIdAndArtifactId(String groupId, String artifactId) {
        // Start with the group ID
        String moduleName = groupId;

        // Add the artifact ID, replacing hyphens with dots
        if (artifactId != null && !artifactId.isEmpty()) {
            moduleName += "." + artifactId.replace('-', '.');
        }

        return moduleName;
    }

    /**
     * Removes the selected module from the project.
     */
    private void removeModule() {
        GuicedEEProjectWizardData.ModuleData selectedModule = myModulesList.getSelectedValue();
        if (selectedModule != null) {
            myWizardData.removeModule(selectedModule);
            updateModulesList();
        }
    }

    /**
     * Updates project-level flags based on module data.
     * This ensures that the old-style flags used in createSingleModuleProject
     * are set based on the checkbox selections in the UI.
     */
    private void updateProjectLevelFlags() {
        boolean webApplication = false;
        boolean databaseApplication = false;
        boolean rabbitMQSupport = false;

        // Check all modules for feature flags
        for (GuicedEEProjectWizardData.ModuleData module : myWizardData.getModules()) {
            // Web application if any module has web reactive features
            if (module.isWebReactive()) {
                webApplication = true;
            }

            // Database application if any module has database features
            if (module.isDatabase()) {
                databaseApplication = true;
            }

            // RabbitMQ support if any module has RabbitMQ messaging
            if (module.isMessagingRabbitMQ()) {
                rabbitMQSupport = true;
            }
        }

        // Set project-level flags
        myWizardData.setWebApplication(webApplication);
        myWizardData.setDatabaseApplication(databaseApplication);
        myWizardData.setRabbitMQSupport(rabbitMQSupport);

        System.out.println("[DEBUG_LOG] Updated project-level flags: webApplication=" + webApplication + 
                           ", databaseApplication=" + databaseApplication + 
                           ", rabbitMQSupport=" + rabbitMQSupport);
    }
}
