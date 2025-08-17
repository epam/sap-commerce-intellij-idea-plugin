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

package sap.commerce.toolset.ccv2.manifest.psi

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

    private fun jsonStringValue() = PlatformPatterns.psiElement(JsonElementTypes.DOUBLE_QUOTED_STRING)
        .withParent<JsonStringLiteral>()

    val EXTENSION_NAME = StandardPatterns.or(
        jsonStringValue()
            .withSuperParent(
                2,
                StandardPatterns.or(
                    PlatformPatterns.psiElement(JsonProperty::class.java).withName("addon"),
                    PlatformPatterns.psiElement(JsonProperty::class.java).withName("storefront"),
                    PlatformPatterns.psiElement(JsonArray::class.java).withParent(
                        StandardPatterns.or(
                            PlatformPatterns.psiElement(JsonProperty::class.java).withName("addons"),
                            PlatformPatterns.psiElement(JsonProperty::class.java).withName("storefronts"),
                        )
                    ),
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

    val TEMPLATE_EXTENSION_NAME = StandardPatterns.or(
        jsonStringValue()
            .withSuperParent(
                2,
                StandardPatterns.or(
                    PlatformPatterns.psiElement(JsonProperty::class.java).withName("template"),
                )
            )
            .inside(PlatformPatterns.psiElement(JsonProperty::class.java).withName("storefrontAddons")),
    )

    val EXTENSION_PACK_NAME = StandardPatterns.or(
        jsonStringValue()
            .withSuperParent(2, PlatformPatterns.psiElement(JsonProperty::class.java).withName("name"))
            .inside(PlatformPatterns.psiElement(JsonProperty::class.java).withName("extensionPacks"))
    )

}