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

package com.intellij.idea.plugin.hybris.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS;

@State(name = "[y] Global Settings",
       category = SettingsCategory.PLUGINS,
       storages = {@Storage(value = STORAGE_HYBRIS_INTEGRATION_SETTINGS)})
public class HybrisApplicationSettingsComponent implements PersistentStateComponent<HybrisApplicationSettings> {

    protected final HybrisApplicationSettings hybrisApplicationSettings = new HybrisApplicationSettings();

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
    public void loadState(final HybrisApplicationSettings state) {
        XmlSerializerUtil.copyBean(state, this.hybrisApplicationSettings);
    }

    public static String[] toIdeaGroup(final String group) {
        if (group == null || group.trim().isEmpty()) {
            return null;
        }
        return StringUtils.split(group, " ,.;>/\\");
    }
}
