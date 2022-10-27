/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.tools.remote.console.preprocess

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.tools.remote.console.CatalogVersionOption
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.console.impl.HybrisImpexConsole

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
interface HybrisConsolePreProcessor {
    fun process(console: HybrisConsole): String
}

class HybrisConsolePreProcessorCatalogVersion : HybrisConsolePreProcessor {
    private val catalogVersionRegexp = """version\s*[\[]\s*default\s*=\s*['"]?\s*(\w*)\s*['"]?\s*[]]""".toRegex()
    
    override fun process(console: HybrisConsole): String {
        var text = console.editorDocument.text

        if (console is HybrisImpexConsole) {
            val selectedItem = console.catalogVersionComboBox.selectedItem
            if (selectedItem is CatalogVersionOption) {
                when (selectedItem.value) {
                    HybrisConstants.IMPEX_CATALOG_VERSION_STAGED -> {
                        text = catalogVersionRegexp.replace(text, "version[default=Staged]")
                        text = text.replace(":Online", ":Staged")
                    }
                    HybrisConstants.IMPEX_CATALOG_VERSION_ONLINE -> {
                        text = catalogVersionRegexp.replace(text, "version[default=Online]")
                        text = text.replace(":Staged", ":Online")
                    }
                }
            }
        }
        
        return text
    }
}