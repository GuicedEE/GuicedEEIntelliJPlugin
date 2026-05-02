package com.guicedee.intellij.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.impl.file.JavaDirectoryServiceImpl;
import com.guicedee.intellij.GuicedIcons;
import com.guicedee.intellij.template.GuicedEEFileTemplateProvider;
import com.guicedee.intellij.wizard.GuicedEEProjectWizardData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * Action group for creating new GuicedEE files from the right-click menu.
 * This action group only appears for projects that meet the criteria for a GuicedEE Application.
 */
public class GuicedEENewFileActionGroup extends DefaultActionGroup implements DumbAware {
    private static final Icon GUICEDEE_ICON = GuicedIcons.Logo;

    public GuicedEENewFileActionGroup() {
        super("GuicedEE", true);
        getTemplatePresentation().setIcon(GUICEDEE_ICON);

        // Add action for creating module-info.java and project structure
        add(new CreateProjectStructureAction());

        // Add core items
        add(new CreateGuicedEEFileAction("Module", "Create a new Module", 
                GuicedEEFileTemplateProvider.GUICE_MODULE_TEMPLATE));

        // Add Messaging subgroup
        DefaultActionGroup messagingGroup = new DefaultActionGroup("Messaging", true);
        messagingGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(messagingGroup);

        messagingGroup.add(new CreateGuicedEEFileAction("RabbitMQ Consumer", "Create a new RabbitMQ Consumer",
                GuicedEEFileTemplateProvider.RABBITMQ_CONSUMER_TEMPLATE));
        messagingGroup.add(new CreateGuicedEEFileAction("Kafka Consumer", "Create a new Kafka Consumer",
                GuicedEEFileTemplateProvider.KAFKA_CONSUMER_TEMPLATE));
        messagingGroup.add(new CreateGuicedEEFileAction("IBM MQ Consumer", "Create a new IBM MQ Consumer",
                GuicedEEFileTemplateProvider.IBMMQ_CONSUMER_TEMPLATE));

        // Add Web subgroup
        DefaultActionGroup webGroup = new DefaultActionGroup("Web", true);
        webGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(webGroup);

        webGroup.add(new CreateGuicedEEFileAction("Router Configuration", "Create a new Router Configuration", 
                GuicedEEFileTemplateProvider.ROUTER_CONFIGURATION_TEMPLATE));
        webGroup.add(new CreateGuicedEEFileAction("WebSocket Channel", "Create a new WebSocket Channel", 
                GuicedEEFileTemplateProvider.WEBSOCKET_CHANNEL_TEMPLATE));
        webGroup.add(new CreateGuicedEEFileAction("HTTP Proxy Module", "Create a new HTTP Proxy Module",
                GuicedEEFileTemplateProvider.HTTP_PROXY_MODULE_TEMPLATE));
        webGroup.add(new CreateGuicedEEFileAction("gRPC Module", "Create a new gRPC Server/Client Module",
                GuicedEEFileTemplateProvider.GRPC_MODULE_TEMPLATE));

        // Add Auth subgroup
        DefaultActionGroup authGroup = new DefaultActionGroup("Auth", true);
        authGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(authGroup);

        authGroup.add(new CreateGuicedEEFileAction("Authentication Provider", "Create a new Authentication Provider",
                GuicedEEFileTemplateProvider.AUTHENTICATION_PROVIDER_TEMPLATE));
        authGroup.add(new CreateGuicedEEFileAction("Authorization Provider", "Create a new Authorization Provider",
                GuicedEEFileTemplateProvider.AUTHORIZATION_PROVIDER_TEMPLATE));

        // Add WebSockets group
        DefaultActionGroup webSocketsGroup = new DefaultActionGroup("WebSockets", true);
        webSocketsGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(webSocketsGroup);

        webSocketsGroup.add(new CreateGuicedEEFileAction("Message Receiver", "Create a new WebSocket Message Receiver", 
                GuicedEEFileTemplateProvider.WEBSOCKET_MESSAGE_RECEIVER_TEMPLATE));
        webSocketsGroup.add(new CreateGuicedEEFileAction("Pre-Configuration", "Create a new WebSocket Pre-Configuration", 
                GuicedEEFileTemplateProvider.WEBSOCKET_PRE_CONFIGURATION_TEMPLATE));
        webSocketsGroup.add(new CreateGuicedEEFileAction("On Publish", "Create a new WebSocket On Publish", 
                GuicedEEFileTemplateProvider.WEBSOCKET_ON_PUBLISH_TEMPLATE));

        // Add REST subgroup
        DefaultActionGroup restGroup = new DefaultActionGroup("REST", true);
        restGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(restGroup);

        restGroup.add(new CreateGuicedEEFileAction("REST Service", "Create a new REST Service", 
                GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE));
        restGroup.add(new CreateGuicedEEFileAction("REST Client", "Create a new REST Client with @Endpoint",
                GuicedEEFileTemplateProvider.REST_CLIENT_TEMPLATE));

        // Add SOAP subgroup
        DefaultActionGroup soapGroup = new DefaultActionGroup("SOAP", true);
        soapGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(soapGroup);

        soapGroup.add(new CreateGuicedEEFileAction("SOAP Service Interface", "Create a new SOAP web service interface (contract)",
                GuicedEEFileTemplateProvider.SOAP_SERVICE_INTERFACE_TEMPLATE));
        soapGroup.add(new CreateGuicedEEFileAction("SOAP Service Implementation", "Create a new SOAP web service implementation",
                GuicedEEFileTemplateProvider.SOAP_SERVICE_IMPL_TEMPLATE));

        // Add Database subgroup
        DefaultActionGroup databaseGroup = new DefaultActionGroup("Database", true);
        databaseGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(databaseGroup);

        databaseGroup.add(new CreateGuicedEEFileAction("Persistence Module", "Create a new Persistence Module", 
                GuicedEEFileTemplateProvider.PERSISTENCE_MODULE_TEMPLATE));

        // Add Mail subgroup
        DefaultActionGroup mailGroup = new DefaultActionGroup("Mail", true);
        mailGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(mailGroup);

        mailGroup.add(new CreateGuicedEEFileAction("Mail Client", "Create a new Mail Client",
                GuicedEEFileTemplateProvider.MAIL_CLIENT_TEMPLATE));

        // Add MicroProfile subgroup
        DefaultActionGroup microProfileGroup = new DefaultActionGroup("MicroProfile", true);
        microProfileGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(microProfileGroup);

        microProfileGroup.add(new CreateGuicedEEFileAction("Health Check", "Create a new Health Check",
                GuicedEEFileTemplateProvider.HEALTH_CHECK_TEMPLATE));

        // Add Hazelcast subgroup
        DefaultActionGroup hazelcastGroup = new DefaultActionGroup("Hazelcast", true);
        hazelcastGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(hazelcastGroup);

        hazelcastGroup.add(new CreateGuicedEEFileAction("Server Config", "Create a new Hazelcast Server Configuration",
                GuicedEEFileTemplateProvider.HAZELCAST_SERVER_CONFIG_TEMPLATE));
        hazelcastGroup.add(new CreateGuicedEEFileAction("Client Config", "Create a new Hazelcast Client Configuration",
                GuicedEEFileTemplateProvider.HAZELCAST_CLIENT_CONFIG_TEMPLATE));

        // Add global Hooks group
        DefaultActionGroup globalHooksGroup = new DefaultActionGroup("Hooks", true);
        globalHooksGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(globalHooksGroup);

        globalHooksGroup.add(new CreateGuicedEEFileAction("Pre-Startup", "Create a new Pre-Startup Hook",
                GuicedEEFileTemplateProvider.PRE_STARTUP_TEMPLATE));
        globalHooksGroup.add(new CreateGuicedEEFileAction("Post-Startup", "Create a new Post-Startup Hook",
                GuicedEEFileTemplateProvider.POST_STARTUP_TEMPLATE));
        globalHooksGroup.add(new CreateGuicedEEFileAction("Pre-Destroy", "Create a new Pre-Destroy Hook",
                GuicedEEFileTemplateProvider.PRE_DESTROY_TEMPLATE));
        globalHooksGroup.add(new CreateGuicedEEFileAction("Log4J Configurator", "Create a new Log4J Configurator",
                GuicedEEFileTemplateProvider.LOG4J_CONFIGURATOR_TEMPLATE));

        // Add global Lifecycles group
        DefaultActionGroup globalLifecyclesGroup = new DefaultActionGroup("Lifecycles", true);
        globalLifecyclesGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(globalLifecyclesGroup);

        globalLifecyclesGroup.add(new CreateGuicedEEFileAction("Scan Module Inclusions", "Create a new Scan Module Inclusions",
                GuicedEEFileTemplateProvider.SCAN_MODULE_INCLUSIONS_TEMPLATE));

        // Add Scanner subgroup under Lifecycles
        DefaultActionGroup scannerGroup = new DefaultActionGroup("Scanner", true);
        scannerGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        globalLifecyclesGroup.add(scannerGroup);

        scannerGroup.add(new CreateGuicedEEFileAction("Configurator", "Create a new Configurator",
                GuicedEEFileTemplateProvider.CONFIGURATOR_TEMPLATE));
        scannerGroup.add(new CreateGuicedEEFileAction("File Contents Scanner", "Create a new File Contents Scanner",
                GuicedEEFileTemplateProvider.FILE_CONTENTS_SCANNER_TEMPLATE));
        scannerGroup.add(new CreateGuicedEEFileAction("File Contents Pattern Scanner", "Create a new File Contents Pattern Scanner",
                GuicedEEFileTemplateProvider.FILE_CONTENTS_PATTERN_SCANNER_TEMPLATE));
        scannerGroup.add(new CreateGuicedEEFileAction("Package Contents Scanner", "Create a new Package Contents Scanner",
                GuicedEEFileTemplateProvider.PACKAGE_CONTENTS_SCANNER_TEMPLATE));
        scannerGroup.add(new CreateGuicedEEFileAction("Path Contents Scanner", "Create a new Path Contents Scanner",
                GuicedEEFileTemplateProvider.PATH_CONTENTS_SCANNER_TEMPLATE));

        // Add Scopes subgroup under Lifecycles
        DefaultActionGroup scopesGroup = new DefaultActionGroup("Scopes", true);
        scopesGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        globalLifecyclesGroup.add(scopesGroup);

        scopesGroup.add(new CreateGuicedEEFileAction("On Call Scope Enter", "Create a new On Call Scope Enter",
                GuicedEEFileTemplateProvider.ON_CALL_SCOPE_ENTER_TEMPLATE));
        scopesGroup.add(new CreateGuicedEEFileAction("On Call Scope Exit", "Create a new On Call Scope Exit",
                GuicedEEFileTemplateProvider.ON_CALL_SCOPE_EXIT_TEMPLATE));

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // Always show the menu if a directory is selected, regardless of whether it's a GuicedEE Application or not
        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        if (element instanceof PsiDirectory) {
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }

        // For non-directory elements, only show if it's a GuicedEE Application
        boolean isGuicedEEApplication = isGuicedEEApplication(project);
        e.getPresentation().setEnabledAndVisible(isGuicedEEApplication);
    }

