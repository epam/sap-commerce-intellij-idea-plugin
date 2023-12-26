/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS;

@State(name = "[y] Global Settings",
       category = SettingsCategory.PLUGINS,
       storages = {@Storage(value = STORAGE_HYBRIS_INTEGRATION_SETTINGS, roamingType = RoamingType.DISABLED)})
@Service
public final class HybrisApplicationSettingsComponent implements PersistentStateComponent<HybrisApplicationSettings> {

    private final HybrisApplicationSettings hybrisApplicationSettings = new HybrisApplicationSettings();

    @NotNull
    public static HybrisApplicationSettingsComponent getInstance() {
        return ApplicationManager.getApplication().getService(HybrisApplicationSettingsComponent.class);
    }

    @NotNull
    @Override
    public HybrisApplicationSettings getState() {
        return this.hybrisApplicationSettings;
    }

    @Override
    public void loadState(final @NotNull HybrisApplicationSettings state) {
        XmlSerializerUtil.copyBean(state, this.hybrisApplicationSettings);
    }

    public static String[] toIdeaGroup(final String group) {
        if (group == null || group.trim().isEmpty()) {
            return null;
        }
        return StringUtils.split(group, " ,.;>/\\");
    }
}
