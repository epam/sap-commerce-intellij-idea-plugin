/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
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

package com.intellij.idea.plugin.hybris.system.manifest.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.idea.plugin.hybris.codeInsight.completion.provider.ExtensionNameCompletionProvider
import com.intellij.idea.plugin.hybris.system.manifest.codeInsight.completion.provider.ExtensionPackNameCompletionProvider
import com.intellij.idea.plugin.hybris.system.manifest.codeInsight.completion.provider.TemplateExtensionNameCompletionProvider
import com.intellij.idea.plugin.hybris.system.manifest.psi.ManifestPatterns

class ManifestJsonCompletionProvider : CompletionContributor() {

    init {
        extend(
                CompletionType.BASIC,
                ManifestPatterns.EXTENSION_NAME,
                ExtensionNameCompletionProvider()
        )
        extend(
                CompletionType.BASIC,
                ManifestPatterns.TEMPLATE_EXTENSION_NAME,
                TemplateExtensionNameCompletionProvider()
        )
        extend(
                CompletionType.BASIC,
                ManifestPatterns.EXTENSION_PACK_NAME,
                ExtensionPackNameCompletionProvider()
        )
    }

}
