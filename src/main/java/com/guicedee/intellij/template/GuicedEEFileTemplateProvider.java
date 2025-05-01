package com.guicedee.intellij.template;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class GuicedEEFileTemplateProvider implements FileTemplateGroupDescriptorFactory {
    public static final String TRANSACTIONAL_EVENT_TEMPLATE = "GuicedEETransactionalEvent.java";
    public static final String GUICE_MODULE_TEMPLATE = "GuicedEEModule.java";
    public static final String PRE_STARTUP_TEMPLATE = "GuicedEEPreStartup.java";
    public static final String POST_STARTUP_TEMPLATE = "GuicedEEPostStartup.java";
    public static final String PRE_DESTROY_TEMPLATE = "GuicedEEPreDestroy.java";
    public static final String LOG4J_CONFIGURATOR_TEMPLATE = "GuicedEELog4JConfigurator.java";
    public static final String VERTX_STARTUP_TEMPLATE = "GuicedEEVertxStartup.java";
    public static final String VERTX_CONFIGURATOR_TEMPLATE = "GuicedEEVertxConfigurator.java";
    public static final String REST_SERVICE_TEMPLATE = "GuicedEERestService.java";
    public static final String REST_SERVICE_IMPL_TEMPLATE = "GuicedEERestServiceImpl.java";
    public static final String PERSISTENCE_MODULE_TEMPLATE = "GuicedEEPersistenceModule.java";
    public static final String WEBSOCKET_CHANNEL_TEMPLATE = "GuicedEEWebSocketChannel.java";
    public static final String WEBSOCKET_MESSAGE_RECEIVER_TEMPLATE = "GuicedEEWebSocketMessageReceiver.java";
    public static final String WEBSOCKET_PRE_CONFIGURATION_TEMPLATE = "GuicedEEWebSocketPreConfiguration.java";
    public static final String WEBSOCKET_ON_PUBLISH_TEMPLATE = "GuicedEEWebSocketOnPublish.java";
    public static final String ROUTER_CONFIGURATION_TEMPLATE = "GuicedEERouterConfiguration.java";
    public static final String RABBITMQ_CONSUMER_TEMPLATE = "GuicedEERabbitMQConsumer.java";
    public static final String SCAN_MODULE_INCLUSIONS_TEMPLATE = "GuicedEEScanModuleInclusions.java";
    public static final String CONFIGURATOR_TEMPLATE = "GuicedEEConfigurator.java";
    public static final String FILE_CONTENTS_SCANNER_TEMPLATE = "GuicedEEFileContentsScanner.java";
    public static final String FILE_CONTENTS_PATTERN_SCANNER_TEMPLATE = "GuicedEEFileContentsPatternScanner.java";
    public static final String PACKAGE_CONTENTS_SCANNER_TEMPLATE = "GuicedEEPackageContentsScanner.java";
    public static final String PATH_CONTENTS_SCANNER_TEMPLATE = "GuicedEEPathContentsScanner.java";
    public static final String ON_CALL_SCOPE_ENTER_TEMPLATE = "GuicedEEOnCallScopeEnter.java";
    public static final String ON_CALL_SCOPE_EXIT_TEMPLATE = "GuicedEEOnCallScopeExit.java";
    public static final String JOB_TEMPLATE = "GuicedEEJob.java";

    private static final Icon GUICEDEE_ICON = IconLoader.getIcon("/META-INF/logo_front.png", GuicedEEFileTemplateProvider.class);

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("GuicedEE", GUICEDEE_ICON);

        group.addTemplate(new FileTemplateDescriptor(GUICE_MODULE_TEMPLATE, GUICEDEE_ICON));
        group.addTemplate(new FileTemplateDescriptor(TRANSACTIONAL_EVENT_TEMPLATE, GUICEDEE_ICON));
        group.addTemplate(new FileTemplateDescriptor(JOB_TEMPLATE, GUICEDEE_ICON));
        group.addTemplate(new FileTemplateDescriptor(RABBITMQ_CONSUMER_TEMPLATE, GUICEDEE_ICON));

        // Create Web subgroup (1st feature)
        FileTemplateGroupDescriptor webGroup = new FileTemplateGroupDescriptor("Web", GUICEDEE_ICON);
        group.addTemplate(webGroup);

        webGroup.addTemplate(new FileTemplateDescriptor(ROUTER_CONFIGURATION_TEMPLATE, GUICEDEE_ICON));
        webGroup.addTemplate(new FileTemplateDescriptor(WEBSOCKET_CHANNEL_TEMPLATE, GUICEDEE_ICON));

        // Create REST subgroup (2nd feature)
        FileTemplateGroupDescriptor restGroup = new FileTemplateGroupDescriptor("REST", GUICEDEE_ICON);
        group.addTemplate(restGroup);

        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_IMPL_TEMPLATE, GUICEDEE_ICON));

        // Create Database subgroup (3rd feature)
        FileTemplateGroupDescriptor databaseGroup = new FileTemplateGroupDescriptor("Database", GUICEDEE_ICON);
        group.addTemplate(databaseGroup);

        databaseGroup.addTemplate(new FileTemplateDescriptor(PERSISTENCE_MODULE_TEMPLATE, GUICEDEE_ICON));

        // Create Caching subgroup (4th feature)
        FileTemplateGroupDescriptor cachingGroup = new FileTemplateGroupDescriptor("Caching", GUICEDEE_ICON);
        group.addTemplate(cachingGroup);

        // Add separator
        group.addTemplate(new FileTemplateDescriptor("", null));

        // Create Hooks subgroup
        FileTemplateGroupDescriptor hooksGroup = new FileTemplateGroupDescriptor("Hooks", GUICEDEE_ICON);
        group.addTemplate(hooksGroup);

        hooksGroup.addTemplate(new FileTemplateDescriptor(PRE_STARTUP_TEMPLATE, GUICEDEE_ICON));
        hooksGroup.addTemplate(new FileTemplateDescriptor(POST_STARTUP_TEMPLATE, GUICEDEE_ICON));
        hooksGroup.addTemplate(new FileTemplateDescriptor(VERTX_STARTUP_TEMPLATE, GUICEDEE_ICON));
        hooksGroup.addTemplate(new FileTemplateDescriptor(VERTX_CONFIGURATOR_TEMPLATE, GUICEDEE_ICON));

        // Create Lifecycle group for service loaders
        FileTemplateGroupDescriptor lifecycleGroup = new FileTemplateGroupDescriptor("Lifecycle", GUICEDEE_ICON);
        group.addTemplate(lifecycleGroup);

        lifecycleGroup.addTemplate(new FileTemplateDescriptor(SCAN_MODULE_INCLUSIONS_TEMPLATE, GUICEDEE_ICON));
        lifecycleGroup.addTemplate(new FileTemplateDescriptor(WEBSOCKET_MESSAGE_RECEIVER_TEMPLATE, GUICEDEE_ICON));

        return group;
    }
}
