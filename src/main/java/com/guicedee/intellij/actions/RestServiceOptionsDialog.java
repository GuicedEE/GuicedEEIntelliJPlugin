package com.guicedee.intellij.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog shown when creating a new REST Service, offering options for
 * associated service class creation and database session handling.
 */
public class RestServiceOptionsDialog extends DialogWrapper {
    private JBCheckBox createServiceCheckBox;
    private JBCheckBox includeDbSessionCheckBox;
    private JBCheckBox sessionPerTransactionCheckBox;
    private JBLabel sessionPerTransactionInfo;

    public RestServiceOptionsDialog(@Nullable Project project) {
        super(project);
        setTitle("REST Service Options");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Checkbox 1: Create associated Service class
        createServiceCheckBox = new JBCheckBox("Create associated Service class");
        createServiceCheckBox.setSelected(true);
        gbc.gridy = 0;
        panel.add(createServiceCheckBox, gbc);

        // Checkbox 2: Include DB Session in Service (indented)
        includeDbSessionCheckBox = new JBCheckBox("Include DB Session in Service");
        includeDbSessionCheckBox.setSelected(false);
        gbc.gridy = 1;
        gbc.insets = JBUI.insets(4, 24, 4, 0);
        panel.add(includeDbSessionCheckBox, gbc);

        // Checkbox 3: Session per Transaction on REST resource (indented more) + info icon
        JPanel sessionPerTxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sessionPerTransactionCheckBox = new JBCheckBox("Session per Transaction (on REST resource)");
        sessionPerTransactionCheckBox.setSelected(false);
        sessionPerTransactionCheckBox.setEnabled(false);
        sessionPerTxPanel.add(sessionPerTransactionCheckBox);

        // Info icon with tooltip
        sessionPerTransactionInfo = new JBLabel(AllIcons.General.Information);
        sessionPerTransactionInfo.setToolTipText(
                "<html><b>Session per Transaction</b><br><br>" +
                "When enabled, the DB session is created at the REST resource level<br>" +
                "using <code>sessionFactory.withTransaction(session -> { ... })</code>.<br><br>" +
                "This allows multiple service calls to participate in the <b>same transaction</b>,<br>" +
                "as the session is passed into each service method.<br><br>" +
                "When disabled, each service manages its own session and transaction independently.</html>");
        sessionPerTransactionInfo.setBorder(JBUI.Borders.emptyLeft(6));
        sessionPerTransactionInfo.setEnabled(false);
        sessionPerTxPanel.add(sessionPerTransactionInfo);

        gbc.gridy = 2;
        gbc.insets = JBUI.insets(4, 48, 4, 0);
        panel.add(sessionPerTxPanel, gbc);

        // Wire up cascading enable/disable
        createServiceCheckBox.addActionListener(e -> updateEnabledStates());
        includeDbSessionCheckBox.addActionListener(e -> updateEnabledStates());

        // Initial state
        updateEnabledStates();

        panel.setPreferredSize(new Dimension(450, 120));
        return panel;
    }

    private void updateEnabledStates() {
        boolean serviceSelected = createServiceCheckBox.isSelected();
        includeDbSessionCheckBox.setEnabled(serviceSelected);
        if (!serviceSelected) {
            includeDbSessionCheckBox.setSelected(false);
        }

        boolean dbSessionSelected = serviceSelected && includeDbSessionCheckBox.isSelected();
        sessionPerTransactionCheckBox.setEnabled(dbSessionSelected);
        sessionPerTransactionInfo.setEnabled(dbSessionSelected);
        if (!dbSessionSelected) {
            sessionPerTransactionCheckBox.setSelected(false);
        }
    }

    public boolean isCreateService() {
        return createServiceCheckBox.isSelected();
    }

    public boolean isIncludeDbSession() {
        return includeDbSessionCheckBox.isSelected();
    }

    public boolean isSessionPerTransaction() {
        return sessionPerTransactionCheckBox.isSelected();
    }
}



