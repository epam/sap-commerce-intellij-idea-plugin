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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui.tree

import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2EnvironmentType
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceDto
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2ServiceReplicaDto
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.util.ui.ThreeStateCheckBox.State
import com.intellij.util.ui.tree.TreeUtil
import java.io.Serial
import javax.swing.tree.DefaultMutableTreeNode

abstract class CCv2TreeNode : DefaultMutableTreeNode() {

    private val myProperSetting = ClearableLazyValue.create<State> { this.calculateState() }

    abstract fun label(): String
    open fun hint(): String? = null
    protected abstract fun calculateState(): State

    fun selectionState() = myProperSetting.getValue()
    fun dropCache() = myProperSetting.drop()

    class RootNode : Group("root") {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -7468617334648819996L
        }
    }

    class EnvironmentTypeNode(environmentType: CCv2EnvironmentType) : Group(environmentType.title) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -693843320512859193L
        }
    }

    class EnvironmentNode(val environment: CCv2EnvironmentDto) : Group(environment.code) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = -693843320512859193L
        }
    }

    class ServiceNode(val service: CCv2ServiceDto) : Group(service.name) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = 7004468229126469011L
        }
    }

    abstract class Group(private val label: String) : CCv2TreeNode() {
        private var selected: Int = 0
        private var unSelected: Int = 0
        private val allReplicas: Int get() = selected + unSelected

        override fun calculateState(): State {
            val childrenStates = TreeUtil.listChildren(this)
                .filterIsInstance<CCv2TreeNode>()
                .groupBy { it.selectionState() }

            selected = (childrenStates[State.SELECTED]?.filterIsInstance<Replica>()?.size ?: 0) +
                childrenStates.values.flatten().filterIsInstance<Group>().sumOf { it.selected }
            unSelected = (childrenStates[State.NOT_SELECTED]?.filterIsInstance<Replica>()?.size ?: 0) +
                childrenStates.values.flatten().filterIsInstance<Group>().sumOf { it.unSelected }

            return when {
                childrenStates.size == 2
                    || (childrenStates.size == 1 && childrenStates.containsKey(State.DONT_CARE))-> State.DONT_CARE
                childrenStates.containsKey(State.SELECTED) -> State.SELECTED
                else -> State.NOT_SELECTED
            }
        }

        override fun hint() = when {
            selected < allReplicas -> "$selected of $allReplicas replica(s)"
            selected == allReplicas -> "$selected replica(s)"
            else -> null
        }

        override fun label(): String = label

        companion object {
            @Serial
            private const val serialVersionUID: Long = -3751728934087860385L
        }
    }

    class Replica(val replica: CCv2ServiceReplicaDto, private val selectedReplicas: Collection<String>) : CCv2TreeNode() {

        init {
            allowsChildren = false
        }

        override fun label(): String = replica.name
        override fun calculateState(): State = if (selectedReplicas.contains(replica.name)) State.SELECTED
        else State.NOT_SELECTED

        companion object {
            @Serial
            private const val serialVersionUID: Long = -2448235934797692419L
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6777454376714796894L
    }
}