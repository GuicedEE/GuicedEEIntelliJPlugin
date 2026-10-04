package com.guicedee.intellij.intentions;

/** Shared safe defaults for the dedicated intentions and annotation picker. */
final class GuicedEEAnnotationDefaults {
    private GuicedEEAnnotationDefaults() { }
    static String annotationText(String name) {
        if (name.equals("com.guicedee.guicedhazelcast.HazelcastServerOptions")) {
            return "@" + name + "(clustered = false, startLocal = true, joinType = "
                    + name + ".JoinType.NONE)";
        }
        return "@" + name;
    }

    static void addAnnotation(com.intellij.openapi.project.Project project,
                              com.intellij.psi.PsiPackageStatement statement, String name) {
        var list = statement.getAnnotationList();
        if (list != null) {
            var factory = com.intellij.psi.JavaPsiFacade.getElementFactory(project);
            list.add(factory.createAnnotationFromText(annotationText(name), statement));
        } else {
            // A plain package-info file can have no annotation modifier list yet.
            var file = (com.intellij.psi.PsiJavaFile) com.intellij.psi.PsiFileFactory.getInstance(project)
                    .createFileFromText("package-info.java", com.intellij.ide.highlighter.JavaFileType.INSTANCE,
                            annotationText(name) + "\n" + statement.getText());
            statement.replace(java.util.Objects.requireNonNull(file.getPackageStatement()));
        }
    }
}
