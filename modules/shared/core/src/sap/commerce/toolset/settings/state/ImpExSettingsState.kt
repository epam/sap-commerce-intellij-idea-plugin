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

package sap.commerce.toolset.settings.state

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("ImpexSettings")
data class ImpExSettingsState(
    @JvmField @OptionTag val groupLocalizedFiles: Boolean = true,
    @JvmField @OptionTag val editMode: ImpExEditModeSettingsState = ImpExEditModeSettingsState(),
    @JvmField @OptionTag val completion: ImpExCompletionSettingsState = ImpExCompletionSettingsState(),
    @JvmField @OptionTag val documentation: ImpExDocumentationSettingsState = ImpExDocumentationSettingsState(),
    // Type name to > set of attribute names
    @JvmField @OptionTag val quoteStringWhitelist: Boolean = true,
    @JvmField @OptionTag val quoteStringWhitelistPattern: Regex = Regex("^([a-zA-Z0-9_$/\\-]+|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$"),
    @JvmField @OptionTag val quoteStringExclusions: Map<String, Set<String>> = mapOf(),
) {
    fun mutable() = Mutable(
        groupLocalizedFiles = groupLocalizedFiles,
        editMode = editMode.mutable(),
        completion = completion.mutable(),
        documentation = documentation.mutable(),
        quoteStringWhitelist = this@ImpExSettingsState.quoteStringWhitelist,
        quoteStringWhitelistPattern = this@ImpExSettingsState.quoteStringWhitelistPattern.pattern,
        quoteStringExclusions = quoteStringExclusions
            .flatMap { (type, attributes) ->
                attributes.map { ImpExQuoteStringExclusion(type, it) }
            }
            .toMutableList(),
    )

    data class Mutable(
        var groupLocalizedFiles: Boolean,
        var editMode: ImpExEditModeSettingsState.Mutable,
        var completion: ImpExCompletionSettingsState.Mutable,
        var documentation: ImpExDocumentationSettingsState.Mutable,
        var quoteStringWhitelist: Boolean,
        var quoteStringWhitelistPattern: String,
        var quoteStringExclusions: MutableList<ImpExQuoteStringExclusion>,
    ) {
        fun immutable() = ImpExSettingsState(
            groupLocalizedFiles = groupLocalizedFiles,
            editMode = editMode.immutable(),
            completion = completion.immutable(),
            documentation = documentation.immutable(),
            quoteStringWhitelist = quoteStringWhitelist,
            quoteStringWhitelistPattern = Regex(quoteStringWhitelistPattern),
            quoteStringExclusions = quoteStringExclusions
                .groupBy(
                    keySelector = { it.typeName },
                    valueTransform = { it.attributeName }
                )
                .mapValues { it.value.toSet() },
        )
    }
}
