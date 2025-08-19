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

package sap.commerce.toolset.beanSystem.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.components.service
import com.intellij.util.ProcessingContext
import com.intellij.util.application
import sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaClassifier
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaEnum
import sap.commerce.toolset.beanSystem.meta.model.BSMetaType
import java.util.*

open class BSClassCompletionProvider(
    private val metaTypes: EnumSet<BSMetaType> = EnumSet.allOf(BSMetaType::class.java)
) : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val project = parameters.editor.project ?: return

        val metaModelAccess = BSMetaModelAccess.getInstance(project)
        metaTypes
            .map { metaType ->
                metaModelAccess.getAll<BSGlobalMetaClassifier<*>>(metaType)
                    .mapNotNull {
                        when (it) {
                            is BSGlobalMetaEnum -> BSLookupElementFactory.build(it)
                            is BSGlobalMetaBean -> BSLookupElementFactory.build(it, metaType)
                            else -> null
                        }
                    }
            }
            .forEach { result.addAllElements(it) }
    }

    companion object {
        fun getInstance(): BSClassCompletionProvider = application.service()
    }
}