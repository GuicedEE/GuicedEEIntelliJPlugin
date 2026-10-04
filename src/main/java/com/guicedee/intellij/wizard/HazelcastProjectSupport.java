package com.guicedee.intellij.wizard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Generates intentional configuration using the built-in GuicedEE cluster manager. */
final class HazelcastProjectSupport {
    private HazelcastProjectSupport() { }

    static void write(Path sourceDirectory, String packageName,
                      GuicedEEProjectWizardData.ModuleData module) throws IOException {
        if (!module.isCaching() || !module.isCachingHazelcast()) return;
        boolean clustered = module.isVertxClustering();
        Path directory = sourceDirectory.resolve(packageName.replace('.', '/')).resolve("cluster");
        Files.createDirectories(directory);
        String options = clustered ? """
                @com.guicedee.vertx.spi.EventBusOptions(
                    host = "127.0.0.1", port = 7801,
                    clusterPublicHost = "127.0.0.1", clusterPublicPort = 7801)
                @com.guicedee.guicedhazelcast.HazelcastServerOptions(
                    clusterName = "%s-cluster", clustered = true, startLocal = false,
                    port = 5701, portAutoIncrement = false,
                    interfaces = "127.0.0.1", interfacesEnabled = true,
                    joinType = com.guicedee.guicedhazelcast.HazelcastServerOptions.JoinType.TCP,
                    tcpMembers = "127.0.0.1:5701,127.0.0.1:5702")
                """.formatted(module.getArtifactId()) : """
                @com.guicedee.guicedhazelcast.HazelcastServerOptions(
                    clusterName = "%s-cache", clustered = false, startLocal = true,
                    joinType = com.guicedee.guicedhazelcast.HazelcastServerOptions.JoinType.NONE)
                """.formatted(module.getArtifactId());
        Files.writeString(directory.resolve("package-info.java"), options
                + "package " + packageName + ".cluster;\n", StandardCharsets.UTF_8);
        if (!clustered) return;
        Path moduleDirectory = sourceDirectory.getParent().getParent().getParent();
        Files.writeString(moduleDirectory.resolve("cluster-a.env.example"), nodeEnvironment(5701, 7801, 8081), StandardCharsets.UTF_8);
        Files.writeString(moduleDirectory.resolve("cluster-b.env.example"), nodeEnvironment(5702, 7802, 8082), StandardCharsets.UTF_8);
        Files.writeString(moduleDirectory.resolve("single-instance.env.example"), """
                VERTX_CLUSTER_ENABLED=false
                HAZELCAST_CLIENT_ENABLED=false
                HTTP_PORT=8080
                """, StandardCharsets.UTF_8);
        Files.writeString(moduleDirectory.resolve("CLUSTERING.md"), """
                # Local cluster examples

                Load cluster-a.env.example and cluster-b.env.example into separate processes running this module.
                These files are not loaded automatically. They bind only to loopback; select reachable membership
                and event-bus bind/advertised addresses for another host or deployment.
                single-instance.env.example disables clustering and starts no member or client.

                The built-in GuicedEE configurator owns the manager; do not create a second one.
                Configure only one HazelcastServerOptions annotation per assembled application.
                Dependency selection alone does not activate clustering. Cache-only wizard selection explicitly
                starts an owned member with clustered=false, without clustering the event bus.

                Preserve cluster publish for public groups. Socket commands and private replies/storage stay with
                the owning connection. Browser GUIDs are correlation, not authenticated identity.
                Shared capabilities need atomic single-use redemption and verified actor/session/destination binding;
                live socket objects remain local. Membership quorum includes failure-detection delay and does not
                establish instantaneous CP partition safety. Configure application readiness explicitly.

                Reconnect with fresh session-bound capabilities, restore subscriptions, then refresh an authoritative
                snapshot. Delivery is best effort without durable replay. Prove single-instance and multi-JVM behavior,
                actual slow TCP peers, and the packaged application's effective artifacts before enabling deployment.

                See https://guicedee.com/capabilities#clustering-websocket-continuity
                and https://guicedee.com/environment-variables#hazelcast-clustering.
                """, StandardCharsets.UTF_8);
    }

    private static String nodeEnvironment(int memberPort, int busPort, int httpPort) {
        return """
                VERTX_CLUSTER_ENABLED=true
                HAZELCAST_CLIENT_ENABLED=false
                HAZELCAST_START_LOCAL=false
                HAZELCAST_PORT=%d
                HAZELCAST_PUBLIC_ADDRESS=127.0.0.1:%d
                VERTX_EVENTBUS_HOST=127.0.0.1
                VERTX_EVENTBUS_PORT=%d
                VERTX_EVENTBUS_CLUSTER_PUBLIC_HOST=127.0.0.1
                VERTX_EVENTBUS_CLUSTER_PUBLIC_PORT=%d
                HTTP_PORT=%d
                """.formatted(memberPort, memberPort, busPort, busPort, httpPort);
    }
}
