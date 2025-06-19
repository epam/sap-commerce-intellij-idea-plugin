/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.flexibleSearch.actions

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.FLEXIBLE_SEARCH_PROPERTIES_KEY
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchSplitEditor
import com.intellij.idea.plugin.hybris.flexibleSearch.file.FlexibleSearchFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import java.text.ParseException
import java.text.SimpleDateFormat

class FlexibleSearchExecuteQueryAction : AbstractExecuteAction(
    FlexibleSearchFileType.defaultExtension,
    HybrisConstants.CONSOLE_TITLE_FLEXIBLE_SEARCH
) {

    init {
        with(templatePresentation) {
            text = message("hybris.fxs.actions.execute_query")
            description = message("hybris.fxs.actions.execute_query.description")
            icon = HybrisIcons.Console.Actions.EXECUTE
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val file = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        val enabled = file != null && file.name.endsWith(".$extension")
        e.presentation.isEnabledAndVisible = enabled
    }

    override fun processContent(content: String, editor: Editor, project: Project): String {
        val flexibleSearchSplitEditor = FileEditorManager.getInstance(project).getEditors(editor.virtualFile).find { it is FlexibleSearchSplitEditor } ?: return content
        val flexibleSearchProperties = flexibleSearchSplitEditor.getUserData(FLEXIBLE_SEARCH_PROPERTIES_KEY)
        var updatedContent = content.trim()

        flexibleSearchProperties?.forEach {
            // Skip empty values
            if (it.value.isNotBlank()) {
                updatedContent = updatedContent.replaceFirst("?${it.name}", formatValue(it.value))
            }
        }

        return updatedContent
    }

    private fun formatValue(value: String): String {
        if (value.isBlank()) return ""

        return when (identifyType(value)) {
            ValueType.STRING -> "'$value'"
            ValueType.DATE -> "'$value'"  // Format dates as strings for SQL compatibility
            else -> value
        }
    }

    private fun identifyType(value: String): ValueType {
        val trimmed = value.trim()

        if (trimmed.isBlank()) {
            return ValueType.STRING
        }

        if (trimmed.equals("true", ignoreCase = true) || trimmed.equals("false", ignoreCase = true)) {
            return ValueType.BOOLEAN
        }

        trimmed.toIntOrNull()?.let {
            return ValueType.INTEGER
        }

        trimmed.toLongOrNull()?.let {
            return ValueType.LONG
        }

        trimmed.toDoubleOrNull()?.let {
            return ValueType.DOUBLE
        }

        // Use ThreadLocal for SimpleDateFormat to improve performance and thread safety
        if (isDate(trimmed)) {
            return ValueType.DATE
        }

        // Default to String
        return ValueType.STRING
    }

    private fun isDate(value: String): Boolean {
        for (format in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(format)
                sdf.isLenient = false
                if (sdf.parse(value) != null) {
                    return true
                }
            } catch (_: ParseException) {
                // continue
            }
        }
        return false
    }

    companion object {
        private val DATE_FORMATS = listOf(
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
    }


}

enum class ValueType {
    INTEGER,
    LONG,
    DOUBLE,
    BOOLEAN,
    DATE,
    STRING
}
