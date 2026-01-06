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

package sap.commerce.toolset.project.context

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicProperty
import sap.commerce.toolset.ui.dsl.builder.MutableListProperty

data class ProjectImportCoreContext(
    val projectName: AtomicProperty<String> = AtomicProperty(""),

    val projectIcon: AtomicBooleanProperty = AtomicBooleanProperty(false),
    val projectIconFile: AtomicProperty<String> = AtomicProperty(""),

    val javadocUrl: AtomicProperty<String> = AtomicProperty(""),

    val platformVersion: AtomicProperty<String> = AtomicProperty(""),
    val platformDirectory: AtomicProperty<String> = AtomicProperty(""),

    val sourceCodeDirectoryOverride: AtomicBooleanProperty = AtomicBooleanProperty(false),
    val sourceCodeDirectory: AtomicProperty<String> = AtomicProperty(""),
    val sourceCodeFile: AtomicProperty<String> = AtomicProperty(""),

    val moduleFilesStorage: AtomicBooleanProperty = AtomicBooleanProperty(true),
    val moduleFilesStorageDirectory: AtomicProperty<String> = AtomicProperty(""),

    val customDirectoryOverride: AtomicBooleanProperty = AtomicBooleanProperty(false),
    val customDirectory: AtomicProperty<String> = AtomicProperty(""),

    val configDirectoryOverride: AtomicBooleanProperty = AtomicBooleanProperty(false),
    val configDirectory: AtomicProperty<String> = AtomicProperty(""),

    val dbDriverDirectoryOverride: AtomicBooleanProperty = AtomicBooleanProperty(false),
    val dbDriverDirectory: AtomicProperty<String> = AtomicProperty(""),

    val isExcludedFromScanning: AtomicBooleanProperty = AtomicBooleanProperty(false),
    val excludedFromScanningDirectories: MutableListProperty = MutableListProperty(),

    val ccv2Token: AtomicProperty<String> = AtomicProperty(""),

    val importSettings: ProjectImportSettings.Mutable,
)