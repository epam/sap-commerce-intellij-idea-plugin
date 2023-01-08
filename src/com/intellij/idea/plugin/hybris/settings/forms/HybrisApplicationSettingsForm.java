/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.settings.forms;

import com.intellij.idea.plugin.hybris.settings.HybrisApplicationSettings;

import javax.swing.*;

/**
 * Created 21:58 29 March 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class HybrisApplicationSettingsForm {

    private JPanel mainPanel;
    private JCheckBox hideEmptyMiddleFoldersCheckBox;
    private JCheckBox defaultPlatformInReadOnly;
    private JCheckBox followSymlink;
    private JCheckBox withMavenSources;
    private JCheckBox withMavenJavadocs;
    private JCheckBox scanThroughExternalModule;
    private JCheckBox excludeTestSources;
    private JCheckBox warnIfGeneratedItemsAreOutOfDateCheckBox;
    private JCheckBox withStandardProvidedSources;

    public void setData(final HybrisApplicationSettings data) {
        hideEmptyMiddleFoldersCheckBox.setSelected(data.isHideEmptyMiddleFolders());
        defaultPlatformInReadOnly.setSelected(data.isDefaultPlatformInReadOnly());
        followSymlink.setSelected(data.isFollowSymlink());
        withMavenSources.setSelected(data.isWithMavenSources());
        withMavenJavadocs.setSelected(data.isWithMavenJavadocs());
        withStandardProvidedSources.setSelected(data.isWithStandardProvidedSources());
        scanThroughExternalModule.setSelected(data.isScanThroughExternalModule());
        excludeTestSources.setSelected(data.isExcludeTestSources());
        warnIfGeneratedItemsAreOutOfDateCheckBox.setSelected(data.isWarnIfGeneratedItemsAreOutOfDate());
    }

    public void getData(final HybrisApplicationSettings data) {
        data.setHideEmptyMiddleFolders(hideEmptyMiddleFoldersCheckBox.isSelected());
        data.setDefaultPlatformInReadOnly(defaultPlatformInReadOnly.isSelected());
        data.setFollowSymlink(followSymlink.isSelected());
        data.setWithMavenSources(withMavenSources.isSelected());
        data.setWithMavenJavadocs(withMavenJavadocs.isSelected());
        data.setWithStandardProvidedSources(withStandardProvidedSources.isSelected());
        data.setScanThroughExternalModule(scanThroughExternalModule.isSelected());
        data.setExcludeTestSources(excludeTestSources.isSelected());
        data.setWarnIfGeneratedItemsAreOutOfDate(warnIfGeneratedItemsAreOutOfDateCheckBox.isSelected());
    }

    public boolean isModified(final HybrisApplicationSettings data) {
        return hideEmptyMiddleFoldersCheckBox.isSelected() != data.isHideEmptyMiddleFolders()
            || defaultPlatformInReadOnly.isSelected() != data.isDefaultPlatformInReadOnly()
            || scanThroughExternalModule.isSelected() != data.isScanThroughExternalModule()
            || followSymlink.isSelected() != data.isFollowSymlink()
            || withMavenSources.isSelected() != data.isWithMavenSources()
            || withMavenJavadocs.isSelected() != data.isWithMavenJavadocs()
            || withStandardProvidedSources.isSelected() != data.isWithStandardProvidedSources()
            || excludeTestSources.isSelected() != data.isExcludeTestSources()
            || warnIfGeneratedItemsAreOutOfDateCheckBox.isSelected() != data.isWarnIfGeneratedItemsAreOutOfDate();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

}
