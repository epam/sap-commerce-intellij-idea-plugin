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

package com.intellij.idea.plugin.hybris.ccv2.ui

import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceReplicaDto
import com.intellij.openapi.util.ClearableLazyValue
import java.io.Serial
import javax.swing.tree.DefaultMutableTreeNode

abstract class CCv2TreeNode : DefaultMutableTreeNode() {

    private val myProperSetting = ClearableLazyValue.create<Boolean> { this.calculateIsProperSettings() }

    abstract fun label(): String
    open fun hint(): String? = null
    protected abstract fun calculateIsProperSettings(): Boolean

    fun isProperSetting() = myProperSetting.getValue()
    fun dropCache() = myProperSetting.drop()

    class RootNode : Group("root") {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -7468617334648819996L
        }
    }

    class EnvironmentNode(val environment: CCv2EnvironmentDto) : Group(environment.code) {
        override fun hint() = "${environment.services?.flatMap { it.replicas }?.size ?: 0} replica(s)"

        companion object {
            @Serial
            private const val serialVersionUID: Long = -693843320512859193L
        }
    }

    class ServiceNode(val service: CCv2ServiceDto) : Group(service.name) {
        override fun hint() = "${service.replicas.size} replica(s)"

        companion object {
            @Serial
            private const val serialVersionUID: Long = 7004468229126469011L
        }
    }

    class Replica(private val replica: CCv2ServiceReplicaDto) : CCv2TreeNode() {
        override fun label(): String = replica.name

        override fun calculateIsProperSettings(): Boolean {
            // TODO: implement me
            return true
        }

        companion object {
            @Serial
            private const val serialVersionUID: Long = -2448235934797692419L
        }
    }

    open class Group(private val label: String) : CCv2TreeNode() {

        override fun calculateIsProperSettings(): Boolean = (0..childCount - 1)
            .map { getChildAt(it) }
            .filterIsInstance<CCv2TreeNode>()
            .any { it.isProperSetting() }

        override fun label(): String = label

        companion object {
            @Serial
            private const val serialVersionUID: Long = -3751728934087860385L
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6777454376714796894L
    }
}