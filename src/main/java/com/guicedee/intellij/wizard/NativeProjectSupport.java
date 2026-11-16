package com.guicedee.intellij.wizard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Opt-in, application-specific SPI and native build scaffolding. */
final class NativeProjectSupport {
    private NativeProjectSupport() {}

    static void write(Path root, String pkg, String artifact, String version, String main) throws IOException {
        Path source = root.resolve("src/main/java");
        var types = new TreeSet<String>();
        var packages = new TreeSet<String>();
        try (var files = Files.walk(source)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (file.getFileName().toString().equals("module-info.java")) continue;
                String text = Files.readString(file);
                var packageMatch = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;").matcher(text);
                if (!packageMatch.find()) continue;
                String packageName = packageMatch.group(1);
                packages.add(packageName);
                if (file.getFileName().toString().equals("package-info.java")) {
                    Files.writeString(file.resolveSibling("DiscoveryPackage.java"),
                            "package " + packageName + ";\npublic final class DiscoveryPackage {}\n");
                    types.add(packageName + ".DiscoveryPackage");
                } else {
                    var type = Pattern.compile("(?m)^\\s*public\\s+(?:(?:final|abstract)\\s+)?(?:class|interface|enum|record)\\s+(\\w+)").matcher(text);
                    if (type.find()) types.add(packageName + "." + type.group(1));
                }
            }
        }
        String literals = types.stream().map(t -> t + ".class").collect(java.util.stream.Collectors.joining(",\n                "));
        Path descriptor = source.resolve("module-info.java");
        String moduleInfo = Files.readString(descriptor);
        String opens = packages.stream().filter(p -> !moduleInfo.contains("opens " + p + " "))
                .map(p -> "    opens " + p + " to com.google.guice, com.guicedee.vertx, tools.jackson.databind;\n")
                .collect(java.util.stream.Collectors.joining());
        int closingBrace = moduleInfo.lastIndexOf('}');
        Files.writeString(descriptor, moduleInfo.substring(0, closingBrace) + opens + moduleInfo.substring(closingBrace));
        Path packageDir = source.resolve(pkg.replace('.', '/'));
        Files.createDirectories(packageDir);
        Files.writeString(packageDir.resolve("ApplicationDiscovery.java"), """
                package %s;
                import com.guicedee.client.services.IGuiceDiscovery;
                import java.util.Collection;
                import java.util.List;
                public final class ApplicationDiscovery implements IGuiceDiscovery<ApplicationDiscovery> {
                    @Override public Collection<Class<?>> classes() {
                        return List.of(%s);
                    }
                }
                """.formatted(pkg, literals));
        Files.writeString(packageDir.resolve("NativeConfiguration.java"), """
                package %s;
                import com.guicedee.nativeimage.GuiceNativeConfiguration;
                import com.guicedee.nativeimage.GuiceNativeRegistration;
                public final class NativeConfiguration implements GuiceNativeConfiguration {
                    @Override public void configure(GuiceNativeRegistration registration) {
                        %s
                        registration.reflect(%s);
                        // Add registration.intercept(...) for your application's intercepted methods.
                    }
                }
                """.formatted(pkg, resource("bootstrap-registration.txt"), literals));
        Path services = root.resolve("src/main/resources/META-INF/services");
        Files.createDirectories(services);
        Files.writeString(services.resolve("com.guicedee.client.services.IGuiceDiscovery"), pkg + ".ApplicationDiscovery\n");
        Files.writeString(services.resolve("com.guicedee.nativeimage.GuiceNativeConfiguration"), pkg + ".NativeConfiguration\n");
        Path nativeMetadata = root.resolve("src/main/resources/META-INF/native-image").resolve(pkg).resolve(artifact);
        Files.createDirectories(nativeMetadata);
        Files.writeString(nativeMetadata.resolve("reachability-metadata.json"), resource("reachability-metadata.json"));
        Path boot = packageDir.resolve(main + ".java");
        Files.writeString(boot, Files.readString(boot)
                .replace("GuiceApplication.run(() -> {", "System.setProperty(\"GUICEDEE_DISCOVERY_MODE\", \"spi\");\n        GuiceApplication.run(() -> {")
                .replace("setServiceLoadWithClassPath(true)", "setServiceLoadWithClassPath(false)"));
        Files.writeString(root.resolve("NativeBuild.java"), resource("NativeBuild.java.txt"));
        String module = artifact.replace('-', '.');
        if (!Character.isJavaIdentifierStart(module.charAt(0))) module = "m." + module;
        Files.writeString(root.resolve("NATIVE.md"), resource("NATIVE.md")
                .replace("@ARTIFACT@", artifact).replace("@VERSION@", version)
                .replace("@MODULE@", module).replace("@MAIN@", pkg + "." + main));
        Path gradle = root.resolve("build.gradle.kts");
        if (Files.exists(gradle)) Files.writeString(gradle, """

                tasks.register<Copy>("copyNativeDependencies") {
                    from(configurations.runtimeClasspath)
                    into(layout.buildDirectory.dir("native-libs"))
                }
                """, StandardOpenOption.APPEND);
    }

    private static String resource(String name) throws IOException {
        try (var input = NativeProjectSupport.class.getResourceAsStream("/native-starter/" + name)) {
            if (input == null) throw new IOException("Missing native starter: " + name);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
