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

package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.idea.plugin.hybris.codeInsight.completion.provider.ItemTypeCodeCompletionProvider
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.idea.plugin.hybris.system.type.codeInsight.lookup.TSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.xml.DomElement

class CngItemTypeCodeCompletionProvider : ItemTypeCodeCompletionProvider() {

    /**
     * In case of `context` tag and `type` or `parent` attribute code completion we have to boost other side values
     * for `type` we are boosting all children of the `parent` type
     * for `parent` we are boosting all extends of the `type` type
     */
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val element = parameters.position

        val currentAttribute = element.parentOfType<XmlAttribute>()
            ?.takeIf { it.localName == Context.TYPE || it.localName == Context.PARENT }
            ?: return super.addCompletions(parameters, context, result)
        val tag = currentAttribute.parentOfType<XmlTag>()
            ?.takeIf { it.localName == Config.CONTEXT }
            ?: return super.addCompletions(parameters, context, result)

        val metaModelAccess = TSMetaModelAccess.getInstance(element.project)
        val currentAttributeName = currentAttribute.localName
        val anotherAttributeValue = getAnotherType(currentAttributeName, tag)
            ?.let { metaModelAccess.findMetaClassifierByName(it) }
            ?: return super.addCompletions(parameters, context, result)

        val allItems = metaModelAccess.getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)

        val boostedItems = getBoostedItems(currentAttributeName, anotherAttributeValue, allItems)
            ?.takeIf { it.isNotEmpty() }
            ?: return super.addCompletions(parameters, context, result)

        addContextSpecificCompletions(currentAttributeName, anotherAttributeValue, result, boostedItems, allItems)
    }

    private fun addContextSpecificCompletions(
        currentAttributeName: String,
        anotherAttributeValue: TSGlobalMetaClassifier<out DomElement>,
        result: CompletionResultSet,
        boostedItems: Collection<TSGlobalMetaItem>,
        allItems: Collection<TSGlobalMetaItem>
    ) {
        val resultCaseInsensitive = result.caseInsensitive()

        boostedItems
            .mapNotNull {
                val typeText = when (currentAttributeName) {
                    Context.TYPE -> " child of ${anotherAttributeValue.name}"
                    Context.PARENT -> " parent of ${anotherAttributeValue.name}"
                    else -> null
                }
                TSLookupElementFactory.build(it)
                    ?.withTypeIconRightAligned(true)
                    ?.withTypeText(typeText, true)
                    ?.withBoldness(true)
            }
            .map { PrioritizedLookupElement.withPriority(it, 1.0) }
            .map { PrioritizedLookupElement.withGrouping(it, 1) }
            .forEach { resultCaseInsensitive.addElement(it) }

        allItems
            .filterNot { boostedItems.contains(it) }
            .mapNotNull { TSLookupElementFactory.build(it) }
            .forEach { resultCaseInsensitive.addElement(it) }
    }

    private fun getBoostedItems(
        currentAttributeName: String,
        anotherAttributeValue: TSGlobalMetaClassifier<out DomElement>,
        allItems: Collection<TSGlobalMetaItem>
    ) = when (currentAttributeName) {
        Context.TYPE -> allItems
            .filter { meta -> meta.allExtends.find { it == anotherAttributeValue } != null }

        Context.PARENT -> (anotherAttributeValue as? TSGlobalMetaItem)
            ?.allExtends

        else -> null
    }

    private fun getAnotherType(
        currentAttributeName: String,
        tag: XmlTag,
    ) = when (currentAttributeName) {
        Context.TYPE -> tag.getAttributeValue(Context.PARENT)
        Context.PARENT -> tag.getAttributeValue(Context.TYPE)
        else -> null
    }

    companion object {
        val instance: CngItemTypeCodeCompletionProvider = ApplicationManager.getApplication().getService(CngItemTypeCodeCompletionProvider::class.java)
    }
}