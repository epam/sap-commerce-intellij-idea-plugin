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
package sap.commerce.toolset.flexibleSearch.options

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import sap.commerce.toolset.flexibleSearch.file.FlexibleSearchFileType
import sap.commerce.toolset.flexibleSearch.settings.FlexibleSearchFoldingSettings

class FlexibleSearchCodeFoldingOptionsProvider : BeanConfigurable<FlexibleSearchFoldingSettings>(
    beanInstance = FlexibleSearchFoldingSettings.getInstance(),
    title = FlexibleSearchFileType.name
), CodeFoldingOptionsProvider {
    private val settings
        get() = FlexibleSearchFoldingSettings.getInstance()

    init {
        checkBox(
            "Enabled",
            { settings.enabled },
            { value -> settings.enabled = value }
        )
        checkBox(
            "Show table alias for folded [y] attributes",
            // "If checked attribute <code>{alias.name[en]}</code> will be represented as <code>alias.name</code>"
            { settings.showSelectedTableNameForYColumn },
            { value -> settings.showSelectedTableNameForYColumn = value }
        )
        checkBox(
            "Show language for folded [y] attribute",
            // "If checked localized attribute <code>{name[en]}</code> will be represented as <code>name:en</code>"
            { settings.showLanguageForYColumn },
            { value -> settings.showLanguageForYColumn = value }
        )
    }
}
