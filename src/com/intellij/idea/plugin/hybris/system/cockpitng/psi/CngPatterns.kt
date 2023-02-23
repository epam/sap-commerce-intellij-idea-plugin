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
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns

object CngPatterns {
    const val CONFIG_ROOT = "config"
    const val WIDGETS_ROOT = "widgets"
    private const val CONFIG_CONTEXT = "context"
    private val cngConfigFile = PlatformPatterns.psiFile()
            .withName(StandardPatterns.string().endsWith(HybrisConstants.COCKPIT_NG_CONFIG_XML))
    private val cngWidgetsFile = PlatformPatterns.psiFile()
            .withName(StandardPatterns.string().endsWith(HybrisConstants.COCKPIT_NG_WIDGETS_XML))

    val WIDGET_SETTING = widgetPattern("key", "setting")

    val WIDGET_ID = XmlPatterns.or(
            widgetPattern("widgetId", "widget-extension"),
            widgetPattern("widgetId", "move"),
            widgetPattern("widgetId", "remove"),
            widgetPattern("targetWidgetId", "move")
    )

    val WIDGET_CONNECTION_WIDGET_ID = XmlPatterns.or(
            widgetPattern("sourceWidgetId", "widget-connection"),
            widgetPattern("targetWidgetId", "widget-connection"),
            widgetPattern("sourceWidgetId", "widget-connection-remove"),
            widgetPattern("targetWidgetId", "widget-connection-remove"),
    )

    val WIDGET_DEFINITION: XmlAttributeValuePattern = attributeValue(
            "widgetDefinitionId",
            "widget"
    )
            .inside(PsiXmlUtils.insideTagPattern(WIDGETS_ROOT))

    val ACTION_DEFINITION = attributeValue(
            "action-id",
            "action"
    )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)

    val EDITOR_DEFINITION = XmlPatterns.or(
            attributeValue(
                    "editor",
                    "field",
                    "advanced-search",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_ADVANCED_SEARCH
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),
            attributeValue(
                    "editor",
                    "attribute",
                    "editorArea",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile)
    )

    val ITEM_ATTRIBUTE = XmlPatterns.or(
            attributeValue(
                    "qualifier",
                    "column",
                    "list-view",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_LIST_VIEW
            )
                    .inside(
                            XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)
                                    .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase("."))),
                    )
                    .inFile(cngConfigFile),

            attributeValue(
                    "qualifier",
                    "attribute",
                    "editorArea",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "name",
                    "field",
                    "advanced-search",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_ADVANCED_SEARCH
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "name",
                    "field",
                    "simple-search",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_SEARCH
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile)
    )

    val FLOW_STEP_CONTENT_PROPERTY_QUALIFIER = attributeValue(
            "qualifier",
            "property",
            "property-list",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
    )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)

    val FLOW_INITIALIZE_TYPE = attributeValue(
            "type",
            "initialize",
            "prepare",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
    )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)

    val ITEM_TYPE = XmlPatterns.or(
            PsiXmlUtils.tagAttributeValuePattern(CONFIG_ROOT, CONFIG_CONTEXT, "type")
                    .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
                    .inFile(cngConfigFile),

            XmlPatterns.xmlAttributeValue()
                    .withAncestor(6, XmlPatterns.xmlTag().withLocalName(CONFIG_ROOT))
                    .withParent(XmlPatterns.xmlAttribute("parent")
                            .withParent(XmlPatterns.xmlTag()
                                    .withLocalName(CONFIG_CONTEXT)
                                    .withAttributeValue("merge-by", "type")
                            )
                    )
                    .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase("auto", ".")))
                    .inFile(cngConfigFile),

            attributeValue(
                    "type",
                    "property",
                    "content",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
            )
                    .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "code",
                    "type-node",
                    "explorer-tree",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_EXPLORER_TREE
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile)
    )

    val SPRING_BEAN = XmlPatterns.or(
            attributeValue(
                    "spring-bean",
                    "mold",
                    "collection-browser",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_COLLECTION_BROWSER
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "column",
                    "list-view",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_LIST_VIEW
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "customPanel",
                    "editorArea",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "customSection",
                    "editorArea",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "customTab",
                    "editorArea",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "renderer",
                    "flow",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "renderer",
                    "compare-view",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "custom-attribute",
                    "summary-view",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile),

            attributeValue(
                    "spring-bean",
                    "additionalRenderer",
                    "grid-view",
                    CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_GRID_VIEW
            )
                    .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
                    .inFile(cngConfigFile)
    )

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

    private fun attributeValue(
            attribute: String,
            tag: String,
            wrappingTag: String,
    ) = XmlPatterns.xmlAttributeValue()
            .withParent(
                    XmlPatterns.xmlAttribute()
                            .withLocalName(attribute)
                            .withParent(
                                    XmlPatterns.xmlTag()
                                            .withLocalName(tag)
                                            .inside(
                                                    XmlPatterns.xmlTag()
                                                            .withLocalName(wrappingTag)
                                            )
                            )
            )

    private fun attributeValue(
            attribute: String,
            tag: String,
    ) = XmlPatterns.xmlAttributeValue()
            .withParent(
                    XmlPatterns.xmlAttribute()
                            .withLocalName(attribute)
                            .withParent(
                                    XmlPatterns.xmlTag()
                                            .withLocalName(tag)
                            )
            )

    private fun widgetPattern(attribute: String, tag: String) = attributeValue(attribute, tag)
            .inside(PsiXmlUtils.insideTagPattern(WIDGETS_ROOT))
            .inFile(cngWidgetsFile)

}