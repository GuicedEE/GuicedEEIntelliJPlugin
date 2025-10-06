package com.guicedee.intellij.wizard;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.Icon;

import java.io.File;
import com.intellij.openapi.application.ApplicationManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class GuicedEEProjectTemplateBuilder extends ModuleBuilder
{
    private static final Icon GUICEDEE_ICON = IconLoader.getIcon("/META-INF/logo_front.png", GuicedEEProjectTemplateBuilder.class);
    private final GuicedEEProjectWizardData myWizardData;

    public GuicedEEProjectTemplateBuilder()
    {
        System.out.println("[DEBUG_LOG] GuicedEEProjectTemplateBuilder constructor called");
        myWizardData = new GuicedEEProjectWizardData();
    }

    @Override
    public ModuleType<?> getModuleType()
    {
        return StdModuleTypes.JAVA;
    }

    @Override
    public String getPresentableName()
    {
        return "GuicedEE Application";
    }

    @Override
    public String getDescription()
    {
        return "Creates a new GuicedEE application with Maven";
    }

    @Override
    public boolean isTemplateBased()
    {
        return true;
    }

    @Override
    public Icon getNodeIcon()
    {
        return GUICEDEE_ICON;
    }

    @Override
    public String getParentGroup()
    {
        return "Java";
    }

    @Override
    public int getWeight()
    {
        return 100;
    }

    @Override
    public String getBuilderId()
    {
        return "guicedee.project";
    }

    @Override
    public String getGroupName()
    {
        return "GuicedEE";
    }

    @Override
    public boolean canCreateModule()
    {
        return true;
    }

    @Override
    public boolean isAvailable()
    {
        return true;
    }

    @Override
    public boolean validateModuleName(@NotNull String moduleName)
    {
        return true;
    }

    @Override
    public String getModuleFilePath()
    {
        return super.getModuleFilePath();
    }

    @Override
    public String getContentEntryPath()
    {
        return super.getContentEntryPath();
    }

    @Override
    public String getName()
    {
        return "GuicedEE Application";
    }

    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable)
    {
        System.out.println("[DEBUG_LOG] GuicedEEProjectTemplateBuilder.getCustomOptionsStep() called");
        return new GuicedEEProjectWizardStep(context, myWizardData);
    }

    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull Disposable disposable)
    {
        System.out.println("[DEBUG_LOG] GuicedEEProjectTemplateBuilder.createWizardSteps() called");
        return new ModuleWizardStep[]{
                new GuicedEEProjectWizardStep(wizardContext, myWizardData)
        };
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel rootModel)
    {
        System.out.println("[DEBUG_LOG] GuicedEEProjectTemplateBuilder.setupRootModel() called");
        Project project = rootModel.getProject();
        String contentEntryPath = FileUtil.toSystemIndependentName(getContentEntryPath());
        System.out.println("[DEBUG_LOG] contentEntryPath: " + contentEntryPath);

        try
        {
            File baseDir = new File(contentEntryPath);

            if (myWizardData.isMultiModuleProject())
            {
                // Create multi-module project
                createMultiModuleProject(baseDir);
            }
            else
            {
                // Create single-module project
                createSingleModuleProject(baseDir);
            }

            // Refresh the project to see the new files
            VirtualFile baseVirtualDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(baseDir);
            if (baseVirtualDir != null)
            {
                baseVirtualDir.refresh(false, true);

                // Add content root
                com.intellij.openapi.roots.ContentEntry contentEntry = rootModel.addContentEntry(baseVirtualDir);

                // Add source roots
                VirtualFile srcDir = baseVirtualDir.findChild("src");
                if (srcDir != null)
                {
                    VirtualFile mainDir = srcDir.findChild("main");
                    if (mainDir != null)
                    {
                        VirtualFile javaDir = mainDir.findChild("java");
                        if (javaDir != null)
                        {
                            contentEntry.addSourceFolder(javaDir, false);
                        }

                        VirtualFile resourcesDir = mainDir.findChild("resources");
                        if (resourcesDir != null)
                        {
                            contentEntry.addSourceFolder(resourcesDir, true);
                        }
                    }
                }

                // Load the project as a Maven project
                VirtualFile pomFile = baseVirtualDir.findChild("pom.xml");
                if (pomFile != null)
                {
                    System.out.println("[DEBUG_LOG] Found pom.xml file: " + pomFile.getPath());
                    // Schedule Maven project loading in a background task to avoid blocking the EDT
                    final VirtualFile finalPomFile = pomFile;
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            // Check if project is disposed before accessing it
                            if (!project.isDisposed()) {
                                try {
                                    MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
                                    mavenProjectsManager.addManagedFiles(Collections.singletonList(finalPomFile));
                                    // Don't call scheduleImportAndResolve() here, let the user manually import if needed
                                } catch (Exception e) {
                                    // Log the exception but don't rethrow it
                                    System.out.println("[DEBUG_LOG] Exception in Maven project loading: " + e.getMessage());
                                }
                            } else {
                                System.out.println("[DEBUG_LOG] Project is disposed, skipping Maven project loading");
                            }
                        });
                    });
                }
                else
                {
                    System.out.println("[DEBUG_LOG] No pom.xml file found in: " + baseVirtualDir.getPath());
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create project files", e);
        }
    }

    private void createSingleModuleProject(File baseDir) throws IOException
    {
        System.out.println("[DEBUG_LOG] GuicedEEProjectTemplateBuilder.createSingleModuleProject() called");
        // Create base directories
        File srcDir = new File(baseDir, "src/main/java");
        File resourcesDir = new File(baseDir, "src/main/resources");
        File metaInfDir = new File(resourcesDir, "META-INF/services");

        // Create test directories
        File testSrcDir = new File(baseDir, "src/test/java");
        File testResourcesDir = new File(baseDir, "src/test/resources");

        FileUtil.createDirectory(srcDir);
        FileUtil.createDirectory(resourcesDir);
        FileUtil.createDirectory(metaInfDir);
        FileUtil.createDirectory(testSrcDir);
        FileUtil.createDirectory(testResourcesDir);

        // Create pom.xml
        createPomXml(baseDir);

        // Create module-info.java
        createModuleInfo(srcDir);

        // Create test module-info.java
        createTestModuleInfo(testSrcDir);

        // Create main application class if needed
        if (myWizardData.isWebApplication())
        {
            createWebApplication(srcDir);
        }
        else
        {
            createBasicApplication(srcDir);
        }

        // Create database module if needed
        if (myWizardData.isDatabaseApplication())
        {
            createDatabaseModule(srcDir);
        }

        // Create RabbitMQ consumer if needed
        if (myWizardData.isRabbitMQSupport())
        {
            createRabbitMQConsumer(srcDir);
        }

        // Don't create scan module inclusions file during app creation
        // createScanModuleInclusions(srcDir);
    }

    private void createMultiModuleProject(File baseDir) throws IOException
    {
        System.out.println("[DEBUG_LOG] GuicedEEProjectTemplateBuilder.createMultiModuleProject() called");
        // Create parent POM
        createParentPom(baseDir);

        // Make sure we have at least one module
        if (myWizardData.getModules().isEmpty())
        {
            myWizardData.initializeModules();
        }

        // Create each module
        for (GuicedEEProjectWizardData.ModuleData moduleData : myWizardData.getModules())
        {
            // Create module directory
            String moduleDirName = moduleData.getArtifactId().replace(myWizardData.getArtifactId() + "-", "");
            File moduleDir = new File(baseDir, moduleDirName);
            FileUtil.createDirectory(moduleDir);

            // Create module directories
            File moduleSrcDir = new File(moduleDir, "src/main/java");
            File moduleResourcesDir = new File(moduleDir, "src/main/resources");
            File moduleMetaInfDir = new File(moduleResourcesDir, "META-INF/services");

            // Create test directories
            File moduleTestSrcDir = new File(moduleDir, "src/test/java");
            File moduleTestResourcesDir = new File(moduleDir, "src/test/resources");

            FileUtil.createDirectory(moduleSrcDir);
            FileUtil.createDirectory(moduleResourcesDir);
            FileUtil.createDirectory(moduleMetaInfDir);
            FileUtil.createDirectory(moduleTestSrcDir);
            FileUtil.createDirectory(moduleTestResourcesDir);

            // Create module POM
            createModulePom(moduleDir, moduleData);

            // Create module-info.java for module
            createModuleInfo(moduleSrcDir, moduleData);

            // Generate a module name based on the artifact ID
            String moduleName = moduleData.getArtifactId().replace('-', '.');
            if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
            {
                moduleName = "m." + moduleName;
            }

            // Create test module-info.java
            createTestModuleInfo(moduleTestSrcDir, moduleName, moduleData);

            // Create main application class if needed
            if (moduleData.isWebApplication())
            {
                createWebApplication(moduleSrcDir, moduleData);
            }
            else
            {
                createBasicApplication(moduleSrcDir, moduleData);
            }

            // Create database module if needed
            if (moduleData.isDatabaseApplication())
            {
                createDatabaseModule(moduleSrcDir, moduleData);
            }

            // Create RabbitMQ consumer if needed
            if (moduleData.isRabbitMQSupport())
            {
                createRabbitMQConsumer(moduleSrcDir, moduleData);
            }

            // Don't create scan module inclusions file during app creation
            // createScanModuleInclusions(moduleSrcDir, moduleData);
        }

        // Create JLink module if needed
        if (myWizardData.isJlinkPackaging())
        {
            createJLinkModule(baseDir);
        }
    }

    private void createPomXml(File baseDir) throws IOException
    {
        Map<String, String> variables = new HashMap<>();
        variables.put("GROUP_ID", myWizardData.getGroupId());
        variables.put("ARTIFACT_ID", myWizardData.getArtifactId());
        variables.put("VERSION", myWizardData.getVersion());

        StringBuilder dependencies = new StringBuilder();
        dependencies.append("        <dependency>\n" +
                "            <groupId>com.guicedee</groupId>\n" +
                "            <artifactId>guice-injection</artifactId>\n" +
                "        </dependency>\n");
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>com.guicedee</groupId>\n");
        dependencies.append("            <artifactId>guiced-vertx</artifactId>\n");
        dependencies.append("        </dependency>\n");
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>org.projectlombok</groupId>\n");
        dependencies.append("            <artifactId>lombok</artifactId>\n");
        dependencies.append("        </dependency>\n");

        // Create a temporary module data object to use the same logic as in createModulePom
        GuicedEEProjectWizardData.ModuleData tempModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        tempModule.setWebReactive(myWizardData.isWebApplication());
        tempModule.setDatabase(myWizardData.isDatabaseApplication());
        if (myWizardData.isDatabaseApplication())
        {
            tempModule.setDatabasePersistence(true);
        }
        tempModule.setMessaging(myWizardData.isRabbitMQSupport());
        tempModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());

        // Web Reactive dependencies
        if (tempModule.isWebReactive())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>com.guicedee</groupId>\n");
            dependencies.append("            <artifactId>guiced-vertx-web</artifactId>\n");
            dependencies.append("        </dependency>\n");

            // Add REST dependency if web application is selected
            // This is needed because the old feature groups don't have a specific REST option
            if (myWizardData.isWebApplication())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-rest</artifactId>\n");
                dependencies.append("        </dependency>\n");

                // Set the REST option for the module-info.java file
                tempModule.setWebReactiveRest(true);
            }
        }

        // Database dependencies
        if (tempModule.isDatabase())
        {
            // Check if JDBC is selected
            if (tempModule.isDatabaseJDBC()) {
                // For JDBC, use a different dependency
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee.persistence</groupId>\n");
                dependencies.append("            <artifactId>guiced-persistence</artifactId>\n");
                dependencies.append("        </dependency>\n");
            } else {
                // For other database types, use the vertxpersistence module
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-vertx-persistence</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // Messaging dependencies
        if (tempModule.isMessaging())
        {
            if (tempModule.isMessagingRabbitMQ())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-rabbit</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // Always add JUnit Jupiter API dependency for tests
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>org.junit.jupiter</groupId>\n");
        dependencies.append("            <artifactId>junit-jupiter-api</artifactId>\n");
        dependencies.append("            <scope>test</scope>\n");
        dependencies.append("        </dependency>\n");

        // Add Test Containers dependency if selected
        if (tempModule.isTestsTestContainers())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>com.guicedee.services</groupId>\n");
            dependencies.append("            <artifactId>testcontainers</artifactId>\n");
            dependencies.append("        </dependency>\n");
        }

        // Add JUnit Vintage Engine dependency if database is selected
        if (tempModule.isDatabase())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>org.junit.vintage</groupId>\n");
            dependencies.append("            <artifactId>junit-vintage-engine</artifactId>\n");
            dependencies.append("            <scope>test</scope>\n");
            dependencies.append("        </dependency>\n");

            // Add Vertx Mutiny and Mutiny dependencies
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>com.guicedee.services</groupId>\n");
            dependencies.append("            <artifactId>vertx-mutiny</artifactId>\n");
            dependencies.append("        </dependency>\n");

            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>io.smallrye.reactive</groupId>\n");
            dependencies.append("            <artifactId>mutiny</artifactId>\n");
            dependencies.append("            <version>2.8.0</version>\n");
            dependencies.append("        </dependency>\n");
        }

        variables.put("DEPENDENCIES", dependencies.toString());

        // Add optional information if provided
        StringBuilder description = new StringBuilder();
        if (!myWizardData.getDescription().isEmpty())
        {
            description.append("    <description>").append(myWizardData.getDescription()).append("</description>\n");
        }

        StringBuilder url = new StringBuilder();
        if (!myWizardData.getUrl().isEmpty())
        {
            url.append("    <url>").append(myWizardData.getUrl()).append("</url>\n");
        }

        StringBuilder scm = new StringBuilder();
        if (!myWizardData.getScmConnection().isEmpty() || !myWizardData.getScmDeveloperConnection().isEmpty() || !myWizardData.getScmUrl().isEmpty())
        {
            scm.append("    <scm>\n");
            if (!myWizardData.getScmConnection().isEmpty())
            {
                scm.append("        <connection>").append(myWizardData.getScmConnection()).append("</connection>\n");
            }
            if (!myWizardData.getScmDeveloperConnection().isEmpty())
            {
                scm.append("        <developerConnection>").append(myWizardData.getScmDeveloperConnection()).append("</developerConnection>\n");
            }
            if (!myWizardData.getScmUrl().isEmpty())
            {
                scm.append("        <url>").append(myWizardData.getScmUrl()).append("</url>\n");
            }
            scm.append("    </scm>\n");
        }

        StringBuilder developers = new StringBuilder();
        if (!myWizardData.getDeveloperName().isEmpty() || !myWizardData.getDeveloperEmail().isEmpty() ||
                !myWizardData.getDeveloperOrganization().isEmpty() || !myWizardData.getDeveloperOrganizationUrl().isEmpty())
        {
            developers.append("    <developers>\n");
            developers.append("        <developer>\n");
            if (!myWizardData.getDeveloperName().isEmpty())
            {
                developers.append("            <name>").append(myWizardData.getDeveloperName()).append("</name>\n");
            }
            if (!myWizardData.getDeveloperEmail().isEmpty())
            {
                developers.append("            <email>").append(myWizardData.getDeveloperEmail()).append("</email>\n");
            }
            if (!myWizardData.getDeveloperOrganization().isEmpty())
            {
                developers.append("            <organization>").append(myWizardData.getDeveloperOrganization()).append("</organization>\n");
            }
            if (!myWizardData.getDeveloperOrganizationUrl().isEmpty())
            {
                developers.append("            <organizationUrl>").append(myWizardData.getDeveloperOrganizationUrl()).append("</organizationUrl>\n");
            }
            developers.append("        </developer>\n");
            developers.append("    </developers>\n");
        }

        String pomXml = getPomXmlTemplate().replace("${GROUP_ID}", myWizardData.getGroupId())
                .replace("${ARTIFACT_ID}", myWizardData.getArtifactId())
                .replace("${VERSION}", myWizardData.getVersion())
                .replace("${DESCRIPTION}", description.toString())
                .replace("${URL}", url.toString())
                .replace("${SCM}", scm.toString())
                .replace("${DEVELOPERS}", developers.toString())
                .replace("${DEPENDENCIES}", dependencies.toString());

        FileUtil.writeToFile(new File(baseDir, "pom.xml"), pomXml);
    }

    private void createModuleInfo(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        StringBuilder requires = new StringBuilder();
        requires.append("requires transitive com.guicedee.guicedinjection;\n");

        // Web Reactive requirements
        if (moduleData.isWebReactive())
        {
            requires.append("\trequires transitive com.guicedee.vertx.web;\n");

            // Web Reactive sub-options
            if (moduleData.isWebReactiveRest())
            {
                requires.append("\trequires transitive com.guicedee.rest;\n");
            }

            if (moduleData.isWebReactiveSwagger())
            {
                requires.append("\trequires transitive com.guicedee.swagger;\n");
            }
        }

        // Database requirements
        if (moduleData.isDatabase())
        {
            if (moduleData.isDatabaseJDBC()) {
                // For JDBC, use a different requires clause
                requires.append("\trequires transitive com.guicedee.guicedpersistence;\n");
            } else {
                // For other database types, use the vertxpersistence module
                requires.append("\trequires transitive com.guicedee.vertxpersistence;\n");
            }
        }

        // Messaging requirements
        if (moduleData.isMessaging())
        {
            if (moduleData.isMessagingRabbitMQ())
            {
                requires.append("\trequires transitive com.guicedee.rabbit;\n");
            }

            if (moduleData.isMessagingKafka() || moduleData.isMessagingAMQP() || moduleData.isMessagingMQTT())
            {
                requires.append("\trequires io.vertx.kafka.client;\n");
                requires.append("\trequires io.vertx.amqp.client;\n");
                requires.append("\trequires io.vertx.mqtt;\n");
            }
        }

        // Caching requirements
        if (moduleData.isCaching())
        {
            if (moduleData.isCachingHazelcast())
            {
                requires.append("\trequires com.hazelcast.core;\n");
            }

            if (moduleData.isCachingVertxHazelcast())
            {
                requires.append("\trequires io.vertx.hazelcast;\n");
            }
        }

        // MicroProfile requirements
        if (moduleData.isMicroProfile())
        {
            if (moduleData.isMicroProfileHealth())
            {
                requires.append("\trequires io.vertx.health;\n");
            }

            if (moduleData.isMicroProfileTelemetry())
            {
                requires.append("\trequires io.vertx.micrometer;\n");
            }

            if (moduleData.isMicroProfileOpenAPI())
            {
                requires.append("\trequires com.guicedee.services.openapi;\n");
            }

            if (moduleData.isMicroProfileLogging())
            {
                requires.append("\trequires org.apache.logging.log4j;\n");
            }

            if (moduleData.isMicroProfileZipkin())
            {
                requires.append("\trequires io.vertx.zipkin;\n");
            }
        }

        requires.append("\trequires static lombok;\n");


        // Generate a module name based on the artifact ID
        String moduleName = moduleData.getArtifactId().replace('-', '.');
        if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
        {
            moduleName = "m." + moduleName;
        }

        // Create provides statements for service loaders
        StringBuilder provides = new StringBuilder();

        // Add provides statement for IGuiceModule if database module is created
        if (moduleData.isDatabase()) {
            // Determine database type and class name
            String dbType = "Postgres";
            if (moduleData.isDatabaseMySQL()) {
                dbType = "MySql";
            } else if (moduleData.isDatabaseOracle()) {
                dbType = "Oracle";
            } else if (moduleData.isDatabaseDB2()) {
                dbType = "DB2";
            } else if (moduleData.isDatabaseSqlServer()) {
                dbType = "SqlServer";
            } else if (moduleData.isDatabaseCassandra()) {
                dbType = "Cassandra";
            } else if (moduleData.isDatabaseMongoDB()) {
                dbType = "MongoDB";
            } else if (moduleData.isDatabaseJDBC()) {
                dbType = "JDBC";
            }

            // Use DB type in class name - same as in createDatabaseModule
            String dbClassName = dbType + "DBModule";
            String dbFullClassName = myWizardData.getGroupId() + ".db." + dbClassName;
            provides.append("\tprovides com.guicedee.guicedinjection.interfaces.IGuiceModule with " + dbFullClassName + ";\n");
        }

        // Add provides statement for IWebSocketMessageReceiver if WebSockets is selected
        if (moduleData.isWebReactiveWebSockets()) {
            String receiverClassName = "WebSocketActionReceiver"; // Same as in createWebSocketClasses
            String receiverFullClassName = myWizardData.getGroupId() + "." + receiverClassName;
            provides.append("\tprovides com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver with " + receiverFullClassName + ";\n");
        }

        String moduleInfo = getModuleInfoTemplate()
                .replace("${MODULE_NAME}", moduleName)
                .replace("${PACKAGE_NAME}", myWizardData.getGroupId())
                .replace("${REQUIRES}", requires.toString())
                .replace("${PROVIDES}", provides.toString());

        FileUtil.writeToFile(new File(srcDir, "module-info.java"), moduleInfo);
    }

    // Keep the old method for backward compatibility
    private void createModuleInfo(File srcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setWebReactive(myWizardData.isWebApplication());
        defaultModule.setDatabase(myWizardData.isDatabaseApplication());
        defaultModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());
        if (myWizardData.isRabbitMQSupport())
        {
            defaultModule.setMessaging(true);
        }

        createModuleInfo(srcDir, defaultModule);
    }

    private void createTestModuleInfo(File testSrcDir, String moduleName, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        StringBuilder requires = new StringBuilder();

        // Add TestContainers requirement if selected
        if (moduleData.isTestsTestContainers())
        {
            requires.append("\trequires org.testcontainers;\n");
        }

        // Add database requirement if database is selected
        if (moduleData.isDatabase())
        {
            requires.append("\trequires com.guicedee.vertxpersistence;\n");
        }

        // Generate a module name based on the artifact ID with .test suffix
        String testModuleName = moduleName + ".test";

        String testModuleInfo = getTestModuleInfoTemplate()
                .replace("${MODULE_NAME}", testModuleName)
                .replace("${TESTED_MODULE_NAME}", moduleName)
                .replace("${REQUIRES}", requires.toString());

        FileUtil.writeToFile(new File(testSrcDir, "module-info.java"), testModuleInfo);
    }

    private void createTestModuleInfo(File testSrcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setWebReactive(myWizardData.isWebApplication());
        defaultModule.setDatabase(myWizardData.isDatabaseApplication());
        defaultModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());
        if (myWizardData.isRabbitMQSupport())
        {
            defaultModule.setMessaging(true);
        }

        // Generate a module name based on the artifact ID
        String moduleName = myWizardData.getArtifactId().replace('-', '.');
        if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
        {
            moduleName = "m." + moduleName;
        }

        createTestModuleInfo(testSrcDir, moduleName, defaultModule);
    }

    private void createBasicApplication(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        String packagePath = myWizardData.getGroupId().replace('.', '/');
        File packageDir = new File(srcDir, packagePath);
        FileUtil.createDirectory(packageDir);

        // Generate a module name based on the artifact ID
        String moduleName = moduleData.getArtifactId().replace('-', '.');
        if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
        {
            moduleName = "m." + moduleName;
        }

        String mainClass = getBasicApplicationTemplate()
                .replace("${PACKAGE}", myWizardData.getGroupId())
                .replace("${MODULE_NAME}", moduleName);

        FileUtil.writeToFile(new File(packageDir, "Application.java"), mainClass);
    }

    // Keep the old method for backward compatibility
    private void createBasicApplication(File srcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setWebReactive(myWizardData.isWebApplication());
        defaultModule.setDatabase(myWizardData.isDatabaseApplication());
        defaultModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());
        if (myWizardData.isRabbitMQSupport())
        {
            defaultModule.setMessaging(true);
        }

        createBasicApplication(srcDir, defaultModule);
    }

    private void createWebApplication(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        String packagePath = myWizardData.getGroupId().replace('.', '/');
        File packageDir = new File(srcDir, packagePath);
        FileUtil.createDirectory(packageDir);

        // Generate a module name based on the artifact ID
        String moduleName = moduleData.getArtifactId().replace('-', '.');
        if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
        {
            moduleName = "m." + moduleName;
        }

        String mainClass = getWebApplicationTemplate()
                .replace("${PACKAGE}", myWizardData.getGroupId())
                .replace("${MODULE_NAME}", moduleName);

        FileUtil.writeToFile(new File(packageDir, "WebApplication.java"), mainClass);

        // Create REST service if REST is selected
        if (moduleData.isWebReactiveRest())
        {
            createRestService(srcDir, moduleData);
        }

        // Create WebSocket classes if WebSockets is selected
        if (moduleData.isWebReactiveWebSockets())
        {
            createWebSocketClasses(srcDir, moduleData);
        }
    }

    /**
     * Creates a REST service in the resources package.
     *
     * @param srcDir The source directory
     * @param moduleData The module data
     * @throws IOException If an I/O error occurs
     */
    private void createRestService(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        // Check if both database and REST are selected in the same module
        if (moduleData.isDatabase() && moduleData.isWebReactiveRest()) {
            // Check if OpenAPI is also selected
            if (moduleData.isMicroProfileOpenAPI()) {
                // Create REST resource and service with entity and OpenAPI
                createRestResourceWithEntityAndOpenAPI(srcDir, moduleData);
                createRestResourceServiceWithEntity(srcDir, moduleData);
            } else {
                // Create REST resource and service with entity
                createRestResourceWithEntity(srcDir, moduleData);
                createRestResourceServiceWithEntity(srcDir, moduleData);
            }
        } else {
            // Create standard REST service without entity
            createStandardRestService(srcDir, moduleData);
        }
    }

    /**
     * Creates a standard REST service without entity integration.
     *
     * @param srcDir The source directory
     * @param moduleData The module data
     * @throws IOException If an I/O error occurs
     */
    private void createStandardRestService(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        // Create resources package
        String packagePath = myWizardData.getGroupId().replace('.', '/') + "/resources";
        File packageDir = new File(srcDir, packagePath);
        FileUtil.createDirectory(packageDir);

        // Create services package for the service implementation
        String servicesPackagePath = myWizardData.getGroupId().replace('.', '/') + "/resources/services";
        File servicesPackageDir = new File(srcDir, servicesPackagePath);
        FileUtil.createDirectory(servicesPackageDir);

        // Get the REST service template from the file template provider
        String templatePath = "fileTemplates/j2ee/GuicedEERestService.java.ft";
        String restServiceTemplate = FileUtil.loadTextAndClose(
                GuicedEEProjectTemplateBuilder.class.getClassLoader().getResourceAsStream(templatePath));

        // Get the REST service implementation template
        String implTemplatePath = "fileTemplates/j2ee/GuicedEERestServiceImpl.java.ft";
        String restServiceImplTemplate = FileUtil.loadTextAndClose(
                GuicedEEProjectTemplateBuilder.class.getClassLoader().getResourceAsStream(implTemplatePath));

        // Replace placeholders
        String className = "RestResource";
        String path = "resources";
        String serviceImplName = className + "Service";

        String restService = restServiceTemplate
                .replace("${PACKAGE_NAME}", myWizardData.getGroupId() + ".resources")
                .replace("${NAME}", className)
                .replace("${PATH}", path);

        String restServiceImpl = restServiceImplTemplate
                .replace("${PACKAGE_NAME}", myWizardData.getGroupId() + ".resources")
                .replace("${SERVICE_NAME}", serviceImplName)
                .replace("${REST_SERVICE_NAME}", className);

        // Write the files
        FileUtil.writeToFile(new File(packageDir, className + ".java"), restService);
        FileUtil.writeToFile(new File(servicesPackageDir, serviceImplName + ".java"), restServiceImpl);

        System.out.println("[DEBUG_LOG] Standard REST service and implementation created successfully");
    }

    /**
     * Creates a REST resource class with entity integration.
     *
     * @param srcDir The source directory
     * @param moduleData The module data
     * @throws IOException If an I/O error occurs
     */
    private void createRestResourceWithEntity(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        // Create resources package
        String packagePath = myWizardData.getGroupId().replace('.', '/') + "/resources";
        File packageDir = new File(srcDir, packagePath);
        FileUtil.createDirectory(packageDir);

        // Create the REST resource class with entity integration
        String restResourceTemplate = 
            "package " + myWizardData.getGroupId() + ".resources;\n\n" +
            "import " + myWizardData.getGroupId() + ".db.entity.YourEntity;\n" +
            "import " + myWizardData.getGroupId() + ".resources.services.RestResourceService;\n" +
            "import io.swagger.v3.oas.annotations.Operation;\n" +
            "import io.swagger.v3.oas.annotations.Parameter;\n" +
            "import io.swagger.v3.oas.annotations.media.Content;\n" +
            "import io.swagger.v3.oas.annotations.media.Schema;\n" +
            "import io.swagger.v3.oas.annotations.responses.ApiResponse;\n" +
            "import io.swagger.v3.oas.annotations.responses.ApiResponses;\n" +
            "import io.swagger.v3.oas.annotations.tags.Tag;\n" +
            "import io.vertx.core.Future;\n" +
            "import com.google.inject.Inject;\n" +
            "import jakarta.ws.rs.*;\n" +
            "import jakarta.ws.rs.core.MediaType;\n" +
            "import lombok.extern.slf4j.Slf4j;\n\n" +
            "/**\n" +
            " * REST endpoint for managing YourEntity resources.\n" +
            " * Provides CRUD operations for entity management using asynchronous processing.\n" +
            " */\n" +
            "@Path(\"/resources\")\n" +
            "@Produces(MediaType.APPLICATION_JSON)\n" +
            "@Consumes(MediaType.APPLICATION_JSON)\n" +
            "@Tag(name = \"Resource Management\", description = \"Operations for managing YourEntity resources\")\n" +
            "@Slf4j\n" +
            "public class RestResource {\n\n" +
            "    @Inject\n" +
            "    private RestResourceService service;\n\n" +
            "    /**\n" +
            "     * Retrieves a sample integer value.\n" +
            "     *\n" +
            "     * @return Future containing a sample integer value\n" +
            "     */\n" +
            "    @GET\n" +
            "    @Operation(\n" +
            "            summary = \"Get sample data\",\n" +
            "            description = \"Retrieves a sample integer value for testing purposes\"\n" +
            "    )\n" +
            "    @ApiResponses({\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"200\",\n" +
            "                    description = \"Successfully retrieved sample data\",\n" +
            "                    content = @Content(schema = @Schema(implementation = Integer.class))\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"500\",\n" +
            "                    description = \"Internal server error\"\n" +
            "            )\n" +
            "    })\n" +
            "    public Future<Integer> get() {\n" +
            "        return service.getSampleData();\n" +
            "    }\n\n" +
            "    /**\n" +
            "     * Retrieves an entity by its ID.\n" +
            "     *\n" +
            "     * @param id The unique identifier of the entity\n" +
            "     * @return Future containing the requested entity\n" +
            "     */\n" +
            "    @GET\n" +
            "    @Path(\"/{id}\")\n" +
            "    @Operation(\n" +
            "            summary = \"Get entity by ID\",\n" +
            "            description = \"Retrieves a specific entity based on its unique identifier\"\n" +
            "    )\n" +
            "    @ApiResponses({\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"200\",\n" +
            "                    description = \"Entity found\",\n" +
            "                    content = @Content(schema = @Schema(implementation = YourEntity.class))\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"404\",\n" +
            "                    description = \"Entity not found\"\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"500\",\n" +
            "                    description = \"Internal server error\"\n" +
            "            )\n" +
            "    })\n" +
            "    public Future<YourEntity> getById(\n" +
            "            @Parameter(description = \"ID of the entity to retrieve\", required = true)\n" +
            "            @PathParam(\"id\") String id) {\n" +
            "        return service.getDataById(id);\n" +
            "    }\n\n" +
            "    /**\n" +
            "     * Creates a new entity.\n" +
            "     *\n" +
            "     * @param entity The entity to create\n" +
            "     * @return Future containing the created entity\n" +
            "     */\n" +
            "    @POST\n" +
            "    @Operation(\n" +
            "            summary = \"Create new entity\",\n" +
            "            description = \"Creates a new entity with the provided data\"\n" +
            "    )\n" +
            "    @ApiResponses({\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"200\",\n" +
            "                    description = \"Entity created successfully\",\n" +
            "                    content = @Content(schema = @Schema(implementation = YourEntity.class))\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"400\",\n" +
            "                    description = \"Invalid entity data provided\"\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"500\",\n" +
            "                    description = \"Internal server error\"\n" +
            "            )\n" +
            "    })\n" +
            "    public Future<YourEntity> create(\n" +
            "            @Parameter(description = \"Entity to create\", required = true)\n" +
            "            YourEntity entity) {\n" +
            "        return service.createData(entity);\n" +
            "    }\n\n" +
            "    /**\n" +
            "     * Updates an existing entity.\n" +
            "     *\n" +
            "     * @param id The ID of the entity to update\n" +
            "     * @param entity The updated entity data\n" +
            "     * @return Future containing the updated entity\n" +
            "     */\n" +
            "    @PUT\n" +
            "    @Path(\"/{id}\")\n" +
            "    @Operation(\n" +
            "            summary = \"Update existing entity\",\n" +
            "            description = \"Updates an existing entity with the provided data\"\n" +
            "    )\n" +
            "    @ApiResponses({\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"200\",\n" +
            "                    description = \"Entity updated successfully\",\n" +
            "                    content = @Content(schema = @Schema(implementation = YourEntity.class))\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"400\",\n" +
            "                    description = \"Invalid entity data provided\"\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"404\",\n" +
            "                    description = \"Entity not found\"\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"500\",\n" +
            "                    description = \"Internal server error\"\n" +
            "            )\n" +
            "    })\n" +
            "    public Future<YourEntity> update(\n" +
            "            @Parameter(description = \"ID of the entity to update\", required = true)\n" +
            "            @PathParam(\"id\") String id,\n" +
            "            @Parameter(description = \"Updated entity data\", required = true)\n" +
            "            YourEntity entity) {\n" +
            "        return service.updateData(id, entity);\n" +
            "    }\n\n" +
            "    /**\n" +
            "     * Deletes an entity by its ID.\n" +
            "     *\n" +
            "     * @param id The ID of the entity to delete\n" +
            "     * @return Future indicating completion of delete operation\n" +
            "     */\n" +
            "    @DELETE\n" +
            "    @Path(\"/{id}\")\n" +
            "    @Operation(\n" +
            "            summary = \"Delete entity\",\n" +
            "            description = \"Deletes an entity based on its unique identifier\"\n" +
            "    )\n" +
            "    @ApiResponses({\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"204\",\n" +
            "                    description = \"Entity successfully deleted\"\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"404\",\n" +
            "                    description = \"Entity not found\"\n" +
            "            ),\n" +
            "            @ApiResponse(\n" +
            "                    responseCode = \"500\",\n" +
            "                    description = \"Internal server error\"\n" +
            "            )\n" +
            "    })\n" +
            "    public Future<Void> delete(\n" +
            "            @Parameter(description = \"ID of the entity to delete\", required = true)\n" +
            "            @PathParam(\"id\") String id) {\n" +
            "        log.info(\"Deleting resource with ID: {}\", id);\n" +
            "        return service.deleteData(id);\n" +
            "    }\n" +
            "}";

        // Write the file
        FileUtil.writeToFile(new File(packageDir, "RestResource.java"), restResourceTemplate);

        System.out.println("[DEBUG_LOG] REST resource with entity integration created successfully");
    }

    /**
     * Creates a REST resource class with entity integration and OpenAPI support.
     *
     * @param srcDir The source directory
     * @param moduleData The module data
     * @throws IOException If an I/O error occurs
     */
    private void createRestResourceWithEntityAndOpenAPI(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        // The REST resource template is the same for both with and without OpenAPI
        // The difference is in the module-info.java and pom.xml files
        createRestResourceWithEntity(srcDir, moduleData);
    }

    /**
     * Creates a REST resource service class with entity integration.
     *
     * @param srcDir The source directory
     * @param moduleData The module data
     * @throws IOException If an I/O error occurs
     */
    private void createRestResourceServiceWithEntity(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        // Create services package for the service implementation
        String servicesPackagePath = myWizardData.getGroupId().replace('.', '/') + "/resources/services";
        File servicesPackageDir = new File(srcDir, servicesPackagePath);
        FileUtil.createDirectory(servicesPackageDir);

        // Create the REST resource service class with entity integration
        String restResourceServiceTemplate = 
            "package " + myWizardData.getGroupId() + ".resources.services;\n\n" +
            "import " + myWizardData.getGroupId() + ".db.entity.YourEntity;\n" +
            "import com.google.inject.Inject;\n" +
            "import io.smallrye.mutiny.Uni;\n" +
            "import com.google.inject.Provider;\n" +
            "import io.vertx.core.Future;\n" +
            "import lombok.extern.slf4j.Slf4j;\n" +
            "import org.hibernate.reactive.mutiny.Mutiny;\n" +
            "import io.vertx.core.Vertx;\n\n" +
            "@Slf4j\n" +
            "public class RestResourceService {\n\n" +
            "    @Inject\n" +
            "    private Provider<Mutiny.Session> session;\n\n" +
            "    @Inject\n" +
            "    private Vertx vertx;\n\n" +
            "    public Future<Integer> getSampleData() {\n" +
            "        return Future.fromCompletionStage(\n" +
            "                session.get().createNativeQuery(\"SELECT 1\", Integer.class)\n" +
            "                        .getSingleResult()\n" +
            "                        .log()\n" +
            "                        .subscribeAsCompletionStage()\n" +
            "        );\n" +
            "    }\n\n" +
            "    public Future<YourEntity> getDataById(String id) {\n" +
            "        return Future.fromCompletionStage(\n" +
            "                session.get().find(YourEntity.class, id)\n" +
            "                        .subscribeAsCompletionStage()\n" +
            "        );\n" +
            "    }\n\n" +
            "    public Future<YourEntity> createData(YourEntity data) {\n" +
            "        return Future.fromCompletionStage(\n" +
            "                session.get().persist(data)\n" +
            "                        .chain(() -> session.flush())\n" +
            "                        .replaceWith(data)\n" +
            "                        .subscribeAsCompletionStage()\n" +
            "        );\n" +
            "    }\n\n" +
            "    public Future<YourEntity> updateData(String id, YourEntity data) {\n" +
            "        return Future.fromCompletionStage(\n" +
            "                session.get().merge(data)\n" +
            "                        .subscribeAsCompletionStage()\n" +
            "        );\n" +
            "    }\n\n" +
            "    public Future<Void> deleteData(String id) {\n" +
            "        return Future.fromCompletionStage(\n" +
            "                session.get().find(YourEntity.class, id)\n" +
            "                        .chain(entity -> entity != null\n" +
            "                                ? session.remove(entity)\n" +
            "                                : Uni.createFrom().nullItem())\n" +
            "                        .subscribeAsCompletionStage()\n" +
            "        );\n" +
            "    }\n" +
            "}";

        // Write the file
        FileUtil.writeToFile(new File(servicesPackageDir, "RestResourceService.java"), restResourceServiceTemplate);

        System.out.println("[DEBUG_LOG] REST resource service with entity integration created successfully");
    }

    /**
     * Creates WebSocket classes.
     *
     * @param srcDir The source directory
     * @param moduleData The module data
     * @throws IOException If an I/O error occurs
     */
    private void createWebSocketClasses(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        System.out.println("[DEBUG_LOG] Creating WebSocket classes");

        // Create package
        String packagePath = myWizardData.getGroupId().replace('.', '/');
        File packageDir = new File(srcDir, packagePath);
        FileUtil.createDirectory(packageDir);

        // Create WebSocket Action Receiver only (not the channel)
        String receiverTemplatePath = "fileTemplates/j2ee/GuicedEEWebSocketMessageReceiver.java.ft";
        String receiverTemplate = FileUtil.loadTextAndClose(
                GuicedEEProjectTemplateBuilder.class.getClassLoader().getResourceAsStream(receiverTemplatePath));

        String receiverClassName = "WebSocketActionReceiver";
        String receiver = receiverTemplate
                .replace("${PACKAGE_NAME}", myWizardData.getGroupId())
                .replace("${NAME}", receiverClassName);

        FileUtil.writeToFile(new File(packageDir, receiverClassName + ".java"), receiver);

        // Create service loader file in META-INF/services
        // Derive the resources directory from the srcDir
        File resourcesDir = new File(srcDir.getParentFile(), "resources");
        File metaInfServicesDir = new File(resourcesDir, "META-INF/services");
        FileUtil.createDirectory(metaInfServicesDir);

        // Create the service loader file
        File serviceFile = new File(metaInfServicesDir, "com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver");

        // Write the full class name of the implementation to the service loader file
        String fullClassName = myWizardData.getGroupId() + "." + receiverClassName;
        FileUtil.writeToFile(serviceFile, fullClassName);

        System.out.println("[DEBUG_LOG] WebSocket classes created successfully");
    }

    // Keep the old method for backward compatibility
    private void createWebApplication(File srcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setWebReactive(true);
        defaultModule.setDatabase(myWizardData.isDatabaseApplication());
        defaultModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());
        if (myWizardData.isRabbitMQSupport())
        {
            defaultModule.setMessaging(true);
        }

        // Set REST option if web application is selected
        if (myWizardData.isWebApplication())
        {
            defaultModule.setWebReactiveRest(true);
        }

        // For backward compatibility, check if any modules have WebSockets enabled
        for (GuicedEEProjectWizardData.ModuleData module : myWizardData.getModules()) {
            if (module.isWebReactiveWebSockets()) {
                defaultModule.setWebReactiveWebSockets(true);
                break;
            }
        }

        createWebApplication(srcDir, defaultModule);
    }

    private void createDatabaseModule(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        String packagePath = myWizardData.getGroupId().replace('.', '/');
        File packageDir = new File(srcDir, packagePath + "/db");
        FileUtil.createDirectory(packageDir);

        // Create entity package and class
        File entityPackageDir = new File(packageDir, "entity");
        FileUtil.createDirectory(entityPackageDir);
        createEntityClass(entityPackageDir);

        // Determine persistence unit name based on database type
        String persistenceUnit = "postgres";
        String dbType = "Postgres";
        if (moduleData.isDatabaseMySQL()) {
            persistenceUnit = "mysql";
            dbType = "MySql";
        } else if (moduleData.isDatabaseOracle()) {
            persistenceUnit = "oracle";
            dbType = "Oracle";
        } else if (moduleData.isDatabaseDB2()) {
            persistenceUnit = "db2";
            dbType = "DB2";
        } else if (moduleData.isDatabaseSqlServer()) {
            persistenceUnit = "sqlserver";
            dbType = "SqlServer";
        } else if (moduleData.isDatabaseCassandra()) {
            persistenceUnit = "cassandra";
            dbType = "Cassandra";
        } else if (moduleData.isDatabaseMongoDB()) {
            persistenceUnit = "mongodb";
            dbType = "MongoDB";
        } else if (moduleData.isDatabaseJDBC()) {
            persistenceUnit = "jdbc";
            dbType = "JDBC";
        }

        // Use DB type in class name
        String className = dbType + "DBModule";

        // Count how many database modules exist in the project
        int databaseModuleCount = 0;
        for (GuicedEEProjectWizardData.ModuleData module : myWizardData.getModules()) {
            if (module.isDatabase()) {
                databaseModuleCount++;
            }
        }

        // Determine if we need to add the EntityManager annotation with a specific value
        String entityManagerAnnotation = "";
        if (databaseModuleCount > 1) {
            entityManagerAnnotation = "@com.guicedee.vertxpersistence.annotations.EntityManager(\"" + persistenceUnit + "_1\")\n";
        }

        String dbModule = getDatabaseModuleTemplate(moduleData)
                .replace("${PACKAGE}", myWizardData.getGroupId() + ".db")
                .replace("${CLASS_NAME}", className)
                .replace("${PERSISTENCE_UNIT}", persistenceUnit)
                .replace("${ENTITY_MANAGER_ANNOTATION}", entityManagerAnnotation);

        FileUtil.writeToFile(new File(packageDir, className + ".java"), dbModule);

        // Create persistence.xml file in META-INF directory
        File resourcesDir = new File(srcDir.getParentFile(), "resources");
        createPersistenceXml(resourcesDir, persistenceUnit, myWizardData.getGroupId() + ".db.entity.YourEntity");

        // Create service loader file in META-INF/services for IGuiceModule
        File metaInfServicesDir = new File(resourcesDir, "META-INF/services");
        FileUtil.createDirectory(metaInfServicesDir);

        // Create the service loader file for IGuiceModule
        File serviceFile = new File(metaInfServicesDir, "com.guicedee.guicedinjection.interfaces.IGuiceModule");

        // Write the full class name of the database module class to the service loader file
        String fullClassName = myWizardData.getGroupId() + ".db." + className;

        // Append to the file if it exists, otherwise create it
        if (serviceFile.exists()) {
            String existingContent = FileUtil.loadFile(serviceFile);
            if (!existingContent.contains(fullClassName)) {
                FileUtil.writeToFile(serviceFile, existingContent + "\n" + fullClassName);
            }
        } else {
            FileUtil.writeToFile(serviceFile, fullClassName);
        }
    }

    /**
     * Creates a simple entity class in the entity package.
     *
     * @param entityPackageDir The entity package directory
     * @throws IOException If an I/O error occurs
     */
    private void createEntityClass(File entityPackageDir) throws IOException
    {
        String entityClass = "package " + myWizardData.getGroupId() + ".db.entity;\n\n" +
                "public class YourEntity\n" +
                "{\n" +
                "}\n";

        FileUtil.writeToFile(new File(entityPackageDir, "YourEntity.java"), entityClass);
    }

    // Keep the old method for backward compatibility
    private void createDatabaseModule(File srcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setDatabase(true);
        defaultModule.setDatabasePersistence(true);
        defaultModule.setWebReactive(myWizardData.isWebApplication());
        defaultModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());
        if (myWizardData.isRabbitMQSupport())
        {
            defaultModule.setMessaging(true);
        }

        createDatabaseModule(srcDir, defaultModule);
    }

    private void createRabbitMQConsumer(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        String packagePath = myWizardData.getGroupId().replace('.', '/');

        // Create package-info.java in the base package
        File basePackageDir = new File(srcDir, packagePath);
        FileUtil.createDirectory(basePackageDir);

        String packageInfo = getRabbitPackageInfoTemplate()
                .replace("${PACKAGE}", myWizardData.getGroupId());

        FileUtil.writeToFile(new File(basePackageDir, "package-info.java"), packageInfo);

        // Create RabbitMQ consumer in the rabbit subpackage
        File packageDir = new File(srcDir, packagePath + "/rabbit");
        FileUtil.createDirectory(packageDir);

        String consumer = getRabbitMQConsumerTemplate()
                .replace("${PACKAGE}", myWizardData.getGroupId() + ".rabbit");

        FileUtil.writeToFile(new File(packageDir, "ExampleConsumer.java"), consumer);
    }

    // Keep the old method for backward compatibility
    private void createRabbitMQConsumer(File srcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setMessaging(true);
        defaultModule.setMessagingRabbitMQ(true);
        defaultModule.setWebReactive(myWizardData.isWebApplication());
        defaultModule.setDatabase(myWizardData.isDatabaseApplication());
        if (myWizardData.isDatabaseApplication())
        {
            defaultModule.setDatabasePersistence(true);
        }

        createRabbitMQConsumer(srcDir, defaultModule);
    }

    private void createScanModuleInclusions(File srcDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        String packagePath = myWizardData.getGroupId().replace('.', '/');
        File implDir = new File(srcDir, packagePath + "/impl");
        FileUtil.createDirectory(implDir);

        // Generate a class name following Java naming conventions
        String artifactIdBase = moduleData.getArtifactId().replace("-", "").replace(".", "");
        // Capitalize the first letter of the artifact ID
        String capitalizedArtifactId = Character.toUpperCase(artifactIdBase.charAt(0)) + artifactIdBase.substring(1);
        String className = capitalizedArtifactId + "ScanInclude";
        if (!Character.isJavaIdentifierStart(className.charAt(0)))
        {
            className = "App" + className;
        }

        // Generate a module name based on the artifact ID
        String moduleName = moduleData.getArtifactId().replace('-', '.');
        if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
        {
            moduleName = "m." + moduleName;
        }

        // Read the template file
        String templatePath = "fileTemplates/j2ee/GuicedEEScanModuleInclusions.java.ft";
        String scanInclude = FileUtil.loadTextAndClose(
                GuicedEEProjectTemplateBuilder.class.getClassLoader().getResourceAsStream(templatePath));

        // Replace placeholders
        scanInclude = scanInclude.replace("${PACKAGE_NAME}", myWizardData.getGroupId())
                .replace("${NAME}", className)
                .replace("${MODULE_NAME}", moduleName)
                .replace("${ARTIFACT_ID}", moduleData.getArtifactId())
                .replace("${GROUP_ID}", myWizardData.getGroupId())
                .replace("${VERSION}", myWizardData.getVersion());

        FileUtil.writeToFile(new File(implDir, className + ".java"), scanInclude);

        // Create service loader file in META-INF/services
        // Derive the resources directory from the srcDir
        File resourcesDir = new File(srcDir.getParentFile(), "resources");
        File metaInfServicesDir = new File(resourcesDir, "META-INF/services");
        FileUtil.createDirectory(metaInfServicesDir);

        // Create the service loader file
        File serviceFile = new File(metaInfServicesDir, "com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions");

        // Write the full class name of the implementation to the service loader file
        String fullClassName = myWizardData.getGroupId() + ".impl." + className;
        FileUtil.writeToFile(serviceFile, fullClassName);
    }

    // Keep the old method for backward compatibility
    private void createScanModuleInclusions(File srcDir) throws IOException
    {
        GuicedEEProjectWizardData.ModuleData defaultModule = new GuicedEEProjectWizardData.ModuleData(
                "Default Module", myWizardData.getArtifactId());

        // Map old feature groups to new ones
        defaultModule.setWebReactive(myWizardData.isWebApplication());
        defaultModule.setDatabase(myWizardData.isDatabaseApplication());
        if (myWizardData.isDatabaseApplication())
        {
            defaultModule.setDatabasePersistence(true);
        }
        defaultModule.setMessaging(myWizardData.isRabbitMQSupport());
        defaultModule.setMessagingRabbitMQ(myWizardData.isRabbitMQSupport());

        createScanModuleInclusions(srcDir, defaultModule);
    }

    private String getPomXmlTemplate()
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <groupId>${GROUP_ID}</groupId>\n" +
                "    <artifactId>${ARTIFACT_ID}</artifactId>\n" +
                "    <version>${VERSION}</version>\n" +
                "${DESCRIPTION}" +
                "${URL}" +
                "\n" +
                "${SCM}" +
                "${DEVELOPERS}" +
                "\n" +
                "    <properties>\n" +
                "        <maven.compiler.release>24</maven.compiler.release>\n" +
                "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "        <guicedee.version>2.0.0-SNAPSHOT</guicedee.version>\n" +
                "    </properties>\n" +
                "\n" +
                "    <dependencyManagement>\n" +
                "        <dependencies>\n" +
                "            <dependency>\n" +
                "                <groupId>com.guicedee</groupId>\n" +
                "                <artifactId>guicedee-bom</artifactId>\n" +
                "                <version>${guicedee.version}</version>\n" +
                "                <scope>import</scope>\n" +
                "                <type>pom</type>\n" +
                "            </dependency>\n" +
                "            <dependency>\n" +
                "                <groupId>com.guicedee</groupId>\n" +
                "                <artifactId>tests-bom</artifactId>\n" +
                "                <version>${guicedee.version}</version>\n" +
                "                <scope>import</scope>\n" +
                "                <type>pom</type>\n" +
                "            </dependency>\n" +
                "        </dependencies>\n" +
                "    </dependencyManagement>\n" +
                "\n" +
                "    <dependencies>\n" +
                "${DEPENDENCIES}" +
                "    </dependencies>\n" +
                "</project>";
    }

    private String getModuleInfoTemplate()
    {
        return "module ${MODULE_NAME} {\n" +
                "    ${REQUIRES}\n" +
                "    \n" +
                "    exports ${PACKAGE_NAME};\n" +
                "    \n" +
                "    ${PROVIDES}\n" +
                "}";
    }

    private String getTestModuleInfoTemplate()
    {
        return "module ${MODULE_NAME} {\n" +
                "    requires transitive ${TESTED_MODULE_NAME};\n" +
                "    requires com.guicedee.client;\n" +
                "    requires org.junit.jupiter.api;\n" +
                "    ${REQUIRES}\n" +
                "}";
    }

    private String getBasicApplicationTemplate()
    {
        return "package ${PACKAGE};\n" +
                "\n" +
                "import com.guicedee.client.IGuiceContext;\n" +
                "import com.guicedee.vertx.spi.VertX;\n" +
                "import com.guicedee.vertx.spi.Verticle;\n" +
                "\n" +
                "@VertX(blockedThreadCheckInterval = 10000) //optional configuration\n" +
                "@Verticle(workerPoolName = \"my-app\") //optional configuration\n" +
                "public class Application {\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        IGuiceContext.registerModule(\"${MODULE_NAME}\");\n" +
                "        IGuiceContext.instance().inject();\n" +
                "    }\n" +
                "}";
    }

    private String getWebApplicationTemplate()
    {
        return "package ${PACKAGE};\n" +
                "\n" +
                "import com.guicedee.client.IGuiceContext;\n" +
                "import com.guicedee.vertx.spi.VertX;\n" +
                "import com.guicedee.vertx.spi.Verticle;\n" +
                "\n" +
                "@VertX(blockedThreadCheckInterval = 10000) //optional configuration\n" +
                "@Verticle(workerPoolName = \"my-web-app\") //optional configuration\n" +
                "public class WebApplication {\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        System.setProperty(\"HTTP_ENABLED\", \"true\");\n" +
                "        System.setProperty(\"HTTPS_ENABLED\", \"false\");\n" +
                "\n" +
                "        //System.setProperty(\"HTTPS_KEYSTORE\", \"\");\n" +
                "        //System.setProperty(\"HTTPS_KEYSTORE_PASSWORD\", \"\");\n" +
                "\n" +
                "        IGuiceContext.registerModule(\"${MODULE_NAME}\");\n" +
                "        IGuiceContext.instance().inject();\n" +
                "    }\n" +
                "}";
    }

    private String getDatabaseModuleTemplate(GuicedEEProjectWizardData.ModuleData moduleData)
    {
        // If JDBC is selected, use a different template
        if (moduleData.isDatabaseJDBC()) {
            return getJdbcDatabaseModuleTemplate();
        }

        // Determine which database type is selected
        String connectionInfoType = "ConnectionBaseInfo";
        String importStatement = "";

        if (moduleData.isDatabasePostgreSQL()) {
            connectionInfoType = "PostgresConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.postgres.PostgresConnectionBaseInfo;\n";
        } else if (moduleData.isDatabaseMySQL()) {
            connectionInfoType = "MySQLConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.mysql.MySQLConnectionBaseInfo;\n";
        } else if (moduleData.isDatabaseOracle()) {
            connectionInfoType = "OracleConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.oracle.OracleConnectionBaseInfo;\n";
        } else if (moduleData.isDatabaseDB2()) {
            connectionInfoType = "DB2ConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.db2.DB2ConnectionBaseInfo;\n";
        } else if (moduleData.isDatabaseSqlServer()) {
            connectionInfoType = "SqlServerConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.sqlserver.SqlServerConnectionBaseInfo;\n";
        } else if (moduleData.isDatabaseCassandra()) {
            connectionInfoType = "CassandraConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.cassandra.CassandraConnectionBaseInfo;\n";
        } else if (moduleData.isDatabaseMongoDB()) {
            connectionInfoType = "MongoDBConnectionBaseInfo";
            importStatement = "import com.guicedee.vertxpersistence.implementations.mongodb.MongoDBConnectionBaseInfo;\n";
        }

        return "package ${PACKAGE};\n" +
                "\n" +
                "import com.guicedee.vertxpersistence.ConnectionBaseInfo;\n" +
                "import com.guicedee.vertxpersistence.DatabaseModule;\n" +
                "import com.guicedee.guicedinjection.interfaces.IGuiceModule;\n" +
                "import com.guicedee.vertxpersistence.annotations.EntityManager;\n" +
                importStatement +
                "import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;\n" +
                "\n" +
                "import java.util.Properties;\n" +
                "\n" +
                "import static com.guicedee.client.Environment.getSystemPropertyOrEnvironment;\n" +
                "\n" +
                "${ENTITY_MANAGER_ANNOTATION}" +
                "public class ${CLASS_NAME} extends DatabaseModule<${CLASS_NAME}> {\n" +
                "    \n" +
                "    @Override\n" +
                "    protected String getPersistenceUnitName() {\n" +
                "        return \"${PERSISTENCE_UNIT}\";\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected ConnectionBaseInfo getConnectionBaseInfo(ParsedPersistenceXmlDescriptor unit, Properties filteredProperties) {\n" +
                "        " + connectionInfoType + " connectionInfo = new " + connectionInfoType + "();\n" +
                "        connectionInfo.setUrl(getSystemPropertyOrEnvironment(\"DB_URL\",\"jdbc:postgresql://localhost:5432/postgres\"));\n" +
                "        //or\n" +
                "        connectionInfo.setServerName(getSystemPropertyOrEnvironment(\"DB_SERVER_NAME\",\"localhost\"));\n" +
                "        connectionInfo.setDatabaseName(getSystemPropertyOrEnvironment(\"DB_SERVER_DATABASE\",\"postgres\"));\n" +
                "        connectionInfo.setPort(getSystemPropertyOrEnvironment(\"DB_SERVER_PORT\",\"5432\"));\n" +
                "        connectionInfo.setUsername(getSystemPropertyOrEnvironment(\"DB_SERVER_USER\",\"postgres\"));\n" +
                "        connectionInfo.setPassword(getSystemPropertyOrEnvironment(\"DB_SERVER_PASSWORD\",\"\"));\n" +
                "        return connectionInfo;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected String getJndiMapping() {\n" +
                "        return \"jdbc/${PERSISTENCE_UNIT}\";\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public Integer sortOrder() {\n" +
                "        return 150;\n" +
                "    }\n" +
                "}";
    }

    private String getJdbcDatabaseModuleTemplate()
    {
        return "package ${PACKAGE};\n" +
                "\n" +
                "import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;\n" +
                "import com.guicedee.guicedpersistence.db.DatabaseModule;\n" +
                "import com.guicedee.guicedinjection.interfaces.IGuiceModule;\n" +
                "import com.guicedee.vertxpersistence.annotations.EntityManager;\n" +
                "import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;\n" +
                "\n" +
                "import javax.sql.DataSource;\n" +
                "import java.util.Properties;\n" +
                "\n" +
                "${ENTITY_MANAGER_ANNOTATION}" +
                "public class ${CLASS_NAME} extends DatabaseModule<${CLASS_NAME}> {\n" +
                "    \n" +
                "    @Override\n" +
                "    protected String getPersistenceUnitName() {\n" +
                "        return \"${PERSISTENCE_UNIT}\";\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected ConnectionBaseInfo getConnectionBaseInfo(ParsedPersistenceXmlDescriptor unit, Properties filteredProperties) {\n" +
                "        return new ConnectionBaseInfo() {\n" +
                "            @Override\n" +
                "            public DataSource toPooledDatasource() {\n" +
                "                return null;\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected String getJndiMapping() {\n" +
                "        return \"jdbc/${PERSISTENCE_UNIT}\";\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public Integer sortOrder() {\n" +
                "        return 20;\n" +
                "    }\n" +
                "}";
    }

    private void createParentPom(File baseDir) throws IOException
    {
        // Add optional information if provided
        StringBuilder description = new StringBuilder();
        if (!myWizardData.getDescription().isEmpty())
        {
            description.append("    <description>").append(myWizardData.getDescription()).append("</description>\n");
        }

        StringBuilder url = new StringBuilder();
        if (!myWizardData.getUrl().isEmpty())
        {
            url.append("    <url>").append(myWizardData.getUrl()).append("</url>\n");
        }

        StringBuilder scm = new StringBuilder();
        if (!myWizardData.getScmConnection().isEmpty() || !myWizardData.getScmDeveloperConnection().isEmpty() || !myWizardData.getScmUrl().isEmpty())
        {
            scm.append("    <scm>\n");
            if (!myWizardData.getScmConnection().isEmpty())
            {
                scm.append("        <connection>").append(myWizardData.getScmConnection()).append("</connection>\n");
            }
            if (!myWizardData.getScmDeveloperConnection().isEmpty())
            {
                scm.append("        <developerConnection>").append(myWizardData.getScmDeveloperConnection()).append("</developerConnection>\n");
            }
            if (!myWizardData.getScmUrl().isEmpty())
            {
                scm.append("        <url>").append(myWizardData.getScmUrl()).append("</url>\n");
            }
            scm.append("    </scm>\n");
        }

        StringBuilder developers = new StringBuilder();
        if (!myWizardData.getDeveloperName().isEmpty() || !myWizardData.getDeveloperEmail().isEmpty() ||
                !myWizardData.getDeveloperOrganization().isEmpty() || !myWizardData.getDeveloperOrganizationUrl().isEmpty())
        {
            developers.append("    <developers>\n");
            developers.append("        <developer>\n");
            if (!myWizardData.getDeveloperName().isEmpty())
            {
                developers.append("            <name>").append(myWizardData.getDeveloperName()).append("</name>\n");
            }
            if (!myWizardData.getDeveloperEmail().isEmpty())
            {
                developers.append("            <email>").append(myWizardData.getDeveloperEmail()).append("</email>\n");
            }
            if (!myWizardData.getDeveloperOrganization().isEmpty())
            {
                developers.append("            <organization>").append(myWizardData.getDeveloperOrganization()).append("</organization>\n");
            }
            if (!myWizardData.getDeveloperOrganizationUrl().isEmpty())
            {
                developers.append("            <organizationUrl>").append(myWizardData.getDeveloperOrganizationUrl()).append("</organizationUrl>\n");
            }
            developers.append("        </developer>\n");
            developers.append("    </developers>\n");
        }

        String parentPom = getParentPomTemplate()
                .replace("${GROUP_ID}", myWizardData.getGroupId())
                .replace("${ARTIFACT_ID}", myWizardData.getArtifactId() + "-parent")
                .replace("${VERSION}", myWizardData.getVersion())
                .replace("${DESCRIPTION}", description.toString())
                .replace("${URL}", url.toString())
                .replace("${SCM}", scm.toString())
                .replace("${DEVELOPERS}", developers.toString());

        FileUtil.writeToFile(new File(baseDir, "pom.xml"), parentPom);
    }

    private void createModulePom(File moduleDir, GuicedEEProjectWizardData.ModuleData moduleData) throws IOException
    {
        Map<String, String> variables = new HashMap<>();
        variables.put("GROUP_ID", myWizardData.getGroupId());
        variables.put("ARTIFACT_ID", moduleData.getArtifactId());
        variables.put("VERSION", myWizardData.getVersion());
        variables.put("PARENT_ARTIFACT_ID", myWizardData.getArtifactId() + "-parent");

        StringBuilder dependencies = new StringBuilder();
        dependencies.append("        <dependency>\n" +
                "            <groupId>com.guicedee</groupId>\n" +
                "            <artifactId>guice-injection</artifactId>\n" +
                "        </dependency>\n");
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>com.guicedee</groupId>\n");
        dependencies.append("            <artifactId>guiced-vertx</artifactId>\n");
        dependencies.append("        </dependency>\n");
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>org.projectlombok</groupId>\n");
        dependencies.append("            <artifactId>lombok</artifactId>\n");
        dependencies.append("        </dependency>\n");

        // Web Reactive dependencies
        if (moduleData.isWebReactive())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>com.guicedee</groupId>\n");
            dependencies.append("            <artifactId>guiced-vertx-web</artifactId>\n");
            dependencies.append("        </dependency>\n");

            // Web Reactive sub-options
            if (moduleData.isWebReactiveRest())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-rest</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isWebReactiveSwagger())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-swagger</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // Database dependencies
        if (moduleData.isDatabase())
        {
            if (moduleData.isDatabaseJDBC()) {
                // For JDBC, use a different dependency
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee.persistence</groupId>\n");
                dependencies.append("            <artifactId>guiced-persistence</artifactId>\n");
                dependencies.append("        </dependency>\n");
            } else {
                // For other database types, use the vertxpersistence module
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-vertx-persistence</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            // Database sub-options
            if (moduleData.isDatabasePostgreSQL())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>org.postgresql</groupId>\n");
                dependencies.append("            <artifactId>postgresql</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isDatabaseMySQL())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>mysql</groupId>\n");
                dependencies.append("            <artifactId>mysql-connector-java</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isDatabaseOracle())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.oracle.database.jdbc</groupId>\n");
                dependencies.append("            <artifactId>ojdbc8</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isDatabaseDB2())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.ibm.db2</groupId>\n");
                dependencies.append("            <artifactId>jcc</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isDatabaseSqlServer())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.microsoft.sqlserver</groupId>\n");
                dependencies.append("            <artifactId>mssql-jdbc</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isDatabaseCassandra())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.datastax.cassandra</groupId>\n");
                dependencies.append("            <artifactId>cassandra-driver-core</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isDatabaseMongoDB())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>org.mongodb</groupId>\n");
                dependencies.append("            <artifactId>mongodb-driver-sync</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // Messaging dependencies
        if (moduleData.isMessaging())
        {
            if (moduleData.isMessagingRabbitMQ())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee</groupId>\n");
                dependencies.append("            <artifactId>guiced-rabbit</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMessagingKafka())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-kafka-client</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMessagingAMQP())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-amqp-client</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMessagingMQTT())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-mqtt</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // Caching dependencies
        if (moduleData.isCaching())
        {
            if (moduleData.isCachingHazelcast())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee.services</groupId>\n");
                dependencies.append("            <artifactId>hazelcast</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isCachingVertxHazelcast())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-hazelcast</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // MicroProfile dependencies
        if (moduleData.isMicroProfile())
        {
            if (moduleData.isMicroProfileHealth())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-health-check</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMicroProfileTelemetry())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-micrometer-metrics</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMicroProfileOpenAPI())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>com.guicedee.services</groupId>\n");
                dependencies.append("            <artifactId>openapi</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMicroProfileLogging())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>org.apache.logging.log4j</groupId>\n");
                dependencies.append("            <artifactId>log4j-core</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }

            if (moduleData.isMicroProfileZipkin())
            {
                dependencies.append("        <dependency>\n");
                dependencies.append("            <groupId>io.vertx</groupId>\n");
                dependencies.append("            <artifactId>vertx-zipkin</artifactId>\n");
                dependencies.append("        </dependency>\n");
            }
        }

        // Always add JUnit Jupiter API dependency for tests
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>org.junit.jupiter</groupId>\n");
        dependencies.append("            <artifactId>junit-jupiter-api</artifactId>\n");
        dependencies.append("            <scope>test</scope>\n");
        dependencies.append("        </dependency>\n");

        // Add Test Containers dependency if selected
        if (moduleData.isTestsTestContainers())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>com.guicedee.services</groupId>\n");
            dependencies.append("            <artifactId>testcontainers</artifactId>\n");
            dependencies.append("        </dependency>\n");
        }

        // Add JUnit Vintage Engine dependency if database is selected
        if (moduleData.isDatabase())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>org.junit.vintage</groupId>\n");
            dependencies.append("            <artifactId>junit-vintage-engine</artifactId>\n");
            dependencies.append("            <scope>test</scope>\n");
            dependencies.append("        </dependency>\n");

            // Add Vertx Mutiny and Mutiny dependencies
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>com.guicedee.services</groupId>\n");
            dependencies.append("            <artifactId>vertx-mutiny</artifactId>\n");
            dependencies.append("        </dependency>\n");

            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>io.smallrye.reactive</groupId>\n");
            dependencies.append("            <artifactId>mutiny</artifactId>\n");
            dependencies.append("            <version>2.8.0</version>\n");
            dependencies.append("        </dependency>\n");
        }

        variables.put("DEPENDENCIES", dependencies.toString());

        String packaging = myWizardData.isJmodPackaging() ? "jmod" : "jar";
        variables.put("PACKAGING", packaging);

        // Add optional information if provided
        StringBuilder description = new StringBuilder();
        if (!myWizardData.getDescription().isEmpty())
        {
            description.append("    <description>").append(myWizardData.getDescription()).append("</description>\n");
        }

        StringBuilder url = new StringBuilder();
        if (!myWizardData.getUrl().isEmpty())
        {
            url.append("    <url>").append(myWizardData.getUrl()).append("</url>\n");
        }

        StringBuilder scm = new StringBuilder();
        if (!myWizardData.getScmConnection().isEmpty() || !myWizardData.getScmDeveloperConnection().isEmpty() || !myWizardData.getScmUrl().isEmpty())
        {
            scm.append("    <scm>\n");
            if (!myWizardData.getScmConnection().isEmpty())
            {
                scm.append("        <connection>").append(myWizardData.getScmConnection()).append("</connection>\n");
            }
            if (!myWizardData.getScmDeveloperConnection().isEmpty())
            {
                scm.append("        <developerConnection>").append(myWizardData.getScmDeveloperConnection()).append("</developerConnection>\n");
            }
            if (!myWizardData.getScmUrl().isEmpty())
            {
                scm.append("        <url>").append(myWizardData.getScmUrl()).append("</url>\n");
            }
            scm.append("    </scm>\n");
        }

        StringBuilder developers = new StringBuilder();
        if (!myWizardData.getDeveloperName().isEmpty() || !myWizardData.getDeveloperEmail().isEmpty() ||
                !myWizardData.getDeveloperOrganization().isEmpty() || !myWizardData.getDeveloperOrganizationUrl().isEmpty())
        {
            developers.append("    <developers>\n");
            developers.append("        <developer>\n");
            if (!myWizardData.getDeveloperName().isEmpty())
            {
                developers.append("            <name>").append(myWizardData.getDeveloperName()).append("</name>\n");
            }
            if (!myWizardData.getDeveloperEmail().isEmpty())
            {
                developers.append("            <email>").append(myWizardData.getDeveloperEmail()).append("</email>\n");
            }
            if (!myWizardData.getDeveloperOrganization().isEmpty())
            {
                developers.append("            <organization>").append(myWizardData.getDeveloperOrganization()).append("</organization>\n");
            }
            if (!myWizardData.getDeveloperOrganizationUrl().isEmpty())
            {
                developers.append("            <organizationUrl>").append(myWizardData.getDeveloperOrganizationUrl()).append("</organizationUrl>\n");
            }
            developers.append("        </developer>\n");
            developers.append("    </developers>\n");
        }

        String modulePom = getAppModulePomTemplate()
                .replace("${GROUP_ID}", variables.get("GROUP_ID"))
                .replace("${ARTIFACT_ID}", variables.get("ARTIFACT_ID"))
                .replace("${VERSION}", variables.get("VERSION"))
                .replace("${PARENT_ARTIFACT_ID}", variables.get("PARENT_ARTIFACT_ID"))
                .replace("${PACKAGING}", variables.get("PACKAGING"))
                .replace("${DESCRIPTION}", description.toString())
                .replace("${URL}", url.toString())
                .replace("${SCM}", scm.toString())
                .replace("${DEVELOPERS}", developers.toString())
                .replace("${DEPENDENCIES}", variables.get("DEPENDENCIES"));

        FileUtil.writeToFile(new File(moduleDir, "pom.xml"), modulePom);
    }

    private void createJLinkModule(File baseDir) throws IOException
    {
        File jlinkModuleDir = new File(baseDir, "jlink");
        FileUtil.createDirectory(jlinkModuleDir);

        // Determine the main module and class
        String appArtifactId;
        String mainClass;
        String moduleName;

        if (!myWizardData.getModules().isEmpty())
        {
            // Use the first module as the main module
            GuicedEEProjectWizardData.ModuleData mainModule = myWizardData.getModules().get(0);
            appArtifactId = mainModule.getArtifactId();
            mainClass = myWizardData.getGroupId() + "." + (mainModule.isWebApplication() ? "WebApplication" : "Application");

            // Generate module name based on the artifact ID
            moduleName = mainModule.getArtifactId().replace('-', '.');
            if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
            {
                moduleName = "m." + moduleName;
            }
        }
        else
        {
            // Fallback to default values
            appArtifactId = myWizardData.getArtifactId() + "-app";
            mainClass = myWizardData.getGroupId() + "." + (myWizardData.isWebApplication() ? "WebApplication" : "Application");

            // Generate module name based on the artifact ID
            moduleName = appArtifactId.replace('-', '.');
            if (!Character.isJavaIdentifierStart(moduleName.charAt(0)))
            {
                moduleName = "m." + moduleName;
            }
        }

        // Build dependencies for all modules
        StringBuilder dependencies = new StringBuilder();
        for (GuicedEEProjectWizardData.ModuleData moduleData : myWizardData.getModules())
        {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>").append(myWizardData.getGroupId()).append("</groupId>\n");
            dependencies.append("            <artifactId>").append(moduleData.getArtifactId()).append("</artifactId>\n");
            dependencies.append("            <version>").append(myWizardData.getVersion()).append("</version>\n");
            dependencies.append("        </dependency>\n");
        }

        String jlinkPom = getJLinkPomTemplate()
                .replace("${GROUP_ID}", myWizardData.getGroupId())
                .replace("${ARTIFACT_ID}", myWizardData.getArtifactId() + "-jlink")
                .replace("${VERSION}", myWizardData.getVersion())
                .replace("${PARENT_ARTIFACT_ID}", myWizardData.getArtifactId() + "-parent")
                .replace("${APP_ARTIFACT_ID}", appArtifactId)
                .replace("${MODULE_NAME}", moduleName)
                .replace("${MAIN_CLASS}", mainClass)
                .replace("${DEPENDENCIES}", dependencies.toString());

        FileUtil.writeToFile(new File(jlinkModuleDir, "pom.xml"), jlinkPom);
    }

    private String getParentPomTemplate()
    {
        StringBuilder modules = new StringBuilder();
        modules.append("    <modules>\n");

        // Add all modules
        for (GuicedEEProjectWizardData.ModuleData moduleData : myWizardData.getModules())
        {
            String moduleDirName = moduleData.getArtifactId().replace(myWizardData.getArtifactId() + "-", "");
            modules.append("        <module>").append(moduleDirName).append("</module>\n");
        }

        // Add JLink module if needed
        if (myWizardData.isJlinkPackaging())
        {
            modules.append("        <module>jlink</module>\n");
        }

        modules.append("    </modules>\n");

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <groupId>${GROUP_ID}</groupId>\n" +
                "    <artifactId>${ARTIFACT_ID}</artifactId>\n" +
                "    <version>${VERSION}</version>\n" +
                "    <packaging>pom</packaging>\n" +
                "${DESCRIPTION}" +
                "${URL}" +
                "\n" +
                "${SCM}" +
                "${DEVELOPERS}" +
                "\n" +
                "    <properties>\n" +
                "        <maven.compiler.source>24</maven.compiler.source>\n" +
                "        <maven.compiler.target>24</maven.compiler.target>\n" +
                "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "        <guicedee.version>2.0.0-SNAPSHOT</guicedee.version>\n" +
                "    </properties>\n" +
                "\n" +
                modules.toString() +
                "\n" +
                "    <dependencyManagement>\n" +
                "        <dependencies>\n" +
                "            <dependency>\n" +
                "                <groupId>com.guicedee</groupId>\n" +
                "                <artifactId>guicedee-bom</artifactId>\n" +
                "                <version>${guicedee.version}</version>\n" +
                "                <scope>import</scope>\n" +
                "                <type>pom</type>\n" +
                "            </dependency>\n" +
                "            <dependency>\n" +
                "                <groupId>com.guicedee</groupId>\n" +
                "                <artifactId>tests-bom</artifactId>\n" +
                "                <version>${guicedee.version}</version>\n" +
                "                <scope>import</scope>\n" +
                "                <type>pom</type>\n" +
                "            </dependency>\n" +
                "        </dependencies>\n" +
                "    </dependencyManagement>\n" +
                "</project>";
    }

    private String getAppModulePomTemplate()
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <parent>\n" +
                "        <groupId>${GROUP_ID}</groupId>\n" +
                "        <artifactId>${PARENT_ARTIFACT_ID}</artifactId>\n" +
                "        <version>${VERSION}</version>\n" +
                "    </parent>\n" +
                "\n" +
                "    <artifactId>${ARTIFACT_ID}</artifactId>\n" +
                "    <packaging>${PACKAGING}</packaging>\n" +
                "${DESCRIPTION}" +
                "${URL}" +
                "\n" +
                "${SCM}" +
                "${DEVELOPERS}" +
                "\n" +
                "    <dependencies>\n" +
                "${DEPENDENCIES}" +
                "    </dependencies>\n" +
                "</project>";
    }

    private String getJLinkPomTemplate()
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <parent>\n" +
                "        <groupId>${GROUP_ID}</groupId>\n" +
                "        <artifactId>${PARENT_ARTIFACT_ID}</artifactId>\n" +
                "        <version>${VERSION}</version>\n" +
                "    </parent>\n" +
                "\n" +
                "    <artifactId>${ARTIFACT_ID}</artifactId>\n" +
                "    <packaging>jlink</packaging>\n" +
                "\n" +
                "    <dependencies>\n" +
                "${DEPENDENCIES}" +
                "    </dependencies>\n" +
                "\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <artifactId>maven-jlink-plugin</artifactId>\n" +
                "                <version>3.2.0</version>\n" +
                "                <extensions>true</extensions>\n" +
                "                <configuration>\n" +
                "                    <noHeaderFiles>true</noHeaderFiles>\n" +
                "                    <noManPages>true</noManPages>\n" +
                "                    <stripDebug>true</stripDebug>\n" +
                "                    <verbose>true</verbose>\n" +
                "                    <compress>zip-9</compress>\n" +
                "                    <launcher>launch=${MODULE_NAME}/${MAIN_CLASS}</launcher>\n" +
                "                </configuration>\n" +
                "                <dependencies>\n" +
                "                    <dependency>\n" +
                "                        <groupId>org.ow2.asm</groupId>\n" +
                "                        <artifactId>asm</artifactId>\n" +
                "                        <version>9.8</version>\n" +
                "                    </dependency>\n" +
                "                </dependencies>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>";
    }

    private String getRabbitPackageInfoTemplate()
    {
        return "@RabbitConnectionOptions(value = \"${RABBIT_CONNECTION_NAME:connection-1}\",\n" +
                "        password = \"${RABBIT_CONNECTION_PASSWORD:guest}\",\n" +
                "        user = \"${RABBIT_CONNECTION_USER:guest}\",\n" +
                "        virtualHost = \"${RABBIT_CONNECTION_VHOST:}\",\n" +
                "        uri = \"${RABBIT_CONNECTION_URI:}\",\n" +
                "        host = \"${RABBIT_CONNECTION_HOST:localhost}\",\n" +
                "        port = 5672,\n" +
                "        addresses = \"${RABBIT_CONNECTION_ADDRESSES:localhost}\",\n" +
                "        automaticRecoveryEnabled = false,\n" +
                "        reconnectAttempts = 2,\n" +
                "        reconnectInterval = 500)\n" +
                "@QueueExchange(value = \"${RABBIT_CONNECTION_EXCHANGE:default}\")\n" +
                "package ${PACKAGE};\n" +
                "\n" +
                "import com.guicedee.rabbit.QueueExchange;\n" +
                "import com.guicedee.rabbit.RabbitConnectionOptions;\n";
    }

    /**
     * Creates a persistence.xml file in the META-INF directory.
     *
     * @param resourcesDir The resources directory
     * @param persistenceUnit The persistence unit name
     * @param entityClass The fully qualified name of the entity class to register
     * @throws IOException If an I/O error occurs
     */
    private void createPersistenceXml(File resourcesDir, String persistenceUnit, String entityClass) throws IOException
    {
        // Create META-INF directory if it doesn't exist
        File metaInfDir = new File(resourcesDir, "META-INF");
        FileUtil.createDirectory(metaInfDir);

        // Generate persistence.xml content
        String persistenceXml = getPersistenceXmlTemplate()
                .replace("${PERSISTENCE_UNIT}", persistenceUnit)
                .replace("${ENTITY_CLASS}", entityClass);

        // Write persistence.xml file
        FileUtil.writeToFile(new File(metaInfDir, "persistence.xml"), persistenceXml);
    }

    /**
     * Creates a persistence.xml file in the META-INF directory without entity classes.
     *
     * @param resourcesDir The resources directory
     * @param persistenceUnit The persistence unit name
     * @throws IOException If an I/O error occurs
     */
    private void createPersistenceXml(File resourcesDir, String persistenceUnit) throws IOException
    {
        createPersistenceXml(resourcesDir, persistenceUnit, "");
    }

    /**
     * Returns the template for persistence.xml.
     *
     * @return The persistence.xml template
     */
    private String getPersistenceXmlTemplate()
    {
        return "<persistence xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "             xmlns=\"http://xmlns.jcp.org/xml/ns/persistence\"\n" +
                "             xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/persistence\n" +
                "             http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd\"\n" +
                "             version=\"2.1\">\n" +
                "\n" +
                "    <persistence-unit name=\"${PERSISTENCE_UNIT}\">\n" +
                "        <provider>org.hibernate.reactive.provider.ReactivePersistenceProvider</provider>\n" +
                "        <class>${ENTITY_CLASS}</class>\n" +
                "        <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                "        <properties>\n" +
                "            <!-- SQL logging properties using system properties -->\n" +
                "            <property name=\"hibernate.show_sql\" value=\"${system.hibernate.show_sql:true}\"/>\n" +
                "            <property name=\"hibernate.format_sql\" value=\"${system.hibernate.format_sql:true}\"/>\n" +
                "            <property name=\"hibernate.use_sql_comments\" value=\"${system.hibernate.use_sql_comments:false}\"/>\n" +
                "        </properties>\n" +
                "    </persistence-unit>\n" +
                "</persistence>";
    }

    private String getRabbitMQConsumerTemplate()
    {
        return "package ${PACKAGE};\n" +
                "\n" +
                "import com.guicedee.rabbit.QueueConsumer;\n" +
                "import com.guicedee.rabbit.QueueDefinition;\n" +
                "import com.guicedee.rabbit.QueueExchange;\n" +
                "import com.guicedee.rabbit.RabbitConnectionOptions;\n" +
                "import io.vertx.core.buffer.Buffer;\n" +
                "import io.vertx.rabbitmq.RabbitMQMessage;\n" +
                "\n" +
                "@QueueDefinition(value = \"example-queue\")\n" +
                "public class ExampleConsumer implements QueueConsumer {\n" +
                "\n" +
                "    @Override\n" +
                "    public void consume(RabbitMQMessage message) {\n" +
                "        Buffer body = message.body();\n" +
                "        System.out.println(\"Consumed message: \" + body.toString());\n" +
                "    }\n" +
                "}";
    }
}
