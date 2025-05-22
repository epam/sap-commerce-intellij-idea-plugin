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

package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.util.xml.DomElement
import io.ktor.util.*

object TSMetaDetailsGenerator {

    fun generateTooltip(meta: TSGlobalMetaClassifier<*>) = "<html><body>${generate(meta)}</body></html>>"

    fun generate(meta: TSGlobalMetaClassifier<*>) = when (meta) {
        is TSGlobalMetaAtomic -> generate(meta)
        is TSGlobalMetaCollection -> generate(meta)
        is TSGlobalMetaMap -> generate(meta)
        is TSGlobalMetaEnum -> generate(meta)
        is TSGlobalMetaItem -> generate(meta)
        is TSGlobalMetaRelation -> generate(meta)
        else -> generate(meta.name, meta.isCustom, meta.declarations)
    }.trimIndent()

    private fun generate(meta: TSGlobalMetaAtomic) = """
        <h2>[ATOMIC] ${meta.name} ${customized(meta.isCustom)}</h2>
        ${generate(meta.declarations)}
        """.trimIndent()

    private fun generate(meta: TSGlobalMetaItem) = """
        <h2>[ITEM] ${meta.name ?: "?"} ${customized(meta.isCustom)}</h2>
        ${description(meta.description)}
        <hr>
        <table>
        <tr>
            <td>${badge("abstract", meta.isAbstract)}</td>
            <td>${badge("catalog aware", meta.isCatalogAware)}</td>
        </tr>
        <tr>
            <td>${badge("jalo only", meta.isJaloOnly)}</td>
            <td>${badge("singleton", meta.isSingleton)}</td>
        </tr>
        <tr>
            <td>${badge("autocreate", meta.isAutoCreate)}</td>
            <td>${badge("generate", meta.isGenerate)}</td>
        </tr>
        </table>
        ${generate(meta.deployment)}
        <hr>
        <h3>Extends</h3>
        <ul>
        ${meta.allExtends.mapNotNull { it.name }.joinToString("") { "<li>$it</li>" }}
        </ul>
        ${generate(meta.declarations)}
    """.trimIndent()

    private fun generate(meta: TSGlobalMetaEnum) = """
        <h2>[ENUM] ${meta.name ?: "?"} ${customized(meta.isCustom)}</h2>
        ${description(meta.description)}
        <hr>
        <div>${badge("dynamic", meta.isDynamic)}</div>
        ${generate(meta.declarations)}
        """.trimIndent()

    private fun generate(meta: TSGlobalMetaCollection) = """
        <h2>[COLLECTION] ${meta.name ?: "?"} ${customized(meta.isCustom)}</h2>
        <hr>
        <div><strong>Flatten Type:</strong> ${meta.flattenType?.escapeHTML() ?: "?"}</div>
        <div><strong>Type:</strong> ${meta.type}</div>
        <div><strong>Element Type:</strong> ${meta.elementType}</div>
        ${generate(meta.declarations)}
        """.trimIndent()

    private fun generate(meta: TSGlobalMetaMap) = """
        <h2>[MAP] ${meta.name ?: "?"} ${customized(meta.isCustom)}</h2>
        <hr>
        <div><strong>Flatten Type:</strong> ${meta.flattenType?.escapeHTML() ?: "?"}</div>
        <div><strong>Argument Type:</strong> ${meta.argumentType}</div>
        <div><strong>Return Type:</strong> ${meta.returnType}</div>
        ${generate(meta.declarations)}
        """.trimIndent()

    private fun generate(meta: TSGlobalMetaRelation) = """
        <h2>[RELATION] ${meta.name ?: "?"} ${customized(meta.isCustom)}</h2>
        ${description(meta.description)}
        <hr>
        <div><strong>Flatten Type:</strong> ${meta.flattenType?.escapeHTML() ?: "?"}</div>
        ${generate(meta.deployment)}
        <hr>
        <table>
            <tbody>
                <tr><td>${generate(meta.source)}</td><td>${generate(meta.target)}</td></tr>
            </tbody>
        </table>
        ${generate(meta.declarations)}
        """.trimIndent()

    private fun generate(meta: TSMetaRelation.TSMetaRelationElement) = """
        <h4>${meta.end} Relation</h4>
        ${description(meta.description)}
        <div>
        ${badge("navigable", meta.isNavigable)}
        ${badge("ordered", meta.isOrdered)}
        ${badge("deprecated", meta.isDeprecated)}
        </div>
        <br>
        <div><strong>Qualifier:</strong> ${meta.qualifier ?: "N/A"}</div>
        <div><strong>Flatten Type:</strong> ${meta.flattenType?.escapeHTML() ?: "?"}</div>
        <div><strong>Cardinality:</strong> ${meta.cardinality}</div>
        <div><strong>Type:</strong> ${meta.type}</div>
        <div><strong>Collection Type:</strong> ${meta.collectionType}</div>
        <div><strong>Meta Type:</strong> ${meta.metaType ?: "?"}</div>
    """.trimIndent()

    private fun generate(meta: TSMetaDeployment?) = meta
        ?.let {
            """
                <hr>
                <h3>Deployment Details</h3>
                <div><strong>Type Code:</strong> ${it.typeCode}</div>
                <div><strong>Table:</strong> ${it.table}</div>
                <div><strong>Property Table:</strong> ${it.propertyTable}</div>
                <br>
        """.trimIndent()
        }
        ?: ""

    private fun generate(name: String?, custom: Boolean, declarations: MutableSet<out TSMetaClassifier<out DomElement>>) = """
        <h2>${name} ${customized(custom)}</h2>
        <hr>
        ${generate(declarations)}
        """.trimIndent()

    private fun generate(declarations: MutableSet<out TSMetaClassifier<out DomElement>>): String = """
        <hr>
        <br>
        <div><strong>Declared in the following modules</strong></div>
        <ul>
        ${declarations.map { it.extensionName }.distinct().joinToString("") { "<li>$it</li>" }}
        </ul>
    """.trimIndent()

    private fun description(description: String?) = description?.let { "<i>$it</i>" } ?: ""

    private fun customized(isCustom: Boolean) = if (isCustom) "<sup>(<i>custom</i>)</sup>" else ""

    private fun badge(label: String? = null, value: Boolean): String {
        val htmlLabel = label?.let { "<span style='background: rgb(85, 85, 85);color:white'>&nbsp;${it}&nbsp;&nbsp;</span>" }
            ?: ""
        val htmlValue = "<span style='background:${if (value) "rgb(153, 201, 58)" else "rgb(224, 96, 74)"};color:white'>&nbsp;${if (value) "YES" else "NO"}&nbsp;</span>"
        return "<span style='margin:2px 2px 2px 0;padding:2px;display:inline;font-family:monospace,sans-serif'>$htmlLabel$htmlValue</span>"
    }

}