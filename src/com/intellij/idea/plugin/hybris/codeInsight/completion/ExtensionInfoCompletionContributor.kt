/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.idea.plugin.hybris.codeInsight.completion.provider.ExtensionsNameCompletionProvider
import com.intellij.idea.plugin.hybris.codeInsight.completion.provider.RequiredExtensionsNameCompletionProvider
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils

class ExtensionInfoCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PsiXmlUtils.tagAttributePattern("requires-extension", "name", HybrisConstants.EXTENSION_INFO_XML),
            RequiredExtensionsNameCompletionProvider.instance
        )
    }
}