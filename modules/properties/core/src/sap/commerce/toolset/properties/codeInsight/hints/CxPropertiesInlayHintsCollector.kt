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

package sap.commerce.toolset.properties.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.util.asSafely
import sap.commerce.toolset.codeInsight.hints.SystemAwareInlayHintsCollector
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxResolvedProperty

/**
 * Annotates each declaration with what the chain makes of it:
 *
 * - a declaration another file overrides is named with the file which wins, so a value that looks authoritative but
 *   never applies stops being a trap;
 * - a declaration carrying `${...}` is followed by the value it expands to.
 *
 * Declarations which already say what they mean get no hint at all.
 */
class CxPropertiesInlayHintsCollector(editor: Editor) : SystemAwareInlayHintsCollector(editor) {

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        val project = editor.project ?: return false
        if (DumbService.isDumb(project)) return false

        val property = element.asSafely<Property>() ?: return true
        val key = property.key ?: return true
        val declaringFile = property.containingFile?.virtualFile ?: return true

        val chain = CxPropertyCollector.getInstance(project).collect()
        val resolved = chain.resolveProperties(listOf(key))[key] ?: return true

        hintFor(resolved, declaringFile.path, property.value)
            ?.let { sink.addInlineElement(element.endOffset, true, textPresentation(it), true) }

        return true
    }

    private fun hintFor(resolved: CxResolvedProperty, declaringPath: String, declaredValue: String?): String? {
        val winningPath = resolved.source.path

        return when {
            winningPath != null && winningPath != declaringPath -> "overridden by ${resolved.source.presentableName}"
            resolved.value != declaredValue -> "= ${resolved.value}"
            else -> null
        }
    }

    private fun textPresentation(text: String) = with(factory) {
        roundWithBackground(smallText(text))
    }
}
