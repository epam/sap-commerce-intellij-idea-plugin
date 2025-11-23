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
package sap.commerce.toolset.beanSystem.folding

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.meta.BSMetaHelper
import sap.commerce.toolset.beanSystem.model.*
import sap.commerce.toolset.beanSystem.model.Enum
import sap.commerce.toolset.beanSystem.settings.BSFoldingSettings
import sap.commerce.toolset.beanSystem.settings.state.BSFoldingSettingsState
import sap.commerce.toolset.folding.XmlFoldingBuilderEx

class BeansXmlFoldingBuilder : XmlFoldingBuilderEx<BSFoldingSettingsState, Beans>(Beans::class.java), DumbAware {

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

    override fun initSettings(project: Project) = BSFoldingSettings.getInstance().state

    override fun getPlaceholderText(node: ASTNode): String = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            Bean.PROPERTY -> buildString {
                val name = psi.getAttributeValue(Property.NAME)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyProperties, Bean.PROPERTY, Property.NAME) }
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val flattenType = BSMetaHelper.flattenType(psi.getAttributeValue(Property.TYPE))
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE

                append(name)
                append(TYPE_SEPARATOR)
                append((flattenType))
            }

            Enum.VALUE -> psi.value.trimmedText

            AbstractPojo.DESCRIPTION -> buildString {
                append(HybrisConstants.Folding.DESCRIPTION_PREFIX)
                append(' ')
                append(psi.value.trimmedText)
            }

            Beans.BEAN -> buildString {
                psi.getAttributeValue(Bean.ABSTRACT)
                    ?.takeIf { "true".equals(it, true) }
                    ?.let { append("[abstract] ") }

                append(BSMetaHelper.flattenType(psi.getAttributeValue(AbstractPojo.CLASS)))

                BSMetaHelper.flattenType(psi.getAttributeValue(Bean.EXTENDS))
                    ?.let { TYPE_SEPARATOR + it }
                    ?.let { append(it) }
            }

            Beans.ENUM -> buildString {
                append("[enum] ")
                append(BSMetaHelper.flattenType(psi.getAttributeValue(AbstractPojo.CLASS)))
            }

            Hints.HINT -> buildString {
                val name = psi.getAttributeValue(Hint.NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                append(name)

                psi.value.trimmedText
                    .takeIf { it.isNotBlank() }
                    ?.let { TYPE_SEPARATOR + it }
                    ?.let { append(it) }
            }

            Bean.HINTS -> psi.subTags
                .mapNotNull { it.getAttributeValue(Hint.NAME) }
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
