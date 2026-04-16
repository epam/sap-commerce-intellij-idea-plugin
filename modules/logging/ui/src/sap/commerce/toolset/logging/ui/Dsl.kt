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
import sap.commerce.toolset.ui.addActionListener
import sap.commerce.toolset.ui.addDocumentListener
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

data class LazyLoggerRow(
    val cxLogger: CxLoggerPresentation,
    val placeholderIcon: Placeholder,
    val placeholderHelp: Placeholder,
    val placeholderName: Placeholder,
)

/**
 * Holds per-row visibility state used for client-side filtering of the rendered
 * logger list. Each entry maps a logger name to the [AtomicBooleanProperty] that
 * the row was built with via `row { ... }.visibleIf(prop)`. Flipping the property
 * hides or reveals the row without rebuilding the panel, so scroll position,
 * lazy PSI resolution, and validation state are preserved.
 *
 * [hasMatches] is set to `true` whenever at least one row matches the current
 * filter; [showNoMatches] is its inverse, convenient for binding to a
 * "no results" row via `.visibleIf(showNoMatches)` without pulling in the
 * observable-negation extension.
 *
 * Thread-safe without external locking: the backing map is a [ConcurrentHashMap]
 * (weakly consistent iteration, never throws CME) and the boolean properties
 * are themselves atomic. Writes happen on the background render coroutine
 * while [apply] can be invoked concurrently from the EDT document listener.
 */
internal class LoggerFilterState {
    private val rowVisibility = ConcurrentHashMap<String, AtomicBooleanProperty>()

    val hasMatches: AtomicBooleanProperty = AtomicBooleanProperty(true)
    val showNoMatches: AtomicBooleanProperty = AtomicBooleanProperty(false)

    /**
     * Register a row's visibility toggle under the given logger name. Called
     * while building the panel on the background coroutine.
     */
    fun track(loggerName: String, visibility: AtomicBooleanProperty) {
        rowVisibility[loggerName] = visibility
    }

    fun clear() {
        rowVisibility.clear()
        hasMatches.set(true)
        showNoMatches.set(false)
    }

    fun apply(filterText: String) {
        val needle = filterText.trim()

        if (needle.isEmpty()) {
            rowVisibility.values.forEach { it.set(true) }
            hasMatches.set(rowVisibility.isNotEmpty())
            // Empty filter never shows the "no matches" banner; the outer
            // view already handles the "no loggers at all" case.
            showNoMatches.set(false)
            return
        }

        var anyMatch = false
        // ConcurrentHashMap's iteration is weakly consistent: safe against
        // concurrent puts/clears without CME. Entries added mid-iteration may
        // or may not be seen — that's fine, they start visible=true and the
        // next apply() call from render() or another keystroke will catch up.
        rowVisibility.forEach { (name, visible) ->
            val matches = name.contains(needle, ignoreCase = true)
            if (matches) anyMatch = true
            visible.set(matches)
        }
        hasMatches.set(anyMatch)
        showNoMatches.set(rowVisibility.isNotEmpty() && !anyMatch)
    }
}

internal fun Row.loggerDetailsPlaceholders(cxLogger: CxLoggerPresentation): LazyLoggerRow {
    val placeholderIcon = placeholder().gap(RightGap.SMALL)
    val placeholderHelp = placeholder()
    val placeholderName = placeholder().resizableColumn().gap(RightGap.SMALL)

    placeholderIcon.component = icon(AnimatedIcon.Default.INSTANCE).component
    placeholderName.component = label(cxLogger.name)
        .align(AlignX.FILL)
        .gap(RightGap.SMALL)
        .resizableColumn()
        .component

    return LazyLoggerRow(cxLogger, placeholderIcon, placeholderHelp, placeholderName)
}

internal suspend fun lazyLoggerDetails(
    project: Project, coroutineScope: CoroutineScope, lazyLoggerRow: LazyLoggerRow,
) {
    coroutineScope.ensureActive()
    val cxLogger = lazyLoggerRow.cxLogger

    coroutineScope.launch {
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

/**
 * Text field that serves a dual purpose: the user can type a logger name and
 * press Enter (or click the associated Apply Logger button) to add it, and as
 * they type the rendered logger list below is filtered by a case-insensitive
 * substring match via [onFilterChanged].
 *
 * The blank-text validation only fires on apply (via `validateAll()` in the
 * caller's apply handler), so it never flashes a red ring during filtering.
 */
internal fun Row.newLoggerTextField(
    parentDisposable: Disposable,
    onFilterChanged: (String) -> Unit = {},
    apply: () -> Unit,
): Cell<JBTextField> = textField()
    .resizableColumn()
    .align(AlignX.FILL)
    .validationOnApply {
        if (it.text.isBlank()) error("Please enter a logger name")
        else null
    }
    .applyToComponent {
        addActionListener(parentDisposable) {
            apply()
        }
        document.addDocumentListener(parentDisposable, object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = onFilterChanged(text)
            override fun removeUpdate(e: DocumentEvent) = onFilterChanged(text)
            override fun changedUpdate(e: DocumentEvent) = onFilterChanged(text)
        })
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