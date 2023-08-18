/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.idea.plugin.hybris.system.bean.codeInsight.lookup.BSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ProcessingContext
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
        val instance: BSClassCompletionProvider = ApplicationManager.getApplication().getService(BSClassCompletionProvider::class.java)
    }
}