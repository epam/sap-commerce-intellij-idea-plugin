/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.type.system.inspections.rules.legacy

/**
 * The selection query will most likely not result in a match for a project; it assumes that the type that is extended is part of the same XML file,
 * but this is most likely not the case as customers will extend standard data models (defined in hybris extensions)
 */
class DeploymentForSubclassesOfAbstractClasses : AbstractTypeSystemInspection() {
    override fun getSelectionQuery(): String = "//itemtype[@extends = //itemtype/@code and //itemtype[@extends='GenericItem' and @abstract='true']]"

    override fun getTestQuery(): String = "count(./deployment) = 1"

    override fun getNameQuery(): String = "./@code"

}