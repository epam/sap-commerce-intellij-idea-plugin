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

package com.intellij.idea.plugin.hybris.system.ccv2.psi

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement

object ManifestPatterns {

    private inline fun <reified T : PsiElement> PsiElementPattern<*, *>.withParent() = this.withParent(T::class.java)

    private fun jsonPropertyName() = PlatformPatterns.psiElement(JsonElementTypes.IDENTIFIER)
    private fun jsonStringValue() = PlatformPatterns.psiElement(JsonElementTypes.DOUBLE_QUOTED_STRING)
            .withParent<JsonStringLiteral>()

    val EXTENSION_NAME = StandardPatterns.or(
            jsonStringValue()
                    .withSuperParent(2,
                            StandardPatterns.or(
                                    PlatformPatterns.psiElement(JsonProperty::class.java).withName("addon"),
                                    PlatformPatterns.psiElement(JsonProperty::class.java).withName("storefront"),
                            )
                    )
                    .inside(PlatformPatterns.psiElement(JsonProperty::class.java).withName("storefrontAddons")),

            jsonStringValue()
                    .withSuperParent(2, PlatformPatterns.psiElement(JsonArray::class.java))
                    .inside(PlatformPatterns.psiElement(JsonProperty::class.java).withName("extensions")),

            jsonStringValue()
                    .withSuperParent(2, PlatformPatterns.psiElement(JsonProperty::class.java).withName("name"))
                    .inside(PlatformPatterns.psiElement(JsonProperty::class.java).withName("webapps"))
    )


}