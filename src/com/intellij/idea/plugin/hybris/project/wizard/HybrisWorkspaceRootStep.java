/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.project.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.AbstractHybrisProjectImportBuilder;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.tasks.SearchHybrisDistributionDirectoryTaskModalWindow;
import com.intellij.idea.plugin.hybris.project.utils.FileUtils;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;
import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettingsComponent;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.projectImport.ProjectImportWizardStep;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message;
import static java.lang.Character.getNumericValue;
import static java.util.Collections.reverse;
import static java.util.Collections.sort;

public class HybrisWorkspaceRootStep extends ProjectImportWizardStep implements OpenSupport, RefreshSupport {

    private static final Logger LOG = Logger.getInstance(HybrisWorkspaceRootStep.class);

    private JPanel rootPanel;
    private TextFieldWithBrowseButton storeModuleFilesInChooser;
    private JCheckBox storeModuleFilesInCheckBox;
    private TextFieldWithBrowseButton sourceCodeFilesInChooser;
    private JTextField projectNameTextField;
    private JCheckBox importOotbModulesInReadOnlyModeCheckBox;
    private TextFieldWithBrowseButton hybrisDistributionDirectoryFilesInChooser;
    private TextFieldWithBrowseButton externalExtensionsDirectoryFilesInChooser;
    private JTextField javadocUrlTextField;
    private JCheckBox sourceCodeCheckBox;
    private JLabel storeModuleFilesInLabel;
    private JPanel directoryOverridePanel;
    private JCheckBox directoryOverrideCheckBox;
    private JLabel directoryOverrideLabel;
    private JLabel sourceCodeLabel;
    private JLabel importOotbModulesInReadOnlyModeLabel;
    private JLabel externalExtensionsPresentLabel;
    private JCheckBox configOverrideCheckBox;
    private JLabel configOverrideLabel;
    private JPanel configOverridePanel;
    private TextFieldWithBrowseButton configOverrideFilesInChooser;
    private JCheckBox dbDriversDirOverrideCheckBox;
    private JPanel dbDriverDirOverridePanel;
    private JLabel dbDriversDirOverrideLabel;
    private TextFieldWithBrowseButton dbDriversDirOverrideFileChooser;
    private JLabel followSimlinkLabel;
    private JCheckBox followSimplinkCheckbox;
    private JLabel scanThroughExternalModuleLabel;
    private JCheckBox scanThroughExternalModuleCheckbox;
    private JCheckBox excludeTestSourcesCheckBox;
    private JLabel excludeTestSourcesLabel;
    private JCheckBox withMavenSources;
    private JCheckBox withMavenJavadocs;
    private JLabel withMavenJavadocsLabel;
    private JLabel withMavenSourcesLabel;
    private JCheckBox withStandardProvidedSources;
    private JLabel withStandardProvidedSourcesLabel;
    private JCheckBox customProjectIconCheckBox;
    private JLabel customProjectIconLabel;
    private TextFieldWithBrowseButton customProjectIconChooser;
    private JCheckBox importCustomAntBuildFilesCheckBox;
    private String hybrisVersion;

    public HybrisWorkspaceRootStep(final WizardContext context) {
        super(context);

        this.storeModuleFilesInChooser.addBrowseFolderListener(
            message(
                "hybris.project.import.select.directory.where.new.idea.module.files.will.be.stored"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.storeModuleFilesInCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                storeModuleFilesInChooser.setEnabled(((JCheckBox) e.getSource()).isSelected());
            }
        });

