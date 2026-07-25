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

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.MarkupEditorFilter
import com.intellij.openapi.editor.markup.MarkupEditorFilterFactory
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.firstLeaf
import com.intellij.psi.util.parentOfType
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.Function
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.ImpExConstants
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.scratch.createScratchFile
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.Transformer
import java.awt.Point
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.function.Supplier
import javax.swing.Icon
import kotlin.time.Duration.Companion.minutes

class ImpExValueLineTransformLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (DumbService.isDumb(element.project)) return null
        if (element !is ImpExValueLine) return null
        element.headerLine ?: return null
        val transformers = Transformer.EP.extensionList.filter { it.isApplicable(element) }
        if (element.headerLine?.uniqueFullHeaderParameters?.any { it.docIdUsages.isNotEmpty() } == true) return null

        return ImpExLineMarkerInfo(
            transformers,
            element.firstLeaf(),
            HybrisIcons.ImpEx.Actions.TRANSFORM
        )
    }

    private fun handler(
        event: MouseEvent,
        leaf: PsiElement?,
        transformers: List<Transformer<in PsiElement, out TransformationResult>>
    ) {
        val element = leaf?.parentOfType<ImpExValueLine>() ?: return
        val project = element.project
        val metaType = element.metaType?.name
        val includeAllAttributes = AtomicBooleanProperty(metaType != null)
        var selectedTransformerIndex = 0
        lateinit var myPopup: JBPopup

        val content = panel {
            if (metaType != null) {
                row {
                    checkBox(i18n("hybris.impex.actions.transform.valueLine.dialog.include_all_attributes", metaType))
                        .bindSelected(includeAllAttributes)
                }
            }

            separator()

            row {
                comboBox(transformers.map { it.fileType.name })
                    .label(i18n("hybris.impex.actions.transform.valueLine.dialog.transformer"))
                    .applyToComponent {
                        addActionListener {
                            selectedTransformerIndex = selectedIndex.coerceAtLeast(0)
                        }
                    }

                button(i18n("hybris.impex.actions.transform.valueLine.dialog.transform")) {
                    myPopup.closeOk(null)
                }.align(AlignX.RIGHT)
            }
        }.apply { border = JBUI.Borders.empty(12) }

        myPopup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(content, content.preferredFocusedComponent)
            .setBorderColor(JBColor.border())
            .setTitle(i18n("hybris.impex.actions.transform.valueLine.dialog.title"))
            .setTitleIcon(ActiveIcon(HybrisIcons.ImpEx.Actions.TRANSFORM))
            .setKeyEventHandler {
                val enterKey = it.keyCode == KeyEvent.VK_ENTER
                if (enterKey) myPopup.closeOk(it)
                enterKey
            }
            .createPopup()
            .also {
                it.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) {
                        if (!event.isOk) return
                        content.apply()
                        val transformer = transformers[selectedTransformerIndex]
                        element.putUserData(ImpExConstants.Transform.INCLUDE_ALL_ATTRIBUTES, includeAllAttributes.get())
                        transformer.transform(element) { result ->
                            notify(project, result, transformer)
                        }
                    }
                })

                it.show(RelativePoint(event.component, Point(event.x + 12, event.y - 4)))
            }
    }

    private fun notify(project: Project, result: TransformationResult, transformer: Transformer<in PsiElement, out TransformationResult>) {
        Notifications.create(
            NotificationType.INFORMATION,
            i18n("hybris.impex.actions.transform.valueLine.notification.title"),
            result.content
        )
            .addAction("Copy to Clipboard") { _, _ ->
                CopyPasteManager.getInstance().setContents(StringSelection(result.content))
            }
            .addAction("Open as a Scratch File") { _, _ ->
                createScratchFile(project, result.content, transformer.fileType.defaultExtension)
            }
            .hideAfter(1.minutes)
            .notify(project)
    }

    private inner class ImpExLineMarkerInfo(
        transformers: List<Transformer<in PsiElement, out TransformationResult>>,
        leaf: PsiElement,
        icon: Icon,
    ) : MergeableLineMarkerInfo<PsiElement?>(
        leaf, leaf.textRange, icon,
        Function { i18n("hybris.impex.actions.transform.valueLine.name") },
        { event, leaf -> handler(event, leaf, transformers) },
        GutterIconRenderer.Alignment.CENTER,
        Supplier { i18n("hybris.impex.actions.transform.valueLine.name") }
    ) {
        override fun getEditorFilter(): MarkupEditorFilter = MarkupEditorFilterFactory.createIsNotDiffFilter()
        override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon = icon
        override fun canMergeWith(info: MergeableLineMarkerInfo<*>) = info is ImpExLineMarkerInfo && info.icon === icon
    }
}
