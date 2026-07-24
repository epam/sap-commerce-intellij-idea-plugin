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

package sap.commerce.toolset.impex.codeInsight.daemon

import com.intellij.database.csv.CsvFormat
import com.intellij.database.csv.CsvRecordFormat
import com.intellij.database.vfs.fragment.CsvTableDataFragmentFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.settings.yDeveloperSettings
import java.util.*

object ImpExDataEditModeHandler {

    private const val VALUE_SEPARATOR = ";"

    fun extract(leaf: PsiElement?): VirtualFile? {
        val element = leaf?.parentOfType<ImpExHeaderLine>()
        val project = element?.project ?: return null
        val tableRange = element.tableRange
        val format = getImpExFormat(project)
        return CsvTableDataFragmentFile(leaf.containingFile.virtualFile, tableRange, format)
    }

    private fun getImpExFormat(project: Project): CsvFormat {
        val editModeSettings = project.yDeveloperSettings.impexSettings.editMode

        val key = BitSet(2).also {
            it.set(0, editModeSettings.firstRowIsHeader)
            it.set(1, editModeSettings.trimWhitespace)
        }

        return xsvImpExFormat(
            firstRowIsHeader = key.get(0),
            trimWhitespace = key.get(1)
        )
    }

    private fun xsvImpExFormat(firstRowIsHeader: Boolean, trimWhitespace: Boolean): CsvFormat {
        val quotationPolicy = CsvRecordFormat.QuotationPolicy.NEVER
        val headerFormat = if (firstRowIsHeader) CsvRecordFormat("", "", null, emptyList(), quotationPolicy, VALUE_SEPARATOR, "\n", trimWhitespace)
        else null
        val dataFormat = CsvRecordFormat("", "", null, emptyList(), quotationPolicy, VALUE_SEPARATOR, "\n", trimWhitespace)

        return CsvFormat("ImpEx", dataFormat, headerFormat, "ImpEx", false)
    }
}