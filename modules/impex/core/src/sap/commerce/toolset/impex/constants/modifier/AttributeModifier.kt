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

package sap.commerce.toolset.impex.constants.modifier

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.codeInsight.completion.JavaClassCompletionService
import sap.commerce.toolset.impex.codeInsight.lookup.ImpExLookupElementFactory
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeName
import sap.commerce.toolset.impex.psi.ImpExAnyAttributeValue

/**
 * https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US
 */
enum class AttributeModifier(
    override val modifierName: String,
    override val modifierValues: Set<String> = emptySet(),
    override val modifierMode: ImpExProcessingMode = ImpExProcessingMode.ANY,
) : ImpExModifier {

    UNIQUE(
        modifierName = "unique",
        modifierValues = HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES
    ),
    ALLOW_NULL(
        modifierName = "allownull",
        modifierValues = HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES,
        modifierMode = ImpExProcessingMode.IMPORT
    ),
    FORCE_WRITE(
        modifierName = "forceWrite",
        modifierValues = HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES,
        modifierMode = ImpExProcessingMode.IMPORT
    ),
    IGNORE_KEY_CASE(
        modifierName = "ignoreKeyCase",
        modifierValues = HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES,
        modifierMode = ImpExProcessingMode.IMPORT
    ),
    IGNORE_NULL(
        modifierName = "ignorenull",
        modifierValues = HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES,
        modifierMode = ImpExProcessingMode.IMPORT
    ),
    VIRTUAL(
        modifierName = "virtual",
        modifierValues = HybrisConstants.IMPEX_MODIFIER_BOOLEAN_VALUES
    ),
    MODE(
        modifierName = "mode",
        modifierMode = ImpExProcessingMode.IMPORT
    ) {
        override fun getLookupElements(project: Project) = mapOf(
            "append" to "(+)",
            "remove" to "(-)",
            "merge" to "(+?)"
        )
            .map { ImpExLookupElementFactory.buildModifierValue(it.key, it.value) }
            .toSet()
    },
    ALIAS(
        modifierName = "alias",
        modifierMode = ImpExProcessingMode.EXPORT
    ),
    COLLECTION_DELIMITER(modifierName = "collection-delimiter"),
    DATE_FORMAT(modifierName = "dateformat"),
    DEFAULT(modifierName = "default"),
    KEY_2_VALUE_DELIMITER(modifierName = "key2value-delimiter"),
    LANG(modifierName = "lang"),
    MAP_DELIMITER(modifierName = "map-delimiter"),
    NUMBER_FORMAT(modifierName = "numberformat"),
    PATH_DELIMITER(modifierName = "path-delimiter"),
    POS(modifierName = "pos"),
    CELL_DECORATOR(
        modifierName = "cellDecorator",
        modifierMode = ImpExProcessingMode.IMPORT
    ) {
        override fun getLookupElements(project: Project) = JavaClassCompletionService.getInstance(project)
            .getImplementationsForClasses(HybrisConstants.CLASS_FQN_IMPEX_CELL_DECORATOR)
    },
    TRANSLATOR(modifierName = "translator") {
        override fun getLookupElements(project: Project) = JavaClassCompletionService.getInstance(project)
            .getImplementationsForClasses(*HybrisConstants.CLASS_FQN_IMPEX_TRANSLATORS)
    },
    EXPR(modifierName = "expr"),
    SYSTEM(modifierName = "system"),
    VERSION(modifierName = "version"),
    CLASSIFICATION_CLASS(modifierName = "class");

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