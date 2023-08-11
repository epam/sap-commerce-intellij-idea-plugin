/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.tableFormatting.actions

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.tableFormatting.ImpexTableEditor
import com.intellij.idea.plugin.hybris.impex.tableFormatting.actions.handler.ImpexTableActionHandler
import com.intellij.idea.plugin.hybris.impex.tableFormatting.actions.operation.SelectImpexTableOperation
import com.intellij.openapi.editor.Editor

class ImpexTableSelectAction : AbstractImpexTableFormatAction(object : ImpexTableActionHandler() {

    override fun action(editor: Editor) = SelectImpexTableOperation(ImpexTableEditor(editor))
}) {

    init {
        with(templatePresentation) {
            text = "Select ImpEx Statement"
            description = "Select ImpEx statement"
            icon = HybrisIcons.TABLE_SELECT
        }
    }
}
