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
package com.intellij.idea.plugin.hybris.system.type.util

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager

object TSUtils {
    private val ITEM_JALO_KEY = Key.create<Boolean>("Y_IS_ITEM_JALO_CLASS")
    private val ITEM_MODEL_KEY = Key.create<Boolean>("Y_IS_ITEM_MODEL_CLASS")
    private val ENUM_MODEL_KEY = Key.create<Boolean>("Y_IS_ENUM_MODEL_CLASS")

    fun isTypeSystemFile(file: PsiFile): Boolean = (file is XmlFile && file.getName().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING))
        && DomManager.getDomManager(file.project).getFileElement(file, Items::class.java) != null

    fun isCustomExtensionFile(file: PsiFile): Boolean = CachedValuesManager.getCachedValue(file) {
        if (!isTypeSystemFile(file)) return@getCachedValue CachedValueProvider.Result.create(false, file)

        val vFile = file.virtualFile
        CachedValueProvider.Result.create(vFile != null && PsiUtils.isCustomExtensionFile(vFile, file.project), file)
    }

    fun cleanItemModelSearchName(searchName: String?): String? {
        if (searchName == null) return null

        val idx = searchName.lastIndexOf(HybrisConstants.MODEL_SUFFIX)

        return if (idx == -1) searchName
        else searchName.substring(0, idx)
    }

    fun isItemJaloFile(psiClass: PsiClass): Boolean {
        val itemJaloFile = psiClass.getUserData(ITEM_JALO_KEY)
        if (itemJaloFile != null) return itemJaloFile

        val superClass = PsiClassImplUtil.getAllSuperClassesRecursively(psiClass)
            .firstOrNull { HybrisConstants.CLASS_FQN_JALO_ITEM_ROOT == it.qualifiedName }

        return storeAndReturn(psiClass, ITEM_JALO_KEY, superClass != null)
    }

    fun isItemModelFile(psiClass: PsiClass): Boolean {
        val itemModelFile = psiClass.getUserData(ITEM_MODEL_KEY)
        if (itemModelFile != null) return itemModelFile

        val virtualFile = psiClass.containingFile
            .let {
                it.virtualFile
                    ?: it.originalFile.virtualFile
            }
            ?: return storeAndReturn(psiClass, ITEM_MODEL_KEY, false)

        val extension = virtualFile.extension
            ?: return storeAndReturn(psiClass, ITEM_MODEL_KEY, false)

        if (!shouldProcessItemType(psiClass, virtualFile, extension))
            return storeAndReturn(psiClass, ITEM_MODEL_KEY, false)

        return storeAndReturn(psiClass, ITEM_MODEL_KEY, true)
    }

    fun isEnumFile(psiClass: PsiClass): Boolean {
        val enumModelFile = psiClass.getUserData(ENUM_MODEL_KEY)
        if (enumModelFile != null) return enumModelFile

        val psiFile = psiClass.containingFile
        val virtualFile = psiFile.virtualFile
            ?: return storeAndReturn(psiClass, ENUM_MODEL_KEY, false)

        val extension = virtualFile.extension
            ?: return storeAndReturn(psiClass, ENUM_MODEL_KEY, false)

        if (!shouldProcessEnum(psiClass, virtualFile, extension)) return storeAndReturn(psiClass, ENUM_MODEL_KEY, false)

        return storeAndReturn(psiClass, ENUM_MODEL_KEY, true)
    }

    private fun storeAndReturn(psiClass: PsiClass, key: Key<Boolean>, verificationResult: Boolean): Boolean {
        psiClass.putUserData(key, verificationResult)
        return verificationResult
    }

    private fun shouldProcessItemType(
        psiClass: PsiClass,
        virtualFile: VirtualFile,
        extension: String
    ): Boolean {
        val path = virtualFile.path
        if (extension == "java" && !path.contains(HybrisConstants.BOOTSTRAP_GEN_SRC_PATH)) return false
        if (extension == "class" && !path.contains(HybrisConstants.JAR_MODELS)) return false

        val className = psiClass.name ?: return false
        if (className.endsWith(HybrisConstants.MODEL_SUFFIX)) return true

        val superClass = psiClass.superClass
        return superClass != null && superClass.name != null && superClass.name!!.startsWith("Generated")
    }

    private fun shouldProcessEnum(
        psiClass: PsiClass,
        virtualFile: VirtualFile,
        extension: String
    ): Boolean {
        val path = virtualFile.path
        if (extension == "java" && !path.contains(HybrisConstants.BOOTSTRAP_GEN_SRC_PATH)) return false
        if (extension == "class" && !path.contains(HybrisConstants.JAR_MODELS)) return false

        for (implementsListType in psiClass.implementsListTypes) {
            if (HybrisConstants.CLASS_NAME_ENUM == implementsListType.className) {
                return true
            }
        }

        return false
    }
}
