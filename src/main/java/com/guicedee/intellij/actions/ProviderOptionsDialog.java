package com.guicedee.intellij.actions;

import com.intellij.ide.util.TreeJavaClassChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * Dialog shown when creating a new GuicedEE Guice {@code Provider}.
 * <p>
 * Collects the provider class name, the provided type (which may be generic, e.g.
 * {@code com.example.Farm<?>}), whether the provider should participate in
 * persistence (injecting a {@code Mutiny.SessionFactory} and an optional service),
 * whether to annotate it with {@code @CallScope}, and — for generic provided types —
 * the Guice module that the {@code TypeLiteral} binding should be added to.
 */
public class ProviderOptionsDialog extends DialogWrapper implements DocumentListener {
    private final Project project;
    private final List<PsiClass> moduleCandidates;

    private JBTextField providerNameField;
    private JBTextField providedTypeField;
    private FixedSizeButton providedTypeChooser;

    private JBCheckBox usePersistenceCheckBox;
    private JBTextField serviceTypeField;
    private FixedSizeButton serviceTypeChooser;

    private JBCheckBox callScopeCheckBox;

    private JComboBox<PsiClass> moduleComboBox;
    private JBLabel genericHint;

    public ProviderOptionsDialog(Project project, List<PsiClass> moduleCandidates) {
        super(project, true);
        this.project = project;
        this.moduleCandidates = moduleCandidates;
        setModal(true);
        setTitle("Create New Provider");
        init();
        validate0();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // Provider class name
        JPanel namePanel = new JPanel(new BorderLayout(6, 0));
        namePanel.add(new JBLabel("Provider class name:"), BorderLayout.WEST);
        providerNameField = new JBTextField();
        providerNameField.getDocument().addDocumentListener(this);
        namePanel.add(providerNameField, BorderLayout.CENTER);
        panel.add(namePanel, gbc);

        // Provided type (+ chooser)
        gbc.gridy++;
        JPanel typePanel = new JPanel(new BorderLayout(6, 0));
        typePanel.add(new JBLabel("Provided type:"), BorderLayout.WEST);
        providedTypeField = new JBTextField();
        providedTypeField.setToolTipText("Fully-qualified type to provide. May be generic, e.g. com.example.Farm<?>");
        providedTypeField.getDocument().addDocumentListener(this);
        typePanel.add(providedTypeField, BorderLayout.CENTER);
        providedTypeChooser = new FixedSizeButton(providedTypeField);
        providedTypeChooser.addActionListener(e -> chooseClass(providedTypeField, "Select Provided Class"));
        typePanel.add(providedTypeChooser, BorderLayout.EAST);
        panel.add(typePanel, gbc);

        // Generic hint
        gbc.gridy++;
        genericHint = new JBLabel(" ");
        genericHint.setForeground(UIManager.getColor("Label.infoForeground"));
        panel.add(genericHint, gbc);

        // @CallScope
        gbc.gridy++;
        callScopeCheckBox = new JBCheckBox("Annotate with @CallScope");
        panel.add(callScopeCheckBox, gbc);

        // Use persistence
        gbc.gridy++;
        usePersistenceCheckBox = new JBCheckBox("Use persistence (inject Mutiny.SessionFactory)");
        usePersistenceCheckBox.addActionListener(e -> updateEnabledStates());
        panel.add(usePersistenceCheckBox, gbc);

        // Injected service type (+ chooser), indented
        gbc.gridy++;
        gbc.insets = JBUI.insets(4, 24, 4, 0);
        JPanel servicePanel = new JPanel(new BorderLayout(6, 0));
        servicePanel.add(new JBLabel("Injected service type (optional):"), BorderLayout.WEST);
        serviceTypeField = new JBTextField();
        serviceTypeField.setToolTipText("Optional service to inject, e.g. com.example.IFarmService<?>");
        servicePanel.add(serviceTypeField, BorderLayout.CENTER);
        serviceTypeChooser = new FixedSizeButton(serviceTypeField);
        serviceTypeChooser.addActionListener(e -> chooseClass(serviceTypeField, "Select Service Class"));
        servicePanel.add(serviceTypeChooser, BorderLayout.EAST);
        panel.add(servicePanel, gbc);

        // Target module for the generic binding
        gbc.gridy++;
        gbc.insets = JBUI.insets(4, 0);
        JPanel modulePanel = new JPanel(new BorderLayout(6, 0));
        modulePanel.add(new JBLabel("Add generic binding to module:"), BorderLayout.WEST);
        moduleComboBox = new JComboBox<>();
        moduleComboBox.addItem(null); // "(none)"
        for (PsiClass module : moduleCandidates) {
            moduleComboBox.addItem(module);
        }
        moduleComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String text = "(none)";
                if (value instanceof PsiClass) {
                    String qn = ((PsiClass) value).getQualifiedName();
                    text = qn != null ? qn : ((PsiClass) value).getName();
                }
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        if (!moduleCandidates.isEmpty()) {
            moduleComboBox.setSelectedItem(moduleCandidates.get(0));
        }
        modulePanel.add(moduleComboBox, BorderLayout.CENTER);
        panel.add(modulePanel, gbc);

        panel.setPreferredSize(new Dimension(560, panel.getPreferredSize().height));

        updateEnabledStates();
        return panel;
    }

