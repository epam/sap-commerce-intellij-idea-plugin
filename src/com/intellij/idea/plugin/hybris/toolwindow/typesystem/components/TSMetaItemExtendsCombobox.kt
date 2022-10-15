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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.components

import com.intellij.idea.plugin.hybris.type.system.meta.MetaType
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.openapi.project.Project
import com.intellij.ui.CollectionComboBoxModel
import javax.swing.JComboBox

class TSMetaItemExtendsCombobox(private val myProject: Project, private val myMeta: TSMetaItem) : JComboBox<String>() {

    init {
        val dom = myMeta.retrieveDom()

        model = createModel(myMeta)
        selectedItem = dom.extends.stringValue ?: TSMetaItem.IMPLICIT_SUPER_CLASS_NAME
    }

    private fun createModel(meta: TSMetaItem): CollectionComboBoxModel<String> = with(CollectionComboBoxModel<String>()) {
        TSMetaModelAccess.getInstance(myProject).metaModel.getMetaType<TSMetaItem>(MetaType.META_ITEM).values
            .filter { it != meta }
            .map { it.name }
            .sortedBy { it }
            .forEach { add(it) }

        this
    }

    companion object {
        private const val serialVersionUID: Long = 3360586061907026244L
    }
}