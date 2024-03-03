/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.lang.injection

import com.intellij.idea.plugin.hybris.system.cockpitng.CngConfigDomFileDescription.Companion.NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Labels
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTagChild
import com.intellij.psi.xml.XmlText
import com.intellij.spring.el.SpringELLanguage
import com.intellij.spring.model.utils.SpringCommonUtils

class HybrisSpringELInjector : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val project = context.project
        if (project.isDefault) return
        if (!SpringCommonUtils.isSpringConfigured(project)) return
        if (context !is XmlTagChild) return

        val tag = context.parentTag ?: return

        if (context !is PsiLanguageInjectionHost) return

        when {
            context is XmlText
                && (tag.localName == Labels.LABEL || tag.localName == Labels.DESCRIPTION)
                && tag.namespace == NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS -> doInject(registrar, context)
        }
    }

    private fun doInject(registrar: MultiHostRegistrar, context: PsiLanguageInjectionHost) {
        registrar
            .startInjecting(SpringELLanguage.INSTANCE).addPlace(
                null, null,
                context, TextRange.from(0, context.textLength)
            )
            .doneInjecting()
    }

    override fun elementsToInjectIn() = mutableListOf(
        XmlText::class.java,
        XmlAttributeValue::class.java,
    )
}
