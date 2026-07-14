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
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.util.application
import sap.commerce.toolset.impex.mcp.dto.ImpExSyntaxIssueDto
import sap.commerce.toolset.impex.mcp.dto.ImpExValidationResult
import sap.commerce.toolset.impex.psi.ImpExElementFactory
import sap.commerce.toolset.path
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class ImpExValidationMcpService(private val project: Project) {

    suspend fun validate(context: ImpExValidationContext): ImpExValidationResult {
        if (DumbService.isDumb(project)) error("Project indexing is in progress; retry once indexing completes.")

        val (psiFile, document, file) = resolveTarget(context)

        val issues = collectIssues(psiFile, document)
            .filter { it.severity >= HighlightSeverity.WEAK_WARNING }
            .sortedBy { it.startOffset }
            .map {
                ImpExSyntaxIssueDto(
                    severity = it.severity.name,
                    message = it.message,
                    line = it.line,
                    column = it.column,
                    startOffset = it.startOffset,
                    endOffset = it.endOffset,
                )
            }

        return ImpExValidationResult(
            file = file,
            issues = issues,
        )
    }

    private suspend fun collectIssues(psiFile: PsiFile, document: Document): List<Issue> {
        val syntaxIssues = collectSyntaxIssues(psiFile, document)
        val inspectionIssues = collectInspectionIssues(psiFile, document)

        return syntaxIssues + inspectionIssues
    }

    private suspend fun collectSyntaxIssues(psiFile: PsiFile, document: Document): List<Issue> = readAction {
        PsiTreeUtil
            .collectElementsOfType(psiFile, PsiErrorElement::class.java)
            .map { it.toIssue(document, HighlightSeverity.ERROR, it.errorDescription) }
    }

    private suspend fun collectInspectionIssues(psiFile: PsiFile, document: Document): List<Issue> = coroutineToIndicator {
        application.runReadAction<List<Issue>> {
            val profile: InspectionProfile = InspectionProjectProfileManager.getInstance(project).currentProfile
            val manager = InspectionManager.getInstance(project)
            val severityRegistrar = SeverityRegistrar.getSeverityRegistrar(project)

            profile.getInspectionTools(psiFile)
                .filterIsInstance<LocalInspectionToolWrapper>()
                .mapNotNull { wrapper -> wrapper.displayKey?.let { wrapper to it } }
                .filter { (_, displayKey) -> profile.isToolEnabled(displayKey, psiFile) }
                .flatMap { (wrapper, displayKey) ->
                    wrapper.tool.processFile(psiFile, manager).mapNotNull { descriptor ->
                        val element = descriptor.psiElement ?: return@mapNotNull null
                        val baseSeverity = profile.getErrorLevel(displayKey, psiFile).severity
                        val severity = ProblemDescriptorUtil.highlightTypeFromDescriptor(descriptor, baseSeverity, severityRegistrar)
                            .getSeverity(element)
                        element.toIssue(document, severity, descriptor.descriptionTemplate)
                    }
                }
        }
    }

    private fun PsiElement.toIssue(document: Document, severity: HighlightSeverity, message: String): Issue {
        val line = document.getLineNumber(startOffset)
        val column = startOffset - document.getLineStartOffset(line)

        return Issue(
            startOffset = startOffset,
            endOffset = endOffset,
            severity = severity,
            message = message,
            line = line + 1,
            column = column + 1,
        )
    }

    private suspend fun resolveTarget(context: ImpExValidationContext): Triple<PsiFile, Document, String?> {
        context.filePath
            ?.let { resolveVirtualFile(it) }
            ?.let { virtualFile ->
                val psiFile = readAction { PsiManager.getInstance(project).findFile(virtualFile) }
                    ?: error("File is not recognized as part of the project: ${context.filePath}")
                val document = readAction { PsiDocumentManager.getInstance(project).getDocument(psiFile) }
                    ?: error("Could not obtain a document for file: ${context.filePath}")
                return Triple(psiFile, document, virtualFile.path)
            }

        val content = context.content
            ?: error("Either 'content' or an existing 'filePath' must be provided.")

        val psiFile = readAction { ImpExElementFactory.createFile(project, content) }
        val document = readAction { PsiDocumentManager.getInstance(project).getDocument(psiFile) }
            ?: error("Could not create an in-memory document for the given content.")

        return Triple(psiFile, document, null)
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
