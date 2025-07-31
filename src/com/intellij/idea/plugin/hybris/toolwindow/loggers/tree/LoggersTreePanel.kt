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

package com.intellij.idea.plugin.hybris.toolwindow.loggers.tree

import com.intellij.ide.IdeBundle
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionListener
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionService
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane

class LoggersTreePanel(
    private val project: Project,
) : OnePixelSplitter(false, 0.25f), Disposable {

    val tree: LoggersOptionsTree

    init {
        tree = LoggersOptionsTree(project)
        firstComponent = JBScrollPane(tree)
        secondComponent = JBPanelWithEmptyText().withEmptyText(IdeBundle.message("empty.text.nothing.selected"))

        //PopupHandler.installPopupMenu(tree, "action.group.id", "place")
        Disposer.register(this, tree)

        val hacSettings = RemoteConnectionService.getInstance(project).getActiveRemoteConnectionSettings(RemoteConnectionType.Hybris)
        tree.update(listOf(hacSettings))


        with(project.messageBus.connect(this)) {
            subscribe(RemoteConnectionListener.TOPIC, object : RemoteConnectionListener {
                override fun onActiveHybrisConnectionChanged(remoteConnection: RemoteConnectionSettings) = tree.update(listOf(remoteConnection))

                override fun onActiveSolrConnectionChanged(remoteConnection: RemoteConnectionSettings) = Unit
            })
        }
    }
}