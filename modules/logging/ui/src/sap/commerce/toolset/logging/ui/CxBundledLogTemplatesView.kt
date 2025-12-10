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

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.logging.presentation.CxLoggerPresentation
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class CxBundledLogTemplatesView(private val project: Project) : Disposable {

    private val showDataPanel = AtomicBooleanProperty(false)
    private val initialized = AtomicBooleanProperty(true)

    private lateinit var dataScrollPane: JBScrollPane
    private val lazyViewPanel by lazy {
        object : ClearableLazyValue<DialogPanel>() {
            override fun compute() = panel {
                row {
                    label("Effective level")
                        .bold()
                        .applyToComponent {
                            preferredSize = Dimension(JBUI.scale(100), preferredSize.height)
                        }
                    label("Logger (package or class name)")
                        .bold()
                        .align(AlignX.FILL)
                }
                    .visibleIf(showDataPanel)

                separator(JBUI.CurrentTheme.Banner.INFO_BORDER_COLOR)
                    .visibleIf(showDataPanel)

                row {
                    dataScrollPane = JBScrollPane(JPanel())
                        .apply { border = null }

                    cell(dataScrollPane)
                        .align(Align.FILL)
                        .visibleIf(showDataPanel)
                }
                    .resizableRow()
            }.apply {
                border = JBUI.Borders.empty(JBUI.insets(10, 16, 0, 16))
            }
        }
    }

    override fun dispose() = lazyViewPanel.drop()

    suspend fun render(coroutineScope: CoroutineScope, loggers: Collection<CxLoggerPresentation>): JComponent {
        initialized.set(false)
        val viewPanel = lazyViewPanel.value

        if (loggers.isEmpty()) {
            val view = noLoggersView("No loggers configured for bundled Log Templates.")

            dataScrollPane.setViewportView(view)
        } else {
            val lazyLoggerRows = mutableListOf<LazyLoggerRow>()
            val view = createLoggersPanel(loggers, lazyLoggerRows)

            lazyLoggerRows.forEach {
                lazyLoggerDetails(project, coroutineScope, it)
            }

            dataScrollPane.setViewportView(view)
        }

        toggleView(showDataPanel, initialized)

        return viewPanel
    }

    private fun toggleView(vararg unhide: AtomicBooleanProperty) = listOf(showDataPanel, initialized)
        .forEach { it.set(unhide.contains(it)) }

    private fun createLoggersPanel(loggers: Collection<CxLoggerPresentation>, lazyLoggerRows: MutableList<LazyLoggerRow>) = panel {
        loggers.forEach { cxLogger ->
            row {
                icon(cxLogger.level.icon)
                label(cxLogger.level.name)
                    .applyToComponent {
                        preferredSize = Dimension(JBUI.scale(68), preferredSize.height)
                    }

                loggerDetailsPlaceholders(cxLogger).also { lazyLoggerRows.add(it) }
            }
        }
    }
}