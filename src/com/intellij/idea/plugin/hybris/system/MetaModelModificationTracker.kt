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

package com.intellij.idea.plugin.hybris.system

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModel
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import java.io.Serial

object TSModificationTracker : MetaModelModificationTracker<TSMetaModel>("SINGLE_TS_MODEL_CACHE")

abstract class MetaModelModificationTracker<T>(private val prefix: String) : UserDataHolderBase(), ModificationTracker {

    private val singleMetaCacheKeys = mutableMapOf<String, Key<CachedValue<T>>>()
    private var modificationTracker = 0L;

    override fun getModificationCount() = modificationTracker

    private fun modify() {
        if (modificationTracker == Long.MAX_VALUE) modificationTracker = 0L
        modificationTracker++
    }

    fun getCacheKey(psiFile: PsiFile) = singleMetaCacheKeys.computeIfAbsent(prefix + "_${psiFile.name}") { key ->
        Key.create(key)
    }

    fun clear() = singleMetaCacheKeys.clear()

    fun resetCache(psiFile: PsiFile) {
        modify()
        resetCache(psiFile.name)
    }

    fun resetCache(keys: Collection<String>) {
        modify()
        keys.forEach { resetCache(it) }
    }

    private fun resetCache(keyName: String) {
        val key = prefix + "_$keyName"
        singleMetaCacheKeys[key]
            ?.let { removeUserData(it) }
            ?.also { singleMetaCacheKeys.remove(key) }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 895024761554485259L
    }
}