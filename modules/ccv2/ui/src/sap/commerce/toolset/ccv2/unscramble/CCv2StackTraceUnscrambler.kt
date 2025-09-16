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

package sap.commerce.toolset.ccv2.unscramble

import com.intellij.openapi.project.Project
import com.intellij.unscramble.UnscrambleSupport
import sap.commerce.toolset.i18n
import javax.swing.JComponent

class CCv2StackTraceUnscrambler : UnscrambleSupport<JComponent> {

    override fun unscramble(project: Project, text: String, logName: String, jComponent: JComponent?) = CCv2UnscrambleService.getInstance()
        .buildStackTraceString(text)

    override fun getPresentableName() = i18n("hybris.project.ccv2.unscramble.jsonStacktrace.name")

}
