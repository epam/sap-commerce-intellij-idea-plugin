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

package sap.commerce.toolset.groovy.actionSystem

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.Transformer
import java.awt.event.KeyEvent
import kotlin.time.Duration.Companion.minutes

class GroovyTransformAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE).asSafely<GroovyFile>()
            ?: return@ifNotFromSearchPopup
        val canTransform = Transformer.EP.extensionList.any { it.isApplicable(psiFile) }
        if (!canTransform) {
            e.presentation.isEnabledAndVisible = false
            return@ifNotFromSearchPopup
        }

        e.presentation.text = i18n("hybris.groovy.actions.transform")
        e.presentation.description = i18n("hybris.groovy.actions.transform.description")
        val isDumb = e.project?.let { DumbService.isDumb(it) } ?: false
        e.presentation.icon = HybrisIcons.Groovy.Actions.TRANSFORM
        e.presentation.isEnabled = !isDumb
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)?.asSafely<GroovyFile>() ?: return

        val applicableTransformers = Transformer.EP.extensionList.filter { it.isApplicable(psiFile) }
        if (applicableTransformers.isEmpty()) return

        val inputEvent = e.inputEvent ?: return

        val defaultName = psiFile.name.removeSuffix(".groovy")
        var scriptName = defaultName
        var selectedTransformerIndex = 0
        lateinit var myPopup: JBPopup

        val content = panel {
            row(i18n("hybris.groovy.actions.transform.dialog.name")) {
                textField()
                    .bindText(getter = { scriptName }, setter = { scriptName = it })
                    .resizableColumn()
                    .align(AlignX.FILL)
            }

            separator()

            row {
                comboBox(applicableTransformers.map { it.presentableTitle })
                    .label(i18n("hybris.groovy.actions.transform.dialog.transformer"))
                    .applyToComponent {
                        addActionListener {
                            selectedTransformerIndex = selectedIndex.coerceAtLeast(0)
                        }
                    }

                button(i18n("hybris.groovy.actions.transform.dialog.transform")) {
                    myPopup.closeOk(null)
                }.align(AlignX.RIGHT)
            }
        }.apply {
            border = JBUI.Borders.empty(8, 16)
        }

        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(content, content.preferredFocusedComponent)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(true)
            .setTitle(i18n("hybris.groovy.actions.transform.dialog.title"))
            .setTitleIcon(ActiveIcon(HybrisIcons.Groovy.Actions.TRANSFORM))
            .setKeyEventHandler {
                val enterKey = it.keyCode == KeyEvent.VK_ENTER
                if (enterKey) myPopup.closeOk(it)
                enterKey
            }
            .createPopup()
            .also { popup ->
                myPopup = popup
                popup.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) {
                        if (!event.isOk) return
                        content.apply()

                        psiFile.putUserData(GroovyConstants.Transform.SCRIPT_NAME, scriptName.trim().ifEmpty { defaultName })

                        val transformer = applicableTransformers[selectedTransformerIndex]
                        transformer.transform(psiFile) { result ->
                            notify(project, result)
                        }
                    }
                })
                popup.showUnderneathOf(inputEvent.component)
            }
    }

    private fun notify(project: Project, result: TransformationResult) = Notifications.create(
        NotificationType.INFORMATION,
        i18n("hybris.groovy.actions.transform.notification.title"),
        result.description
    )
        .apply {
            result.handlers.forEach {
                this.addAction(it.presentationTitle) { _, _ -> it.handle() }
            }
        }
        .hideAfter(1.minutes)
        .notify(project)
}
