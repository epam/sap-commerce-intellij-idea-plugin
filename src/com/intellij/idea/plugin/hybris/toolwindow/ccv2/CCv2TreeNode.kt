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

package com.intellij.idea.plugin.hybris.toolwindow.ccv2

import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceReplicaDto
import com.intellij.openapi.util.ClearableLazyValue
import javax.swing.tree.DefaultMutableTreeNode

abstract class CCv2TreeNode : DefaultMutableTreeNode() {

    private val myProperSetting = ClearableLazyValue.create<Boolean> { this.calculateIsProperSettings() }

    abstract fun label(): String
    protected abstract fun calculateIsProperSettings(): Boolean

    fun isProperSetting() = myProperSetting.getValue()
    fun dropCache() = myProperSetting.drop()

    class RootNode : Group("root")
    class EnvironmentNode(val environment: CCv2EnvironmentDto): Group(environment.name)
    class ServiceNode(val service: CCv2ServiceDto): Group(service.name)

    class Replica(private val replica: CCv2ServiceReplicaDto) : CCv2TreeNode() {
        override fun label(): String = replica.name

        override fun calculateIsProperSettings(): Boolean {
            // TODO: implement me
            return true
        }
    }

    open class Group(private val label: String) : CCv2TreeNode() {

        override fun calculateIsProperSettings(): Boolean = (0..childCount)
            .map { getChildAt(it) }
            .filterIsInstance<CCv2TreeNode>()
            .any { it.isProperSetting() }
        override fun label(): String = label
    }
}