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
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.impl.file.JavaDirectoryServiceImpl;
import com.guicedee.intellij.template.GuicedEEFileTemplateProvider;
import com.guicedee.intellij.wizard.GuicedEEProjectWizardData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Properties;

/**
 * Action group for creating new GuicedEE files from the right-click menu.
 * This action group only appears for projects that meet the criteria for a GuicedEE Application.
 */
public class GuicedEENewFileActionGroup extends DefaultActionGroup implements DumbAware {
    private static final Icon GUICEDEE_ICON = IconLoader.getIcon("/META-INF/logo_front.png", GuicedEENewFileActionGroup.class);

    public GuicedEENewFileActionGroup() {
        super("GuicedEE", true);
        getTemplatePresentation().setIcon(GUICEDEE_ICON);

        // Add action for creating module-info.java and project structure
        add(new CreateProjectStructureAction());

        // Add core items
        add(new CreateGuicedEEFileAction("Module", "Create a new Module", 
                GuicedEEFileTemplateProvider.GUICE_MODULE_TEMPLATE));
        add(new CreateGuicedEEFileAction("Transactional Event", "Create a new Transactional Event", 
                GuicedEEFileTemplateProvider.TRANSACTIONAL_EVENT_TEMPLATE));
        add(new CreateGuicedEEFileAction("Job", "Create a new Job", 
                GuicedEEFileTemplateProvider.JOB_TEMPLATE));
        add(new CreateGuicedEEFileAction("RabbitMQ Consumer", "Create a new RabbitMQ Consumer", 
                GuicedEEFileTemplateProvider.RABBITMQ_CONSUMER_TEMPLATE));

        // Add Web subgroup
        DefaultActionGroup webGroup = new DefaultActionGroup("Web", true);
        webGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(webGroup);

        webGroup.add(new CreateGuicedEEFileAction("Router Configuration", "Create a new Router Configuration", 
                GuicedEEFileTemplateProvider.ROUTER_CONFIGURATION_TEMPLATE));
        webGroup.add(new CreateGuicedEEFileAction("WebSocket Channel", "Create a new WebSocket Channel", 
                GuicedEEFileTemplateProvider.WEBSOCKET_CHANNEL_TEMPLATE));

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
        restGroup.add(new CreateGuicedEEFileAction("REST Service Implementation", "Create a new REST Service Implementation", 
                GuicedEEFileTemplateProvider.REST_SERVICE_IMPL_TEMPLATE));

        // Add Database subgroup
        DefaultActionGroup databaseGroup = new DefaultActionGroup("Database", true);
        databaseGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(databaseGroup);

        databaseGroup.add(new CreateGuicedEEFileAction("Persistence Module", "Create a new Persistence Module", 
                GuicedEEFileTemplateProvider.PERSISTENCE_MODULE_TEMPLATE));

        // Add Caching subgroup
        DefaultActionGroup cachingGroup = new DefaultActionGroup("Caching", true);
        cachingGroup.getTemplatePresentation().setIcon(GUICEDEE_ICON);
        add(cachingGroup);


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
        PsiFile[] moduleInfoFiles = FilenameIndex.getFilesByName(
                project, "module-info.java", GlobalSearchScope.projectScope(project));

        for (PsiFile file : moduleInfoFiles) {
            if (file instanceof PsiJavaFile) {
                String text = file.getText();
                // Check if the module-info.java file contains references to GuicedEE dependencies
                if (text.contains("requires") && 
                    (text.contains("com.guicedee.guicedinjection") || 
                     text.contains("com.guicedee.guicedservlets"))) {
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
            javaPsiFacade.findClass("com.guicedee.guicedinjection.interfaces.IGuiceContext", scope) != null;

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

                // For REST service, add the PATH property if not already set
                if (templateName.equals(GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE)) {
                    if (!defaultProperties.containsKey("PATH")) {
                        defaultProperties.setProperty("PATH", name.toLowerCase());
                    }
                }

                // Create the file with the default properties
                PsiFile file;
                try {
                    file = super.createFile(name, templateName, dir);
                    if (file == null) {
                        Messages.showErrorDialog(dir.getProject(),
                            "Failed to create file from template: " + templateName,
                            "Error Creating File");
                        return null;
                    }
                } catch (Exception e) {
                    Messages.showErrorDialog(dir.getProject(),
                        "Error creating file from template: " + e.getMessage(),
                        "Error Creating File");
                    return null;
                }

                // If creating a REST service, also create the service implementation
                if (templateName.equals(GuicedEEFileTemplateProvider.REST_SERVICE_TEMPLATE)) {
                    try {
                        // Create services directory if it doesn't exist
                        PsiDirectory servicesDir;
                        if (dir.findSubdirectory("services") == null) {
                            servicesDir = dir.createSubdirectory("services");
                        } else {
                            servicesDir = dir.findSubdirectory("services");
                        }

                        // Create the service implementation file
                        FileTemplate template = fileTemplateManager.getJ2eeTemplate(GuicedEEFileTemplateProvider.REST_SERVICE_IMPL_TEMPLATE);
                        if (template == null) {
                            Messages.showErrorDialog(dir.getProject(),
                                "REST service implementation template not found: " + GuicedEEFileTemplateProvider.REST_SERVICE_IMPL_TEMPLATE,
                                "Template Not Found");
                            return file;
                        }

                        Properties properties = new Properties(defaultProperties);
                        // Use the same name as the REST service but with "Service" suffix
                        String serviceImplName = name + "Service";
                        // Add the service name to the properties
                        properties.setProperty("SERVICE_NAME", serviceImplName);
                        // Add the REST service name to the properties
                        properties.setProperty("REST_SERVICE_NAME", name);

                        PsiElement createdFile = FileTemplateUtil.createFromTemplate(template, serviceImplName, properties, servicesDir);
                        if (createdFile == null) {
                            Messages.showErrorDialog(dir.getProject(),
                                "Failed to create REST service implementation file",
                                "Error Creating REST Service Implementation");
                        }
                    } catch (Exception e) {
                        // Log error or show notification
                        Messages.showErrorDialog(dir.getProject(), 
                            "Failed to create REST service implementation: " + e.getMessage(), 
                            "Error Creating REST Service Implementation");
                    }
                }

                // Create META-INF/services file and update module-info.java for service interfaces
                createServiceRegistration(dir.getProject(), templateName, packageName, name);

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
        private void createServiceRegistration(Project project, String templateName, String packageName, String className) {
            // Map of template names to their corresponding interface names
            java.util.Map<String, String> templateToInterface = new java.util.HashMap<>();
            templateToInterface.put(GuicedEEFileTemplateProvider.PRE_STARTUP_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IGuicePreStartup");
            templateToInterface.put(GuicedEEFileTemplateProvider.POST_STARTUP_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IGuicePostStartup");
            templateToInterface.put(GuicedEEFileTemplateProvider.PRE_DESTROY_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IGuicePreDestroy");
            templateToInterface.put(GuicedEEFileTemplateProvider.SCAN_MODULE_INCLUSIONS_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions");
            templateToInterface.put(GuicedEEFileTemplateProvider.CONFIGURATOR_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IGuiceConfigurator");
            templateToInterface.put(GuicedEEFileTemplateProvider.FILE_CONTENTS_SCANNER_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IFileContentsScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.FILE_CONTENTS_PATTERN_SCANNER_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IFileContentsPatternScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.PACKAGE_CONTENTS_SCANNER_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IPackageContentsScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.PATH_CONTENTS_SCANNER_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IPathContentsScanner");
            templateToInterface.put(GuicedEEFileTemplateProvider.ON_CALL_SCOPE_ENTER_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IOnCallScopeEnter");
            templateToInterface.put(GuicedEEFileTemplateProvider.ON_CALL_SCOPE_EXIT_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IOnCallScopeExit");
            templateToInterface.put(GuicedEEFileTemplateProvider.GUICE_MODULE_TEMPLATE, "com.guicedee.guicedinjection.interfaces.IGuiceModule");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_CHANNEL_TEMPLATE, "com.guicedee.guicedservlets.websockets.services.IGuicedWebSocket");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_MESSAGE_RECEIVER_TEMPLATE, "com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver");
            templateToInterface.put(GuicedEEFileTemplateProvider.WEBSOCKET_PRE_CONFIGURATION_TEMPLATE, "com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration");

            // Check if this template corresponds to a service interface
            String interfaceName = templateToInterface.get(templateName);
            if (interfaceName == null) {
                return; // Not a service interface
            }

            // Full class name of the implementation
            String fullClassName = packageName + "." + className;

            try {
                // Create META-INF/services file
                createMetaInfServicesFile(project, interfaceName, fullClassName);

                // Update module-info.java with provides entry
                updateModuleInfoFile(project, interfaceName, fullClassName);
            } catch (Exception e) {
                Messages.showErrorDialog(project,
                    "Error creating service registration: " + e.getMessage(),
                    "Error Creating Service Registration");
            }
        }

        /**
         * Creates META-INF/services file for the given interface
         */
        private void createMetaInfServicesFile(Project project, String interfaceName, String fullClassName) {
            try {
                // Find or create resources directory
                VirtualFile projectDir = project.getBaseDir();
                if (projectDir == null) {
                    return;
                }

                // Look for src/main/resources directory
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
                    // Split by lines and check if any line exactly matches the fullClassName
                    boolean found = false;
                    String[] lines = content.split("\\r?\\n");
                    for (String line : lines) {
                        if (line.trim().equals(fullClassName)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        // Append the new implementation on a new line
                        String newContent = content.trim().isEmpty() ? fullClassName : content.trim() + "\n" + fullClassName;
                        serviceFile.setBinaryContent(newContent.getBytes());
                    }
                }
            } catch (IOException e) {
                Messages.showErrorDialog(project,
                    "Error creating META-INF/services file: " + e.getMessage(),
                    "Error Creating Service Registration");
            }
        }

        /**
         * Updates module-info.java with provides entry
         */
        private void updateModuleInfoFile(Project project, String interfaceName, String fullClassName) {
            try {
                // Find module-info.java
                PsiFile[] moduleInfoFiles = FilenameIndex.getFilesByName(
                    project, "module-info.java", GlobalSearchScope.projectScope(project));

                if (moduleInfoFiles.length == 0) {
                    return; // No module-info.java found
                }

                for (PsiFile moduleInfoFile : moduleInfoFiles) {
                    if (!(moduleInfoFile instanceof PsiJavaFile)) {
                        continue;
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
                            String newImplementations = existingImplementations + "," + fullClassName;

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
                        "\t" + providesStatement + ";\n" +
                        fileText.substring(closingBraceIndex);

                    // Update the file
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        javaFile.getViewProvider().getDocument().setText(newFileText);
                    });
                }
            } catch (Exception e) {
                Messages.showErrorDialog(project,
                    "Error updating module-info.java: " + e.getMessage(),
                    "Error Creating Service Registration");
            }
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
            PsiFile[] moduleInfoFiles = FilenameIndex.getFilesByName(
                    project, "module-info.java", GlobalSearchScope.projectScope(project));

            for (PsiFile file : moduleInfoFiles) {
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