    /**
     * Checks if the project is a GuicedEE Application.
     * A GuicedEE Application is identified by having:
     * 1. A reference to IGuiceContext in the project
     * 2. A reference to Inject in the project
     */
    private static boolean isGuicedEEApplication(Project project) {
        // Look for module-info.java files
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<VirtualFile> moduleInfoVFiles = FilenameIndex.getVirtualFilesByName(
                "module-info.java", GlobalSearchScope.projectScope(project));

        for (VirtualFile vf : moduleInfoVFiles) {
            PsiFile file = psiManager.findFile(vf);
            if (file == null) continue;
            if (file instanceof PsiJavaFile) {
                String text = file.getText();
                // Check if the module-info.java file contains references to GuicedEE dependencies
                if (text.contains("requires") && 
                    (text.contains("com.guicedee.client") ||
                     text.contains("com.guicedee.vertx") ||
                     text.contains("com.guicedee.inject"))) {
                    return true;
                }
            }
        }

        // If no module-info.java files with GuicedEE dependencies, check for class existence
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        // Check for GuiceContext or IGuiceContext
        boolean hasGuiceContext = 
            javaPsiFacade.findClass("com.guicedee.guicedinjection.GuiceContext", scope) != null ||
            javaPsiFacade.findClass("com.guicedee.client.IGuiceContext", scope) != null;

        // Check for Inject
        boolean hasInject = 
            javaPsiFacade.findClass("jakarta.inject.Inject", scope) != null ||
            javaPsiFacade.findClass("javax.inject.Inject", scope) != null;

        return hasGuiceContext && hasInject;
    }

    /**
     * Action for creating or updating module-info.java and project structure.
     */
    private static class CreateProjectStructureAction extends AnAction {
        public CreateProjectStructureAction() {
            super("Create Project Structure", "Create or update module-info.java and project structure", GUICEDEE_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project == null) {
                return;
            }

            // Get the selected directory
            PsiDirectory directory = null;
            PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
            if (element instanceof PsiDirectory) {
                directory = (PsiDirectory) element;
            }

            if (directory == null) {
                Messages.showErrorDialog(project, "Please select a directory", "Error");
                return;
            }

            // Show information dialog with instructions
            Messages.showInfoMessage(project, 
                "To create or update module-info.java and project structure:\n\n" +
                "1. Use the GuicedEE Project Wizard to create a new project with the desired features\n" +
                "2. Copy the generated module-info.java and other files to your project\n" +
                "3. Modify the files as needed for your project\n\n" +
                "This approach ensures that all dependencies and configurations are properly set up.",
                "Create Project Structure");
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project == null) {
                e.getPresentation().setEnabledAndVisible(false);
                return;
            }

