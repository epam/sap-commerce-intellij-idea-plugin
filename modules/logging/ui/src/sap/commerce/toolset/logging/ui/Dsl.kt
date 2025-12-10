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

package sap.commerce.toolset.logging.ui

import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Iconable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.startOffset
import com.intellij.ui.*
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import kotlinx.coroutines.*
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.logging.CxLogLevel
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import sap.commerce.toolset.ui.addKeyListener
import sap.commerce.toolset.ui.event.KeyListener
import java.awt.event.KeyEvent
import javax.swing.JComponent

data class LazyLoggerRow(
    val cxLogger: CxLoggerPresentation,
    val placeholderIcon: Placeholder,
    val placeholderHelp: Placeholder,
    val placeholderName: Placeholder,
)

internal fun Row.loggerDetailsPlaceholders(cxLogger: CxLoggerPresentation): LazyLoggerRow {
    val placeholderIcon = placeholder().gap(RightGap.SMALL)
    val placeholderHelp = placeholder()
    val placeholderName = placeholder().resizableColumn().gap(RightGap.SMALL)

    placeholderIcon.component = icon(AnimatedIcon.Default.INSTANCE).component
    placeholderName.component = label(cxLogger.name).component

    return LazyLoggerRow(cxLogger, placeholderIcon, placeholderHelp, placeholderName)
}

internal suspend fun lazyLoggerDetails(
    project: Project, coroutineScope: CoroutineScope, lazyLoggerRow: LazyLoggerRow,
) {
    coroutineScope.ensureActive()
    val cxLogger = lazyLoggerRow.cxLogger

    val psiElement = smartReadAction(project) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)

        javaPsiFacade.findPackage(cxLogger.name)
            ?: javaPsiFacade.findClass(cxLogger.name, GlobalSearchScope.allScope(project))
    }

    val icon = smartReadAction(project) {
        psiElement?.getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS)
    } ?: HybrisIcons.Log.Identifier.NA

    lateinit var iconComponent: JComponent
    lateinit var navigableComponent: JComponent
    var placeholderHelp: JComponent? = null

    val pointer = if (psiElement != null) smartReadAction(project) {
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElement)
    } else null


    panel {
        row {
            cxLogger.presentableParent
                ?.let { placeholderHelp = contextHelp(it).component }

            iconComponent = icon(icon).component

            if (pointer != null) {
                navigableComponent = link(cxLogger.name) {
                    when (val resolvedPsiElement = pointer.element) {
                        is PsiPackage -> {
                            CoroutineScope(Dispatchers.Default).launch {
                                val directory = smartReadAction(project) {
                                    resolvedPsiElement.getDirectories(GlobalSearchScope.allScope(project))
                                        .firstOrNull()
                                } ?: return@launch

                                edtWriteAction {
                                    ProjectView.getInstance(project).selectPsiElement(directory, true)
                                }
                            }
                        }

                        is PsiClass -> PsiNavigationSupport.getInstance()
                            .createNavigatable(project, resolvedPsiElement.containingFile.virtualFile, resolvedPsiElement.startOffset)
                            .navigate(true)

                        else -> Notifications.warning(
                            "Logger declaration could not be found",
                            "Unable to locate logger declaration for name: ${cxLogger.name}.",
                        )
                            .hideAfter(10)
                            .notify(project)
                    }
                }.component
            } else {
                navigableComponent = label(cxLogger.name).component
            }
        }
    }

    withContext(Dispatchers.EDT) {
        ensureActive()
        lazyLoggerRow.placeholderIcon.component = iconComponent
        lazyLoggerRow.placeholderName.component = navigableComponent

        if (placeholderHelp != null) lazyLoggerRow.placeholderHelp.component = placeholderHelp
    }
}

internal fun Row.logLevelComboBox(): Cell<ComboBox<CxLogLevel>> = comboBox(
    model = EnumComboBoxModel(CxLogLevel::class.java),
    renderer = SimpleListCellRenderer.create { label, value, _ ->
        if (value != null) {
            label.icon = value.icon
            label.text = value.name
        }
    }
)

internal fun Row.newLoggerTextField(parentDisposable: Disposable, apply: () -> Unit): Cell<JBTextField> = textField()
    .resizableColumn()
    .align(AlignX.FILL)
    .validationOnApply {
        if (it.text.isBlank()) error("Please enter a logger name")
        else null
    }
    .addKeyListener(parentDisposable, object : KeyListener {
        override fun keyReleased(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_ENTER) {
                apply()
            }
        }
    })


internal fun noLoggersView(
    messageText: String,
    status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Warning
) = panel {
    row {
        cell(
            InlineBanner(
                messageText,
                status
            )
                .showCloseButton(false)
        )
            .align(Align.CENTER)
            .resizableColumn()
    }.resizableRow()
}

internal fun Row.cellNoData(property: AtomicBooleanProperty, text: String) = text(text)
    .visibleIf(property)
    .align(Align.CENTER)
    .resizableColumn()
