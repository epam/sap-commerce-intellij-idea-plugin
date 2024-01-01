/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.constants.modifier

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.codeInsight.lookup.ImpExLookupElementFactory
import com.intellij.idea.plugin.hybris.impex.completion.ImpexImplementationClassCompletionContributor
import com.intellij.idea.plugin.hybris.impex.constants.InterceptorType
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.system.type.spring.TSSpringHelper
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope

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
) : ImpexModifier {

    DISABLE_INTERCEPTOR_BEANS("disable.interceptor.beans") {
        override fun getLookupElements(project: Project): Set<LookupElement> {
            if (!PluginCommon.isPluginActive(PluginCommon.SPRING_PLUGIN_ID)) return emptySet()

            val interceptorClass = JavaPsiFacade.getInstance(project)
                .findClass(HybrisConstants.CLASS_FQN_INTERCEPTOR_MAPPING, GlobalSearchScope.allScope(project))
                ?: return emptySet()

            return TSSpringHelper.getBeansLazy(interceptorClass).value
                .mapNotNull {
                    it.springBean.beanName
                        ?.let { lookupElement -> ImpExLookupElementFactory.buildInterceptor(lookupElement, it.beanClass?.name) }
                }
                .toSet()
        }
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
        override fun getLookupElements(project: Project) = ImpexImplementationClassCompletionContributor.getInstance(project)
            ?.getImplementationsForClass(HybrisConstants.CLASS_FQN_IMPEX_PROCESSOR)
            ?: emptySet()
    };

    override fun getLookupElements(project: Project): Set<LookupElement> = modifierValues
        .map { ImpExLookupElementFactory.buildModifierValue(it) }
        .toSet()

    companion object {
        private val CACHE = entries.associateBy { it.modifierName }

        fun getByModifierName(modifierName: String) = CACHE[modifierName]
    }
}
