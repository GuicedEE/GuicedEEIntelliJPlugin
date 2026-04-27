package com.guicedee.intellij.template;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.guicedee.intellij.GuicedIcons;

import javax.swing.*;

public class GuicedEEFileTemplateProvider implements FileTemplateGroupDescriptorFactory {
    public static final String GUICE_MODULE_TEMPLATE = "GuicedEEModule.java";
    public static final String PRE_STARTUP_TEMPLATE = "GuicedEEPreStartup.java";
    public static final String POST_STARTUP_TEMPLATE = "GuicedEEPostStartup.java";
    public static final String PRE_DESTROY_TEMPLATE = "GuicedEEPreDestroy.java";
    public static final String LOG4J_CONFIGURATOR_TEMPLATE = "GuicedEELog4JConfigurator.java";
    public static final String VERTX_STARTUP_TEMPLATE = "GuicedEEVertxStartup.java";
    public static final String VERTX_CONFIGURATOR_TEMPLATE = "GuicedEEVertxConfigurator.java";
    public static final String REST_SERVICE_TEMPLATE = "GuicedEERestService.java";
    public static final String REST_SERVICE_NO_SERVICE_TEMPLATE = "GuicedEERestServiceNoService.java";
    public static final String REST_SERVICE_WITH_SESSION_TEMPLATE = "GuicedEERestServiceWithSession.java";
    public static final String REST_SERVICE_IMPL_TEMPLATE = "GuicedEERestServiceImpl.java";
    public static final String REST_SERVICE_IMPL_WITH_SESSION_TEMPLATE = "GuicedEERestServiceImplWithSession.java";
    public static final String REST_SERVICE_IMPL_WITH_SESSION_PARAM_TEMPLATE = "GuicedEERestServiceImplWithSessionParam.java";
    public static final String REST_CLIENT_TEMPLATE = "GuicedEERestClient.java";
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
    public static final String KAFKA_CONSUMER_TEMPLATE = "GuicedEEKafkaConsumer.java";
    public static final String IBMMQ_CONSUMER_TEMPLATE = "GuicedEEIBMMQConsumer.java";
    public static final String AUTHENTICATION_PROVIDER_TEMPLATE = "GuicedEEAuthenticationProvider.java";
    public static final String AUTHORIZATION_PROVIDER_TEMPLATE = "GuicedEEAuthorizationProvider.java";
    public static final String MAIL_CLIENT_TEMPLATE = "GuicedEEMailClient.java";
    public static final String HEALTH_CHECK_TEMPLATE = "GuicedEEHealthCheck.java";
    public static final String HAZELCAST_SERVER_CONFIG_TEMPLATE = "GuicedEEHazelcastServerConfig.java";
    public static final String HAZELCAST_CLIENT_CONFIG_TEMPLATE = "GuicedEEHazelcastClientConfig.java";
    public static final String SOAP_SERVICE_INTERFACE_TEMPLATE = "GuicedEESoapServiceInterface.java";
    public static final String SOAP_SERVICE_IMPL_TEMPLATE = "GuicedEESoapServiceImpl.java";

    private static final Icon GUICEDEE_ICON = GuicedIcons.Logo;

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("GuicedEE", GUICEDEE_ICON);

        group.addTemplate(new FileTemplateDescriptor(GUICE_MODULE_TEMPLATE, GUICEDEE_ICON));

        // Create Messaging subgroup
        FileTemplateGroupDescriptor messagingGroup = new FileTemplateGroupDescriptor("Messaging", GUICEDEE_ICON);
        group.addTemplate(messagingGroup);

        messagingGroup.addTemplate(new FileTemplateDescriptor(RABBITMQ_CONSUMER_TEMPLATE, GUICEDEE_ICON));
        messagingGroup.addTemplate(new FileTemplateDescriptor(KAFKA_CONSUMER_TEMPLATE, GUICEDEE_ICON));
        messagingGroup.addTemplate(new FileTemplateDescriptor(IBMMQ_CONSUMER_TEMPLATE, GUICEDEE_ICON));

