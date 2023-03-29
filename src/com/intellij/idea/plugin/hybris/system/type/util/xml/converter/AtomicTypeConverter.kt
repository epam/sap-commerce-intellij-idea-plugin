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
package com.intellij.idea.plugin.hybris.system.type.util.xml.converter

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaAtomic
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaType
import com.intellij.idea.plugin.hybris.system.type.model.AtomicType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.xml.ConvertContext

class AtomicTypeConverter : AbstractTSConverterBase<AtomicType>(AtomicType::class.java) {

    override fun searchForName(name: String, context: ConvertContext, meta: TSMetaModelAccess) = meta.findMetaAtomicByName(name)
        ?.retrieveDom()

    override fun searchAll(context: ConvertContext, meta: TSMetaModelAccess) = meta.getAll<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC)
        .mapNotNull { it.retrieveDom() }

    override fun toString(dom: AtomicType?, context: ConvertContext): String? = useAttributeValue(dom) { it.clazz }
    override fun getPsiElement(resolvedValue: AtomicType?): XmlAttributeValue? = navigateToValue(resolvedValue) { it.clazz }

    override fun createLookupElement(dom: AtomicType?) = dom?.clazz?.stringValue
        ?.let {
            LookupElementBuilder.create(it)
                .withIcon(HybrisIcons.TS_ATOMIC)
        }
}
