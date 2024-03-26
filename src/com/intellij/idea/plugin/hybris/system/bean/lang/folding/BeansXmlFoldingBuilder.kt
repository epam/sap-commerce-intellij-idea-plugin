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
package com.intellij.idea.plugin.hybris.system.bean.lang.folding

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.lang.folding.AbstractXmlFoldingBuilderEx
import com.intellij.idea.plugin.hybris.settings.BeanSystemFoldingSettings
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaHelper
import com.intellij.idea.plugin.hybris.system.bean.model.*
import com.intellij.idea.plugin.hybris.system.bean.model.Enum
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken

class BeansXmlFoldingBuilder : AbstractXmlFoldingBuilderEx<BeanSystemFoldingSettings, Beans>(Beans::class.java), DumbAware {

    private val foldHints = "[hints]"

    override val filter = PsiElementFilter {
        when (it) {
            is XmlTag -> when (it.localName) {
                Bean.PROPERTY,
                Enum.VALUE,
                Beans.BEAN,
                Beans.ENUM,
                AbstractPojo.DESCRIPTION,
                Hints.HINT,
                Bean.HINTS -> true

                else -> false
            }

            is XmlToken -> when (it.text) {
                HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED,
                HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED -> true

                else -> false
            }

            else -> false
        }
    }

    override fun initSettings(project: Project) = DeveloperSettingsComponent.getInstance(project).state
        .beanSystemSettings
        .folding

    override fun getPlaceholderText(node: ASTNode): String = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY -> psi.getAttributeValue(Property.NAME)
                ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyProperties, Bean.PROPERTY, Property.NAME) } +
                TYPE_SEPARATOR +
                (BSMetaHelper.flattenType(psi.getAttributeValue(Property.TYPE)) ?: "?")

            Enum.VALUE -> psi.value.trimmedText
            AbstractPojo.DESCRIPTION -> "-- ${psi.value.trimmedText} --"

            Beans.BEAN -> (psi.getAttributeValue(Bean.ABSTRACT)
                ?.takeIf { "true".equals(it, true) }
                ?.let { "[abstract] " }
                ?: "") +
                BSMetaHelper.flattenType(psi.getAttributeValue(AbstractPojo.CLASS)) +
                (BSMetaHelper.flattenType(psi.getAttributeValue(Bean.EXTENDS))
                    ?.let { TYPE_SEPARATOR + it }
                    ?: "")

            Beans.ENUM -> "[enum] " + BSMetaHelper.flattenType(psi.getAttributeValue(AbstractPojo.CLASS))

            Hints.HINT -> psi.getAttributeValue(Hint.NAME) +
                (psi.value.trimmedText
                    .takeIf { it.isNotBlank() }
                    ?.let { TYPE_SEPARATOR + it } ?: "")

            Bean.HINTS -> psi.subTags
                .map { it.getAttributeValue(Hint.NAME) }
                .joinToString()
                .takeIf { it.isNotBlank() }
                ?.let { "$foldHints : $it" }
                ?: foldHints

            else -> FALLBACK_PLACEHOLDER
        }

        is XmlToken -> when (val text = psi.text) {
            HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED -> HybrisConstants.BS_SIGN_LESS_THAN
            HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED -> HybrisConstants.BS_SIGN_GREATER_THAN

            else -> text
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY,
            Enum.VALUE,
            AbstractPojo.DESCRIPTION,
            Hints.HINT,
            Bean.HINTS -> true

            else -> false
        }

        is XmlToken -> when (psi.text) {
            HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED,
            HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED -> true

            else -> false
        }

        else -> false
    }

}
