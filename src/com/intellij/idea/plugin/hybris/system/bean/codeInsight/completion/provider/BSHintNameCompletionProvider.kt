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
import com.intellij.idea.plugin.hybris.system.bean.model.Bean
import com.intellij.idea.plugin.hybris.system.bean.model.Beans
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext

/**
 * See Swagger Documentation Annotations https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/8bc53579866910149472ccbef0222ec5.html#swagger-documentation-annotations
 */
class BSHintNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val classLevelNameLookupElements = classLevelName.map { BSLookupElementFactory.buildWsHint(it) }
    private val propertyLevelNameLookupElements = propertyLevelNames.map { BSLookupElementFactory.buildWsHint(it) }

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val parentName = parameters.position.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == Beans.BEAN || it.localName == Bean.PROPERTY }
            ?.localName
            ?: return

        when (parentName) {
            Beans.BEAN -> result.addAllElements(classLevelNameLookupElements)
            Bean.PROPERTY -> result.addAllElements(propertyLevelNameLookupElements)
        }
    }

    companion object {
        val classLevelName = setOf("wsRelated", "alias")
        val propertyLevelNames = setOf("alias", "allowedValues", "example", "required")
        val instance: BSHintNameCompletionProvider = ApplicationManager.getApplication().getService(BSHintNameCompletionProvider::class.java)
    }
}