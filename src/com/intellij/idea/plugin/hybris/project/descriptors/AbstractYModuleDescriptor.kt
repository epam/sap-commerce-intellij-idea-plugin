/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.project.descriptors

import java.io.File

abstract class AbstractYModuleDescriptor(
    moduleRootDirectory: File,
    rootProjectDescriptor: HybrisProjectDescriptor,
    name: String
) : AbstractModuleDescriptor(moduleRootDirectory, rootProjectDescriptor, name), YModuleDescriptor {

    private val dependenciesTree = mutableSetOf<YModuleDescriptor>()
    private var springFileSet = mutableSetOf<String>()

    override fun getDependenciesTree() = dependenciesTree
    override fun getSpringFileSet() = springFileSet
    override fun addSpringFile(springFile: String) = springFileSet.add(springFile)

    override fun setDependenciesTree(moduleDescriptors: Set<YModuleDescriptor>) {
        with(dependenciesTree) {
            clear()
            addAll(moduleDescriptors)
        }
    }
}
