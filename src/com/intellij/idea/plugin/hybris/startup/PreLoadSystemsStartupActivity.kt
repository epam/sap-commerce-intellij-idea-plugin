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
package com.intellij.idea.plugin.hybris.startup

import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.TSModificationTracker
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngMetaModelAccess
import com.intellij.idea.plugin.hybris.system.spring.SimpleSpringService
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.Items
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.xml.DomFileElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.DomUtil

class PreLoadSystemsStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!ProjectSettingsComponent.getInstance(project).isHybrisProject()) return

        project.service<GlobalMetaTypeSystemService>().init()

        refreshSystem(project) { TSMetaModelAccess.getInstance(project).initMetaModel() }
        refreshSystem(project) { BSMetaModelAccess.getInstance(project).initMetaModel() }
        refreshSystem(project) { CngMetaModelAccess.getInstance(project).initMetaModel() }

        SimpleSpringService.getService(project)
            ?.let { service -> refreshSystem(project) { service.initCache() } }
        PropertyService.getInstance(project)
            ?.let { service -> refreshSystem(project) { service.initCache() } }
    }

    private fun refreshSystem(project: Project, refresher: (Project) -> Unit) {
        DumbService.getInstance(project).runWhenSmart {
            try {
                refresher.invoke(project)
            } catch (e: ProcessCanceledException) {
                // ignore
            }
        }
    }

    @Service(Service.Level.PROJECT)
    private class GlobalMetaTypeSystemService(private val project: Project) : Disposable {

        fun init() {
            DomManager.getDomManager(project).addDomEventListener(DomEventListener@{ event ->
                val psiFile = (DomUtil.getParentOfType(event.element, DomFileElement::class.java, false)
                    ?.takeIf { it.isValid }
                    ?.let { it.rootElement as? Items }
                    ?.xmlElement
                    ?.containingFile
                    ?: return@DomEventListener)

                with (TSMetaModelAccess.getInstance(project)) {
                    TSModificationTracker.resetCache(psiFile)
                    getMetaModel()
                }
            }, this)
        }

        override fun dispose() {
            TSModificationTracker.clear()
        }
    }
}