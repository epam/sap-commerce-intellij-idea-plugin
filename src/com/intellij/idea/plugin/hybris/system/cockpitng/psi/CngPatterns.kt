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
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlTag

object CngPatterns {
    const val ROOT = "config"
    private const val CONTEXT = "context"

    private val cngFile = PlatformPatterns.psiFile()
        .withName(StandardPatterns.string().endsWith(HybrisConstants.COCKPIT_NG_CONFIG_XML))

    val LIST_VIEW_COLUMN_QUALIFIER = XmlPatterns.xmlAttributeValue()
        .withParent(
            XmlPatterns.xmlAttribute().withLocalName("qualifier")
                .withParent(
                    XmlPatterns.xmlTag().withLocalName("column")
                        .withParent(XmlPatterns.xmlTag().withLocalName("list-view"))
                )
        )
        .inside(
            XmlPatterns.xmlTag().withLocalName(CONTEXT)
                .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase("."))),
        )
        .inFile(cngFile)

    val CONTEXT_PARENT = PsiXmlUtils.tagAttributeValuePattern(ROOT, CONTEXT, "parent")
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().oneOfIgnoreCase("auto", ".")))
        .inFile(cngFile)

    val CONTEXT_TYPE = PsiXmlUtils.tagAttributeValuePattern(ROOT, CONTEXT, "type")
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
        .inFile(cngFile)

    val FLOW_STEP_CONTENT_PROPERTY_TYPE = XmlPatterns.xmlAttributeValue()
        .withParent(
            XmlPatterns.xmlAttribute().withLocalName("type")
                .withParent(
                    XmlPatterns.xmlTag().withLocalName("property")
                        .inside(
                            XmlPatterns.xmlTag()
                                .withNamespace("http://www.hybris.com/cockpitng/config/wizard-config")
                                .withLocalName("content")
                        )
                )
        )
        .andNot(XmlPatterns.xmlAttributeValue().withValue(StandardPatterns.string().contains(".")))
        .inside(XmlPatterns.xmlTag().withLocalName(CONTEXT))
        .inFile(cngFile)

}