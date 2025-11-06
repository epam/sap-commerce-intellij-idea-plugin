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

package sap.commerce.toolset.flexibleSearch.ui

import com.intellij.CommonBundle
import com.intellij.notification.NotificationType
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.LanguageTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.flexibleSearch.FlexibleSearchLanguage
import sap.commerce.toolset.flexibleSearch.restrictions.FlexibleSearchRestriction
import sap.commerce.toolset.scratch.createScratchFile
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.ui.copyLink
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.io.Serial
import javax.swing.ScrollPaneConstants

class FlexibleSearchRestrictionsDialog(
    private val project: Project,
    private val userUid: String,
    private val restrictions: Collection<FlexibleSearchRestriction>,
) : DialogWrapper(project, null, false, IdeModalityType.IDE) {

    private val copyToImpExButton = object : DialogWrapperAction("Copy as ImpEx and Close") {
        @Serial
        private val serialVersionUID: Long = -6131274562037160651L

        override fun doAction(e: ActionEvent) {
            val impexFile = buildString {
                appendLine("#")
                appendLine("# Update FlexibleSearch restrictions applicable for user: $userUid")
                appendLine("#")
                appendLine()
                appendLine("UPDATE SearchRestriction; code[unique=true];restrictedType(code)[unique=true];principal(uid)[unique=true];query")

                restrictions.forEach {
                    appendLine("; ${it.code} ; ${it.typeCode} ; \"${it.principal}\" ; \"${it.query}\"")
                }
            }

            CopyPasteManager.getInstance().setContents(StringSelection(impexFile))

            close(CANCEL_EXIT_CODE)

            Notifications.create(NotificationType.INFORMATION, "Copied All Restrictions to Clipboard", "")
                .addAction("Open as a Scratch File") { _, _ -> createScratchFile(project, impexFile, "impex") }
                .hideAfter(10)
                .notify(project)
        }
    }

    init {
        title = "FlexibleSearch Restrictions"
        super.init()
        setCancelButtonText(CommonBundle.getCloseButtonText())
    }

    override fun createActions() = arrayOf(cancelAction)
    override fun getInitialSize() = JBUI.DialogSizes.large()
    override fun createLeftSideActions() = arrayOf(copyToImpExButton)

    override fun createCenterPanel() = panel {
        row {
            text("The following search restrictions have been identified for the <strong>$userUid</strong> and the given FlexibleSearch query.")
        }

        row {
            scrollCell(restrictions())
                .resizableColumn()
                .align(Align.FILL)
        }
            .resizableRow()
            .topGap(TopGap.MEDIUM)
    }.apply {
        preferredSize = JBUI.DialogSizes.large()
    }

    private fun restrictions() = panel {
        val metaModelAccess = TSMetaModelAccess.getInstance(project)

        restrictions.forEach { restriction ->
            row {
                icon(HybrisIcons.FlexibleSearch.RESTRICTIONS)
                    .gap(RightGap.SMALL)
                copyLink(project, "Restriction", StringUtil.first(restriction.code, 45, true))
                    .bold()
                    .gap(RightGap.COLUMNS)

                val typeIcon = (metaModelAccess.findMetaClassifierByName(restriction.typeCode)
                    ?.icon
                    ?: HybrisIcons.TypeSystem.Types.ITEM)

                icon(typeIcon)
                    .gap(RightGap.SMALL)
                copyLink(project, "Type", StringUtil.first(restriction.typeCode, 30, true))
                    .gap(RightGap.COLUMNS)

                icon(HybrisIcons.FlexibleSearch.PRINCIPAL)
                    .gap(RightGap.SMALL)
                copyLink(project, "Principal", StringUtil.first(restriction.principal, 40, true))
            }.layout(RowLayout.PARENT_GRID)

            row {
                cell(LanguageTextField(FlexibleSearchLanguage, project, restriction.query, false))
                    .align(Align.FILL)
                    .applyToComponent {
                        preferredSize = Dimension(preferredSize.width, JBUI.scale(60))
                        isViewer = true

                        setDisposedWith(disposable)
                        val editorEx = getEditor(true)

                        editorEx?.settings?.isUseSoftWraps = true
                        editorEx?.scrollPane?.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
                    }
            }
                .layout(RowLayout.PARENT_GRID)
                .bottomGap(BottomGap.MEDIUM)
        }
    }
}