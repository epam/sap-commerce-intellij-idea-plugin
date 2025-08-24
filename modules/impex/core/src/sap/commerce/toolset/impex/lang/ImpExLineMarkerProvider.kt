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
package sap.commerce.toolset.impex.lang

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.database.csv.CsvFormat
import com.intellij.database.csv.CsvRecordFormat
import com.intellij.database.vfs.fragment.CsvTableDataFragmentFile
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.util.Function
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Plugin
import sap.commerce.toolset.impex.psi.ImpExHeaderLine
import sap.commerce.toolset.settings.yDeveloperSettings
import java.util.*
import java.util.function.Supplier
import javax.swing.Icon

class ImpExLineMarkerProvider : LineMarkerProvider {

    private val valueSeparator = ";"
    private val quotationPolicy = CsvRecordFormat.QuotationPolicy.NEVER

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (Plugin.DATABASE.isDisabled()) return null
        if (Plugin.GRID.isDisabled()) return null
        if (element !is ImpExHeaderLine) return null

        return ImpExDataEditModeLineMarkerInfo(
            element.anyHeaderMode.firstLeaf(),
            HybrisIcons.ImpEx.Actions.TABLE_FRAGMENT_MODE
        )
    }

    private fun openEditMode(leaf: PsiElement?) {
        val element = leaf?.parentOfType<ImpExHeaderLine>()
        val project = element?.project ?: return
        val tableRange = element.tableRange
        val format = getImpExFormat(project)
        val fragmentFile = CsvTableDataFragmentFile(leaf.containingFile.virtualFile, tableRange, format)

        FileEditorManager.getInstance(project).openFile(fragmentFile)
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
        val headerFormat = if (firstRowIsHeader) CsvRecordFormat("", "", null, emptyList(), quotationPolicy, valueSeparator, "\n", trimWhitespace)
        else null
        val dataFormat = CsvRecordFormat("", "", null, emptyList(), quotationPolicy, valueSeparator, "\n", trimWhitespace)

        return CsvFormat("ImpEx", dataFormat, headerFormat, "ImpEx", false)
    }

    private inner class ImpExDataEditModeLineMarkerInfo(
        leaf: PsiElement,
        icon: Icon,
    ) : MergeableLineMarkerInfo<PsiElement?>(
        leaf, leaf.textRange, icon,
        Function { "Enter Data Edit Mode" },
        { _, e -> openEditMode(e) },
        GutterIconRenderer.Alignment.CENTER,
        Supplier { "Enter Data Edit Mode" }
    ) {
        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()
        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is ImpExDataEditModeLineMarkerInfo && info.icon === icon
    }
}
