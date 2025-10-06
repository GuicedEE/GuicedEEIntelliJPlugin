package com.guicedee.intellij.wizard;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class to store GuicedEE project wizard settings.
 */
public class GuicedEEProjectWizardData {

    /**
     * Data class to store module-specific settings.
     */
    public static class ModuleData {
        private String name;
        private String artifactId;

        // Main feature groups
        private boolean webReactive;
        private boolean database;
        private boolean messaging;
        private boolean caching;
        private boolean microProfile;
        private boolean tests;

        // Tests sub-options
        private boolean testsTestContainers;

        // Web Reactive sub-options
        private boolean webReactiveRest;
        private boolean webReactiveWebSockets;
        private boolean webReactiveSwagger;

        // Database sub-options
        private boolean databasePersistence;
        private boolean databasePostgreSQL;
        private boolean databaseMySQL;
        private boolean databaseOracle;
        private boolean databaseDB2;
        private boolean databaseSqlServer;
        private boolean databaseCassandra;
        private boolean databaseMongoDB;
        private boolean databaseJDBC;

        // Messaging sub-options
        private boolean messagingRabbitMQ;
        private boolean messagingKafka;
        private boolean messagingAMQP;
        private boolean messagingMQTT;

        // Caching sub-options
        private boolean cachingHazelcast;
        private boolean cachingVertxHazelcast;

        // MicroProfile sub-options
        private boolean microProfileHealth;
        private boolean microProfileTelemetry;
        private boolean microProfileOpenAPI;
        private boolean microProfileLogging;
        private boolean microProfileZipkin;

