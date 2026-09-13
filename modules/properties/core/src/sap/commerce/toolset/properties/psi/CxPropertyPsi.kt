/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.psi

import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.meta.CxPropertyDeclaration

/**
 * Finds the `key=value` line a collected declaration was read from.
 *
 * The chain is built from files rather than PSI, so getting back to the editor means looking the declaration up again.
 * A file may declare the same key more than once, and the recorded offset is what tells those apart.
 */
fun CxPropertyDeclaration.findPsi(project: Project): IProperty? {
    val file = source.file ?: return null
    val propertiesFile = PsiManager.getInstance(project).findFile(file)?.asSafely<PropertiesFile>() ?: return null
    val candidates = propertiesFile.findPropertiesByKey(key)

    return candidates.find { it.psiElement.textOffset == offset }
        ?: candidates.firstOrNull()
}
