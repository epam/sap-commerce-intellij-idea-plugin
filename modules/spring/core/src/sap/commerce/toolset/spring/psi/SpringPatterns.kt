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

package sap.commerce.toolset.spring.psi

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlPatterns
import sap.commerce.toolset.HybrisConstants

object SpringPatterns {

    private val itemsXmlFile = PlatformPatterns.psiFile()
        .withName(StandardPatterns.string().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING))

    private val ITEMS_XML_ATTRIBUTE_HANDLER = XmlPatterns.xmlAttributeValue("attributeHandler")
        .inside(
            XmlPatterns.xmlTag()
                .withLocalName("persistence")
        )
        .inFile(itemsXmlFile)

    private val BP_ACTION_BEAN = XmlPatterns.xmlAttributeValue("bean")
        .inside(
            XmlPatterns.xmlTag().withLocalName("action")
                .inside(
                    XmlPatterns.xmlTag()
                        .withLocalName(HybrisConstants.ROOT_TAG_BUSINESS_PROCESS_XML)
                        .withNamespace(HybrisConstants.SCHEMA_BUSINESS_PROCESS)
                )
        )

    val SPRING_MULTI_PATTERN = XmlPatterns.or(
        ITEMS_XML_ATTRIBUTE_HANDLER,
        BP_ACTION_BEAN
    )
}