        public ModuleData(String name, String artifactId) {
            this.name = name;
            this.artifactId = artifactId;

            // Initialize all features to false
            this.webReactive = false;
            this.database = false;
            this.messaging = false;
            this.caching = false;
            this.microProfile = false;

            // Initialize all sub-options to false
            this.webReactiveRest = false;
            this.webReactiveWebSockets = false;
            this.webReactiveSwagger = false;

            this.databasePersistence = false;
            this.databasePostgreSQL = false;
            this.databaseMySQL = false;
            this.databaseOracle = false;
            this.databaseDB2 = false;
            this.databaseSqlServer = false;
            this.databaseCassandra = false;
            this.databaseMongoDB = false;
            this.databaseJDBC = false;

            this.messagingRabbitMQ = false;
            this.messagingKafka = false;
            this.messagingAMQP = false;
            this.messagingMQTT = false;

            this.cachingHazelcast = false;
            this.cachingVertxHazelcast = false;

            this.microProfileHealth = false;
            this.microProfileTelemetry = false;
            this.microProfileOpenAPI = false;
            this.microProfileLogging = false;
            this.microProfileZipkin = false;

            this.tests = false;
            this.testsTestContainers = false;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        // For backward compatibility
        public boolean isWebApplication() {
            return webReactive;
        }

        public void setWebApplication(boolean webApplication) {
            this.webReactive = webApplication;
        }

        public boolean isDatabaseApplication() {
            return database;
        }

        public void setDatabaseApplication(boolean databaseApplication) {
            this.database = databaseApplication;
        }

        public boolean isRabbitMQSupport() {
            return messagingRabbitMQ;
        }

        public void setRabbitMQSupport(boolean rabbitMQSupport) {
            this.messagingRabbitMQ = rabbitMQSupport;
            if (rabbitMQSupport) {
                this.messaging = true;
            }
        }

        // New getters and setters for main feature groups
        public boolean isWebReactive() {
            return webReactive;
        }

        public void setWebReactive(boolean webReactive) {
            this.webReactive = webReactive;
        }

        public boolean isDatabase() {
            return database;
        }

        public void setDatabase(boolean database) {
            this.database = database;
            if (database) {
                this.databasePersistence = true;
            }
        }

        public boolean isMessaging() {
            return messaging;
        }

        public void setMessaging(boolean messaging) {
            this.messaging = messaging;
        }

        public boolean isCaching() {
            return caching;
        }

        public void setCaching(boolean caching) {
            this.caching = caching;
        }

        public boolean isMicroProfile() {
            return microProfile;
        }

        public void setMicroProfile(boolean microProfile) {
            this.microProfile = microProfile;
        }

        // Getters and setters for Web Reactive sub-options
        public boolean isWebReactiveRest() {
            return webReactiveRest;
        }

        public void setWebReactiveRest(boolean webReactiveRest) {
            this.webReactiveRest = webReactiveRest;
            if (webReactiveRest) {
                this.webReactive = true;
            }
        }

        public boolean isWebReactiveWebSockets() {
            return webReactiveWebSockets;
        }

        public void setWebReactiveWebSockets(boolean webReactiveWebSockets) {
            this.webReactiveWebSockets = webReactiveWebSockets;
            if (webReactiveWebSockets) {
                this.webReactive = true;
            }
        }

        public boolean isWebReactiveSwagger() {
            return webReactiveSwagger;
        }

        public void setWebReactiveSwagger(boolean webReactiveSwagger) {
            this.webReactiveSwagger = webReactiveSwagger;
            if (webReactiveSwagger) {
                this.webReactive = true;
            }
        }

        // Getters and setters for Database sub-options
        public boolean isDatabasePersistence() {
            return databasePersistence;
        }

        public void setDatabasePersistence(boolean databasePersistence) {
            this.databasePersistence = databasePersistence;
            if (databasePersistence) {
                this.database = true;
            }
        }

        public boolean isDatabasePostgreSQL() {
            return databasePostgreSQL;
        }

        public void setDatabasePostgreSQL(boolean databasePostgreSQL) {
            this.databasePostgreSQL = databasePostgreSQL;
            if (databasePostgreSQL) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseMySQL() {
            return databaseMySQL;
        }

        public void setDatabaseMySQL(boolean databaseMySQL) {
            this.databaseMySQL = databaseMySQL;
            if (databaseMySQL) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseOracle() {
            return databaseOracle;
        }

        public void setDatabaseOracle(boolean databaseOracle) {
            this.databaseOracle = databaseOracle;
            if (databaseOracle) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseDB2() {
            return databaseDB2;
        }

        public void setDatabaseDB2(boolean databaseDB2) {
            this.databaseDB2 = databaseDB2;
            if (databaseDB2) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseSqlServer() {
            return databaseSqlServer;
        }

        public void setDatabaseSqlServer(boolean databaseSqlServer) {
            this.databaseSqlServer = databaseSqlServer;
            if (databaseSqlServer) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseCassandra() {
            return databaseCassandra;
        }

        public void setDatabaseCassandra(boolean databaseCassandra) {
            this.databaseCassandra = databaseCassandra;
            if (databaseCassandra) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseMongoDB() {
            return databaseMongoDB;
        }

        public void setDatabaseMongoDB(boolean databaseMongoDB) {
            this.databaseMongoDB = databaseMongoDB;
            if (databaseMongoDB) {
                this.database = true;
                if (this.databaseJDBC) {
                    this.databaseJDBC = false;
                }
            }
        }

        public boolean isDatabaseJDBC() {
            return databaseJDBC;
        }

        public void setDatabaseJDBC(boolean databaseJDBC) {
            this.databaseJDBC = databaseJDBC;
            if (databaseJDBC) {
                this.database = true;
                // If JDBC is selected, deselect all other database options
                this.databasePostgreSQL = false;
                this.databaseMySQL = false;
                this.databaseOracle = false;
                this.databaseDB2 = false;
                this.databaseSqlServer = false;
                this.databaseCassandra = false;
                this.databaseMongoDB = false;
            }
        }

        // Getters and setters for Messaging sub-options
        public boolean isMessagingRabbitMQ() {
            return messagingRabbitMQ;
        }

        public void setMessagingRabbitMQ(boolean messagingRabbitMQ) {
            this.messagingRabbitMQ = messagingRabbitMQ;
            if (messagingRabbitMQ) {
                this.messaging = true;
            }
        }

        public boolean isMessagingKafka() {
            return messagingKafka;
        }

        public void setMessagingKafka(boolean messagingKafka) {
            this.messagingKafka = messagingKafka;
            if (messagingKafka) {
                this.messaging = true;
            }
        }

        public boolean isMessagingAMQP() {
            return messagingAMQP;
        }

        public void setMessagingAMQP(boolean messagingAMQP) {
            this.messagingAMQP = messagingAMQP;
            if (messagingAMQP) {
                this.messaging = true;
            }
        }

        public boolean isMessagingMQTT() {
            return messagingMQTT;
        }

        public void setMessagingMQTT(boolean messagingMQTT) {
            this.messagingMQTT = messagingMQTT;
            if (messagingMQTT) {
                this.messaging = true;
            }
        }

        // Getters and setters for Caching sub-options
        public boolean isCachingHazelcast() {
            return cachingHazelcast;
        }

        public void setCachingHazelcast(boolean cachingHazelcast) {
            this.cachingHazelcast = cachingHazelcast;
            if (cachingHazelcast) {
                this.caching = true;
            }
        }

        public boolean isCachingVertxHazelcast() {
            return cachingVertxHazelcast;
        }

        public void setCachingVertxHazelcast(boolean cachingVertxHazelcast) {
            this.cachingVertxHazelcast = cachingVertxHazelcast;
            if (cachingVertxHazelcast) {
                this.caching = true;
            }
        }

        // Getters and setters for MicroProfile sub-options
        public boolean isMicroProfileHealth() {
            return microProfileHealth;
        }

        public void setMicroProfileHealth(boolean microProfileHealth) {
            this.microProfileHealth = microProfileHealth;
            if (microProfileHealth) {
                this.microProfile = true;
            }
        }

        public boolean isMicroProfileTelemetry() {
            return microProfileTelemetry;
        }

        public void setMicroProfileTelemetry(boolean microProfileTelemetry) {
            this.microProfileTelemetry = microProfileTelemetry;
            if (microProfileTelemetry) {
                this.microProfile = true;
            }
        }

        public boolean isMicroProfileOpenAPI() {
            return microProfileOpenAPI;
        }

        public void setMicroProfileOpenAPI(boolean microProfileOpenAPI) {
            this.microProfileOpenAPI = microProfileOpenAPI;
            if (microProfileOpenAPI) {
                this.microProfile = true;
            }
        }

        public boolean isMicroProfileLogging() {
            return microProfileLogging;
        }

        public void setMicroProfileLogging(boolean microProfileLogging) {
            this.microProfileLogging = microProfileLogging;
            if (microProfileLogging) {
                this.microProfile = true;
            }
        }

        public boolean isMicroProfileZipkin() {
            return microProfileZipkin;
        }

        public void setMicroProfileZipkin(boolean microProfileZipkin) {
            this.microProfileZipkin = microProfileZipkin;
            if (microProfileZipkin) {
                this.microProfile = true;
            }
        }

        public boolean isTests() {
            return tests;
        }

        public void setTests(boolean tests) {
            this.tests = tests;
        }

        public boolean isTestsTestContainers() {
            return testsTestContainers;
        }

        public void setTestsTestContainers(boolean testsTestContainers) {
            this.testsTestContainers = testsTestContainers;
            if (testsTestContainers) {
                this.tests = true;
            }
        }
    }

    private String groupId;
    private String artifactId;
    private String version;
    private String description;
    private String url;
    private String scmConnection;
    private String scmDeveloperConnection;
    private String scmUrl;
    private String developerName;
    private String developerEmail;
    private String developerOrganization;
    private String developerOrganizationUrl;
    private boolean webApplication;
    private boolean databaseApplication;
    private boolean rabbitMQSupport;
    private boolean multiModuleProject;
    private boolean jmodPackaging;
    private boolean jlinkPackaging;

    // List of modules for multi-module projects
    private List<ModuleData> modules = new ArrayList<>();

    /**
     * Adds a new module to the project.
     * 
     * @param name The display name of the module
     * @param artifactId The Maven artifact ID of the module
     * @return The newly created module data
     */
    public ModuleData addModule(String name, String artifactId) {
        ModuleData module = new ModuleData(name, artifactId);
        modules.add(module);
        return module;
    }

    /**
     * Removes a module from the project.
     * 
     * @param module The module to remove
     */
    public void removeModule(ModuleData module) {
        modules.remove(module);
    }

    /**
     * Gets the list of modules.
     * 
     * @return The list of modules
     */
    public List<ModuleData> getModules() {
        return modules;
    }

    /**
     * Initializes the modules list with a default app module if it's empty.
     */
    public void initializeModules() {
        if (modules.isEmpty() && multiModuleProject) {
            addModule("App Module", artifactId + "-app");
        }
    }

    public GuicedEEProjectWizardData() {
        // Default values
        groupId = "com.example";
        artifactId = "guicedee-app";
        version = "1.0-SNAPSHOT";
        description = "";
        url = "";
        scmConnection = "";
        scmDeveloperConnection = "";
        scmUrl = "";
        developerName = "";
        developerEmail = "";
        developerOrganization = "";
        developerOrganizationUrl = "";
        webApplication = false;
        databaseApplication = false;
        rabbitMQSupport = false;
        multiModuleProject = false;
        jmodPackaging = false;
        jlinkPackaging = false;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isWebApplication() {
        return webApplication;
    }

    public void setWebApplication(boolean webApplication) {
        this.webApplication = webApplication;
    }

    public boolean isDatabaseApplication() {
        return databaseApplication;
    }

    public void setDatabaseApplication(boolean databaseApplication) {
        this.databaseApplication = databaseApplication;
    }

    public boolean isRabbitMQSupport() {
        return rabbitMQSupport;
    }

    public void setRabbitMQSupport(boolean rabbitMQSupport) {
        this.rabbitMQSupport = rabbitMQSupport;
    }

    public boolean isMultiModuleProject() {
        return multiModuleProject;
    }

    public void setMultiModuleProject(boolean multiModuleProject) {
        this.multiModuleProject = multiModuleProject;
    }

    public boolean isJmodPackaging() {
        return jmodPackaging;
    }

    public void setJmodPackaging(boolean jmodPackaging) {
        this.jmodPackaging = jmodPackaging;
    }

    public boolean isJlinkPackaging() {
        return jlinkPackaging;
    }

    public void setJlinkPackaging(boolean jlinkPackaging) {
        this.jlinkPackaging = jlinkPackaging;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScmConnection() {
        return scmConnection;
    }

    public void setScmConnection(String scmConnection) {
        this.scmConnection = scmConnection;
    }

    public String getScmDeveloperConnection() {
        return scmDeveloperConnection;
    }

    public void setScmDeveloperConnection(String scmDeveloperConnection) {
        this.scmDeveloperConnection = scmDeveloperConnection;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getDeveloperEmail() {
        return developerEmail;
    }

    public void setDeveloperEmail(String developerEmail) {
        this.developerEmail = developerEmail;
    }

    public String getDeveloperOrganization() {
        return developerOrganization;
    }

    public void setDeveloperOrganization(String developerOrganization) {
        this.developerOrganization = developerOrganization;
    }

    public String getDeveloperOrganizationUrl() {
        return developerOrganizationUrl;
    }

    public void setDeveloperOrganizationUrl(String developerOrganizationUrl) {
        this.developerOrganizationUrl = developerOrganizationUrl;
    }
}
