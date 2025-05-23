/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.meta

import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement
import kotlinx.coroutines.coroutineScope

abstract class MetaModelProcessor<D : DomElement, M>(private val project: Project) {

    suspend fun process(foundMeta: FoundMeta<D>): M? = coroutineScope {
        readAction {
            process(
                foundMeta.moduleName,
                foundMeta.extensionName,
                foundMeta.name,
                PsiUtils.isCustomExtensionFile(foundMeta.virtualFile, project),
                foundMeta.rootElement
            )
        }
    }

    protected abstract fun process(moduleName: String, extensionName: String, fileName: String, custom: Boolean, dom: D): M
}