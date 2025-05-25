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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi

import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils
import com.intellij.idea.plugin.hybris.system.cockpitng.CngConfigDomFileDescription
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Config
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.MergeAttrTypeKnown
import com.intellij.idea.plugin.hybris.system.cockpitng.model.core.Widgets
import com.intellij.patterns.DomPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns

object CngPatterns {
    const val CONFIG_ROOT = "config"
    const val WIDGETS_ROOT = "widgets"
    private const val CONFIG_CONTEXT = "context"
    private val cngConfigFile = DomPatterns.inDomFile(Config::class.java)
    private val cngWidgetsFile = DomPatterns.inDomFile(Widgets::class.java)

    val I18N_PROPERTY = XmlPatterns.or(
        attributeValue("label", "attribute", "editorArea", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "section", "editorArea", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("description", "section", "editorArea", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "essentialSection", "editorArea", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "tab", "editorArea", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "panel", "editorArea", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "step", "flow", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("sublabel", "step", "flow", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("label", "custom", "flow", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "column", "list-view", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_LIST)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "data-quality-group", "summary-view", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("label", "custom-attribute", "summary-view", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "section", "summary-view", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "option", "value-chooser", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_VALUE_CHOOSER)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("name", "section", "compare-view", CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
    )

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

    val WIDGET_COMPONENT_RENDERER_CLASS = XmlPatterns.or(
        attributeValue(
            "class",
            "column",
            "list-view",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_LIST_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "custom-section",
            "summary-view",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "custom-attribute",
            "summary-view",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_SUMMARY_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "customSection",
            "editorArea",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "customPanel",
            "editorArea",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "customTab",
            "editorArea",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)
    )

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
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "property",
            "flow",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "property",
            "editors",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "attribute",
            "compare-view",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "field",
            "fulltext-search",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH
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
            "sort-field",
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
            .inFile(cngConfigFile),

        attributeValue(
            "name",
            "sort-field",
            "simple-search",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_SIMPLE_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "qualifier",
            "attribute",
            "compare-view",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_COMPONENT_COMPARE_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        XmlPatterns.xmlAttributeValue()
            .withParent(
                XmlPatterns.xmlAttribute()
                    .withLocalName("name")
                    .withParent(
                        XmlPatterns.xmlTag()
                            .withLocalName("field")
                            .inside(
                                XmlPatterns.or(
                                    XmlPatterns.xmlTag()
                                        .withNamespace(CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH)
                                        .withLocalName("fulltext-search")
                                        .andNot(
                                            XmlPatterns.xmlTag()
                                                .withNamespace(CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH)
                                                .withLocalName("fulltext-search")
                                                .withChild(
                                                    XmlPatterns.xmlTag().withLocalName("preferred-search-strategy")
                                                )
                                        ),
                                    XmlPatterns.xmlTag()
                                        .withNamespace(CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_FULLTEXT_SEARCH)
                                        .withLocalName("fulltext-search")
                                        .withChild(
                                            XmlPatterns.xmlTag().withLocalName("preferred-search-strategy")
                                                .withChild(
                                                    XmlPatterns.xmlText().withText("flexible")
                                                )
                                        )
                                )
                            )
                    )
            )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)
    )

    val FLOW_STEP_CONTENT_PROPERTY_LIST_PROPERTY_QUALIFIER = attributeValue(
        "qualifier",
        "property",
        "property-list",
        CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
        .inFile(cngConfigFile)

    val FLOW_STEP_PROPERTY = XmlPatterns.or(
        attributeValueExact(
            "qualifier",
            "property",
            "content",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValueExact(
            "property",
            "assign",
            "prepare",
            CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)
    )

    val FLOW_INITIALIZE_TYPE = attributeValue(
        "type",
        "initialize",
        "prepare",
        CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
        .inFile(cngConfigFile)

    val FLOW_PROPERTY_LIST_ROOT = attributeValue(
        "root",
        "property-list",
        "content",
        CngConfigDomFileDescription.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
        .inFile(cngConfigFile)

    val CONTEXT_PARENT_NON_ITEM_TYPE = XmlPatterns.xmlAttributeValue()
        .withAncestor(6, XmlPatterns.xmlTag().withLocalName(CONFIG_ROOT))
        .withParent(
            XmlPatterns.xmlAttribute("parent")
                .withParent(
                    XmlPatterns.xmlTag()
                        .withLocalName(CONFIG_CONTEXT)
                        .withoutAttributeValue(Context.MERGE_BY, MergeAttrTypeKnown.TYPE.value)
                        .withoutAttributeValue(Context.MERGE_BY, MergeAttrTypeKnown.MODULE.value)
                )
        )
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase(Context.PARENT_AUTO, ".")))
        .inFile(cngConfigFile)

    val ITEM_TYPE = XmlPatterns.or(
        PsiXmlUtils.tagAttributeValuePattern(CONFIG_ROOT, CONFIG_CONTEXT, Context.TYPE)
            .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
            .inFile(cngConfigFile),

        XmlPatterns.xmlAttributeValue()
            .withAncestor(6, XmlPatterns.xmlTag().withLocalName(CONFIG_ROOT))
            .withParent(
                XmlPatterns.xmlAttribute("parent")
                    .withParent(
                        XmlPatterns.xmlTag()
                            .withLocalName(CONFIG_CONTEXT)
                            .withAttributeValue(Context.MERGE_BY, MergeAttrTypeKnown.TYPE.value)
                    )
            )
            .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase(Context.PARENT_AUTO, ".")))
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

    private fun attributeValueExact(
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
                        .withParent(
                            XmlPatterns.xmlTag()
                                .withNamespace(namespace)
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