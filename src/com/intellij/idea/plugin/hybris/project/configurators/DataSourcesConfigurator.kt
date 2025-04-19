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

package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.autoconfig.DataSourceConfigUtil
import com.intellij.database.autoconfig.DataSourceDetector
import com.intellij.database.autoconfig.DataSourceRegistry
import com.intellij.database.dataSource.DatabaseAuthProviderNames
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.model.DasDataSource
import com.intellij.database.util.DataSourceUtil
import com.intellij.database.util.DbImplUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.ui.classpath.SingleRootClasspathElement

class DataSourcesConfigurator {

    fun configureAfterImport(project: Project): List<() -> Unit> {
        val propertyService = PropertyService.getInstance(project) ?: return emptyList()

        val projectProperties = propertyService.findAllProperties()
        val dataSources = mutableListOf<LocalDataSource>()
        val dataSourceRegistry = DataSourceRegistry(project)

        dataSourceRegistry.setImportedFlag(false)
        dataSourceRegistry.builder
            .withName("[y] local")
            .withGroupName("[y] SAP Commerce")
            .withUrl(projectProperties["db.url"]?.replace("\\", ""))
            .withUser(projectProperties["db.username"])
            .withPassword(projectProperties["db.password"])
            .withAuthProviderId(DatabaseAuthProviderNames.CREDENTIALS_ID)
            .withCallback(object : DataSourceDetector.Callback() {
                override fun onCreated(dataSource: DasDataSource) {
                    if (dataSource is LocalDataSource) {
                        dataSource.passwordStorage = LocalDataSource.Storage.PERSIST
                        dataSources += dataSource
                    }
                }
            })
            .commit()

        return listOf {
            DataSourceConfigUtil.configureDetectedDataSources(project, dataSourceRegistry, false, true, DatabaseCredentials.getInstance())

            for (dataSource in dataSources) {
                LocalDataSourceManager.getInstance(project).addDataSource(dataSource)
                /**
                 * @deprecated Use performAutoIntrospection
                 * Yann Cébron
                 * :jetbrains:  Aug 1st at 10:53 AM
                 * the specified method is the replacement
                 * The only difference is that method is suspend
                 * you need to call it from coroutine context
                 * the current version returning AsyncTask (completable future) is deprecated
                 */
                // see replacement: https://intellij-support.jetbrains.com/hc/en-us/community/posts/21964027826706-Replacement-from-DataSourceUiUtil-performAutoSyncTask
                DataSourceUtil.performAutoSyncTask(project, dataSource)
                loadDatabaseDriver(project, dataSource)
            }
        }
    }

    private fun loadDatabaseDriver(project: Project, dataSource: LocalDataSource) {
        if (DbImplUtil.hasDriverFiles(dataSource)) return

        val driver = dataSource.databaseDriver ?: return

        if (driver.additionalClasspathElements.isNotEmpty()) return

        // let's try to pick up a suitable driver located in the Database Drivers library
        ModuleManager.getInstance(project).modules
            .firstOrNull { it.name.endsWith(HybrisConstants.EXTENSION_NAME_PLATFORM) }
            ?.let { LibraryUtil.findLibrary(it, HybrisConstants.PLATFORM_DATABASE_DRIVER_LIBRARY) }
            ?.let { library ->
                library.rootProvider.getFiles(OrderRootType.CLASSES)
                    .filter { it.name.startsWith(driver.sqlDialect, true) }
                    .map { VfsUtilCore.virtualToIoFile(it) }
                    .map {
                        if (SystemInfo.isWindows) "file:/$it"
                        else "file://$it"
                    }
                    .map { root -> SingleRootClasspathElement(root) }
                    .forEach { driver.additionalClasspathElements.add(it) }
            }

        dataSource.resolveDriver()
        dataSource.ensureDriverConfigured()
    }

}