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
package com.intellij.idea.plugin.hybris.beans.meta.impl

import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModel
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaType
import com.intellij.idea.plugin.hybris.beans.meta.model.impl.BeansMetaEnumImpl
import com.intellij.idea.plugin.hybris.beans.model.Enum
import com.intellij.idea.plugin.hybris.beans.model.EnumValue
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile

class BeansMetaModelBuilder(
    private val myModule: Module,
    private val myPsiFile: PsiFile,
    private val myCustom: Boolean
) {

    private val myMetaModel = BeansMetaModel(myModule, myPsiFile, myCustom)

    fun build() = myMetaModel

    fun withEnumTypes(types: List<Enum>): BeansMetaModelBuilder {
        types
            .mapNotNull { create(it) }
            .forEach { myMetaModel.addMetaModel(it, BeansMetaType.META_ENUM) }

        return this
    }

    private fun create(dom: Enum): BeansMetaEnum? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaEnumImpl(
            dom, myModule, name, myCustom,
            values = createEnumValues(dom)
        )
    }


    private fun createEnumValues(dom: Enum): Map<String, BeansMetaEnum.BeansMetaEnumValue> = dom.values
        .mapNotNull { create(it) }
        .associateByTo(CaseInsensitive.CaseInsensitiveConcurrentHashMap()) { attr -> attr.name?.trim { it <= ' ' } }

    private fun create(dom: EnumValue): BeansMetaEnum.BeansMetaEnumValue? {
        val name = BeansMetaModelNameProvider.extract(dom) ?: return null
        return BeansMetaEnumImpl.BeansMetaEnumValueImpl(dom, myModule, myCustom, name)
    }

}