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

package sap.commerce.toolset.impex.mcp

import com.intellij.codeInsight.daemon.impl.SeverityRegistrar
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.InspectionProfile
import com.intellij.codeInspection.ProblemDescriptorUtil
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.LocalTimeCounter
import com.intellij.util.application
import sap.commerce.toolset.impex.file.ImpExFileType
import sap.commerce.toolset.impex.mcp.dto.ImpExSyntaxIssueDto
import sap.commerce.toolset.impex.mcp.dto.ImpExValidationResult
import sap.commerce.toolset.path
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class ImpExValidationMcpService(private val project: Project) {

    suspend fun validate(context: ImpExValidationContext): ImpExValidationResult {
        if (DumbService.isDumb(project)) error("Project indexing is in progress; retry once indexing completes.")

        val (psiFile, document, file) = resolveTarget(context)

        val issues = coroutineToIndicator {
            application.runReadAction<List<Issue>> { collectIssues(psiFile, document) }
        }
            .filter { it.severity >= HighlightSeverity.WEAK_WARNING }
            .sortedWith(compareBy({ it.line }, { it.startOffset }))
            .map {
                ImpExSyntaxIssueDto(
                    severity = it.severity.name,
                    message = it.message,
                    line = it.line,
                    column = it.column,
                )
            }

        return ImpExValidationResult(
            file = file,
            valid = issues.none { it.severity == HighlightSeverity.ERROR.name },
            issues = issues,
        )
    }

    private fun collectIssues(psiFile: PsiFile, document: Document): List<Issue> {
        val syntaxIssues = PsiTreeUtil.collectElementsOfType(psiFile, PsiErrorElement::class.java)
            .map { toIssue(document, it.textRange.startOffset, HighlightSeverity.ERROR, it.errorDescription) }

        val profile: InspectionProfile = InspectionProjectProfileManager.getInstance(project).currentProfile
        val manager = InspectionManager.getInstance(project)
        val severityRegistrar = SeverityRegistrar.getSeverityRegistrar(project)

        val inspectionIssues = profile.getInspectionTools(psiFile)
            .filterIsInstance<LocalInspectionToolWrapper>()
            .mapNotNull { wrapper -> wrapper.displayKey?.let { wrapper to it } }
            .filter { (_, displayKey) -> profile.isToolEnabled(displayKey, psiFile) }
            .flatMap { (wrapper, displayKey) ->
                wrapper.tool.processFile(psiFile, manager).mapNotNull { descriptor ->
                    val element = descriptor.psiElement ?: return@mapNotNull null
                    val baseSeverity = profile.getErrorLevel(displayKey, psiFile).severity
                    val severity = ProblemDescriptorUtil.highlightTypeFromDescriptor(descriptor, baseSeverity, severityRegistrar)
                        .getSeverity(element)
                    toIssue(document, element.textRange.startOffset, severity, descriptor.descriptionTemplate)
                }
            }

        return syntaxIssues + inspectionIssues
    }

    private fun toIssue(document: Document, startOffset: Int, severity: HighlightSeverity, message: String): Issue {
        val line = document.getLineNumber(startOffset)
        val column = startOffset - document.getLineStartOffset(line)
        return Issue(
            startOffset = startOffset,
            severity = severity,
            message = message,
            line = line + 1,
            column = column + 1,
        )
    }

    private data class Issue(val startOffset: Int, val severity: HighlightSeverity, val message: String, val line: Int, val column: Int)

    private suspend fun resolveTarget(context: ImpExValidationContext): Triple<PsiFile, Document, String?> {
        context.filePath
            ?.let { resolveVirtualFile(it) }
            ?.let { virtualFile ->
                return readAction {
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                        ?: error("File is not recognized as part of the project: ${context.filePath}")
                    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                        ?: error("Could not obtain a document for file: ${context.filePath}")
                    Triple(psiFile, document, virtualFile.path)
                }
            }

        val content = context.content
            ?: error("Either 'content' or an existing 'filePath' must be provided.")

        return readAction {
            val psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText("validation.impex", ImpExFileType, content, LocalTimeCounter.currentTime(), true)
            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
                ?: error("Could not create an in-memory document for the given content.")
            Triple(psiFile, document, null)
        }
    }

    private fun resolveVirtualFile(filePath: String): VirtualFile? {
        val path = Path.of(filePath)
        val absolute = if (path.isAbsolute) path else project.path?.resolve(path) ?: return null
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(absolute)
    }

    companion object {
        fun getInstance(project: Project): ImpExValidationMcpService = project.service()
    }
}
