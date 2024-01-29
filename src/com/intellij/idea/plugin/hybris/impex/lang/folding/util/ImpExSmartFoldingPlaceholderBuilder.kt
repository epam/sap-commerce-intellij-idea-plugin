/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.impex.lang.folding.util

import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.ImpexModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier
import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexFoldingPlaceholderBuilder
import com.intellij.idea.plugin.hybris.impex.psi.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.firstLeaf
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

@Service
class ImpExSmartFoldingPlaceholderBuilder : ImpexFoldingPlaceholderBuilder {

    override fun getPlaceholder(psiElement: PsiElement): String = when (psiElement) {
        is ImpexAttribute -> getPlaceholder(psiElement)
        is ImpexParameters -> getParametersPlaceholder(psiElement.parameterList)
        is ImpexUserRightsPermissionValue -> getPlaceholder(psiElement)
        else -> psiElement.text
    }

    private fun getParametersPlaceholder(parameters: Collection<ImpexParameter>): String {
        if (parameters.isEmpty()) {
            return IMPEX_PARAMETERS_PLACEHOLDER
        }

        return parameters.joinToString(", ", "(", ")") {
            val subParameters = it.subParameters
                ?.let { subParameters -> getParametersPlaceholder(subParameters.parameterList) }
                ?: ""

            it.text
                .substringBefore("(")
                .substringBefore("[")
                .trim() + subParameters
        }
    }

    private fun getPlaceholder(element: ImpexUserRightsPermissionValue) = when (element.firstLeaf().elementType) {
        ImpexTypes.PERMISSION_DENIED -> "❌"
        ImpexTypes.PERMISSION_ALLOWED -> "✅"
        else -> StringUtils.EMPTY
    }

    private fun getPlaceholder(impexAttribute: ImpexAttribute) = with(impexAttribute.anyAttributeName.text) {
        when {
            quoteAwareStringEquals(this, TypeModifier.DISABLE_INTERCEPTOR_TYPES) -> {
                val foldedText = impexAttribute.anyAttributeValue
                    ?.text
                    ?: impexAttribute.text
                "!$foldedText"
            }

            quoteAwareStringEquals(
                this,
                AttributeModifier.LANG,
                AttributeModifier.DATE_FORMAT,
                AttributeModifier.MODE,
                AttributeModifier.NUMBER_FORMAT,
                TypeModifier.DISABLE_INTERCEPTOR_BEANS,
                TypeModifier.DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES
            ) -> {
                impexAttribute.anyAttributeValue
                    ?.text
                    ?: impexAttribute.text
            }

            quoteAwareStringEquals(
                this,
                AttributeModifier.TRANSLATOR,
                AttributeModifier.CELL_DECORATOR,
                TypeModifier.PROCESSOR
            ) -> {
                val value = impexAttribute.anyAttributeValue?.text ?: return impexAttribute.text
                val clearedString = QUOTES_PATTERN.matcher(value).replaceAll(StringUtils.EMPTY)

                clearedString.substringAfterLast(".")
                    .ifBlank { value }
            }

            else -> {
                isBooleanAttributeModifier(
                    impexAttribute,
                    AttributeModifier.UNIQUE,
                    AttributeModifier.DEFAULT,
                    AttributeModifier.VIRTUAL,
                    AttributeModifier.ALLOW_NULL,
                    AttributeModifier.FORCE_WRITE,
                    AttributeModifier.IGNORE_NULL,
                    AttributeModifier.IGNORE_KEY_CASE,
                    TypeModifier.BATCH_MODE,
                    TypeModifier.SLD_ENABLED,
                    TypeModifier.IMPEX_LEGACY_MODE,
                    TypeModifier.BATCH_MODE,
                    TypeModifier.CACHE_UNIQUE
                )
                    ?: StringUtils.EMPTY
            }
        }
    }

    private fun isBooleanAttributeModifier(
        impexAttribute: ImpexAttribute,
        vararg modifiers: ImpexModifier
    ): String? {
        val value = impexAttribute.anyAttributeValue
            ?.text
            ?.takeIf { it.contains("false", true) || it.contains("true", true) }
            ?: return null
        val text = impexAttribute.anyAttributeName.text

        return modifiers
            .find { quoteAwareStringEquals(text, it) }
            ?.modifierName
            ?.let { if (value.contains("false")) "!$it" else it }
    }

    private fun quoteAwareStringEquals(quotedString: String?, value: String?): Boolean {
        return (null == quotedString == (null == value)
            && (null == quotedString || quotedString == value || "'$quotedString'" == value || "\"$quotedString\"" == value))
    }

    private fun quoteAwareStringEquals(value: String?, vararg modifiers: ImpexModifier) = modifiers
        .any { quoteAwareStringEquals(value, it.modifierName) }

    companion object {
        const val IMPEX_PARAMETERS_PLACEHOLDER = "()"
        private val QUOTES_PATTERN = Pattern.compile("[\"\']")

        fun getInstance(): ImpExSmartFoldingPlaceholderBuilder = ApplicationManager.getApplication().getService(ImpExSmartFoldingPlaceholderBuilder::class.java)
    }
}
