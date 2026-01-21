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

package sap.commerce.toolset.java.decompilation

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.LegalNoticeDialog
import com.intellij.ide.plugins.PluginManagerCore
import org.jetbrains.java.decompiler.IdeaDecompilerBundle
import org.jetbrains.java.decompiler.IdeaDecompiler

/**
 * Service to wrap IdeaDecompiler.
 */
@Service(Service.Level.APP)
class DecompilerService {

    private val consentKey = "decompiler.legal.notice.accepted"

    private val decompiler by lazy { IdeaDecompiler() }

    /**
     * Decompile the given class file.
     *
     * @param file The virtual file of the class.
     * @return The decompiled source code.
     */
    fun decompile(file: VirtualFile): String {
        return decompiler.getText(file).toString()
    }

    fun isConsentGranted(): Boolean {
        val properties = PropertiesComponent.getInstance()
        return properties.isValueSet(consentKey)
    }

    fun ensureConsentAccepted(): Boolean {
        val properties = PropertiesComponent.getInstance()
        if (properties.isValueSet(consentKey)) return true

        val title = IdeaDecompilerBundle.message("legal.notice.title", "sap-commerce-ootb")
        val message = IdeaDecompilerBundle.message("legal.notice.text")
        val result = LegalNoticeDialog.build(title, message)
            .withCancelText(IdeaDecompilerBundle.message("legal.notice.action.postpone"))
            .withCustomAction(IdeaDecompilerBundle.message("legal.notice.action.reject"), 2)
            .show()

        return when (result) {
            0 -> {
                properties.setValue(consentKey, true)
                true
            }
            2 -> {
                disableDecompilerPlugin()
                false
            }
            else -> false
        }
    }

    private fun disableDecompilerPlugin() {
        val pluginId = PluginId.getId("org.jetbrains.java.decompiler")
        PluginManagerCore.disablePlugin(pluginId)
    }

    companion object {
        fun getInstance(): DecompilerService = service()
    }
}
