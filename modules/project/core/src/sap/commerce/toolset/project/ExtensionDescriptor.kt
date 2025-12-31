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

package sap.commerce.toolset.project

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.xmlb.annotations.OptionTag
import sap.commerce.toolset.extensioninfo.EiModelAccess
import sap.commerce.toolset.extensioninfo.context.ExtensionInfoContext
import sap.commerce.toolset.project.descriptor.ModuleDescriptorType
import sap.commerce.toolset.project.descriptor.SubModuleDescriptorType
import java.io.Serial
import kotlin.io.path.Path

data class ExtensionDescriptor(
    @OptionTag val name: String = "",
    @OptionTag val path: String = "",
    @OptionTag val type: ModuleDescriptorType = ModuleDescriptorType.NONE,
    @OptionTag val subModuleType: SubModuleDescriptorType? = null,
    @OptionTag var readonly: Boolean = false,
    @OptionTag val addon: Boolean = false,
    @OptionTag val installedIntoExtensions: Set<String> = emptySet(),
) : UserDataHolderBase(), Comparable<ExtensionDescriptor> {

    fun getContext() = getOrCreateUserDataUnsafe(KEY_INFO) {
        if (path.isEmpty()
            || subModuleType != null
            || (type != ModuleDescriptorType.EXT
                && type != ModuleDescriptorType.OOTB
                && type != ModuleDescriptorType.CUSTOM)
        ) return null

        val moduleRootDirectory = Path(FileUtil.toSystemDependentName(path))

        return EiModelAccess.getInstance().getContext(moduleRootDirectory)
    }

    override fun compareTo(other: ExtensionDescriptor) = name.compareTo(other.name)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4281602487713822529L
        private val KEY_INFO = Key<ExtensionInfoContext?>("sap.commerce.toolset.extensioninfo")
    }
}
