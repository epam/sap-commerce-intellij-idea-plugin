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

package com.intellij.idea.plugin.hybris.diagram.module.node.graph;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class ModuleDepGraphNode {

    public static final String PROJECT_ITEM_NAME = "project";
    public static final String MODULE_PREFIX = "module:";
    public static final String CUSTOM_MODULE_PREFIX = "custom_module:";

    @Nullable
    private final Module myModule;

    private final boolean myCustomExtension;

    public ModuleDepGraphNode(@Nullable final Module module, final boolean customExtension) {
        myModule = module;
        myCustomExtension = customExtension;
    }

    @Nullable
    public Module getModule() {
        return myModule;
    }

    @NotNull
    public String getQualifiedName() {
        if (myModule != null) {
            final String prefix = myCustomExtension ? CUSTOM_MODULE_PREFIX : MODULE_PREFIX;
            return prefix + myModule.getName();
        }
        return PROJECT_ITEM_NAME;
    }

    public boolean isCustomExtension() {
        return myCustomExtension;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ModuleDepGraphNode that = (ModuleDepGraphNode) obj;

        if (myCustomExtension != that.myCustomExtension) {
            return false;
        }
        return myModule != null ? myModule.equals(that.myModule) : that.myModule == null;
    }

    @Override
    public int hashCode() {
        int result = myModule != null ? myModule.hashCode() : 0;
        result = 31 * result + (myCustomExtension ? 1 : 0);
        return result;
    }

    @NotNull
    public String getName() {
        return myModule != null ? myModule.getName() : "project";
    }

    @Override
    public String toString() {
        return myModule != null ? myModule.getName() : "[y] Module Dependencies";
    }
}
