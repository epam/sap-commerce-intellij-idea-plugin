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
package com.intellij.idea.plugin.hybris.impex.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexMacroDescriptor
import com.intellij.idea.plugin.hybris.psi.util.getLineNumber
import com.intellij.openapi.util.Key
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.HashSetValuedHashMap
import java.io.Serial

class ImpexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ImpexLanguage) {

    override fun getFileType() = ImpexFileType
    override fun toString() = "ImpEx File"
    override fun getIcon(flags: Int) = super.getIcon(flags)

    fun getHeaderLines(): Map<ImpexHeaderLine, Collection<ImpexValueLine>> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_HEADER_LINES, {
        val headerLines = childrenOfType<ImpexHeaderLine>()
            .associateWith { it.valueLines }

        CachedValueProvider.Result.createSingleDependency(
            headerLines,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    fun getMacroDescriptors(): MultiValuedMap<String, ImpexMacroDescriptor> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_MACRO_DESCRIPTORS, {
        CachedValueProvider.Result.createSingleDependency(
            HashSetValuedHashMap(),
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    fun getSuitableMacroDescriptor(macroName: String, macroUsage: PsiElement) = getMacroDescriptors()[macroName]
        ?.asSequence()
        ?.map { it.psiElement.getLineNumber() to it }
        ?.filter { macroUsage.getLineNumber() >= it.first }
        ?.sortedByDescending { it.first }
        ?.map { it.second }
        ?.firstOrNull()

    companion object {
        val CACHE_KEY_HEADER_LINES = Key.create<CachedValue<Map<ImpexHeaderLine, Collection<ImpexValueLine>>>>("SAP_CX_IMPEX_HEADER_LINES")
        val CACHE_KEY_MACRO_DESCRIPTORS = Key.create<CachedValue<MultiValuedMap<String, ImpexMacroDescriptor>>>("SAP_CX_IMPEX_MACRO_DESCRIPTORS")

        @Serial
        private val serialVersionUID: Long = 5112646813557523662L
    }
}