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

package sap.commerce.toolset.beanSystem

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.model.Beans

fun isGeneratedFile(psiClass: PsiClass): Boolean {
    val virtualFile = psiClass.containingFile.virtualFile

    if (virtualFile?.extension == null) return false

    return (virtualFile.extension == "class" && virtualFile.path.contains(HybrisConstants.JAR_MODELS))
        || (virtualFile.extension == "java" && virtualFile.path.contains("${HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY}/${HybrisConstants.GEN_SRC_DIRECTORY}"))
}

fun isBeanFile(psiClass: PsiClass): Boolean {
    return !psiClass.isEnum && isGeneratedFile(psiClass)
}

fun isEnumFile(psiClass: PsiClass): Boolean {
    if (!psiClass.isEnum) return false

    return isGeneratedFile(psiClass)
}

val PsiFile.isBeansXmlFile
    get() = this is XmlFile && this.isBeansXmlFile

val XmlFile.isBeansXmlFile
    get() = this.name.endsWith(HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING)
        && DomManager.getDomManager(this.project).getFileElement(this, Beans::class.java) != null
