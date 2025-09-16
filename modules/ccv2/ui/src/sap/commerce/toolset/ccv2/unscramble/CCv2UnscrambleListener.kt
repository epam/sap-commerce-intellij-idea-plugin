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

import com.intellij.openapi.application.ClipboardAnalyzeListener
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.wm.IdeFrame
import com.intellij.unscramble.UnscrambleDialog
import java.awt.event.ActionEvent

class CCv2UnscrambleListener : ClipboardAnalyzeListener() {

    override fun applicationActivated(ideFrame: IdeFrame) {
        if (!Registry.`is`("analyze.exceptions.on.the.fly")) return
        super.applicationActivated(ideFrame)
    }

    override fun applicationDeactivated(ideFrame: IdeFrame) {
        if (!Registry.`is`("analyze.exceptions.on.the.fly")) return
        super.applicationDeactivated(ideFrame)
    }

    override fun canHandle(value: String) = CCv2UnscrambleService.getInstance()
        .canHandle(value)

    override fun handle(project: Project, value: String) {
        val dialog = CCv2UnscrambleDialog(project)
        dialog.createNormalizeTextAction().actionPerformed(null as ActionEvent?)
        if (!DumbService.isDumb(project)) {
            dialog.doOKAction()
        }
    }

    class CCv2UnscrambleDialog(project: Project) : UnscrambleDialog(project) {
        public override fun doOKAction() {
            super.doOKAction()
        }
    }
}