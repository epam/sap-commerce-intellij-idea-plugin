/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.constants.modifier

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.impex.completion.ImpexImplementationClassCompletionContributor
import com.intellij.openapi.project.Project

/**
 * https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US
 */
enum class AttributeModifier(
    override val modifierName: String,
    modifierValues: Set<String> = emptySet()
) : ImpexModifier {

    UNIQUE("unique", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    ALLOW_NULL("allownull", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    FORCE_WRITE("forceWrite", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    IGNORE_KEY_CASE("ignoreKeyCase", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    IGNORE_NULL("ignorenull", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    VIRTUAL("virtual", HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES),
    MODE("mode", HybrisConstants.IMPEX_MODIFIER_MODE_VALUES),
    ALIAS("alias"),
    COLLECTION_DELIMITER("collection-delimiter"),
    DATE_FORMAT("dateformat"),
    DEFAULT("default"),
    KEY_2_VALUE_DELIMITER("key2value-delimiter"),
    LANG("lang"),
    MAP_DELIMITER("map-delimiter"),
    NUMBER_FORMAT("numberformat"),
    PATH_DELIMITER("path-delimiter"),
    POS("pos"),
    CELL_DECORATOR("cellDecorator") {
        override fun getLookupElements(project: Project) = ImpexImplementationClassCompletionContributor.getInstance(project)
            .getImplementationsForClass(HybrisConstants.CLASS_IMPEX_CELL_DECORATOR)
    },
    TRANSLATOR("translator") {
        override fun getLookupElements(project: Project) = ImpexImplementationClassCompletionContributor.getInstance(project)
            .getImplementationsForClass(HybrisConstants.CLASS_IMPEX_TRANSLATOR)
    };

    private val lookupElements = modifierValues
        .map { LookupElementBuilder.create(it) }
        .toSet()

    override fun getLookupElements(project: Project): Set<LookupElement> = lookupElements

    companion object {
        private val CACHE = entries.associateBy { it.modifierName }

        fun getByModifierName(modifierName: String) = CACHE[modifierName]
    }
}
