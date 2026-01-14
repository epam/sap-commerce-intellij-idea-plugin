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
package sap.commerce.toolset.database.configurator

import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.autoconfig.DataSourceConfigUtil
import com.intellij.database.autoconfig.DataSourceDetector
import com.intellij.database.autoconfig.DataSourceRegistry
import com.intellij.database.dataSource.DatabaseAuthProviderNames
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.model.DasDataSource
import com.intellij.database.util.LoaderContext
import com.intellij.database.util.performAutoIntrospection
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryRootTypeId
import com.intellij.util.ui.classpath.SingleRootClasspathElement
import sap.commerce.toolset.java.JavaConstants
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.project.configurator.ProjectPostImportAsyncConfigurator
import sap.commerce.toolset.project.context.ProjectPostImportContext

class DataSourceConfigurator : ProjectPostImportAsyncConfigurator {

    override val name: String
        get() = "Database - Data Sources"

    override suspend fun configure(context: ProjectPostImportContext) {
        val project = context.project
        val projectProperties = smartReadAction(project) { PropertyService.getInstance(project).findAllProperties() }
        val dataSources = mutableListOf<LocalDataSource>()
        val dataSourceRegistry = DataSourceRegistry(project)
        dataSourceRegistry.setImportedFlag(false)

        val dataSourceDetectorBuilder = dataSourceRegistry.builder
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

        edtWriteAction {
            dataSourceDetectorBuilder.commit()

            val credentials = DatabaseCredentials.getInstance()
            DataSourceConfigUtil.configureDetectedDataSources(project, dataSourceRegistry, false, true, credentials)

            for (dataSource in dataSources) {
                LocalDataSourceManager.getInstance(project).addDataSource(dataSource)

                loadDatabaseDriver(context, dataSource)
            }
        }

        for (dataSource in dataSources) {
            val context = LoaderContext.selectGeneralTask(project, dataSource)
            performAutoIntrospection(context, true)
        }
    }

    private fun loadDatabaseDriver(context: ProjectPostImportContext, dataSource: LocalDataSource) {
        val driver = dataSource.databaseDriver ?: return

        if (driver.additionalClasspathElements.isNotEmpty()) return

        // let's try to pick up a suitable driver located in the Database Drivers library

        context.storage
            .entities(LibraryEntity::class.java)
            .find { it.name == JavaConstants.ProjectLibrary.DATABASE_DRIVERS }
            ?.roots
            ?.asSequence()
            ?.filter { it.type == LibraryRootTypeId.COMPILED }
            ?.mapNotNull { VirtualFileManager.getInstance().findFileByUrl(it.url.url) }
            ?.mapNotNull { vf ->
                vf.children
                    ?.filter { it.extension == "jar" }
                    ?.filter { it.nameWithoutExtension.startsWith(driver.sqlDialect, true) }
            }
            ?.flatten()
            ?.map { SingleRootClasspathElement(it.url) }
            ?.toList()
            ?.forEach { driver.additionalClasspathElements.add(it) }

        dataSource.resolveDriver()
        dataSource.ensureDriverConfigured()
    }
}
