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

package sap.commerce.toolset.impex.codeInspection.context

import com.intellij.openapi.observable.properties.PropertyGraph

data class ImpExDocIdGenerationContext(
    val mode: ImpExDocIdGenerationMode = ImpExDocIdGenerationMode.COLUMN_BASED,
    val name: String = "docId",
    val prefix: String = "",
    val postfix: String = "",
    val columns: List<ImpExColumnContext>
) {
    fun mutable(computePreview: Mutable.() -> String) = Mutable(
        mode = mode,
        name = name,
        prefix = prefix,
        postfix = postfix,
        columns = columns.map { it.mutable() },
        computePreview = computePreview
    )

    data class Mutable(
        private var mode: ImpExDocIdGenerationMode,
        private var name: String,
        private var prefix: String,
        private var postfix: String,
        val columns: List<ImpExColumnContext.Mutable>,
        val computePreview: Mutable.() -> String
    ) {
        private val graph = PropertyGraph()

        val modeProperty = graph.property(mode)
        val nameProperty = graph.property(name)
        val prefixProperty = graph.property(prefix)
        val postfixProperty = graph.property(postfix)
        val previewProperty = graph.property("")

        init {
            graph.dependsOn(previewProperty, modeProperty) { computePreview() }
            graph.dependsOn(previewProperty, nameProperty) { computePreview() }
            graph.dependsOn(previewProperty, prefixProperty) { computePreview() }
            graph.dependsOn(previewProperty, postfixProperty) { computePreview() }
            columns.forEach { col ->
                graph.dependsOn(previewProperty, col.nameProperty) { computePreview() }
                graph.dependsOn(previewProperty, col.numberProperty) { computePreview() }
                graph.dependsOn(previewProperty, col.uniqueProperty) { computePreview() }
                graph.dependsOn(previewProperty, col.includeProperty) { computePreview() }
            }
        }

        fun immutable() = ImpExDocIdGenerationContext(
            mode = modeProperty.get(),
            name = nameProperty.get(),
            prefix = prefixProperty.get(),
            postfix = postfixProperty.get(),
            columns = columns.map { it.immutable() }
        )
    }

}