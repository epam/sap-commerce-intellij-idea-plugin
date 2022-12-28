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
package com.intellij.idea.plugin.hybris.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import javax.swing.JPanel

class HybrisDynamicAttributeInlayProvider : InlayHintsProvider<NoSettings> {

    override fun getCollectorFor(file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink) =
        HybrisDynamicAttributeHintsCollector(editor)

    override val previewText: String = """
public void foo() {
  final CustomerModel customer = new CustomerModel();
  customer. `dynamic` getAllGroups();
  customer.getName();
}
  """.trimIndent()

    override fun createSettings() = NoSettings()

    override val name = HybrisI18NBundleUtils.message("hybris.editor.java.inlay_provider.dynamic_attribute.name")
    override val description = HybrisI18NBundleUtils.message("hybris.editor.java.inlay_provider.dynamic_attribute.description")
    override val group: InlayGroup get() = InlayGroup.METHOD_CHAINS_GROUP
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener) = JPanel()
        }
    }

    companion object {
        private val settingsKey = SettingsKey<NoSettings>("HybrisDynamicAttributeInlayProviderSettingsKey")
    }
}