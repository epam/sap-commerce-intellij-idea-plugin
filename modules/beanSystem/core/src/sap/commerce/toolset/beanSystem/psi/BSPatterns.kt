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

package sap.commerce.toolset.beanSystem.psi

import com.intellij.patterns.*
import com.intellij.psi.xml.XmlAttributeValue
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.BSConstants
import sap.commerce.toolset.beanSystem.model.*
import sap.commerce.toolset.beanSystem.model.Enum

object BSPatterns {

    private val springBeansXmlFile = XmlPatterns.xmlTag()
        .withLocalName("beans")
        .withNamespace(HybrisConstants.SPRING_NAMESPACE)

    private val beansXmlFile = PlatformPatterns.psiFile()
        .withName(StandardPatterns.string().endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING))

    val BEAN_EXTENDS: XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue(Bean.EXTENDS)
        .withParent(
            XmlPatterns.xmlAttribute()
                .withLocalName("extends")
        )
        .inside(beansXmlFile)

    val BEAN_CLASS: ElementPattern<XmlAttributeValue> = XmlPatterns.or(
        XmlPatterns.xmlAttributeValue(Bean.CLASS)
            .withSuperParent(
                2,
                XmlPatterns.xmlTag()
                    .withLocalName(Beans.BEAN)
            )
            .inside(beansXmlFile),

        XmlPatterns.xmlAttributeValue("value")
            .withSuperParent(
                2,
                XmlPatterns.xmlTag()
                    .withLocalName("property")
                    .withAttributeValue("name", BSConstants.ATTRIBUTE_VALUE_DTO_CLASS)
                    .withParent(
                        XmlPatterns.xmlTag().withLocalName("bean")
                    )
            )
            .inside(springBeansXmlFile)
    )

    val ENUM_CLASS: XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue(Enum.CLASS)
        .withSuperParent(
            2,
            XmlPatterns.xmlTag()
                .withLocalName(Beans.ENUM)
        )
        .inside(beansXmlFile)

    val BEAN_PROPERTY_TYPE: XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue(Property.TYPE)
        .withSuperParent(
            2,
            XmlPatterns.xmlTag()
                .withLocalName(Bean.PROPERTY)
                .withParent(
                    XmlPatterns.xmlTag()
                        .withLocalName(Beans.BEAN)
                )
        )
        .inside(beansXmlFile)

    val HINT_NAME: XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue(Hint.NAME)
        .withSuperParent(
            2,
            XmlPatterns.xmlTag()
                .withLocalName(Hints.HINT)
        )
        .inside(beansXmlFile)

}