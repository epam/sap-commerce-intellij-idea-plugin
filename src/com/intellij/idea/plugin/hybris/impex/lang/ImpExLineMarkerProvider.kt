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
package com.intellij.idea.plugin.hybris.impex.lang

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.database.csv.CsvFormats
import com.intellij.database.vfs.fragment.CsvTableDataFragmentFile
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.codeInsight.daemon.FlexibleSearchQueryLineMarkerProvider.FlexibleSearchQueryLineMarkerInfo
import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderLine
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import java.util.function.Supplier
import javax.swing.Icon

class ImpExLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is ImpexHeaderLine) return null

        val tooltipProvider = Function { _: PsiElement? -> "Enter Data Edit Mode..." }

        return FlexibleSearchQueryLineMarkerInfo(
            element,
            HybrisIcons.ImpEx.Actions.TABLE_FRAGMENT_MODE,
            tooltipProvider,
            OpenInPreview(element.containingFile.virtualFile, element.tableRange)
        )
    }

    internal class OpenInPreview(val vf: VirtualFile, val tableRange: TextRange) : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val fragmentFile = CsvTableDataFragmentFile(vf, tableRange, CsvFormats.SEMICOLON_SEPARATED_FORMAT.get())

            FileEditorManager.getInstance(project).openFile(fragmentFile)
        }
    }

    internal class ImpExLineMarkerInfo(
        element: PsiElement,
        icon: Icon,
        tooltipProvider: Function<in PsiElement?, String>,
        val action: AnAction
    ) : MergeableLineMarkerInfo<PsiElement?>(
        element, element.textRange, icon, tooltipProvider, null, GutterIconRenderer.Alignment.CENTER,
        Supplier { tooltipProvider.`fun`(element) }
    ) {

        override fun createGutterRenderer(): GutterIconRenderer = object : LineMarkerGutterIconRenderer<PsiElement?>(this) {
            override fun getClickAction() = action
            override fun isNavigateAction() = true
            override fun getPopupMenuActions() = null
        }

        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()
        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is ImpExLineMarkerInfo && info.icon === icon
    }
}