    private void chooseClass(JTextField target, String title) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        TreeJavaClassChooserDialog chooser = new TreeJavaClassChooserDialog(title, project, scope, null, null);
        String current = stripGenerics(target.getText().trim());
        if (!current.isEmpty()) {
            PsiClass currentClass = JavaPsiFacade.getInstance(project).findClass(current, scope);
            if (currentClass != null) {
                chooser.select(currentClass);
            }
        }
        chooser.show();
        PsiClass selected = chooser.getSelected();
        if (selected != null && selected.getQualifiedName() != null) {
            // Preserve any generics the user already typed
            String existing = target.getText().trim();
            int lt = existing.indexOf('<');
            String generics = lt >= 0 ? existing.substring(lt) : "";
            target.setText(selected.getQualifiedName() + generics);
            validate0();
        }
    }

    private void updateEnabledStates() {
        boolean persistence = usePersistenceCheckBox.isSelected();
        serviceTypeField.setEnabled(persistence);
        serviceTypeChooser.setEnabled(persistence);

        boolean generic = isGeneric();
        if (generic) {
            genericHint.setText("Generic type detected — a TypeLiteral binding will be added to the selected module.");
        } else {
            genericHint.setText(" ");
        }
    }

    private void validate0() {
        PsiNameHelper nameHelper = PsiNameHelper.getInstance(project);
        boolean validName = nameHelper.isIdentifier(getProviderName());
        boolean validType = nameHelper.isQualifiedName(stripGenerics(getProvidedType()));
        getOKAction().setEnabled(validName && validType);
        updateEnabledStates();
    }

    private static String stripGenerics(String type) {
        if (type == null) return "";
        int lt = type.indexOf('<');
        return (lt >= 0 ? type.substring(0, lt) : type).trim();
    }

    // ----- public accessors -----

    public String getProviderName() {
        return providerNameField.getText().trim();
    }

    /** The provided type exactly as entered, possibly including generics. */
    public String getProvidedType() {
        return providedTypeField.getText().trim();
    }

    public boolean isGeneric() {
        return getProvidedType().contains("<");
    }

    public boolean isUsePersistence() {
        return usePersistenceCheckBox.isSelected();
    }

    /** The optional service type to inject (may be empty), possibly including generics. */
    public String getServiceType() {
        return serviceTypeField.getText().trim();
    }

    public boolean isCallScope() {
        return callScopeCheckBox.isSelected();
    }

    /** The Guice module class chosen for the generic binding, or {@code null} for none. */
    public @Nullable PsiClass getSelectedModule() {
        Object selected = moduleComboBox.getSelectedItem();
        return selected instanceof PsiClass ? (PsiClass) selected : null;
    }

    // ----- DocumentListener -----

    @Override
    public void insertUpdate(DocumentEvent e) {
        validate0();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validate0();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validate0();
    }
}




