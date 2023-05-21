/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.completion.ImpexImplementationClassCompletionContributor
import com.intellij.openapi.project.Project

/**
 * https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US
 * <br></br>
 * Service-Layer Direct (SLD) mode -> https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/ccf4dd14636b4f7eac2416846ffd5a70.html?locale=en-US
 */
enum class TypeModifier(
    override val modifierName: String,
    modifierValues: Set<String> = emptySet()
) : ImpexModifier {

    BATCH_MODE("batchmode", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    SLD_ENABLED("sld.enabled", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    CACHE_UNIQUE("cacheUnique", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    IMPEX_LEGACY_MODE("impex.legacy.mode", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    PROCESSOR("processor") {
        override fun getLookupElements(project: Project) = ImpexImplementationClassCompletionContributor.getInstance(project)
            .getImplementationsForClass(HybrisConstants.CLASS_IMPEX_PROCESSOR)
    };

    private val lookupElements = modifierValues
        .map { LookupElementBuilder.create(it) }
        .toSet()

    override fun getLookupElements(project: Project): Set<LookupElement> = lookupElements

    companion object {
        private val CACHE = values().associateBy { it.modifierName }

        fun getByModifierName(modifierName: String) = CACHE[modifierName]
    }
}