        this.storeModuleFilesInLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                storeModuleFilesInCheckBox.doClick();
            }
        });

        this.sourceCodeFilesInChooser.setVisible(false);

        this.sourceCodeCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                sourceCodeFilesInChooser.setVisible(((JCheckBox) e.getSource()).isSelected());
            }
        });

        this.sourceCodeLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                sourceCodeCheckBox.doClick();
            }
        });


        customProjectIconChooser.setVisible(false);
        customProjectIconChooser.addActionListener(new MyChooserActionListener(
            FileChooserDescriptorFactory.createSingleFileDescriptor("svg"),
            customProjectIconChooser,
            message("hybris.import.label.select.hybris.project.icon.file"))
        );
        customProjectIconCheckBox.addActionListener(e -> customProjectIconChooser.setVisible(((JCheckBox) e.getSource()).isSelected()));
        customProjectIconLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                customProjectIconCheckBox.doClick();
            }
        });

        this.importOotbModulesInReadOnlyModeLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                importOotbModulesInReadOnlyModeCheckBox.doClick();
            }
        });

        this.directoryOverridePanel.setVisible(false);

        this.directoryOverrideCheckBox.addActionListener(
            e -> directoryOverridePanel.setVisible(((JCheckBox) e.getSource()).isSelected())
        );

        this.directoryOverrideLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                directoryOverrideCheckBox.doClick();
            }
        });

        this.configOverridePanel.setVisible(false);

        this.configOverrideCheckBox.addActionListener(
            e -> configOverridePanel.setVisible(((JCheckBox) e.getSource()).isSelected())
        );

        this.configOverrideLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                configOverrideCheckBox.doClick();
            }
        });

        this.dbDriverDirOverridePanel.setVisible(false);

        this.dbDriversDirOverrideCheckBox.addActionListener(
            e -> dbDriverDirOverridePanel.setVisible(((JCheckBox) e.getSource()).isSelected())
        );

        this.dbDriversDirOverrideLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                dbDriversDirOverrideCheckBox.doClick();
            }
        });

        sourceCodeFilesInChooser.addActionListener(new MyChooserActionListener(
            FileChooserDescriptorFactory.createSingleLocalFileDescriptor(),
            sourceCodeFilesInChooser,
            message("hybris.import.label.select.hybris.src.file"))
        );

        this.hybrisDistributionDirectoryFilesInChooser.addBrowseFolderListener(
            message("hybris.import.label.select.hybris.distribution.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.externalExtensionsDirectoryFilesInChooser.addBrowseFolderListener(
            message("hybris.import.label.select.custom.extensions.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.configOverrideFilesInChooser.addBrowseFolderListener(
            message("hybris.import.label.select.config.extensions.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        this.dbDriversDirOverrideFileChooser.addBrowseFolderListener(
            message("hybris.import.label.select.dbdriver.extensions.directory"),
            "",
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
    }

    @Override
    public JComponent getComponent() {
        return this.rootPanel;
    }

    @Override
    public void updateDataModel() {

        this.getContext().cleanup();

        final var projectDescriptor = this.getContext().getHybrisProjectDescriptor();
        if (this.storeModuleFilesInCheckBox.isSelected()) {
            projectDescriptor.setModulesFilesDirectory(
                FileUtils.toFile(this.storeModuleFilesInChooser.getText())
            );
        }

        this.getWizardContext().setProjectName(this.projectNameTextField.getText());

        final String externalExtensionsDirPath = directoryOverrideCheckBox.isSelected()
            ? externalExtensionsDirectoryFilesInChooser.getText()
            : "";
        projectDescriptor.setExternalExtensionsDirectory(externalExtensionsDirPath.isEmpty()
            ? null
            : FileUtils.toFile(externalExtensionsDirPath)
        );

        projectDescriptor.setExternalConfigDirectory(configOverrideCheckBox.isSelected()
            ? FileUtils.toFile(this.configOverrideFilesInChooser.getText())
            : null
        );

        projectDescriptor.setExternalDbDriversDirectory(dbDriversDirOverrideCheckBox.isSelected()
            ? FileUtils.toFile(this.dbDriversDirOverrideFileChooser.getText())
            : null
        );
        projectDescriptor.setWithMavenSources(withMavenSources.isSelected());
        projectDescriptor.setWithMavenJavadocs(withMavenJavadocs.isSelected());
        projectDescriptor.setWithStandardProvidedSources(withStandardProvidedSources.isSelected());
        projectDescriptor.setJavadocUrl(this.javadocUrlTextField.getText());
        projectDescriptor.setHybrisVersion(hybrisVersion);
        projectDescriptor.setHybrisDistributionDirectory(FileUtils.toFile(this.hybrisDistributionDirectoryFilesInChooser.getText()));
        projectDescriptor.setImportOotbModulesInReadOnlyMode(this.importOotbModulesInReadOnlyModeCheckBox.isSelected());
        projectDescriptor.setFollowSymlink(this.followSimplinkCheckbox.isSelected());
        projectDescriptor.setExcludeTestSources(this.excludeTestSourcesCheckBox.isSelected());
        projectDescriptor.setImportCustomAntBuildFiles(this.importCustomAntBuildFilesCheckBox.isSelected());
        projectDescriptor.setScanThroughExternalModule(this.scanThroughExternalModuleCheckbox.isSelected());
        projectDescriptor.setSourceCodeFile(getValidSourceCode());
        projectDescriptor.setProjectIconFile(getProjectIcon());

        LOG.info("importing a project with the following settings: " + projectDescriptor);

        this.getContext().setRootProjectDirectory(FileUtils.toFile(this.getContext().getFileToImport()));
    }

    private File getValidSourceCode() {
        if (!sourceCodeCheckBox.isSelected()) {
            return null;
        }
        final File sourceCodeFile = new File(this.sourceCodeFilesInChooser.getText());
        if (!sourceCodeFile.exists()) {
            return null;
        }
        if (sourceCodeFile.isDirectory()) {
            final File hybrisFile = new File(this.hybrisDistributionDirectoryFilesInChooser.getText());
            if (sourceCodeFile.getAbsolutePath().equals(hybrisFile.getAbsolutePath())) {
                return null;
            }
        }
        return sourceCodeFile;
    }

    private File getProjectIcon() {
        if (!customProjectIconCheckBox.isSelected()) {
            return null;
        }
        final var projectIconFile = new File(customProjectIconChooser.getText());
        if (!projectIconFile.exists() || projectIconFile.isDirectory()) return null;
        return projectIconFile;
    }

    @Override
    public void updateStep() {
        final HybrisApplicationSettings appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        this.storeModuleFilesInChooser.setText(
            new File(
                this.getBuilder().getFileToImport(), HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
            ).getAbsolutePath()
        );

        this.projectNameTextField.setText(getWizardContext().getProjectName());

        final HybrisProjectDescriptor hybrisProjectDescriptor = this.getContext().getHybrisProjectDescriptor();

        hybrisProjectDescriptor.setImportOotbModulesInReadOnlyMode(
            appSettings.getDefaultPlatformInReadOnly()
        );
        hybrisProjectDescriptor.setFollowSymlink(
            appSettings.getFollowSymlink()
        );
        hybrisProjectDescriptor.setScanThroughExternalModule(
            appSettings.getScanThroughExternalModule()
        );
        hybrisProjectDescriptor.setExcludeTestSources(appSettings.getExcludeTestSources());
        hybrisProjectDescriptor.setImportCustomAntBuildFiles(appSettings.getImportCustomAntBuildFiles());
        hybrisProjectDescriptor.setWithMavenSources(appSettings.getWithMavenSources());
        hybrisProjectDescriptor.setWithMavenJavadocs(appSettings.getWithMavenJavadocs());
        hybrisProjectDescriptor.setWithStandardProvidedSources(appSettings.getWithStandardProvidedSources());
        this.importOotbModulesInReadOnlyModeCheckBox.setSelected(hybrisProjectDescriptor.isImportOotbModulesInReadOnlyMode());
        this.scanThroughExternalModuleCheckbox.setSelected(hybrisProjectDescriptor.isScanThroughExternalModule());
        this.followSimplinkCheckbox.setSelected(hybrisProjectDescriptor.isFollowSymlink());
        this.excludeTestSourcesCheckBox.setSelected(hybrisProjectDescriptor.isExcludeTestSources());
        this.importCustomAntBuildFilesCheckBox.setSelected(hybrisProjectDescriptor.isImportCustomAntBuildFiles());

        if (StringUtils.isBlank(this.hybrisDistributionDirectoryFilesInChooser.getText())) {

            ProgressManager.getInstance().run(new SearchHybrisDistributionDirectoryTaskModalWindow(
                new File(this.getBuilder().getFileToImport()), parameter -> {
                hybrisDistributionDirectoryFilesInChooser.setText(parameter);

                if (StringUtils.isBlank(sourceCodeFilesInChooser.getText())) {
                    sourceCodeFilesInChooser.setText(hybrisDistributionDirectoryFilesInChooser.getText());
                }
            }
            ));
        }

        if (StringUtils.isNotBlank(this.hybrisDistributionDirectoryFilesInChooser.getText())) {

            if (StringUtils.isBlank(this.externalExtensionsDirectoryFilesInChooser.getText())) {

                this.externalExtensionsDirectoryFilesInChooser.setText(
                    new File(
                        this.hybrisDistributionDirectoryFilesInChooser.getText(),
                        HybrisConstants.CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH
                    ).getAbsolutePath()
                );
            }

            if (StringUtils.isBlank(this.configOverrideFilesInChooser.getText())) {
                this.configOverrideFilesInChooser.setText(
                    new File(
                        this.hybrisDistributionDirectoryFilesInChooser.getText(),
                        HybrisConstants.EXTENSION_NAME_CONFIG
                    ).getAbsolutePath()
                );
            }

            if (StringUtils.isBlank(this.dbDriversDirOverrideFileChooser.getText())) {
                String dbDriversDirAbsPath = new File(
                    this.hybrisDistributionDirectoryFilesInChooser.getText(),
                    HybrisConstants.PLATFORM_MODULE_PREFIX + HybrisConstants.PLATFORM_DB_DRIVER
                ).getAbsolutePath();

                if (StringUtils.isNotEmpty(appSettings.getExternalDbDriversDirectory())) {
                    final File dbDriversDir = new File(appSettings.getExternalDbDriversDirectory());
                    if (dbDriversDir.isDirectory()) {
                        dbDriversDirAbsPath = dbDriversDir.getAbsolutePath();
                        if (!dbDriversDirOverrideCheckBox.isSelected()) {
                            dbDriversDirOverrideCheckBox.doClick();
                        }
                    }
                }
                this.dbDriversDirOverrideFileChooser.setText(dbDriversDirAbsPath);
            }

            hybrisVersion = getHybrisVersion(this.hybrisDistributionDirectoryFilesInChooser.getText(), false);
            final String sourceCodeDirectory = appSettings.getSourceCodeDirectory();
            final File sourceFile = appSettings.getSourceZipUsed()
                ? findSourceZip(sourceCodeDirectory, hybrisVersion)
                : new File(sourceCodeDirectory);

            if (sourceFile != null) {
                if (!sourceCodeCheckBox.isSelected()) {
                    sourceCodeCheckBox.doClick();
                }
                sourceCodeFilesInChooser.setText(sourceFile.getPath());
            }

            final String defaultJavadocUrl = getDefaultJavadocUrl(getHybrisVersion(this.hybrisDistributionDirectoryFilesInChooser.getText(), true));
            if (StringUtils.isNotBlank(defaultJavadocUrl)) {
                this.javadocUrlTextField.setText(defaultJavadocUrl);
            }
        }

        if (StringUtils.isBlank(this.sourceCodeFilesInChooser.getText())) {
            sourceCodeFilesInChooser.setText(this.hybrisDistributionDirectoryFilesInChooser.getText());
        }

    }

    private File findSourceZip(final String sourceCodeDir, final String hybrisApiVersion) {
        if (StringUtils.isBlank(sourceCodeDir) || StringUtils.isBlank(hybrisApiVersion)) {
            return null;
        }
        File sourceCodeDirectory = new File(sourceCodeDir);
        if (!sourceCodeDirectory.isDirectory()) {
            return null;
        }
        final List<File> sourceZipList =
            Arrays.stream(sourceCodeDirectory.listFiles())
                  .filter(e -> e.getName().endsWith(".zip"))
                  .filter(e -> e.getName().contains(hybrisApiVersion))
                  .collect(Collectors.toList());
        if (sourceZipList.isEmpty()) {
            return null;
        }
        if (sourceZipList.size() > 1) {
            sort(sourceZipList, new Comparator<File>() {

                @Override
                public int compare(final File zip1, final File zip2) {
                    return getPatch(zip1, hybrisApiVersion).compareTo(getPatch(zip2, hybrisApiVersion));
                }
            });
            reverse(sourceZipList);
        }
        return sourceZipList.get(0);
    }

    private Integer getPatch(@NotNull final File sourceZip, @NotNull final String hybrisApiVersion) {
        final String name = sourceZip.getName();
        final int index = name.indexOf(hybrisApiVersion);
        final char firstDigit = name.charAt(index + hybrisApiVersion.length() + 1);
        final char secondDigit = name.charAt(index + hybrisApiVersion.length() + 2);
        if (Character.isDigit(secondDigit)) {
            return getNumericValue(firstDigit) * 10 + getNumericValue(secondDigit);
        }
        return getNumericValue(firstDigit);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (!super.validate()) {
            return false;
        }

        if (this.storeModuleFilesInCheckBox.isSelected()) {
            final File moduleFilesDirectory = new File(this.storeModuleFilesInChooser.getText());
            if (!moduleFilesDirectory.exists()) {
                return moduleFilesDirectory.mkdirs();
            }
        }

        if (directoryOverrideCheckBox.isSelected()) {
            if (!new File(this.externalExtensionsDirectoryFilesInChooser.getText()).isDirectory()) {
                throw new ConfigurationException(
                    message(
                        "hybris.import.wizard.validation.custom.extensions.directory.does.not.exist"));
            }
        }
        if (configOverrideCheckBox.isSelected()) {
            if (!new File(this.configOverrideFilesInChooser.getText()).isDirectory()) {
                throw new ConfigurationException(
                    message(
                        "hybris.import.wizard.validation.config.directory.does.not.exist"));
            }
        }

        if (dbDriversDirOverrideCheckBox.isSelected()) {
            if (!new File(this.dbDriversDirOverrideFileChooser.getText()).isDirectory()) {
                throw new ConfigurationException(
                    message(
                        "hybris.import.wizard.validation.dbdriver.directory.does.not.exist"));
            }
        }

        if (StringUtils.isBlank(this.hybrisDistributionDirectoryFilesInChooser.getText())) {
            throw new ConfigurationException(
                message("hybris.import.wizard.validation.hybris.distribution.directory.empty")
            );
        }

        if (!new File(this.hybrisDistributionDirectoryFilesInChooser.getText()).isDirectory()) {
            throw new ConfigurationException(
                message(
                    "hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"));
        }

        return true;
    }

    private AbstractHybrisProjectImportBuilder getContext() {
        return (AbstractHybrisProjectImportBuilder) this.getBuilder();
    }

    @Nullable
    private String getHybrisVersion(@NotNull final String hybrisRootDir, final boolean apiOnly) {
        Validate.notNull(hybrisRootDir);

        final File buildInfoFile = new File(hybrisRootDir + HybrisConstants.BUILD_NUMBER_FILE_PATH);
        final Properties buildProperties = new Properties();

        try (final FileInputStream fis = new FileInputStream(buildInfoFile)) {
            buildProperties.load(fis);
        } catch (IOException e) {
            return null;
        }

        if (!apiOnly) {
            final String version = buildProperties.getProperty(HybrisConstants.HYBRIS_VERSION_KEY);
            if (version != null) {
                return version;
            }
        }

        return buildProperties.getProperty(HybrisConstants.HYBRIS_API_VERSION_KEY);
    }

    @Nullable
    private String getDefaultJavadocUrl(@Nullable final String hybrisApiVersion) {
        return StringUtils.isNotEmpty(hybrisApiVersion)
            ? String.format(HybrisConstants.JAVADOC_URL, hybrisApiVersion)
            : HybrisConstants.JAVADOC_FALLBACK_URL;
    }

    @Override
    public void open(@Nullable final HybrisProjectSettings settings) throws ConfigurationException {
        updateStep();
        updateDataModel();
    }

    @Override
    public void refresh(final HybrisProjectSettings settings) throws ConfigurationException {

        this.getContext().cleanup();

        final var hybrisProjectDescriptor = this.getContext().getHybrisProjectDescriptor();

        hybrisProjectDescriptor.setSourceCodeFile(FileUtils.toFile(settings.getSourceCodeFile(), true));
        hybrisProjectDescriptor.setExternalExtensionsDirectory(FileUtils.toFile(settings.getExternalExtensionsDirectory(), true));
        hybrisProjectDescriptor.setExternalConfigDirectory(FileUtils.toFile(settings.getExternalConfigDirectory(), true));
        hybrisProjectDescriptor.setExternalDbDriversDirectory(FileUtils.toFile(settings.getExternalDbDriversDirectory(), true));
        hybrisProjectDescriptor.setImportOotbModulesInReadOnlyMode(settings.getImportOotbModulesInReadOnlyMode());
        hybrisProjectDescriptor.setFollowSymlink(settings.getFollowSymlink());
        hybrisProjectDescriptor.setExcludeTestSources(settings.getExcludeTestSources());
        hybrisProjectDescriptor.setImportCustomAntBuildFiles(settings.getImportCustomAntBuildFiles());
        hybrisProjectDescriptor.setScanThroughExternalModule(settings.getScanThroughExternalModule());

        final var appSettings = HybrisApplicationSettingsComponent.getInstance().getState();
        hybrisProjectDescriptor.setWithMavenSources(appSettings.getWithMavenSources());
        hybrisProjectDescriptor.setWithMavenJavadocs(appSettings.getWithMavenJavadocs());
        hybrisProjectDescriptor.setWithStandardProvidedSources(appSettings.getWithStandardProvidedSources());

        final var ideModulesFilesDirectory = settings.getIdeModulesFilesDirectory();
        if (ideModulesFilesDirectory != null) {
            hybrisProjectDescriptor.setModulesFilesDirectory(
                new File(ideModulesFilesDirectory)
            );
        } else {
            hybrisProjectDescriptor.setModulesFilesDirectory(
                new File(
                    this.getBuilder().getFileToImport(),
                    HybrisConstants.DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES
                )
            );
        }

        final var hybrisDirectory = settings.getHybrisDirectory();
        if (hybrisDirectory != null) {
            hybrisProjectDescriptor.setHybrisDistributionDirectory(
                FileUtils.toFile(
                    this.getBuilder().getFileToImport(),
                    settings.getHybrisDirectory()
                )
            );
        }

        hybrisProjectDescriptor.setJavadocUrl(settings.getJavadocUrl());
        hybrisProjectDescriptor.setHybrisVersion(settings.getHybrisVersion());

        this.getContext().setRootProjectDirectory(FileUtils.toFile(this.getBuilder().getFileToImport()));

        if (hybrisDirectory == null) {
            // refreshing a project which was never imported by this plugin
            ProgressManager.getInstance().run(new SearchHybrisDistributionDirectoryTaskModalWindow(
                FileUtils.toFile(this.getBuilder().getFileToImport()), parameter -> hybrisProjectDescriptor.setHybrisDistributionDirectory(FileUtils.toFile(parameter))
            ));
        }

        LOG.info("refreshing a project with the following settings: " + this.getContext().getHybrisProjectDescriptor());
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private static class MyChooserActionListener
        extends ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> {

        MyChooserActionListener(final FileChooserDescriptor fileChooserDescriptor, final TextFieldWithBrowseButton chooser, final @NotNull String title) {
            super(
                title,
                "",
                chooser,
                null,
                fileChooserDescriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            );
        }
    }

}
