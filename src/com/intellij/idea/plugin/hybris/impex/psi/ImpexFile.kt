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
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiManager
import com.intellij.psi.util.*
import org.apache.commons.lang3.StringUtils
import java.io.File
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

    fun getExternalImpExFiles(): Collection<ImpexFile> = CachedValuesManager.getManager(project).getCachedValue(this, CACHE_KEY_EXTERNAL_FILES, {
        val externalImpExFiles = mutableListOf<ImpexFile>()

        this.acceptChildren(object : ImpexVisitor() {
            override fun visitScript(o: ImpexScript) {
                super.visitScript(o)

                resolveIncludeExternalData(o)
                    ?.let { externalImpExFiles.add(it) }
            }
        })

        CachedValueProvider.Result.createSingleDependency(
            externalImpExFiles,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }, false)

    private fun resolveIncludeExternalData(impexScript: ImpexScript): ImpexFile? {
        val text = impexScript.text
        val streamIndex = text.indexOf("impex.includeExternalData")
            .takeIf { it != -1 }
            ?.let { text.indexOf("getResourceAsStream") }
            ?.takeIf { it != -1 }
            ?: return null
        val startIndex = text.indexOf('(', streamIndex)
            .takeIf { it != -1 }
            ?: return null
        val endIndex = text.indexOf(')', startIndex)
            .takeIf { it != -1 }
            ?: return null

        val resource = StringUtils.strip(text.substring(startIndex + 1, endIndex), "\"' ")
        val module = ModuleUtil.findModuleForPsiElement(impexScript)
            ?: return null

        return ModuleRootManager.getInstance(module).sourceRoots
            .filter { it.path.endsWith("/resources") }
            .map { File(it.path, resource) }
            .mapNotNull { LocalFileSystem.getInstance().findFileByIoFile(it) }
            .firstOrNull { it.exists() }
            ?.let { PsiManager.getInstance(impexScript.project).findFile(it) }
            ?.let { it as? ImpexFile }
    }

    companion object {
        val CACHE_KEY_HEADER_LINES = Key.create<CachedValue<Map<ImpexHeaderLine, Collection<ImpexValueLine>>>>("SAP_CX_IMPEX_HEADER_LINES")
        val CACHE_KEY_EXTERNAL_FILES = Key.create<CachedValue<Collection<ImpexFile>>>("SAP_CX_IMPEX_EXTERNAL_FILES")

        @Serial
        private val serialVersionUID: Long = 5112646813557523662L
    }
}