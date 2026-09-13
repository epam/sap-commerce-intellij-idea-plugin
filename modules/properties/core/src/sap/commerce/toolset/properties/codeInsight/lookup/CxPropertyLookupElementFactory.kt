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

package sap.commerce.toolset.properties.codeInsight.lookup

import com.intellij.codeInsight.lookup.LookupElementBuilder
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.meta.CxProperty
import sap.commerce.toolset.properties.meta.CxPropertyModel
import sap.commerce.toolset.properties.meta.CxPropertyRule
import sap.commerce.toolset.properties.meta.CxPropertyRuleSeverity

object CxPropertyLookupElementFactory {

    /**
     * A key the project already declares somewhere, shown with the value it currently resolves to and the file which
     * decides it — which is the difference between picking a name and knowing what picking it would mean.
     */
    fun build(property: CxProperty, chain: CxPropertyModel) = LookupElementBuilder.create(property.key)
        .withTypeText(property.declaration?.source?.presentableName, true)
        .withTailText(chain.resolve(property.key)?.let { " = $it" }, true)
        .withIcon(
            when {
                CxPropertyRule.of(property.key)?.severity == CxPropertyRuleSeverity.ERROR -> HybrisIcons.Property.IGNORED
                property.isIgnored -> HybrisIcons.Property.IGNORED
                property.isShadowed -> HybrisIcons.Property.SHADOWED
                else -> HybrisIcons.Property.EFFECTIVE
            }
        )

    /**
     * A key nothing declares yet, offered because SAP Commerce Cloud documents it — so it can be recognised before it
     * is written rather than reported afterwards.
     */
    fun build(key: String, rule: CxPropertyRule) = LookupElementBuilder.create(key)
        .withTypeText(rule.title, true)
        .withIcon(
            when (rule.severity) {
                CxPropertyRuleSeverity.ERROR -> HybrisIcons.Property.IGNORED
                CxPropertyRuleSeverity.WARNING -> HybrisIcons.Property.SHADOWED
            }
        )
}
