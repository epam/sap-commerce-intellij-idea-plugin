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

package com.intellij.idea.plugin.hybris.project.utils;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 20/3/17.
 */
public final class PluginCommon {

    public static final String ANT_SUPPORT_PLUGIN_ID = "AntSupport";
    public static final String JAVAEE_PLUGIN_ID = "com.intellij.javaee";
    public static final String SPRING_PLUGIN_ID = "com.intellij.spring";
    public static final String KOTLIN_PLUGIN_ID = "org.jetbrains.kotlin";
    public static final String SHOW_UNLINKED_GRADLE_POPUP = "show.inlinked.gradle.project.popup";

    private PluginCommon() {
    }

    public static boolean isPluginActive(final String id) {
        final IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(id));
        if (plugin == null) {
            return false;
        }
        return plugin.isEnabled();
    }
}
