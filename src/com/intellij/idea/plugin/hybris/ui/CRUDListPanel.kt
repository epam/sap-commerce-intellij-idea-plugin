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
package com.intellij.idea.plugin.hybris.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.AddEditDeleteListPanel
import java.io.Serial
import java.util.*

class CRUDListPanel(
    private val addTitle: String,
    private val addText: String,
    private val editTitle: String,
    private val editText: String
) : AddEditDeleteListPanel<String>(null, emptyList<String>()) {

    override fun findItemToAdd() = showEditDialog(
        "",
        HybrisI18NBundleUtils.message(addTitle),
        HybrisI18NBundleUtils.message(addText)
    )

    override fun editSelectedItem(item: String) = showEditDialog(
        item,
        HybrisI18NBundleUtils.message(editTitle),
        HybrisI18NBundleUtils.message(editText)
    )

    private fun showEditDialog(
        initialValue: String,
        title: String,
        message: String
    ) = Messages.showInputDialog(
        this,
        message,
        title,
        null,
        initialValue,
        object : InputValidatorEx {
            override fun checkInput(inputString: String) = StringUtil.isNotEmpty(inputString)

            override fun canClose(inputString: String) = StringUtil.isNotEmpty(inputString)
                && (myListModel.contains(inputString).not() || initialValue == inputString)

            override fun getErrorText(inputString: String): String? {
                if (checkInput(inputString).not()) return "directory/file name string cannot be empty"
                if (canClose(inputString).not()) return "duplicities are not allowed (nor make any sense)"

                return null
            }
        }
    )

    var data: List<String>
        get() = Collections.list(myListModel.elements())
        set(itemList) {
            myListModel.clear()
            for (itemToAdd in itemList) {
                super.addElement(itemToAdd)
            }
        }

    companion object {
        @Serial
        private val serialVersionUID = -6339262026248471671L
    }
}
