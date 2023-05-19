package com.intellij.idea.plugin.hybris.toolwindow;

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.notifications.Notifications;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient;
import com.intellij.idea.plugin.hybris.toolwindow.document.filter.UnsignedIntegerDocumentFilter;
import com.intellij.idea.plugin.hybris.toolwindow.document.listener.SimpleDocumentListener;
import com.intellij.notification.NotificationType;
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

public class RemoteHacConnectionDialog extends DialogWrapper {

    private JPanel contentPane;
    private JTextField projectIpTextField;
    private JLabel projectIpLabel;
    private JTextField loginTextField;
    private JPasswordField passwordField;
    private JButton testConnectionButton;
    private JLabel loginNameLabel;
    private JLabel passwordLabel;
    private JLabel projectPortLabel;
    private JTextField projectPortTextField;
    private JLabel projectUrlPreviewValueLabel;
    private JLabel projectUrlPreviewLabel;
    private JLabel hacWebrootLabel;
    private JTextField hacWebrootTextField;
    private JTextField displayNameTextField;
    private OnOffButton sslButton;
    private JLabel sslLabel;
    private JComboBox sslProtocol;
    private JLabel sslProtocolLabel;

    private Project myProject;
    private HybrisRemoteConnectionSettings mySettings;
    private HybrisRemoteConnectionSettings setting;

    public RemoteHacConnectionDialog(
        @Nullable final Project project,
        @Nullable final Component parentComponent,
        @NotNull final HybrisRemoteConnectionSettings settings
    ) {
        super(project, parentComponent, false, PROJECT);
        myProject = project;
        mySettings = settings;

        displayNameTextField.setText(mySettings.getDisplayName());
        projectIpTextField.setText(mySettings.getHostIP());
        projectPortTextField.setText(mySettings.getPort());
        ((PlainDocument) projectPortTextField.getDocument()).setDocumentFilter(new UnsignedIntegerDocumentFilter());
        hacWebrootTextField.setText(mySettings.getHacWebroot());
        loginTextField.setText(mySettings.getHacLogin());
        passwordField.setText(mySettings.getHacPassword());
        sslButton.setSelected(mySettings.isSsl());
        if (mySettings.getSslProtocol() != null) {
            sslProtocol.setSelectedItem(mySettings.getSslProtocol());
        }
        setting = new HybrisRemoteConnectionSettings();
        saveSettings(settings);

        final SimpleDocumentListener validateDocumentListener = new SimpleDocumentListener() {

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
        init();

        projectPortTextField.getDocument().addDocumentListener(validateDocumentListener);
        projectIpTextField.getDocument().addDocumentListener(validateDocumentListener);
        displayNameTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        projectIpTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        projectPortTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        hacWebrootTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        displayNameTextField.addActionListener(action->saveSettings(setting));
        projectIpTextField.addActionListener(action->saveSettings(setting));
        hacWebrootTextField.addActionListener(action->saveSettings(setting));
        loginTextField.addActionListener(action->saveSettings(setting));
        passwordField.addActionListener(action->saveSettings(setting));
        sslButton.addActionListener(action-> toggleSslButton(setting));
        sslProtocol.addActionListener(action->saveSettings(setting));

        Objects.requireNonNull(getButton(getOKAction())).addActionListener(action->saveSettings(mySettings));
        testConnectionButton.addActionListener(action -> testConnection());
    }

    private void testConnection() {
        saveSettings(setting);
        HybrisHacHttpClient hybrisHacHttpClient = HybrisHacHttpClient.getInstance(myProject);
        final String errorMessage = hybrisHacHttpClient.login(myProject, setting);
        final String testedHacURL = hybrisHacHttpClient.getHostHacURL(myProject, setting);

        final NotificationType type;
        final String message;
        if (errorMessage.isEmpty()) {
            message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.success", "hac", testedHacURL);
            type = NotificationType.INFORMATION;
        } else {
            type = NotificationType.WARNING;
            message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.fail", testedHacURL, errorMessage);
        }

        Notifications.create(type, HybrisI18NBundleUtils.message("hybris.notification.toolwindow.hac.test.connection.title"), message)
                     .notify(myProject);
    }

    private void toggleSslButton(final HybrisRemoteConnectionSettings setting) {
        sslProtocol.setEnabled(sslButton.isSelected());
        saveSettings(setting);
    }

    private void saveSettings(final HybrisRemoteConnectionSettings mySettings) {
        mySettings.setType(HybrisRemoteConnectionSettings.Type.Hybris);
        mySettings.setSsl(sslButton.isSelected());
        mySettings.setSslProtocol((String) sslProtocol.getSelectedItem());
        mySettings.setDisplayName(displayNameTextField.getText());
        mySettings.setHostIP(projectIpTextField.getText());
        mySettings.setPort(projectPortTextField.getText());
        mySettings.setHacWebroot(hacWebrootTextField.getText());
        mySettings.setHacLogin(loginTextField.getText());
        mySettings.setHacPassword(new String(passwordField.getPassword()));
        final String previewUrl = CommonIdeaService.Companion.getInstance().getHostHacUrl(myProject, mySettings);
        projectUrlPreviewValueLabel.setText(previewUrl);
        mySettings.setGeneratedURL(previewUrl);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void validateParams() {
        testConnectionButton
                .setEnabled(!projectPortTextField.getText().isEmpty()
                        && !projectIpTextField.getText().isEmpty());

        getOKAction().setEnabled(!projectPortTextField.getText().isEmpty()
                && !projectIpTextField.getText().isEmpty());
    }

    private void createUIComponents() {
    }
}
