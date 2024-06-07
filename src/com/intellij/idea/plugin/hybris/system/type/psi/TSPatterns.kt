/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.psi

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils.insideTagPattern
import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils.tagAttributeValuePattern
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns

object TSPatterns {

    private val itemsXmlFile = PlatformPatterns.psiFile()
        .withName(StandardPatterns.string().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING))

    val INDEX_KEY_ATTRIBUTE = XmlPatterns.or(
        tagAttributeValuePattern(Index.KEY, IndexKey.ATTRIBUTE)
            .inside(
                insideTagPattern(ItemType.INDEXES)
                    .inside(insideTagPattern(ItemTypes.ITEMTYPE))
            )
            .inFile(itemsXmlFile),
        tagAttributeValuePattern(Index.INCLUDE, IndexInclude.ATTRIBUTE)
            .inside(
                insideTagPattern(ItemType.INDEXES)
                    .inside(insideTagPattern(ItemTypes.ITEMTYPE))
            )
            .inFile(itemsXmlFile)
    )


    val SPRING_INTERCEPTOR_TYPE_CODE: XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue("value")
        .withSuperParent(
            2,
            XmlPatterns.xmlTag()
                .withLocalName("property")
                .withAttributeValue("name", "typeCode")
                .withParent(
                    XmlPatterns.xmlTag()
                        .withLocalName("bean")
                        .withAttributeValue("class", HybrisConstants.CLASS_FQN_INTERCEPTOR_MAPPING)
                )
        )
        .inside(
            XmlPatterns.xmlTag()
                .withLocalName("beans")
                .withNamespace(HybrisConstants.SPRING_NAMESPACE)
        )
}