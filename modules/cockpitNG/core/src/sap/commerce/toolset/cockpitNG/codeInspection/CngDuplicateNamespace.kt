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
package sap.commerce.toolset.cockpitNG.codeInspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.XmlSuppressableInspectionTool
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.cockpitNG.model.config.Config
import sap.commerce.toolset.i18n
import sap.commerce.toolset.isNotHybrisProject

class CngDuplicateNamespace : XmlSuppressableInspectionTool() {

    override fun isAvailableForFile(file: PsiFile): Boolean {
        val project = file.project
        if (project.isNotHybrisProject) return false
        if (file !is XmlFile) return false
        return DomManager.getDomManager(project).getFileElement(file, Config::class.java) != null
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : XmlElementVisitor() {
        override fun visitXmlAttribute(attribute: XmlAttribute) {
            val xmlFile = holder.file.asSafely<XmlFile>() ?: return
            val rootTag = xmlFile.rootTag ?: return
            attribute
                .takeIf { it.name.startsWith("xmlns:") }
                ?.parent?.parent
                ?.asSafely<XmlDocument>()
                ?: return
            val namespace = attribute.value ?: return

            val duplicates = rootTag.attributes
                .filter { it.isNamespaceDeclaration }
                .filter { it.value == namespace }

            if (duplicates.size > 1) {
                holder.registerProblem(
                    attribute,
                    i18n("hybris.inspections.fix.cng.CngDuplicateNamespace.message", attribute.localName, namespace),
                    ProblemHighlightType.WARNING,
                )
            }
        }
    }
}