        // Create Web subgroup (1st feature)
        FileTemplateGroupDescriptor webGroup = new FileTemplateGroupDescriptor("Web", GUICEDEE_ICON);
        group.addTemplate(webGroup);

        webGroup.addTemplate(new FileTemplateDescriptor(ROUTER_CONFIGURATION_TEMPLATE, GUICEDEE_ICON));
        webGroup.addTemplate(new FileTemplateDescriptor(WEBSOCKET_CHANNEL_TEMPLATE, GUICEDEE_ICON));

        // Create Auth subgroup
        FileTemplateGroupDescriptor authGroup = new FileTemplateGroupDescriptor("Auth", GUICEDEE_ICON);
        group.addTemplate(authGroup);

        authGroup.addTemplate(new FileTemplateDescriptor(AUTHENTICATION_PROVIDER_TEMPLATE, GUICEDEE_ICON));
        authGroup.addTemplate(new FileTemplateDescriptor(AUTHORIZATION_PROVIDER_TEMPLATE, GUICEDEE_ICON));

        // Create REST subgroup (2nd feature)
        FileTemplateGroupDescriptor restGroup = new FileTemplateGroupDescriptor("REST", GUICEDEE_ICON);
        group.addTemplate(restGroup);

        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_NO_SERVICE_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_WITH_SESSION_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_IMPL_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_IMPL_WITH_SESSION_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_SERVICE_IMPL_WITH_SESSION_PARAM_TEMPLATE, GUICEDEE_ICON));
        restGroup.addTemplate(new FileTemplateDescriptor(REST_CLIENT_TEMPLATE, GUICEDEE_ICON));

        // Create SOAP subgroup
        FileTemplateGroupDescriptor soapGroup = new FileTemplateGroupDescriptor("SOAP", GUICEDEE_ICON);
        group.addTemplate(soapGroup);

        soapGroup.addTemplate(new FileTemplateDescriptor(SOAP_SERVICE_INTERFACE_TEMPLATE, GUICEDEE_ICON));
        soapGroup.addTemplate(new FileTemplateDescriptor(SOAP_SERVICE_IMPL_TEMPLATE, GUICEDEE_ICON));

        // Create Database subgroup (3rd feature)
        FileTemplateGroupDescriptor databaseGroup = new FileTemplateGroupDescriptor("Database", GUICEDEE_ICON);
        group.addTemplate(databaseGroup);

        databaseGroup.addTemplate(new FileTemplateDescriptor(PERSISTENCE_MODULE_TEMPLATE, GUICEDEE_ICON));

        // Create Mail subgroup
        FileTemplateGroupDescriptor mailGroup = new FileTemplateGroupDescriptor("Mail", GUICEDEE_ICON);
        group.addTemplate(mailGroup);

        mailGroup.addTemplate(new FileTemplateDescriptor(MAIL_CLIENT_TEMPLATE, GUICEDEE_ICON));

        // Create MicroProfile subgroup
        FileTemplateGroupDescriptor microProfileGroup = new FileTemplateGroupDescriptor("MicroProfile", GUICEDEE_ICON);
        group.addTemplate(microProfileGroup);

        microProfileGroup.addTemplate(new FileTemplateDescriptor(HEALTH_CHECK_TEMPLATE, GUICEDEE_ICON));

        // Create Hazelcast subgroup
        FileTemplateGroupDescriptor hazelcastGroup = new FileTemplateGroupDescriptor("Hazelcast", GUICEDEE_ICON);
        group.addTemplate(hazelcastGroup);

        hazelcastGroup.addTemplate(new FileTemplateDescriptor(HAZELCAST_SERVER_CONFIG_TEMPLATE, GUICEDEE_ICON));
        hazelcastGroup.addTemplate(new FileTemplateDescriptor(HAZELCAST_CLIENT_CONFIG_TEMPLATE, GUICEDEE_ICON));

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
