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

package com.intellij.idea.plugin.hybris.moduleDiagram;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class ModuleDepDiagramItem {

    private static final String PROJECT_ITEM_NAME = "project";
    private static final String MODULE_PREFIX = "module:";
    private static final String CUSTOM_MODULE_PREFIX = "custom_module:";

    @Nullable
    private final Module myModule;

    private final boolean myCustomExtension;

    public ModuleDepDiagramItem(@Nullable final Module module, final boolean customExtension) {
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

    @Nullable
    public static ModuleDepDiagramItem fromFQN(@NotNull final String fqn, @NotNull final Project project) {
        String moduleName = null;
        boolean customExtension = false;

        if (fqn.startsWith(MODULE_PREFIX)) {
            moduleName = fqn.substring(MODULE_PREFIX.length());
            customExtension = false;
        } else if (fqn.startsWith(CUSTOM_MODULE_PREFIX)) {
            moduleName = fqn.substring(CUSTOM_MODULE_PREFIX.length());
            customExtension = true;
        }
        if (moduleName == null) {
            return new ModuleDepDiagramItem(null, false);
        }
        final Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
        return module == null ? null : new ModuleDepDiagramItem(module, customExtension);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ModuleDepDiagramItem that = (ModuleDepDiagramItem) obj;

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
