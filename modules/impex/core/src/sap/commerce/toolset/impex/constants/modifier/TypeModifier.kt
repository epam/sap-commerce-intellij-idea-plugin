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

package sap.commerce.toolset.impex.constants.modifier

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.codeInsight.completion.JavaClassCompletionService
import sap.commerce.toolset.impex.codeInsight.lookup.ImpExLookupElementFactory
import sap.commerce.toolset.impex.constants.InterceptorType
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeName
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeValue
import sap.commerce.toolset.typeSystem.codeInsight.completion.TSCompletionService
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType

/**
 * https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US
 * <br></br>
 * Service-Layer Direct (SLD) mode -> https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/ccf4dd14636b4f7eac2416846ffd5a70.html?locale=en-US
 * <br>
 * Interceptors in the ImpEx -> https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex
 */
enum class TypeModifier(
    override val modifierName: String,
    private val modifierValues: Set<String> = emptySet()
) : ImpExModifier {

    DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES("disable.UniqueAttributesValidator.for.types") {
        override fun getLookupElements(project: Project) = TSCompletionService.getInstance(project)
            .getCompletions(TSMetaType.META_ITEM, TSMetaType.META_ENUM, TSMetaType.META_RELATION)
            .toSet()
    },
    DISABLE_INTERCEPTOR_BEANS("disable.interceptor.beans") {
        override fun getLookupElements(project: Project): Set<LookupElement> = InterceptorProvider.EP.extensionList
            .map { it.collect(project, HybrisConstants.CLASS_FQN_INTERCEPTOR_MAPPING) }
            .flatten()
            .map { ImpExLookupElementFactory.buildInterceptor(it) }
            .toSet()
    },
    DISABLE_INTERCEPTOR_TYPES("disable.interceptor.types") {
        override fun getLookupElements(project: Project) = InterceptorType.entries
            .map { ImpExLookupElementFactory.buildModifierValue(it.code, it.code, it.title) }
            .toSet()
    },
    BATCH_MODE("batchmode", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    SLD_ENABLED("sld.enabled", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    CACHE_UNIQUE("cacheUnique", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    IMPEX_LEGACY_MODE("impex.legacy.mode", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    PROCESSOR("processor") {
        override fun getLookupElements(project: Project) = JavaClassCompletionService.getInstance(project)
            .getImplementationsForClasses(HybrisConstants.CLASS_FQN_IMPEX_PROCESSOR)
    };

    override fun getLookupElements(project: Project): Set<LookupElement> = modifierValues
        .map { ImpExLookupElementFactory.buildModifierValue(it) }
        .toSet()

    companion object {
        private val CACHE = entries.associateBy { it.modifierName }

        fun getModifier(modifierName: String) = CACHE[modifierName]
        fun getModifier(modifierValue: ImpExAnyAttributeValue?) = modifierValue
            ?.anyAttributeName
            ?.let { getModifier(it) }

        fun getModifier(modifierName: ImpExAnyAttributeName?) = modifierName
            ?.text
            ?.let { CACHE[it] }
    }
}