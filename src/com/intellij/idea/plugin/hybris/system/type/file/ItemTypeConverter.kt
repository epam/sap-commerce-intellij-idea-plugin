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
package com.intellij.idea.plugin.hybris.system.type.file

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.psi.PsiElement
import com.intellij.util.xml.ConvertContext

class ItemTypeConverter : TSConverterBase<ItemType>(ItemType::class.java) {

    override fun searchForName(name: String, context: ConvertContext, meta: TSMetaModelAccess) = meta.findMetaItemByName(name)
        ?.retrieveDom()

    override fun searchAll(context: ConvertContext, meta: TSMetaModelAccess) = meta.getAll<TSGlobalMetaItem>(TSMetaType.META_ITEM)
        .mapNotNull { it.retrieveDom() }

    override fun toString(dom: ItemType?, context: ConvertContext): String? = useAttributeValue(dom) { it.code }
    override fun getPsiElement(resolvedValue: ItemType?): PsiElement? = navigateToValue(resolvedValue) { it.code }

    override fun createLookupElement(dom: ItemType?) = dom?.code?.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withIcon(HybrisIcons.TS_ITEM)
        }
}
