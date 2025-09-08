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

package sap.commerce.toolset.project.codeInsight.lookup

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import sap.commerce.toolset.project.ExtensionDescriptor
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType

object ExtensionLookupElementFactory {

    fun build(ed: ExtensionDescriptor) = LookupElementBuilder.create(ed.name)
        .withTailText(tail(ed), true)
        .withTypeText(ed.type.title, true)
        .withIcon(ed.type.icon)
        .let {
            PrioritizedLookupElement.withPriority(
                it,
                when (ed.type) {
                    ModuleDescriptorType.CUSTOM -> 5.0
                    ModuleDescriptorType.CCV2_EXTERNAL -> 4.0
                    ModuleDescriptorType.OOTB -> 3.0
                    ModuleDescriptorType.EXT -> 2.0
                    else -> 1.0
                }
            )
        }

    private fun tail(extensionDescriptor: ExtensionDescriptor): String? {
        val tail = listOfNotNull(
            if (extensionDescriptor.deprecated) "deprecated" else null,
            if (extensionDescriptor.extGenTemplateExtension) "template" else null,
            if (extensionDescriptor.addon) "addon" else null,
            if (extensionDescriptor.hacModule) "hac" else null,
            if (extensionDescriptor.backofficeModule) "backoffice" else null,
        ).joinToString(", ")
        return if (tail.isBlank()) null
        else " ($tail)"
    }

}