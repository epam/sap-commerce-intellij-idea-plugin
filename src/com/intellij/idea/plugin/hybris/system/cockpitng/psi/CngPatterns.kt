/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.CngConfigDomFileDescription
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlPatterns

object CngPatterns {
    const val ROOT = "config"
    private const val CONTEXT = "context"

    private val cngFile = PlatformPatterns.psiFile()
        .withName(StandardPatterns.string().endsWith(HybrisConstants.COCKPIT_NG_CONFIG_XML))

    val CONTEXT_PARENT = PsiXmlUtils.tagAttributeValuePattern(ROOT, CONTEXT, "parent")
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase("auto", ".")))
        .inFile(cngFile)

    val CONTEXT_TYPE = PsiXmlUtils.tagAttributeValuePattern(ROOT, CONTEXT, "type")
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
        .inFile(cngFile)

    val LIST_VIEW_COLUMN_QUALIFIER = attributeValue(
        "qualifier",
        "column",
        "list-view",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_COMPONENT_LIST_VIEW
    )
        .inside(
            XmlPatterns.xmlTag().withLocalName(CONTEXT)
                .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase("."))),
        )
        .inFile(cngFile)

    val FLOW_STEP_CONTENT_PROPERTY_TYPE = attributeValue(
        "type",
        "property",
        "content",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_CONFIG_WIZARD_CONFIG
    )
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val FLOW_STEP_CONTENT_PROPERTY_QUALIFIER = attributeValue(
        "qualifier",
        "property",
        "property-list",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val FLOW_INITIALIZE_TYPE = attributeValue(
        "type",
        "initialize",
        "prepare",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val TREE_NODE_TYPE_CODE = attributeValue(
        "code",
        "type-node",
        "explorer-tree",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_CONFIG_EXPLORER_TREE
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val EDITOR_AREA_ATTRIBUTE = attributeValue(
        "qualifier",
        "attribute",
        "editorArea",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_COMPONENT_EDITOR_AREA
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val EDITOR_AREA_EDITOR = attributeValue(
        "editor",
        "attribute",
        "editorArea",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_COMPONENT_EDITOR_AREA
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val ADVANCED_SEARCH_FIELD_NAME = attributeValue(
        "name",
        "field",
        "advanced-search",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_CONFIG_ADVANCED_SEARCH
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    val SIMPLE_SEARCH_FIELD_NAME = attributeValue(
        "name",
        "field",
        "simple-search",
        CngConfigDomFileDescription.NAMESPACE_COCKPITNG_CONFIG_SIMPLE_SEARCH
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

    private fun attributeValue(
        attribute: String,
        tag: String,
        wrappingTag: String,
        namespace: String
    ) = XmlPatterns.xmlAttributeValue()
        .withParent(
            XmlPatterns.xmlAttribute()
                .withLocalName(attribute)
                .withParent(
                    XmlPatterns.xmlTag()
                        .withLocalName(tag)
                        .inside(
                            XmlPatterns.xmlTag()
                                .withNamespace(namespace)
                                .withLocalName(wrappingTag)
                        )
                )
        )

}