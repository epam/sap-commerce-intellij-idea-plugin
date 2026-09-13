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

package sap.commerce.toolset.properties.ui

import sap.commerce.toolset.properties.presentation.CxPropertyPresentation

/**
 * A section of the comparison between two instances.
 *
 * Three questions, each about a different set of properties, and each with its own reading: what one instance has and
 * the other does not is usually something set by hand and forgotten, while a value both have and disagree about is
 * usually the one that explains why an environment behaves differently.
 */
internal enum class CxInstanceComparisonSection(val title: String) {

    ONLY_IN_FIRST("Only on"),
    ONLY_IN_SECOND("Only on"),
    DIFFERING("Different values"),
    ;

    fun titleFor(first: String, second: String) = when (this) {
        ONLY_IN_FIRST -> "$title $first"
        ONLY_IN_SECOND -> "$title $second"
        DIFFERING -> title
    }

    /** The instance whose value a row of this section carries, of the two given. */
    fun <T> own(first: T, second: T) = if (this == ONLY_IN_SECOND) second else first

    /** The instance a row of this section is held against, which is the other one. */
    fun <T> other(first: T, second: T) = if (this == ONLY_IN_SECOND) first else second
}

/** The whole comparison, computed once so switching sections costs nothing. */
internal data class CxInstanceComparison(
    val first: String,
    val second: String,
    val firstTotal: Int,
    val secondTotal: Int,
    val onlyInFirst: List<CxPropertyPresentation>,
    val onlyInSecond: List<CxPropertyPresentation>,
    val differing: List<CxPropertyPresentation>,
    val secondValues: Map<String, String>,
) {

    fun rowsOf(section: CxInstanceComparisonSection) = when (section) {
        CxInstanceComparisonSection.ONLY_IN_FIRST -> onlyInFirst
        CxInstanceComparisonSection.ONLY_IN_SECOND -> onlyInSecond
        CxInstanceComparisonSection.DIFFERING -> differing
    }

    /**
     * The other side rows of [section] are held against — only for the differing section, where both values exist and
     * the disagreement is the point. An absence has nothing to compare with.
     */
    fun counterpartOf(section: CxInstanceComparisonSection) = when (section) {
        CxInstanceComparisonSection.DIFFERING -> CxPropertyCounterpart.instance(first, second, secondValues)
        else -> null
    }
}
