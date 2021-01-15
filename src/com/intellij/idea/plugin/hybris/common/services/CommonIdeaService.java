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

package com.intellij.idea.plugin.hybris.common.services;


import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Created 10:20 PM 10 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public interface CommonIdeaService {

    boolean isTypingActionInProgress();

    @NotNull
    Optional<String> getHybrisDirectory(@NotNull Project project);

    @NotNull
    Optional<String> getCustomDirectory(@NotNull Project project);

    boolean isHybrisProject(@NotNull Project project);

    boolean isOutDatedHybrisProject(@NotNull Project project);

    boolean isPotentiallyHybrisProject(@NotNull Project project);

    PlatformHybrisModuleDescriptor getPlatformDescriptor(HybrisProjectDescriptor hybrisProjectDescriptor);

    @NotNull
    static CommonIdeaService getInstance() {
        return ServiceManager.getService(CommonIdeaService.class);
    }

    String getActiveHacUrl(@NotNull Project project);

    String getHostHacUrl(@NotNull final Project project, @Nullable HybrisRemoteConnectionSettings mySettings);

    String getSolrUrl(Project project, HybrisRemoteConnectionSettings settings);

    String getBackofficeWebInfLib(Project project);

    String getBackofficeWebInfClasses(Project project);

    void fixRemoteConnectionSettings(Project project);
}