            // Only enable for directories and when the project is NOT a GuicedEE Application
            PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
            boolean isDirectory = element instanceof PsiDirectory;
            boolean isGuicedEEApplication = isGuicedEEApplication(project);

            e.getPresentation().setEnabledAndVisible(isDirectory && !isGuicedEEApplication);
        }
    }

    /**
     * Dialog for selecting GuicedEE features.
     */
    private static class GuicedEEFeatureDialog extends DialogWrapper {
        private JCheckBox webReactiveCheckBox;
        private JCheckBox databaseCheckBox;
        private JCheckBox messagingCheckBox;
        private JCheckBox cachingCheckBox;
        private JCheckBox microProfileCheckBox;

        private JCheckBox webReactiveRestCheckBox;
        private JCheckBox webReactiveWebSocketsCheckBox;
        private JCheckBox webReactiveSwaggerCheckBox;

        private JCheckBox databaseJDBCCheckBox;

        private JCheckBox messagingRabbitMQCheckBox;

        private GuicedEEProjectWizardData.ModuleData moduleData;

        public GuicedEEFeatureDialog(Project project) {
            super(project);
            setTitle("Select GuicedEE Features");
            moduleData = new GuicedEEProjectWizardData.ModuleData("Default Module", "app");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new BorderLayout());

            JPanel featuresPanel = new JPanel(new GridLayout(0, 1));

            // Main feature groups
            webReactiveCheckBox = new JCheckBox("Web Reactive");
            databaseCheckBox = new JCheckBox("Database");
            messagingCheckBox = new JCheckBox("Messaging");
            cachingCheckBox = new JCheckBox("Caching");
            microProfileCheckBox = new JCheckBox("MicroProfile");

            featuresPanel.add(webReactiveCheckBox);
            featuresPanel.add(databaseCheckBox);
            featuresPanel.add(messagingCheckBox);
            featuresPanel.add(cachingCheckBox);
            featuresPanel.add(microProfileCheckBox);

            // Web Reactive sub-options
            JPanel webReactivePanel = new JPanel(new GridLayout(0, 1));
            webReactivePanel.setBorder(BorderFactory.createTitledBorder("Web Reactive Options"));

            webReactiveRestCheckBox = new JCheckBox("REST");
            webReactiveWebSocketsCheckBox = new JCheckBox("WebSockets");
            webReactiveSwaggerCheckBox = new JCheckBox("Swagger");

            webReactivePanel.add(webReactiveRestCheckBox);
            webReactivePanel.add(webReactiveWebSocketsCheckBox);
            webReactivePanel.add(webReactiveSwaggerCheckBox);

            // Database sub-options
            JPanel databasePanel = new JPanel(new GridLayout(0, 1));
            databasePanel.setBorder(BorderFactory.createTitledBorder("Database Options"));

            databaseJDBCCheckBox = new JCheckBox("JDBC");

            databasePanel.add(databaseJDBCCheckBox);

            // Messaging sub-options
            JPanel messagingPanel = new JPanel(new GridLayout(0, 1));
            messagingPanel.setBorder(BorderFactory.createTitledBorder("Messaging Options"));

            messagingRabbitMQCheckBox = new JCheckBox("RabbitMQ");

            messagingPanel.add(messagingRabbitMQCheckBox);

            // Add all panels to main panel
            JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
            optionsPanel.add(webReactivePanel);
            optionsPanel.add(databasePanel);
            optionsPanel.add(messagingPanel);

            panel.add(featuresPanel, BorderLayout.NORTH);
            panel.add(optionsPanel, BorderLayout.CENTER);

            // Add listeners
            webReactiveCheckBox.addActionListener(e -> {
                boolean selected = webReactiveCheckBox.isSelected();
                webReactiveRestCheckBox.setEnabled(selected);
                webReactiveWebSocketsCheckBox.setEnabled(selected);
                webReactiveSwaggerCheckBox.setEnabled(selected);
            });

            databaseCheckBox.addActionListener(e -> {
                boolean selected = databaseCheckBox.isSelected();
                databaseJDBCCheckBox.setEnabled(selected);
            });

            messagingCheckBox.addActionListener(e -> {
                boolean selected = messagingCheckBox.isSelected();
                messagingRabbitMQCheckBox.setEnabled(selected);
            });

            // Initialize enabled state
            webReactiveRestCheckBox.setEnabled(false);
            webReactiveWebSocketsCheckBox.setEnabled(false);
            webReactiveSwaggerCheckBox.setEnabled(false);
            databaseJDBCCheckBox.setEnabled(false);
            messagingRabbitMQCheckBox.setEnabled(false);

            return panel;
        }

        @Override
        protected void doOKAction() {
            // Update module data based on selected options
            moduleData.setWebReactive(webReactiveCheckBox.isSelected());
            moduleData.setDatabase(databaseCheckBox.isSelected());
            moduleData.setMessaging(messagingCheckBox.isSelected());
            moduleData.setCaching(cachingCheckBox.isSelected());
            moduleData.setMicroProfile(microProfileCheckBox.isSelected());

            moduleData.setWebReactiveRest(webReactiveRestCheckBox.isSelected());
            moduleData.setWebReactiveWebSockets(webReactiveWebSocketsCheckBox.isSelected());
            moduleData.setWebReactiveSwagger(webReactiveSwaggerCheckBox.isSelected());

            moduleData.setDatabaseJDBC(databaseJDBCCheckBox.isSelected());

            moduleData.setMessagingRabbitMQ(messagingRabbitMQCheckBox.isSelected());

            super.doOKAction();
        }

        public GuicedEEProjectWizardData.ModuleData getModuleData() {
            return moduleData;
        }
    }

    /**
     * Action for creating a new GuicedEE file from a template.
     */
    private static class CreateGuicedEEFileAction extends CreateFileFromTemplateAction {
        private final String templateName;
        private boolean isProjectStructureAction = false;

        // REST service options — set during buildDialog, consumed in createFile
        private volatile boolean restCreateService = true;
        private volatile boolean restIncludeDbSession = false;
        private volatile boolean restSessionPerTransaction = false;
        private volatile boolean restDialogCancelled = false;

        public CreateGuicedEEFileAction(String text, String description, String templateName) {
            super(text, description, GUICEDEE_ICON);
            this.templateName = templateName;
        }

        /**
         * Constructor for creating a project structure action.
         */
        public CreateGuicedEEFileAction(String text, String description) {
            super(text, description, GUICEDEE_ICON);
            this.templateName = "";
            this.isProjectStructureAction = true;
        }

        @Override
        protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
            builder.setTitle("Create New " + getActionName())
                   .addKind(getActionName(), GUICEDEE_ICON, templateName);

            // For REST Service, show options dialog before the name dialog
            if (templateName.equals(GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE)) {
                RestServiceOptionsDialog optionsDialog = new RestServiceOptionsDialog(project);
                if (optionsDialog.showAndGet()) {
                    restCreateService = optionsDialog.isCreateService();
                    restIncludeDbSession = optionsDialog.isIncludeDbSession();
                    restSessionPerTransaction = optionsDialog.isSessionPerTransaction();
                    restDialogCancelled = false;
                } else {
                    restDialogCancelled = true;
                }
            }
        }

        @Override
        protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
            return getActionName();
        }

        private String getActionName() {
            return getTemplatePresentation().getText();
        }

        @Override
        protected PsiFile createFile(String name, String templateName, PsiDirectory dir) {
            try {
                // Get the package name from the directory before creating the file
                String packageName = getPackageNameFromDirectory(dir.getProject(), dir);

                // Set the package name in the default properties
                FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(dir.getProject());
                Properties defaultProperties = fileTemplateManager.getDefaultProperties();
                defaultProperties.setProperty("PACKAGE_NAME", packageName);

                // REST service options — use values captured in buildDialog
                String actualRestTemplateName = templateName;

                if (templateName.equals(GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE)) {
                    if (restDialogCancelled) {
                        return null; // User cancelled the options dialog
                    }

                    if (!defaultProperties.containsKey("PATH")) {
                        defaultProperties.setProperty("PATH", name.toLowerCase());
                    }

                    // Select the correct REST endpoint template based on dialog choices
                    if (!restCreateService) {
                        actualRestTemplateName = GuicedEEFileTemplateProvider.REST_SERVICE_NO_SERVICE_TEMPLATE;
                    } else if (restSessionPerTransaction) {
                        actualRestTemplateName = GuicedEEFileTemplateProvider.REST_SERVICE_WITH_SESSION_TEMPLATE;
                    }
                    // else: use default REST_SERVICE_TEMPLATE (with service, no session on resource)
                }

                // REST Client defaults
                if (templateName.equals(GuicedEEFileTemplateProvider.REST_CLIENT_TEMPLATE)) {
                    if (!defaultProperties.containsKey("ENDPOINT_URL")) {
                        defaultProperties.setProperty("ENDPOINT_URL", "/api/" + name.toLowerCase().replace("client", ""));
                    }
                    if (!defaultProperties.containsKey("ENDPOINT_NAME")) {
                        // Convert class name to kebab-case endpoint name e.g. UserClient -> user
                        String endpointName = name.replaceAll("Client$", "")
                                .replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
                        defaultProperties.setProperty("ENDPOINT_NAME", endpointName);
                    }
                }

                // Create the file with the default properties
                PsiFile file;
                try {
                    file = super.createFile(name, actualRestTemplateName, dir);
                    if (file == null) {
                        Messages.showErrorDialog(dir.getProject(),
                            "Failed to create file from template: " + actualRestTemplateName,
                            "Error Creating File");
                        return null;
                    }
                } catch (Exception e) {
                    Messages.showErrorDialog(dir.getProject(),
                        "Error creating file from template: " + e.getMessage(),
                        "Error Creating File");
                    return null;
                }

                // If creating a REST service with an associated service class
                if (templateName.equals(GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE) && restCreateService) {
                    try {
                        // Create services directory if it doesn't exist
                        PsiDirectory servicesDir;
                        if (dir.findSubdirectory("services") == null) {
                            servicesDir = dir.createSubdirectory("services");
                        } else {
                            servicesDir = dir.findSubdirectory("services");
                        }

                        // Determine which service template to use
                        String serviceTemplateName;
                        if (restSessionPerTransaction) {
                            // Session managed by REST resource, passed to service
                            serviceTemplateName = GuicedEEFileTemplateProvider.REST_SERVICE_IMPL_WITH_SESSION_PARAM_TEMPLATE;
                        } else if (restIncludeDbSession) {
                            // Service manages its own session
                            serviceTemplateName = GuicedEEFileTemplateProvider.REST_SERVICE_IMPL_WITH_SESSION_TEMPLATE;
                        } else {
                            // No DB session
                            serviceTemplateName = GuicedEEFileTemplateProvider.REST_SERVICE_IMPL_TEMPLATE;
                        }

                        // Create the service implementation file
                        FileTemplate template = fileTemplateManager.getJ2eeTemplate(serviceTemplateName);
                        if (template == null) {
                            Messages.showErrorDialog(dir.getProject(),
                                "REST service implementation template not found: " + serviceTemplateName,
                                "Template Not Found");
                            return file;
                        }

                        Properties properties = new Properties(defaultProperties);
                        String serviceImplName = name + "Service";
                        properties.setProperty("SERVICE_NAME", serviceImplName);
                        properties.setProperty("REST_SERVICE_NAME", name);

                        PsiElement createdFile = FileTemplateUtil.createFromTemplate(template, serviceImplName, properties, servicesDir);
                        if (createdFile == null) {
                            Messages.showErrorDialog(dir.getProject(),
                                "Failed to create REST service implementation file",
                                "Error Creating REST Service Implementation");
                        }
                    } catch (Exception e) {
                        Messages.showErrorDialog(dir.getProject(),
                            "Failed to create REST service implementation: " + e.getMessage(), 
                            "Error Creating REST Service Implementation");
                    }

                    // If DB session was selected, ensure persistence dependency is present
                    if (restIncludeDbSession) {
                        ensureRequiredDependency(dir.getProject(), GuicedEEFileTemplateProvider.PERSISTENCE_MODULE_TEMPLATE, dir);
                    }
                }

                // Create META-INF/services file and update module-info.java for service interfaces
                createServiceRegistration(dir.getProject(), templateName, packageName, name, dir);

                // If creating a SOAP service interface, also create the implementation class
                if (templateName.equals(GuicedEEFileTemplateProvider.SOAP_SERVICE_INTERFACE_TEMPLATE)) {
                    try {
                        String implName = name + "Impl";
                        FileTemplate implTemplate = fileTemplateManager.getJ2eeTemplate(GuicedEEFileTemplateProvider.SOAP_SERVICE_IMPL_TEMPLATE);
                        if (implTemplate != null) {
                            Properties implProps = new Properties(defaultProperties);
                            implProps.setProperty("INTERFACE_NAME", name);
                            FileTemplateUtil.createFromTemplate(implTemplate, implName, implProps, dir);
                        }
                    } catch (Exception e) {
                        Messages.showErrorDialog(dir.getProject(),
                            "Failed to create SOAP service implementation: " + e.getMessage(),
                            "Error Creating SOAP Service Implementation");
                    }
                }

                // Ensure required Maven dependency is present for this template
                ensureRequiredDependency(dir.getProject(), templateName, dir);

                return file;
            } catch (Exception e) {
                Messages.showErrorDialog(dir.getProject(),
                    "Unexpected error: " + e.getMessage(),
                    "Error Creating File");
                return null;
            }
        }

        /**
         * Creates META-INF/services file and updates module-info.java for service interfaces
         */
        private void createServiceRegistration(Project project, String templateName, String packageName, String className, PsiDirectory dir) {
            // Map of template names to their corresponding interface names
            java.util.Map<String, String> templateToInterface = new java.util.HashMap<>();
            templateToInterface.put(GuicedEEFileTemplateProvider.PRE_STARTUP_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuicePreStartup");
            templateToInterface.put(GuicedEEFileTemplateProvider.POST_STARTUP_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuicePostStartup");
            templateToInterface.put(GuicedEEFileTemplateProvider.PRE_DESTROY_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuicePreDestroy");
            templateToInterface.put(GuicedEEFileTemplateProvider.SCAN_MODULE_INCLUSIONS_TEMPLATE, "com.guicedee.client.services.config.IGuiceScanModuleInclusions");
            templateToInterface.put(GuicedEEFileTemplateProvider.CONFIGURATOR_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuiceConfigurator");
            templateToInterface.put(GuicedEEFileTemplateProvider.FILE_CONTENTS_SCANNER_TEMPLATE, "com.guicedee.client.services.config.IFileContentsScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.FILE_CONTENTS_PATTERN_SCANNER_TEMPLATE, "com.guicedee.client.services.config.IFileContentsPatternScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.PACKAGE_CONTENTS_SCANNER_TEMPLATE, "com.guicedee.client.services.config.IPackageContentsScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.PATH_CONTENTS_SCANNER_TEMPLATE, "com.guicedee.client.services.config.IPathContentsScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.ON_CALL_SCOPE_ENTER_TEMPLATE, "com.guicedee.client.services.lifecycle.IOnCallScopeEnter");
            templateToInterface.put(GuicedEEFileTemplateProvider.ON_CALL_SCOPE_EXIT_TEMPLATE, "com.guicedee.client.services.lifecycle.IOnCallScopeExit");
            templateToInterface.put(GuicedEEFileTemplateProvider.GUICE_MODULE_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuiceModule");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_CHANNEL_TEMPLATE, "com.guicedee.client.services.websocket.IGuicedWebSocket");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_MESSAGE_RECEIVER_TEMPLATE, "com.guicedee.client.services.websocket.IWebSocketMessageReceiver");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_PRE_CONFIGURATION_TEMPLATE, "com.guicedee.client.services.websocket.IWebSocketPreConfiguration");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_ON_PUBLISH_TEMPLATE, "com.guicedee.client.services.websocket.GuicedWebSocketOnPublish");
            templateToInterface.put(GuicedEEFileTemplateProvider.ROUTER_CONFIGURATION_TEMPLATE, "com.guicedee.vertx.web.spi.VertxRouterConfigurator");
            templateToInterface.put(GuicedEEFileTemplateProvider.LOG4J_CONFIGURATOR_TEMPLATE, "com.guicedee.client.services.Log4JConfigurator");
            templateToInterface.put(GuicedEEFileTemplateProvider.PERSISTENCE_MODULE_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuiceModule");
            templateToInterface.put(GuicedEEFileTemplateProvider.RABBITMQ_CONSUMER_TEMPLATE, "com.guicedee.rabbit.QueueConsumer");
            templateToInterface.put(GuicedEEFileTemplateProvider.KAFKA_CONSUMER_TEMPLATE, "com.guicedee.kafka.KafkaTopicConsumer");
            templateToInterface.put(GuicedEEFileTemplateProvider.IBMMQ_CONSUMER_TEMPLATE, "com.guicedee.ibmmq.IBMMQConsumer");
            templateToInterface.put(GuicedEEFileTemplateProvider.VERTX_STARTUP_TEMPLATE, "com.guicedee.vertx.spi.VerticleStartup");
            templateToInterface.put(GuicedEEFileTemplateProvider.VERTX_CONFIGURATOR_TEMPLATE, "com.guicedee.vertx.spi.VertxConfigurator");
            templateToInterface.put(GuicedEEFileTemplateProvider.HTTP_PROXY_MODULE_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuiceModule");
            templateToInterface.put(GuicedEEFileTemplateProvider.GRPC_MODULE_TEMPLATE, "com.guicedee.client.services.lifecycle.IGuiceModule");
            templateToInterface.put(GuicedEEFileTemplateProvider.AUTHENTICATION_PROVIDER_TEMPLATE, "com.guicedee.vertx.auth.IGuicedAuthenticationProvider");
            templateToInterface.put(GuicedEEFileTemplateProvider.AUTHORIZATION_PROVIDER_TEMPLATE, "com.guicedee.vertx.auth.IGuicedAuthorizationProvider");
            templateToInterface.put(GuicedEEFileTemplateProvider.HAZELCAST_SERVER_CONFIG_TEMPLATE, "com.guicedee.guicedhazelcast.services.IGuicedHazelcastServerConfig");
            templateToInterface.put(GuicedEEFileTemplateProvider.HAZELCAST_CLIENT_CONFIG_TEMPLATE, "com.guicedee.guicedhazelcast.services.IGuicedHazelcastClientConfig");

            // Check if this template corresponds to a service interface
            String interfaceName = templateToInterface.get(templateName);
            if (interfaceName == null) {
                return; // Not a service interface
            }

            // Full class name of the implementation
            String fullClassName = packageName + "." + className;

            try {
                // Create META-INF/services file
                createMetaInfServicesFile(project, interfaceName, fullClassName, dir);

                // Update module-info.java with provides entry
                updateModuleInfoFile(project, interfaceName, fullClassName, dir);
            } catch (Exception e) {
                Messages.showErrorDialog(project,
                    "Error creating service registration: " + e.getMessage(),
                    "Error Creating Service Registration");
            }
        }

        /**
         * Creates META-INF/services file for the given interface.
         * Finds the resources directory relative to the source root where the file is being created.
         */
        private void createMetaInfServicesFile(Project project, String interfaceName, String fullClassName, PsiDirectory dir) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    // Find the resources directory by traversing up from the selected directory
                    // to find src/main/java, then use src/main/resources as sibling
                    VirtualFile resourcesDir = findResourcesDirectory(dir);
                    if (resourcesDir == null) {
                        return;
                    }

                    // Create META-INF/services directory if it doesn't exist
                    VirtualFile metaInfDir = resourcesDir.findChild("META-INF");
                    if (metaInfDir == null) {
                        metaInfDir = resourcesDir.createChildDirectory(this, "META-INF");
                    }

                    VirtualFile servicesDir = metaInfDir.findChild("services");
                    if (servicesDir == null) {
                        servicesDir = metaInfDir.createChildDirectory(this, "services");
                    }

                    // Create or update the service file
                    VirtualFile serviceFile = servicesDir.findChild(interfaceName);
                    if (serviceFile == null) {
                        serviceFile = servicesDir.createChildData(this, interfaceName);
                        serviceFile.setBinaryContent(fullClassName.getBytes());
                    } else {
                        // Check if the implementation is already registered
                        String content = new String(serviceFile.contentsToByteArray());
                        boolean found = false;
                        String[] lines = content.split("\\r?\\n");
                        for (String line : lines) {
                            if (line.trim().equals(fullClassName)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            String newContent = content.trim().isEmpty() ? fullClassName : content.trim() + "\n" + fullClassName;
                            serviceFile.setBinaryContent(newContent.getBytes());
                        }
                    }
                } catch (IOException e) {
                    Messages.showErrorDialog(project,
                        "Error creating META-INF/services file: " + e.getMessage(),
                        "Error Creating Service Registration");
                }
            });
        }

        /**
         * Finds the src/main/resources directory relative to the source root where the file is being created.
         * Traverses up from the selected directory to find a "java" directory under "main" under "src",
         * then returns the sibling "resources" directory (creating it if needed).
         */
        private VirtualFile findResourcesDirectory(PsiDirectory dir) throws IOException {
            VirtualFile current = dir.getVirtualFile();

            // Walk up to find the "java" directory that is under src/main/
            while (current != null) {
                if ("java".equals(current.getName())) {
                    VirtualFile mainDir = current.getParent();
                    if (mainDir != null && "main".equals(mainDir.getName())) {
                        // Found src/main/java — use src/main/resources
                        VirtualFile resourcesDir = mainDir.findChild("resources");
                        if (resourcesDir == null) {
                            resourcesDir = mainDir.createChildDirectory(this, "resources");
                        }
                        return resourcesDir;
                    }
                }
                current = current.getParent();
            }

            // Fallback: use project base dir / src / main / resources
            VirtualFile projectDir = com.intellij.openapi.project.ProjectUtil.guessProjectDir(dir.getProject());
            if (projectDir == null) {
                return null;
            }
            VirtualFile srcDir = projectDir.findChild("src");
            if (srcDir == null) {
                srcDir = projectDir.createChildDirectory(this, "src");
            }
            VirtualFile mainDir = srcDir.findChild("main");
            if (mainDir == null) {
                mainDir = srcDir.createChildDirectory(this, "main");
            }
            VirtualFile resourcesDir = mainDir.findChild("resources");
            if (resourcesDir == null) {
                resourcesDir = mainDir.createChildDirectory(this, "resources");
            }
            return resourcesDir;
        }

        /**
         * Updates module-info.java with provides entry.
         * Finds the correct module-info.java in the same source root as where the file was created.
         */
        private void updateModuleInfoFile(Project project, String interfaceName, String fullClassName, PsiDirectory dir) {
            try {
                // Find module-info.java files
                PsiManager psiManager = PsiManager.getInstance(project);
                Collection<VirtualFile> moduleInfoVFiles = FilenameIndex.getVirtualFilesByName(
                    "module-info.java", GlobalSearchScope.projectScope(project));

                if (moduleInfoVFiles.isEmpty()) {
                    return; // No module-info.java found
                }

                // Find the source root for the directory where the file was created
                VirtualFile dirFile = dir.getVirtualFile();
                VirtualFile sourceRoot = findSourceRoot(dirFile);

                for (VirtualFile vf : moduleInfoVFiles) {
                    PsiFile moduleInfoFile = psiManager.findFile(vf);
                    if (!(moduleInfoFile instanceof PsiJavaFile)) {
                        continue;
                    }

                    // Skip test module-info.java files
                    String path = moduleInfoFile.getVirtualFile().getPath();
                    if (path.contains("/test/") || path.contains("\\test\\")) {
                        continue;
                    }

                    // If we found a source root, only update module-info.java in the same source tree
                    if (sourceRoot != null) {
                        VirtualFile moduleInfoRoot = findSourceRoot(moduleInfoFile.getVirtualFile());
                        if (moduleInfoRoot != null && !sourceRoot.equals(moduleInfoRoot)) {
                            // Check if they share the same parent (same module in a multi-module project)
                            VirtualFile dirMainDir = sourceRoot.getParent(); // src/main
                            VirtualFile moduleMainDir = moduleInfoRoot.getParent();
                            if (dirMainDir == null || !dirMainDir.equals(moduleMainDir)) {
                                continue; // Different module
                            }
                        }
                    }

                    PsiJavaFile javaFile = (PsiJavaFile) moduleInfoFile;
                    String fileText = javaFile.getText();

                    // Check if the implementation is already registered
                    String providesStatement = "provides " + interfaceName + " with " + fullClassName;
                    if (fileText.contains(providesStatement)) {
                        continue; // Already has the exact provides statement
                    }

                    // Check if there's already a provides statement for this interface
                    String providesPrefix = "provides " + interfaceName + " with ";
                    int providesIndex = fileText.indexOf(providesPrefix);

                    if (providesIndex != -1) {
                        // Find the end of the existing provides statement
                        int semicolonIndex = fileText.indexOf(";", providesIndex);
                        if (semicolonIndex != -1) {
                            // Extract the existing implementations
                            String existingImplementations = fileText.substring(providesIndex + providesPrefix.length(), semicolonIndex);

                            // Append the new implementation
                            String newImplementations = existingImplementations.trim() + ",\n\t\t\t" + fullClassName;

                            // Replace the existing provides statement with the updated one
                            String newFileText = fileText.substring(0, providesIndex) +
                                providesPrefix + newImplementations + fileText.substring(semicolonIndex);

                            // Update the file
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                javaFile.getViewProvider().getDocument().setText(newFileText);
                            });

                            return; // Done updating
                        }
                    }

                    // If we get here, there's no existing provides statement for this interface
                    // Find the closing brace of the module declaration
                    int closingBraceIndex = fileText.lastIndexOf("}");
                    if (closingBraceIndex == -1) {
                        continue; // Invalid module-info.java
                    }

                    // Insert the provides statement before the closing brace
                    String newFileText = fileText.substring(0, closingBraceIndex) +
                        "\tprovides " + interfaceName + " with " + fullClassName + ";\n" +
                        fileText.substring(closingBraceIndex);

                    // Update the file
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        javaFile.getViewProvider().getDocument().setText(newFileText);
                    });

                    return; // Only update the first matching module-info.java
                }
            } catch (Exception e) {
                Messages.showErrorDialog(project,
                    "Error updating module-info.java: " + e.getMessage(),
                    "Error Creating Service Registration");
            }
        }

        /**
         * Finds the source root (e.g., src/main/java) for a given VirtualFile by walking up.
         */
        private VirtualFile findSourceRoot(VirtualFile file) {
            VirtualFile current = file;
            while (current != null) {
                if ("java".equals(current.getName())) {
                    VirtualFile parent = current.getParent();
                    if (parent != null && ("main".equals(parent.getName()) || "test".equals(parent.getName()))) {
                        return current;
                    }
                }
                current = current.getParent();
            }
            return null;
        }

        /**
         * Ensures required Maven dependencies are present in the pom.xml for the given template.
         * If a dependency is missing, it is added automatically.
         * Also adds the corresponding requires statement to module-info.java.
         */
        private void ensureRequiredDependency(Project project, String templateName, PsiDirectory dir) {
            // Map templates to their required dependencies (groupId, artifactId, module-info requires name)
            java.util.Map<String, String[][]> templateToDependencies = new java.util.HashMap<>();

            // Web / Router: com.guicedee:web -> requires com.guicedee.vertx.web
            String[][] webDeps = {{"com.guicedee", "web", "com.guicedee.vertx.web"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.ROUTER_CONFIGURATION_TEMPLATE, webDeps);

            // REST: com.guicedee:rest -> requires com.guicedee.rest
            String[][] restDeps = {{"com.guicedee", "rest", "com.guicedee.rest"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE, restDeps);

            // REST Client: com.guicedee:rest-client -> requires com.guicedee.rest.client
            String[][] restClientDeps = {{"com.guicedee", "rest-client", "com.guicedee.rest.client"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.REST_CLIENT_TEMPLATE, restClientDeps);

            // Persistence / Database: com.guicedee:persistence -> requires com.guicedee.persistence
            String[][] persistDeps = {{"com.guicedee", "persistence", "com.guicedee.persistence"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.PERSISTENCE_MODULE_TEMPLATE, persistDeps);

            // RabbitMQ: com.guicedee:rabbitmq -> requires com.guicedee.rabbit
            String[][] rabbitDeps = {{"com.guicedee", "rabbitmq", "com.guicedee.rabbit"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.RABBITMQ_CONSUMER_TEMPLATE, rabbitDeps);

            // Kafka: com.guicedee:kafka -> requires com.guicedee.kafka
            String[][] kafkaDeps = {{"com.guicedee", "kafka", "com.guicedee.kafka"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.KAFKA_CONSUMER_TEMPLATE, kafkaDeps);

            // IBM MQ: com.guicedee:ibmmq -> requires com.guicedee.ibmmq
            String[][] ibmmqDeps = {{"com.guicedee", "ibmmq", "com.guicedee.ibmmq"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.IBMMQ_CONSUMER_TEMPLATE, ibmmqDeps);

            // Auth: com.guicedee:web -> requires com.guicedee.vertx.web
            templateToDependencies.put(GuicedEEFileTemplateProvider.AUTHENTICATION_PROVIDER_TEMPLATE, webDeps);
            templateToDependencies.put(GuicedEEFileTemplateProvider.AUTHORIZATION_PROVIDER_TEMPLATE, webDeps);

            // Mail: com.guicedee:mailclient -> requires com.guicedee.mail
            String[][] mailDeps = {{"com.guicedee", "mailclient", "com.guicedee.mail"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.MAIL_CLIENT_TEMPLATE, mailDeps);

            // Health: com.guicedee:health -> requires com.guicedee.health
            String[][] healthDeps = {{"com.guicedee", "health", "com.guicedee.health"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.HEALTH_CHECK_TEMPLATE, healthDeps);

            // Hazelcast: com.guicedee:hazelcast -> requires com.guicedee.guicedhazelcast
            String[][] hazelcastDeps = {{"com.guicedee", "hazelcast", "com.guicedee.guicedhazelcast"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.HAZELCAST_SERVER_CONFIG_TEMPLATE, hazelcastDeps);
            templateToDependencies.put(GuicedEEFileTemplateProvider.HAZELCAST_CLIENT_CONFIG_TEMPLATE, hazelcastDeps);

            // WebSockets: com.guicedee:websockets -> requires com.guicedee.vertx.sockets
            String[][] wsDeps = {{"com.guicedee", "websockets", "com.guicedee.vertx.sockets"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.WEBSOCKET_CHANNEL_TEMPLATE, wsDeps);
            templateToDependencies.put(GuicedEEFileTemplateProvider.WEBSOCKET_MESSAGE_RECEIVER_TEMPLATE, wsDeps);
            templateToDependencies.put(GuicedEEFileTemplateProvider.WEBSOCKET_PRE_CONFIGURATION_TEMPLATE, wsDeps);
            templateToDependencies.put(GuicedEEFileTemplateProvider.WEBSOCKET_ON_PUBLISH_TEMPLATE, wsDeps);

            // SOAP Web Services: com.guicedee:web-services -> requires com.guicedee.webservices
            String[][] soapDeps = {
                {"com.guicedee", "web-services", "com.guicedee.webservices"},
                {"org.glassfish.jaxb", "jaxb-runtime", null}
            };
            templateToDependencies.put(GuicedEEFileTemplateProvider.SOAP_SERVICE_INTERFACE_TEMPLATE, soapDeps);
            templateToDependencies.put(GuicedEEFileTemplateProvider.SOAP_SERVICE_IMPL_TEMPLATE, soapDeps);

            // HTTP Proxy: com.guicedee:vertx + io.vertx:vertx-http-proxy
            String[][] proxyDeps = {{"io.vertx", "vertx-http-proxy", "io.vertx.httpproxy"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.HTTP_PROXY_MODULE_TEMPLATE, proxyDeps);

            // gRPC: io.vertx:vertx-grpc-server + io.vertx:vertx-grpc-client
            String[][] grpcDeps = {
                {"io.vertx", "vertx-grpc-server", "io.vertx.grpc.server"},
                {"io.vertx", "vertx-grpc-client", "io.vertx.grpc.client"}
            };
            templateToDependencies.put(GuicedEEFileTemplateProvider.GRPC_MODULE_TEMPLATE, grpcDeps);

            // Redis: io.vertx:vertx-redis-client
            String[][] redisDeps = {{"io.vertx", "vertx-redis-client", "io.vertx.redis.client"}};
            templateToDependencies.put(GuicedEEFileTemplateProvider.REDIS_MODULE_TEMPLATE, redisDeps);

            String[][] deps = templateToDependencies.get(templateName);
            if (deps == null) {
                return; // No special dependency needed (core inject/vertx already present)
            }

            // Find the pom.xml for the module where the file was created
            VirtualFile pomFile = findPomXml(dir);
            if (pomFile == null) {
                return;
            }

            try {
                String pomContent = new String(pomFile.contentsToByteArray());

                java.util.List<String[]> missingDeps = new java.util.ArrayList<>();
                for (String[] dep : deps) {
                    String artifactId = dep[1];
                    // Check if the dependency already exists in the pom
                    if (!pomContent.contains("<artifactId>" + artifactId + "</artifactId>")) {
                        missingDeps.add(dep);
                    }
                }

                if (missingDeps.isEmpty()) {
                    return; // All dependencies present
                }

                // Build the dependency XML to insert
                StringBuilder depXml = new StringBuilder();
                for (String[] dep : missingDeps) {
                    depXml.append("\n        <dependency>\n");
                    depXml.append("            <groupId>").append(dep[0]).append("</groupId>\n");
                    depXml.append("            <artifactId>").append(dep[1]).append("</artifactId>\n");
                    depXml.append("        </dependency>");
                }

                // Find the insertion point: before </dependencies>
                String insertionMarker = "</dependencies>";
                int insertionIndex = pomContent.lastIndexOf(insertionMarker);
                if (insertionIndex == -1) {
                    return; // No dependencies section found
                }

                String newPomContent = pomContent.substring(0, insertionIndex)
                        + depXml
                        + "\n    " + pomContent.substring(insertionIndex);

                // Write the updated pom.xml
                final String finalContent = newPomContent;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    try {
                        pomFile.setBinaryContent(finalContent.getBytes());
                    } catch (IOException e) {
                        // Silently fail - dependency can be added manually
                    }
                });

                // Also add requires statements to module-info.java for each missing dependency
                for (String[] dep : missingDeps) {
                    if (dep.length >= 3 && dep[2] != null) {
                        addRequiresToModuleInfo(project, dep[2], dir);
                    }
                }
            } catch (IOException e) {
                // Silently fail - dependency can be added manually
            }
        }

        /**
         * Adds a "requires transitive" statement to the module-info.java if not already present.
         */
        private void addRequiresToModuleInfo(Project project, String moduleName, PsiDirectory dir) {
            try {
                PsiManager psiManager = PsiManager.getInstance(project);
                Collection<VirtualFile> moduleInfoVFiles = FilenameIndex.getVirtualFilesByName(
                    "module-info.java", GlobalSearchScope.projectScope(project));

                if (moduleInfoVFiles.isEmpty()) {
                    return;
                }

                VirtualFile dirFile = dir.getVirtualFile();
                VirtualFile sourceRoot = findSourceRoot(dirFile);

                for (VirtualFile vf : moduleInfoVFiles) {
                    PsiFile moduleInfoFile = psiManager.findFile(vf);
                    if (!(moduleInfoFile instanceof PsiJavaFile)) continue;

                    String path = moduleInfoFile.getVirtualFile().getPath();
                    if (path.contains("/test/") || path.contains("\\test\\")) continue;

                    // Match to same source tree
                    if (sourceRoot != null) {
                        VirtualFile moduleInfoRoot = findSourceRoot(moduleInfoFile.getVirtualFile());
                        if (moduleInfoRoot != null && !sourceRoot.equals(moduleInfoRoot)) {
                            VirtualFile dirMainDir = sourceRoot.getParent();
                            VirtualFile moduleMainDir = moduleInfoRoot.getParent();
                            if (dirMainDir == null || !dirMainDir.equals(moduleMainDir)) {
                                continue;
                            }
                        }
                    }

                    PsiJavaFile javaFile = (PsiJavaFile) moduleInfoFile;
                    String fileText = javaFile.getText();

                    // Check if requires already exists
                    String requiresStatement = "requires " + moduleName;
                    if (fileText.contains(requiresStatement)) {
                        return; // Already present
                    }

                    // Find a good insertion point — after the last "requires" line, or after the module declaration
                    int lastRequiresIndex = fileText.lastIndexOf("requires ");
                    int insertIndex;
                    if (lastRequiresIndex != -1) {
                        // Find end of that requires statement (next semicolon + newline)
                        int semiIndex = fileText.indexOf(";", lastRequiresIndex);
                        if (semiIndex != -1) {
                            insertIndex = semiIndex + 1;
                            // Skip past the newline if present
                            if (insertIndex < fileText.length() && fileText.charAt(insertIndex) == '\n') {
                                insertIndex++;
                            } else if (insertIndex + 1 < fileText.length() && fileText.charAt(insertIndex) == '\r' && fileText.charAt(insertIndex + 1) == '\n') {
                                insertIndex += 2;
                            }
                        } else {
                            insertIndex = -1;
                        }
                    } else {
                        // No requires found, insert after the opening brace
                        int braceIndex = fileText.indexOf("{");
                        insertIndex = braceIndex != -1 ? braceIndex + 1 : -1;
                    }

                    if (insertIndex == -1) return;

                    String newFileText = fileText.substring(0, insertIndex)
                        + "\trequires transitive " + moduleName + ";\n"
                        + fileText.substring(insertIndex);

                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        javaFile.getViewProvider().getDocument().setText(newFileText);
                    });

                    return; // Only update the first matching module-info.java
                }
            } catch (Exception e) {
                // Silently fail
            }
        }

        /**
         * Finds the pom.xml for the module where the file is being created.
         * Walks up from the source root to find the nearest pom.xml.
         */
        private VirtualFile findPomXml(PsiDirectory dir) {
            VirtualFile current = dir.getVirtualFile();

            // Walk up to find pom.xml
            while (current != null) {
                VirtualFile pom = current.findChild("pom.xml");
                if (pom != null) {
                    return pom;
                }
                current = current.getParent();
            }
            return null;
        }

        /**
         * Gets the package name from the directory.
         * If the directory is not a package, returns a base package name.
         */
        private String getPackageNameFromDirectory(Project project, PsiDirectory directory) {
            // Try to get the package name from the directory
            PsiPackage psiPackage = JavaDirectoryServiceImpl.getInstance().getPackage(directory);
            if (psiPackage != null && !psiPackage.getQualifiedName().isEmpty()) {
                return psiPackage.getQualifiedName();
            }

            // If no package found, try to find a base package name from the project
            return findBasePackageName(project);
        }

        /**
         * Finds a base package name from the project.
         * Returns "com.example" if no base package can be determined.
         */
        private String findBasePackageName(Project project) {
            // Try to find a common package name in the project
            PsiPackage rootPackage = JavaPsiFacade.getInstance(project).findPackage("");
            if (rootPackage != null) {
                PsiPackage[] subPackages = rootPackage.getSubPackages(GlobalSearchScope.projectScope(project));
                if (subPackages.length > 0) {
                    // Use the first subpackage as a base
                    return subPackages[0].getQualifiedName();
                }
            }

            // Default base package name
            return "com.example";
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);

            Project project = e.getProject();
            if (project == null) {
                e.getPresentation().setEnabledAndVisible(false);
                return;
            }

            // If this is the "Create Project Structure" action, only check if it's a directory
            if (isProjectStructureAction) {
                PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
                e.getPresentation().setEnabledAndVisible(element instanceof PsiDirectory);
                return;
            }

            // For all other templates, check if it's a GuicedEE Application
            boolean isGuicedEEApplication = isGuicedEEApplication(project);
            if (!isGuicedEEApplication) {
                e.getPresentation().setEnabledAndVisible(false);
                return;
            }

            // Additional checks for specific templates
            if (templateName.equals(GuicedEEFileTemplateProvider.RABBITMQ_CONSUMER_TEMPLATE)) {
                // Only enable RabbitMQ Consumer if RabbitMQ is enabled in the project
                boolean isRabbitMQEnabled = isRabbitMQEnabled(project);
                e.getPresentation().setEnabledAndVisible(isRabbitMQEnabled);
            }
        }

        private boolean isRabbitMQEnabled(Project project) {
            // Look for module-info.java files
            PsiManager psiManager = PsiManager.getInstance(project);
            Collection<VirtualFile> moduleInfoVFiles = FilenameIndex.getVirtualFilesByName(
                    "module-info.java", GlobalSearchScope.projectScope(project));

            for (VirtualFile vf : moduleInfoVFiles) {
                PsiFile file = psiManager.findFile(vf);
                if (file instanceof PsiJavaFile) {
                    String text = file.getText();
                    // Check if the module-info.java file contains a reference to com.guicedee.rabbit
                    if (text.contains("requires") && text.contains("com.guicedee.rabbit")) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
