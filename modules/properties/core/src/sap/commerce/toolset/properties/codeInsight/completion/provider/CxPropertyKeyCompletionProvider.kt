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

package sap.commerce.toolset.properties.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.properties.codeInsight.lookup.CxPropertyLookupElementFactory
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyRule

/**
 * Offers the property keys the project already declares, plus the ones SAP Commerce Cloud documents.
 *
 * Naming a property is otherwise done from memory: the platform has thousands of them, spread over every extension's
 * `project.properties`, and nothing in the editor knew any of them.
 */
class CxPropertyKeyCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val project = parameters.position.project
        val chain = CxPropertyCollector.getInstance(project).collect()

        chain.properties.values.forEach { result.addElement(CxPropertyLookupElementFactory.build(it, chain)) }

        // A documented key nobody has declared yet is worth knowing about; one already in the chain is described
        // better by the declaration above.
        CxPropertyRule.entries
            .flatMap { rule -> rule.keys.map { it to rule } }
            .filterNot { (key, _) -> key in chain }
            .forEach { (key, rule) -> result.addElement(CxPropertyLookupElementFactory.build(key, rule)) }
    }
}
