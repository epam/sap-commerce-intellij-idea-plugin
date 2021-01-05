package com.intellij.idea.plugin.hybris.toolwindow;

import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.notifications.NotificationUtil;
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

import static com.intellij.openapi.ui.DialogWrapper.IdeModalityType.PROJECT;

public class RemoteConnectionDialog extends DialogWrapper {

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
    private Project myProject;
    private HybrisRemoteConnectionSettings mySettings;

    public RemoteConnectionDialog(
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
        ((PlainDocument)projectPortTextField.getDocument()).setDocumentFilter(new UnsignedIntegerDocumentFilter());
        hacWebrootTextField.setText(mySettings.getHacWebroot());
        loginTextField.setText(mySettings.getHacLogin());
        passwordField.setText(mySettings.getHacPassword());
        sslButton.setSelected(mySettings.isSsl());

        final SimpleDocumentListener saveSettingsDocumentListener = new SimpleDocumentListener() {
            @Override
            public void update(final DocumentEvent e) {
                saveSettings();
            }
        };

        saveSettings();
        init();

        displayNameTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        projectIpTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        projectPortTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        hacWebrootTextField.getDocument().addDocumentListener(saveSettingsDocumentListener);
        displayNameTextField.addActionListener(action->saveSettings());
        projectIpTextField.addActionListener(action->saveSettings());
        hacWebrootTextField.addActionListener(action->saveSettings());
        loginTextField.addActionListener(action->saveSettings());
        passwordField.addActionListener(action->saveSettings());
        sslButton.addActionListener(action->saveSettings());
        testConnectionButton.addActionListener(action->testConnection());
    }

    private void testConnection() {
        saveSettings();

        HybrisHacHttpClient hybrisHacHttpClient = HybrisHacHttpClient.getInstance(myProject);
        final String errorMessage = hybrisHacHttpClient.login(myProject, mySettings);
        final String testedHacURL = hybrisHacHttpClient.getHostHacURL(myProject, mySettings);

        final NotificationType type;
        final String message;
        if (errorMessage.isEmpty()) {
            message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.success", "hac", testedHacURL);
            type = NotificationType.INFORMATION;
        } else {
            type = NotificationType.WARNING;
            message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.fail", testedHacURL, errorMessage);
        }

        NotificationUtil.NOTIFICATION_GROUP.createNotification(
            HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.title"), message, type, null
        ).notify(myProject);
    }

    private void saveSettings() {
        mySettings.setSsl(sslButton.isSelected());
        sslButton.updateUI();
        mySettings.setDisplayName(displayNameTextField.getText());
        mySettings.setHostIP(projectIpTextField.getText());
        mySettings.setPort(projectPortTextField.getText());
        mySettings.setHacWebroot(hacWebrootTextField.getText());
        mySettings.setHacLogin(loginTextField.getText());
        mySettings.setHacPassword(new String(passwordField.getPassword()));
        final String previewUrl = CommonIdeaService.getInstance().getHostHacUrl(myProject, mySettings);
        projectUrlPreviewValueLabel.setText(previewUrl);
        mySettings.setGeneratedURL(previewUrl);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void createUIComponents() {
    }
}
