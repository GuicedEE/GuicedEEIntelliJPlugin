package com.guicedee.intellij.wizard;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import static org.junit.Assert.*;

public class HazelcastProjectSupportTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private GuicedEEProjectWizardData.ModuleData module() {
        return new GuicedEEProjectWizardData.ModuleData("Orders", "orders");
    }

    private Path source() throws Exception {
        Path path = temporary.newFolder().toPath().resolve("src/main/java");
        Files.createDirectories(path);
        return path;
    }

    @Test public void incidentalDependencySelectionIsAbsentByDefault() throws Exception {
        Path source = source();
        HazelcastProjectSupport.write(source, "example.orders", module());
        assertFalse(Files.exists(source.resolve("example/orders/cluster/package-info.java")));
    }

    @Test public void cacheOnlyCreatesOwnedMemberWithoutEventBusCluster() throws Exception {
        var module = module();
        module.setCachingHazelcast(true);
        Path source = source();
        HazelcastProjectSupport.write(source, "example.orders", module);
        String config = Files.readString(source.resolve("example/orders/cluster/package-info.java"));
        assertTrue(config.contains("clustered = false, startLocal = true"));
        assertTrue(config.contains("JoinType.NONE"));
        assertFalse(config.contains("EventBusOptions"));
        assertFalse(Files.exists(source.getParent().getParent().getParent().resolve("cluster-a.env.example")));
    }

    @Test public void clusterCreatesSeparateMembershipAndAdvertisedBusEndpoints() throws Exception {
        var module = module();
        module.setVertxClustering(true);
        assertTrue(module.isCachingHazelcast());
        assertTrue(module.isCaching());
        Path source = source();
        HazelcastProjectSupport.write(source, "example.orders", module);
        String config = Files.readString(source.resolve("example/orders/cluster/package-info.java"));
        assertTrue(config.contains("clustered = true, startLocal = false"));
        assertTrue(config.contains("JoinType.TCP"));
        assertTrue(config.contains("127.0.0.1:5701,127.0.0.1:5702"));
        assertTrue(config.contains("clusterPublicPort = 7801"));
        Path root = source.getParent().getParent().getParent();
        for (int node = 1; node <= 2; node++) {
            Properties env = new Properties();
            try (var reader = Files.newBufferedReader(root.resolve("cluster-" + (node == 1 ? "a" : "b") + ".env.example"))) {
                env.load(reader);
            }
            assertEquals("true", env.getProperty("VERTX_CLUSTER_ENABLED"));
            assertEquals("false", env.getProperty("HAZELCAST_CLIENT_ENABLED"));
            assertEquals("570" + node, env.getProperty("HAZELCAST_PORT"));
            assertEquals("127.0.0.1:570" + node, env.getProperty("HAZELCAST_PUBLIC_ADDRESS"));
            assertEquals("780" + node, env.getProperty("VERTX_EVENTBUS_PORT"));
            assertEquals("780" + node, env.getProperty("VERTX_EVENTBUS_CLUSTER_PUBLIC_PORT"));
        }
        assertTrue(Files.readString(root.resolve("single-instance.env.example")).contains("VERTX_CLUSTER_ENABLED=false"));
        assertTrue(Files.readString(root.resolve("CLUSTERING.md")).contains("not loaded automatically"));
    }

    @Test public void removingHazelcastClearsClusterChoice() {
        var module = module();
        module.setVertxClustering(true);
        module.setCachingHazelcast(false);
        assertFalse(module.isVertxClustering());
        module.setCachingHazelcast(true);
        assertFalse(module.isVertxClustering());
    }

    @Test public void disablingClusterPreservesCacheSelection() {
        var module = module();
        module.setVertxClustering(true);
        module.setVertxClustering(false);
        assertTrue(module.isCachingHazelcast());
        assertFalse(module.isVertxClustering());
    }
}
