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

package sap.commerce.toolset.properties.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.properties.parsing.PropertiesTokenTypes
import com.intellij.patterns.PlatformPatterns
import sap.commerce.toolset.isNotHybrisProject
import sap.commerce.toolset.properties.codeInsight.completion.provider.CxPropertyKeyCompletionProvider
import sap.commerce.toolset.properties.meta.CxPropertyCollector

class CxPropertyCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PropertiesTokenTypes.KEY_CHARACTERS),
            CxPropertyKeyCompletionProvider(),
        )
    }

    /**
     * Only files the chain was built from are completed in. A `.properties` file which is not part of the
     * configuration has its own vocabulary, and drowning it in platform keys would make completion useless there.
     */
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val project = parameters.position.project
        if (project.isNotHybrisProject) return

        val file = parameters.originalFile.virtualFile ?: return
        if (CxPropertyCollector.getInstance(project).collect().sources.none { it.file == file }) return

        super.fillCompletionVariants(parameters, result)
    }
}
