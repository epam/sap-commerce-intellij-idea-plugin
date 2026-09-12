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

package sap.commerce.toolset.properties.ui

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.meta.CxProperty
import sap.commerce.toolset.properties.meta.CxPropertyDeclaration
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import javax.swing.JComponent

/**
 * Reports where a property comes from: the value the remote instance serves, the one the project declares, the whole
 * precedence chain behind it, and whether any of the declaring files takes part in the active system configuration.
 *
 * @param declared the project's own view of the property, or `null` when the project does not declare it at all.
 * @param resolvedValue value the project resolves to, `null` when every declaration is ignored.
 */
class CxPropertyReportDialog(
    private val project: Project,
    private val remote: CxPropertyPresentation,
    private val declared: CxProperty?,
    private val resolvedValue: String?,
) : DialogWrapper(project) {

    init {
        title = "Property Report"
        setOKButtonText("Close")
        init()
    }

    override fun createActions() = arrayOf(okAction)

    override fun createCenterPanel(): JComponent = JBScrollPane(reportPanel()).apply {
        border = JBUI.Borders.empty()
        preferredSize = JBUI.size(DIALOG_WIDTH, DIALOG_HEIGHT)
    }

    private fun reportPanel() = panel {
        row("Property:") {
            label(remote.key)
        }.layout(RowLayout.PARENT_GRID)

        row("Remote value:") {
            valueLabel(remote.value)
        }.layout(RowLayout.PARENT_GRID)

        when {
            declared == null -> row {
                icon(HybrisIcons.Property.IGNORED)
                label("Not declared by this project — the value exists only on the remote instance.")
            }

            declared.isIgnored -> row {
                icon(HybrisIcons.Property.IGNORED)
                label("Declared only by extensions which `localextensions.xml` does not list, so the running system never reads them.")
            }

            else -> row("Project value:") {
                valueLabel(resolvedValue.orEmpty())
            }.layout(RowLayout.PARENT_GRID)
        }

        declared?.let { property ->
            group("Declarations — highest precedence first") {
                property.declarations.asReversed().forEach { declaration -> declarationRow(property, declaration) }
            }
        }
    }.apply {
        border = JBUI.Borders.empty(8, 16)
    }

    private fun Panel.declarationRow(property: CxProperty, declaration: CxPropertyDeclaration) = row {
        icon(statusIcon(property, declaration))

        if (declaration.navigatable) {
            link(declaration.source.presentableName) { navigate(declaration) }
        } else {
            label(declaration.source.presentableName)
        }

        valueLabel(declaration.value)
        label(statusText(property, declaration))
    }.layout(RowLayout.PARENT_GRID)

    private fun Row.valueLabel(value: String) = label(StringUtil.shortenTextWithEllipsis(value.ifBlank { EMPTY_VALUE }, VALUE_MAX_LENGTH, VALUE_SUFFIX_LENGTH))
        .applyToComponent { toolTipText = value.takeIf { it.isNotBlank() } }

    private fun statusIcon(property: CxProperty, declaration: CxPropertyDeclaration) = when {
        !declaration.source.active -> HybrisIcons.Property.IGNORED
        declaration == property.declaration -> HybrisIcons.Property.EFFECTIVE
        else -> HybrisIcons.Property.SHADOWED
    }

    private fun statusText(property: CxProperty, declaration: CxPropertyDeclaration) = when {
        !declaration.source.active -> "Ignored"
        declaration == property.declaration -> "Effective"
        else -> "Overridden"
    }

    private fun navigate(declaration: CxPropertyDeclaration) {
        val file = declaration.source.file ?: return

        PsiNavigationSupport.getInstance()
            .createNavigatable(project, file, declaration.offset)
            .navigate(true)

        close(OK_EXIT_CODE)
    }

    companion object {
        private const val DIALOG_WIDTH = 720
        private const val DIALOG_HEIGHT = 420
        private const val VALUE_MAX_LENGTH = 70
        private const val VALUE_SUFFIX_LENGTH = 12
        private const val EMPTY_VALUE = "<empty>"
    }
}
