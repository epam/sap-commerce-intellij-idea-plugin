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

package sap.commerce.toolset.groovy.transform

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.handlers.CopyToClipboardTransformResultHandler
import sap.commerce.toolset.transform.handlers.CreateScratchFileTransformResultHandler

@Service(Service.Level.PROJECT)
internal class GroovyImpExScriptTransformationService(
    private val project: Project,
    private val coroutineScope: CoroutineScope,
) {

    fun transform(
        outputFileType: LanguageFileType,
        psiFile: GroovyFile,
        onComplete: (TransformationResult) -> Unit,
    ) {
        coroutineScope.launch {
            val result = transform(outputFileType, psiFile)
            onComplete(result)
        }
    }

    suspend fun transform(outputFileType: LanguageFileType, psiFile: GroovyFile): TransformationResult {
        val scriptName = psiFile.getUserData(GroovyConstants.Transform.SCRIPT_NAME) ?: "myScript"
        val scriptContent = readAction { psiFile.text }
        val escapedContent = scriptContent.replace("\"", "'")

        val rawImpex = buildString {
            appendLine("INSERT_UPDATE Script;code[unique=true];content")
            appendLine(";$scriptName;\"$escapedContent\"")
            appendLine()
            appendLine("# JOB")
            appendLine("INSERT_UPDATE ScriptingJob;code[unique=true];scriptURI;")
            append(";${scriptName}Job;model://$scriptName;")
        }

        val formattedImpex = edtWriteAction {
            ImpExElementFactory.createFile(project, rawImpex)
                .let { CodeStyleManager.getInstance(project).reformat(it) }
                .text
        }

        val description = "$scriptName to ${outputFileType.name}"

        return TransformationResult(
            content = formattedImpex,
            description = description,
            handlers = listOf(
                CopyToClipboardTransformResultHandler(formattedImpex),
                CreateScratchFileTransformResultHandler(project, formattedImpex, outputFileType),
            ),
        )
    }

    companion object {
        fun getInstance(project: Project): GroovyImpExScriptTransformationService = project.service()
    }
}
