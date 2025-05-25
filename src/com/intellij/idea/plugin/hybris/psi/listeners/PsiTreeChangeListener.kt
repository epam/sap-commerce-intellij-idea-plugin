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
package com.intellij.idea.plugin.hybris.psi.listeners

import com.intellij.idea.plugin.hybris.settings.components.ProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.bean.BSDomFileDescription
import com.intellij.idea.plugin.hybris.system.bean.meta.BSModificationTracker
import com.intellij.idea.plugin.hybris.system.cockpitng.*
import com.intellij.idea.plugin.hybris.system.cockpitng.meta.CngModificationTracker
import com.intellij.idea.plugin.hybris.system.type.file.TSDomFileDescription
import com.intellij.idea.plugin.hybris.system.type.meta.TSModificationTracker
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionNotApplicableException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import com.intellij.util.xml.DomManager

/**
 * Psi Tree Change Listener is required to reset Meta Cache before invocation of the Inspections.
 * AsyncFileListener will be invoked after in-project Psi Modifications and after Inspection Rules in other files.
 */
class PsiTreeChangeListener(private val project: Project) : PsiTreeChangeListener {

    init {
        if (!ProjectSettingsComponent.getInstance(project).isHybrisProject()) throw ExtensionNotApplicableException.create()
    }

    private val domManager = DomManager.getDomManager(project)
    private val tsModificationTracker = project.service<TSModificationTracker>()
    private val bsModificationTracker = project.service<BSModificationTracker>()
    private val cngModificationTracker = project.service<CngModificationTracker>()

    override fun beforeChildAddition(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildRemoval(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildReplacement(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildMovement(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforeChildrenChange(event: PsiTreeChangeEvent) = doChange(event)
    override fun beforePropertyChange(event: PsiTreeChangeEvent) = doChange(event)
    override fun childAdded(event: PsiTreeChangeEvent) = doChange(event)
    override fun childRemoved(event: PsiTreeChangeEvent) = doChange(event)
    override fun childReplaced(event: PsiTreeChangeEvent) = doChange(event)
    override fun childrenChanged(event: PsiTreeChangeEvent) = doChange(event)
    override fun childMoved(event: PsiTreeChangeEvent) = doChange(event)
    override fun propertyChanged(event: PsiTreeChangeEvent) = doChange(event)

    private fun doChange(event: PsiTreeChangeEvent) {
        val fileName = event.file
            ?.asSafely<XmlFile>()
            ?.let { xmlFile -> domManager.getDomFileDescription(xmlFile)?.let { it to xmlFile } }
            ?: return

        val domFileDescription = fileName.first
        val xmlFile = fileName.second

        when (domFileDescription) {
            is CngConfigDomFileDescription,
            is CngWidgetsDomFileDescription,
            is CngActionDefinitionDomFileDescription,
            is CngEditorDefinitionDomFileDescription,
            is CngWidgetDefinitionDomFileDescription -> cngModificationTracker.resetCache(xmlFile)

            is BSDomFileDescription -> bsModificationTracker.resetCache(xmlFile)
            is TSDomFileDescription -> tsModificationTracker.resetCache(xmlFile)
        }
    }
}
