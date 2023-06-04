/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.project.configurators.ConfiguratorFactory;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionsArea;

// TODO: remove this, free-Plugin is NOT extendible
public final class ConfiguratorFactoryProvider {

    private ConfiguratorFactoryProvider() {
    }

    public static ConfiguratorFactory get() {
        final Application application = ApplicationManager.getApplication();
        final ExtensionsArea extensionsArea = application.getExtensionArea();

        if (extensionsArea.hasExtensionPoint(HybrisConstants.CONFIGURATOR_FACTORY_ID)) {
            final ExtensionPoint<ConfiguratorFactory> ep = extensionsArea.getExtensionPoint(HybrisConstants.CONFIGURATOR_FACTORY_ID);
            return ep.extensions()
                     .findFirst()
                     .orElseGet(() -> application.getService(DefaultConfiguratorFactory.class));
        }

        return application.getService(DefaultConfiguratorFactory.class);
    }

}
