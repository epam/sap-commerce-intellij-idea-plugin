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

package sap.commerce.toolset.occ

import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.BSConstants

object OccPatterns {

    private val springBeansXmlFile = XmlPatterns.xmlTag()
        .withLocalName("beans")
        .withNamespace(HybrisConstants.SPRING_NAMESPACE)

    val OCC_LEVEL_MAPPING_PROPERTY: XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue("value")
        .withSuperParent(
            4,
            XmlPatterns.xmlTag()
                .withLocalName("property")
                .withAttributeValue("name", BSConstants.ATTRIBUTE_VALUE_LEVEL_MAPPING)
                .withParent(
                    XmlPatterns.xmlTag()
                        .withLocalName("bean")
                        .withChild(
                            XmlPatterns.xmlTag()
                                .withLocalName("property")
                                .withAttributeValue("name", BSConstants.ATTRIBUTE_VALUE_DTO_CLASS)
                        )
                )
        )
        .inside(springBeansXmlFile)
}