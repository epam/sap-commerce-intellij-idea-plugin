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
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiPackage
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.startOffset
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation

internal fun Row.loggerName(project: Project, cxLogger: CxLoggerPresentation) {
    val placeholderIcon = placeholder().gap(RightGap.SMALL)
    val placeholderHelp = placeholder()
    val placeholderName = placeholder().resizableColumn().gap(RightGap.SMALL)

    placeholderIcon.component = icon(AnimatedIcon.Default.INSTANCE).component

    CoroutineScope(Dispatchers.Default).launch {
        val psiElement = smartReadAction(project) {
            val javaPsiFacade = JavaPsiFacade.getInstance(project)

            javaPsiFacade.findPackage(cxLogger.name)
                ?: javaPsiFacade.findClass(cxLogger.name, GlobalSearchScope.allScope(project))
        }

        val icon = smartReadAction(project) {
            psiElement?.getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS)
        } ?: HybrisIcons.Log.Identifier.NA

        val navigableComponent = if (psiElement != null) {
            val pointer = smartReadAction(project) {
                SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiElement)
            }

            link(cxLogger.name) {
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
            label(cxLogger.name).component
        }

        withContext(Dispatchers.EDT) {
            placeholderIcon.component = icon(icon).component
            placeholderName.component = navigableComponent

            cxLogger.presentableParent
                ?.let { placeholderHelp.component = contextHelp(it).component }
        }
    }
}

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
