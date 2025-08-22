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

package sap.commerce.toolset.cockpitNG.psi

import com.intellij.patterns.DomPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import sap.commerce.toolset.cockpitNG.CockpitNGConstants
import sap.commerce.toolset.cockpitNG.model.config.Config
import sap.commerce.toolset.cockpitNG.model.config.Context
import sap.commerce.toolset.cockpitNG.model.config.MergeAttrTypeKnown
import sap.commerce.toolset.cockpitNG.model.core.Widgets
import sap.commerce.toolset.psi.insideTagPattern
import sap.commerce.toolset.psi.tagAttributeValuePattern

object CngPatterns {
    const val CONFIG_ROOT = "config"
    const val WIDGETS_ROOT = "widgets"
    private const val CONFIG_CONTEXT = "context"
    private val cngConfigFile = DomPatterns.inDomFile(Config::class.java)
    private val cngWidgetsFile = DomPatterns.inDomFile(Widgets::class.java)

    val I18N_PROPERTY = XmlPatterns.or(
        attributeValue("label", "attribute", "editorArea", CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "section", "editorArea", CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("description", "section", "editorArea", CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "essentialSection", "editorArea", CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "tab", "editorArea", CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "panel", "editorArea", CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "step", "flow", CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("sublabel", "step", "flow", CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("label", "custom", "flow", CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "column", "list-view", CockpitNGConstants.Namespace.CONFIG_SIMPLE_LIST)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "data-quality-group", "summary-view", CockpitNGConstants.Namespace.COMPONENT_SUMMARY_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("label", "custom-attribute", "summary-view", CockpitNGConstants.Namespace.COMPONENT_SUMMARY_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),
        attributeValue("name", "section", "summary-view", CockpitNGConstants.Namespace.COMPONENT_SUMMARY_VIEW)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("label", "option", "value-chooser", CockpitNGConstants.Namespace.COMPONENT_VALUE_CHOOSER)
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT)).inFile(cngConfigFile),

        attributeValue("name", "section", "compare-view", CockpitNGConstants.Namespace.COMPONENT_COMPARE_VIEW)
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
        .inside(insideTagPattern(WIDGETS_ROOT))

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
            CockpitNGConstants.Namespace.COMPONENT_LIST_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "custom-section",
            "summary-view",
            CockpitNGConstants.Namespace.COMPONENT_SUMMARY_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "custom-attribute",
            "summary-view",
            CockpitNGConstants.Namespace.COMPONENT_SUMMARY_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "customSection",
            "editorArea",
            CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "customPanel",
            "editorArea",
            CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "class",
            "customTab",
            "editorArea",
            CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)
    )

    val EDITOR_DEFINITION = XmlPatterns.or(
        attributeValue(
            "editor",
            "field",
            "advanced-search",
            CockpitNGConstants.Namespace.CONFIG_ADVANCED_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "attribute",
            "editorArea",
            CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "property",
            "flow",
            CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "property",
            "editors",
            CockpitNGConstants.Namespace.CONFIG_HYBRIS
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "attribute",
            "compare-view",
            CockpitNGConstants.Namespace.COMPONENT_COMPARE_VIEW
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "editor",
            "field",
            "fulltext-search",
            CockpitNGConstants.Namespace.CONFIG_FULLTEXT_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)
    )

    val ITEM_ATTRIBUTE = XmlPatterns.or(
        attributeValue(
            "qualifier",
            "column",
            "list-view",
            CockpitNGConstants.Namespace.COMPONENT_LIST_VIEW
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
            CockpitNGConstants.Namespace.COMPONENT_EDITOR_AREA
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "name",
            "field",
            "advanced-search",
            CockpitNGConstants.Namespace.CONFIG_ADVANCED_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "name",
            "sort-field",
            "advanced-search",
            CockpitNGConstants.Namespace.CONFIG_ADVANCED_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "name",
            "field",
            "simple-search",
            CockpitNGConstants.Namespace.CONFIG_SIMPLE_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "name",
            "sort-field",
            "simple-search",
            CockpitNGConstants.Namespace.CONFIG_SIMPLE_SEARCH
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "qualifier",
            "attribute",
            "compare-view",
            CockpitNGConstants.Namespace.COMPONENT_COMPARE_VIEW
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
                                        .withNamespace(CockpitNGConstants.Namespace.CONFIG_FULLTEXT_SEARCH)
                                        .withLocalName("fulltext-search")
                                        .andNot(
                                            XmlPatterns.xmlTag()
                                                .withNamespace(CockpitNGConstants.Namespace.CONFIG_FULLTEXT_SEARCH)
                                                .withLocalName("fulltext-search")
                                                .withChild(
                                                    XmlPatterns.xmlTag().withLocalName("preferred-search-strategy")
                                                )
                                        ),
                                    XmlPatterns.xmlTag()
                                        .withNamespace(CockpitNGConstants.Namespace.CONFIG_FULLTEXT_SEARCH)
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
        CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
        .inFile(cngConfigFile)

    val FLOW_STEP_PROPERTY = XmlPatterns.or(
        attributeValueExact(
            "qualifier",
            "property",
            "content",
            CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValueExact(
            "property",
            "assign",
            "prepare",
            CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
        )
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile)
    )

    val FLOW_INITIALIZE_TYPE = attributeValue(
        "type",
        "initialize",
        "prepare",
        CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
    )
        .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
        .inFile(cngConfigFile)

    val FLOW_PROPERTY_LIST_ROOT = attributeValue(
        "root",
        "property-list",
        "content",
        CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
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
        tagAttributeValuePattern(CONFIG_ROOT, CONFIG_CONTEXT, Context.TYPE)
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
            CockpitNGConstants.Namespace.CONFIG_WIZARD_CONFIG
        )
            .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
            .inside(XmlPatterns.xmlTag().withLocalName(CONFIG_CONTEXT))
            .inFile(cngConfigFile),

        attributeValue(
            "code",
            "type-node",
            "explorer-tree",
            CockpitNGConstants.Namespace.CONFIG_EXPLORER_TREE
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
        .inside(insideTagPattern(WIDGETS_ROOT))
        .inFile(cngWidgetsFile)

}