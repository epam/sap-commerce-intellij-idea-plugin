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
package sap.commerce.toolset.codeInsight.injection

import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.LanguageInjector
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.asSafely
import com.intellij.util.xml.DomManager
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.model.Bean
import sap.commerce.toolset.beanSystem.model.Beans

class JavaToBeansXmlLanguageInjector : LanguageInjector {

    override fun getLanguagesToInject(host: PsiLanguageInjectionHost, injectionPlacesRegistrar: InjectedLanguagePlaces) {
        val project = host.project
        val xmlText = host
            .asSafely<XmlText>()
            ?.takeIf { it.language == XMLLanguage.INSTANCE }
            ?.takeIf { it.parentOfType<XmlTag>()?.localName == "annotations" }
            ?.takeIf {
                val xmlFile = it.containingFile.asSafely<XmlFile>()
                    ?: return@takeIf false

                DomManager.getDomManager(project).getFileElement(xmlFile, Beans::class.java) != null
            }
            ?: return

        val meta = xmlText.parentsOfType(XmlTag::class.java)
            .firstOrNull { it.localName == Beans.BEAN }
            ?.getAttributeValue(Bean.CLASS)
            ?.let { BSMetaModelAccess.getInstance(project).findMetaBeanByName(it) }
            ?: return

        val imports = meta.imports
            .mapNotNull { it.type }
            .joinToString("\n") { "import $it;" }

        val classLevelAnnotations = xmlText.parentOfType<XmlTag>()?.parentOfType<XmlTag>()?.localName == Beans.BEAN

        val prefix = if (classLevelAnnotations) imports
        else """
            $imports
            public class EvaluateContext {
        """.trimIndent()

        val suffix = if (classLevelAnnotations) "public class EvaluateContext {}"
        else """
            public void evaluateMethod() {}
            }
        """.trimIndent()

        injectionPlacesRegistrar.addPlace(
            JavaLanguage.INSTANCE,
            TextRange(0, host.textLength),
            prefix,
            suffix,
        )
    }
}
