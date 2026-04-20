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

package sap.commerce.toolset.welcomescreen

import com.intellij.ui.JBColor
import sap.commerce.toolset.welcomescreen.ui.tags.TagColors

object WelcomeScreenUiConstants {
    object Tags {
        // Version tag — neutral gray.
        // Light: near-white bg, light-gray border, mid-gray text.
        // Dark:  dark-gray bg, mid-gray border, light-gray text.
        val TAG_COLORS_VERSION = TagColors(
            background = JBColor(0xF8F8F8, 0x2E3035),
            border = JBColor(0xDDDDDD, 0x43464C),
            foreground = JBColor(0x6C6C6C, 0x9DA0A8)
        )

        // Version tag on hover — darker neutral gray so it reads as "active"
        // without competing with the semantic hosting tag colors.
        val TAG_COLORS_VERSION_HOVERED = TagColors(
            background = JBColor(0xE8E8E8, 0x4A4D52),
            border = JBColor(0xB0B0B0, 0x6B6E75),
            foreground = JBColor(0x3C3C3C, 0xD4D5D6)
        )

        // CCV2 hosting tag — muted blue.
        // Light: pale blue bg, medium blue border, deep navy text.
        // Dark:  dark navy bg, steel blue border, sky-blue text.
        val TAG_COLORS_CCV2 = TagColors(
            background = JBColor(0xDCEEFD, 0x1F3A55),
            border = JBColor(0x6CACE4, 0x3D7FAD),
            foreground = JBColor(0x1A5E8F, 0x6CACE4)
        )

        // On-Premise hosting tag — muted amber.
        // Light: pale amber bg, warm ochre border, dark brown text.
        // Dark:  dark brown bg, golden border, soft gold text.
        val TAG_COLORS_ON_PREMISE = TagColors(
            background = JBColor(0xFDF3DC, 0x3D2E10),
            border = JBColor(0xD4960A, 0x9A6700),
            foreground = JBColor(0x7A5000, 0xD4960A)
        )
    }
}