package com.guicedee.intellij.intentions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Dialog for selecting which GuicedEE annotation to add to a package-info file.
 */
public class AddAnnotationDialog extends DialogWrapper {

    private final List<String> availableAnnotations = Arrays.asList(
            "com.guicedee.rabbit.RabbitConnectionOptions",
            "com.guicedee.rabbit.QueueExchange",
            "com.guicedee.vertx.spi.Verticle"
    );

    private JBList<String> annotationList;

    public AddAnnotationDialog(Project project) {
        super(project);
        setTitle("Add GuicedEE Annotation");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        annotationList = new JBList<>(availableAnnotations);
        annotationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annotationList.setSelectedIndex(0);
        
        JScrollPane scrollPane = new JScrollPane(annotationList);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        
        panel.add(new JLabel("Select an annotation to add:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Returns the selected annotation.
     */
    public String getSelectedAnnotation() {
        return annotationList.getSelectedValue();
    }
}