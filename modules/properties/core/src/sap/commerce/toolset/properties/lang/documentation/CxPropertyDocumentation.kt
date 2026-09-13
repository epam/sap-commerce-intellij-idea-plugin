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

package sap.commerce.toolset.properties.lang.documentation

import sap.commerce.toolset.properties.lang.documentation.renderer.propertyDoc
import sap.commerce.toolset.properties.meta.CxProperty
import sap.commerce.toolset.properties.meta.CxPropertyModel
import sap.commerce.toolset.properties.meta.CxPropertyRule

/**
 * What is worth knowing about a property without leaving the line it is written on: the value the platform ends up
 * applying, where that value comes from, everywhere else it is declared, and whether declaring it is a mistake.
 */
object CxPropertyDocumentation {

    fun of(key: String, chain: CxPropertyModel): String? {
        val property = chain[key] ?: return unknown(key)
        val effective = chain.resolve(key)

        return propertyDoc {
            property(key)

            effective?.let { value ->
                val raw = property.rawValue
                if (raw != null && raw != value) {
                    subHeader("<code>$value</code>", "written as <code>${raw.escaped()}</code>")
                } else {
                    subHeader("<code>${value.escaped()}</code>")
                }
            }

            property.declaration
                ?.let { contentsWithSeparator("Applied from <b>${it.source.presentableName}</b>") }
                ?: contentsWithSeparator("Declared only where the running system never reads it")

            property.shadowedDeclarations
                .takeIf { it.isNotEmpty() }
                ?.let { shadowed -> list(*shadowed.map { "${it.source.presentableName} &mdash; <code>${it.value.escaped()}</code>" }.toTypedArray()) }

            property.ignoredDeclarations
                .takeIf { it.isNotEmpty() }
                ?.let { ignored ->
                    texts("Not read by the running system:")
                    list(*ignored.map { "${it.source.presentableName} &mdash; <code>${it.value.escaped()}</code>" }.toTypedArray())
                }

            CxPropertyRule.of(key)?.let { rule ->
                texts("<b>${rule.severity.title.replaceFirstChar { it.uppercase() }}</b>: this property is ${rule.title}.")
                externalLink("SAP Help Portal", rule.documentationUrl)
            }
        }.build()
    }

    private fun unknown(key: String) = propertyDoc {
        property(key)

        CxPropertyRule.of(key)
            ?.let { rule ->
                texts("No file of this project declares it. It is ${rule.title}, so it is not the project's to declare.")
                externalLink("SAP Help Portal", rule.documentationUrl)
            }
            ?: texts("No file of this project declares it.")
    }.build()

    private fun String.escaped() = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
