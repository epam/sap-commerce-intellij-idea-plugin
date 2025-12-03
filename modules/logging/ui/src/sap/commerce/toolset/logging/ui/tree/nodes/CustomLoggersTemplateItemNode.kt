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

package sap.commerce.toolset.logging.ui.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import sap.commerce.toolset.logging.CxLoggerModel
import sap.commerce.toolset.logging.template.CxLoggersTemplateModel
import java.util.*
import javax.swing.Icon

class CustomLoggersTemplateItemNode private constructor(
    val uuid: String = UUID.randomUUID().toString(),
    loggers: List<CxLoggerModel>,
    override var text: String = "",
    icon: Icon?,
    project: Project
) : LoggersOptionsNode(text, icon, project) {

    var loggers = loggers
        private set

    override fun getName() = text

    fun update(template: CxLoggersTemplateModel) {
        if (uuid != template.uuid) return

        loggers = template.loggers
        text = template.name
        icon = template.icon

        this.update(presentation)
    }

    fun update(source: LoggersNode) {
        text = source.name
        update(presentation)
    }

    override fun update(presentation: PresentationData) {
        super.update(presentation)

        val tip = " ${loggers.size} logger(s)"

        presentation.addText(ColoredFragment(tip, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES))
    }

    companion object {
        fun of(project: Project, template: CxLoggersTemplateModel) = CustomLoggersTemplateItemNode(
            uuid = template.uuid,
            loggers = template.loggers,
            text = template.name,
            icon = template.icon,
            project = project,
        )
    }
}
