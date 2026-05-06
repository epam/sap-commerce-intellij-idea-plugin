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

package sap.commerce.toolset.logging.lookup

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import sap.commerce.toolset.logging.CxLogConstants
import javax.swing.Icon

internal class CxLoggerCompletionState(private val prefix: String) {
    val matched: MutableList<LookupElement> = mutableListOf()
    var overflow: Int = 0
        private set
    var overflowCapped: Boolean = false
        private set

    fun tryAdd(fqn: String, shortName: String?, icon: () -> Icon): Boolean {
        if (!matches(fqn, shortName)) return true
        if (matched.size < CxLogConstants.Lookup.MAX_VISIBLE_SUGGESTIONS) {
            matched += buildElement(fqn, shortName, icon())
            return true
        }
        overflow++
        if (overflow >= CxLogConstants.Lookup.OVERFLOW_PROBE_BUDGET) {
            overflowCapped = true
            return false
        }
        return true
    }

    private fun matches(fqn: String, shortName: String?): Boolean = fqn.startsWith(prefix, ignoreCase = true)
        || (shortName != null && shortName.startsWith(prefix, ignoreCase = true))

    private fun buildElement(fqn: String, shortName: String?, icon: Icon): LookupElement {
        val builder = LookupElementBuilder.create(fqn).withIcon(icon)
        return if (shortName != null) builder.withLookupString(shortName) else builder
    }
}