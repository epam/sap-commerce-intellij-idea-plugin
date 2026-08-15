/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.ui.components

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.settings.state.ImpExQuoteStringExclusion
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.ui.ifOk
import sap.commerce.toolset.ui.list.AddEditDeleteList
import java.io.Serial
import javax.swing.Icon

class ImpExQuoteStringExclusionsListPanel(
    private val project: Project,
    disposable: Disposable?,
) : AddEditDeleteList<ImpExQuoteStringExclusion>(disposable) {

    override fun getName(element: ImpExQuoteStringExclusion): String = element.presentationTitle

    override fun getIcon(element: ImpExQuoteStringExclusion): Icon = element.typeName
        .let {
            runCatching { TSMetaModelAccess.getInstance(project).findMetaItemByName(it) }
        }
        .getOrNull()
        ?.icon
        ?: HybrisIcons.TypeSystem.Types.UNKNOWN

    override fun newItem(element: ImpExQuoteStringExclusion?) = ImpExQuoteStringExclusion("", "")

    override fun createDialog(item: ImpExQuoteStringExclusion): DialogWrapper = ImpExQuoteStringExclusionDialog(
        project = project,
        exclusion = item,
        parentComponent = this,
        dialogTitle = "Define Exclusion"
    )

    override fun editDialog(item: ImpExQuoteStringExclusion): DialogWrapper = ImpExQuoteStringExclusionDialog(
        project = project,
        exclusion = item,
        parentComponent = this,
        dialogTitle = "Edit Exclusion"
    )

    override fun findItemToAdd() = ImpExQuoteStringExclusion("", "").let { item ->
        ImpExQuoteStringExclusionDialog(
            project = project,
            exclusion = item,
            parentComponent = this,
            dialogTitle = "Define Exclusion"
        )
            .ifOk { item }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4193538914487200332L
    }
}
