package com.intellij.idea.plugin.hybris.toolwindow;

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.notifications.Notifications;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrHttpClient;
import com.intellij.idea.plugin.hybris.toolwindow.document.filter.UnsignedIntegerDocumentFilter;
import com.intellij.idea.plugin.hybris.toolwindow.document.listener.SimpleDocumentListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.OnOffButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Objects;

import static com.intellij.openapi.ui.DialogWrapper.IdeModalityType.PROJECT;

public class RemoteSolrConnectionDialog extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(RemoteSolrConnectionDialog.class);

    private JPanel contentPane;
    private JTextField displayNameTextField;
    private JButton testConnectionButton;
    private JTextField solrWebrootTextField;
    private JLabel hacWebrootLabel;
    private JLabel projectUrlPreviewValueLabel;
    private JLabel projectUrlPreviewLabel;
    private JTextField solrPortTextField;
    private JLabel projectPortLabel;
    private JPasswordField passwordField;
    private JLabel passwordLabel;
    private JLabel loginNameLabel;
    private JTextField loginTextField;
    private JTextField solrIpTextField;
    private JLabel projectIpLabel;
    private OnOffButton sslButton;
    private JLabel sslLabel;
    private Project myProject;
    private HybrisRemoteConnectionSettings mySettings;
    private HybrisRemoteConnectionSettings setting;

    public RemoteSolrConnectionDialog(
            @Nullable final Project project,
            @Nullable final Component parentComponent,
            @NotNull final HybrisRemoteConnectionSettings settings
    ) {
        super(project, parentComponent, false, PROJECT);
        myProject = project;
        mySettings = settings;

        displayNameTextField.setText(mySettings.getDisplayName());
        solrIpTextField.setText(mySettings.getHostIP());
        solrPortTextField.setText(mySettings.getPort());
        ((PlainDocument) solrPortTextField.getDocument()).setDocumentFilter(new UnsignedIntegerDocumentFilter());

        solrWebrootTextField.setText(mySettings.getSolrWebroot());
        loginTextField.setText(mySettings.getAdminLogin());
        passwordField.setText(mySettings.getAdminPassword());
        sslButton.setSelected(mySettings.isSsl());
        setting = new HybrisRemoteConnectionSettings();
        saveSettings(settings);

        final SimpleDocumentListener validateListener = new SimpleDocumentListener() {
            @Override
            public void update(final DocumentEvent e) {
                validateParams();
            }
        };

        final SimpleDocumentListener saveSettingsDocumentListener = new SimpleDocumentListener() {
            @Override
            public void update(final DocumentEvent e) {
                saveSettings(setting);
            }
        };

        validateParams();
        saveSettings(setting);
        init();

        solrIpTextField.getDocument().addDocumentListener(validateListener);
        solrPortTextField.getDocument().addDocumentListener(validateListener);

        displayNameTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        solrIpTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        solrPortTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        solrWebrootTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        displayNameTextField.addActionListener(action->saveSettings(setting));
        solrIpTextField.addActionListener(action->saveSettings(setting));
        solrWebrootTextField.addActionListener(action->saveSettings(setting));
        loginTextField.addActionListener(action->saveSettings(setting));
        passwordField.addActionListener(action->saveSettings(setting));
        sslButton.addActionListener(action->saveSettings(setting));


        Objects.requireNonNull(getButton(getOKAction())).addActionListener(action->saveSettings(mySettings));
        testConnectionButton.addActionListener(action -> testConnection());
    }

    private void testConnection() {
        saveSettings(setting);
        String message;
        NotificationType type;
        try {
            SolrHttpClient.getInstance(myProject).listOfCores(myProject, setting);
            message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.success", "SOLR" , setting.getGeneratedURL());
            type = NotificationType.INFORMATION;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            type = NotificationType.WARNING;
            message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.fail", setting.getGeneratedURL(), e.getMessage());
        }

        Notifications.create(type, HybrisI18NBundleUtils.message("hybris.notification.toolwindow.hac.test.connection.title"), message)
                     .notify(myProject);
    }

    private void saveSettings(final HybrisRemoteConnectionSettings mySettings) {
        mySettings.setSsl(sslButton.isSelected());
        mySettings.setDisplayName(displayNameTextField.getText());
        mySettings.setType(HybrisRemoteConnectionSettings.Type.SOLR);
        mySettings.setHostIP(solrIpTextField.getText());
        mySettings.setPort(solrPortTextField.getText());
        mySettings.setSolrWebroot(solrWebrootTextField.getText());
        mySettings.setAdminLogin(loginTextField.getText());
        mySettings.setAdminPassword(new String(passwordField.getPassword()));
        final String previewUrl = CommonIdeaService.Companion.getInstance().getSolrUrl(myProject, mySettings);
        projectUrlPreviewValueLabel.setText(previewUrl);
        mySettings.setGeneratedURL(previewUrl);
    }

    private void validateParams() {
        final boolean enabledFlag = !solrPortTextField.getText().isEmpty() && !solrIpTextField.getText().isEmpty();

        testConnectionButton.setEnabled(enabledFlag);
        getOKAction().setEnabled(enabledFlag);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
