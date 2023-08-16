/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.groovy.actions

import com.intellij.idea.plugin.hybris.actions.AbstractExecuteAction
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import org.jetbrains.plugins.groovy.GroovyFileType

class GroovyExecuteAction : AbstractExecuteAction() {

    init {
        with (templatePresentation) {
            text = "Execute Groovy Script"
            description = "Execute Groovy Script on a remote SAP Commerce instance"
            icon = HybrisIcons.CONSOLE_EXECUTE
        }
    }

    override val extension = GroovyFileType.GROOVY_FILE_TYPE.defaultExtension
    override val consoleName = HybrisConstants.CONSOLE_TITLE_GROOVY
}
