/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS;

@State(name = "HybrisDeveloperSpecificProjectSettings", storages = {@Storage(value = STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS, roamingType = RoamingType.DISABLED)})
@Service(Service.Level.PROJECT)
public final class HybrisDeveloperSpecificProjectSettingsComponent implements PersistentStateComponent<HybrisDeveloperSpecificProjectSettings> {

    private final HybrisDeveloperSpecificProjectSettings state = new HybrisDeveloperSpecificProjectSettings();


    public static HybrisDeveloperSpecificProjectSettingsComponent getInstance(@NotNull final Project project) {
        return project.getService(HybrisDeveloperSpecificProjectSettingsComponent.class);
    }

    @NotNull
    @Override
    public HybrisDeveloperSpecificProjectSettings getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull final HybrisDeveloperSpecificProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

}
