/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.type.util.xml.converter

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.idea.plugin.hybris.properties.PropertyService
import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.codeInsight.lookup.TSLookupElementFactory
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.deployment.DatabaseSchema
import com.intellij.idea.plugin.hybris.system.type.model.deployment.Model
import com.intellij.idea.plugin.hybris.system.type.model.deployment.TypeMapping
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.xml.XmlFile
import com.intellij.util.Processor
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.stubs.index.DomElementClassIndex
import java.util.*

class AdvancedTypeMappingConverter : AbstractTSConverterBase<TypeMapping>(TypeMapping::class.java) {

    override fun toString(t: TypeMapping?, context: ConvertContext) = t?.type?.stringValue

    override fun searchForName(name: String, context: ConvertContext, meta: TSMetaModelAccess?) = getTypeMappings(context)
        ?.firstOrNull { it.type.stringValue == name }

    override fun searchAll(context: ConvertContext, meta: TSMetaModelAccess?): MutableList<TypeMapping> = getTypeMappings(context)
        ?: mutableListOf()

    override fun createLookupElement(t: TypeMapping?): LookupElement? {
        val typeMapping = t
            ?.type
            ?.stringValue
            ?: return null
        return when {
            typeMapping.startsWith("java.") -> TSLookupElementFactory.buildObject(t)
            typeMapping.startsWith("HYBRIS.") -> TSLookupElementFactory.buildSpecial(t)
            else -> TSLookupElementFactory.buildPrimitive(t)
        }
    }

    private fun getTypeMappings(context: ConvertContext): MutableList<TypeMapping>? = getDatabaseSchema(context.project)
        ?.typeMappings
        ?.filterNot { it.type.stringValue?.startsWith("java.lang.") ?: false }
        ?.toMutableList()

    private fun getDatabaseSchema(project: Project): DatabaseSchema? {
        val propertyService = PropertyService.getInstance(project) ?: return null
        val database = propertyService.findProperty("db.url")
            ?.let { DATABASE_REGEX.find(it) }
            ?.takeIf { it.groups.size == 3 }
            ?.let { it.groupValues[2] }
            ?.lowercase(Locale.ROOT)
            ?: return null

        val domManager = DomManager.getDomManager(project)
        var deploymentModel: Model? = null

        StubIndex.getInstance().processElements(
            DomElementClassIndex.KEY,
            Model::class.java.name,
            project,
            ProjectScope.getAllScope(project),
            PsiFile::class.java,
            object : Processor<PsiFile> {
                override fun process(psiFile: PsiFile): Boolean {
                    psiFile.virtualFile ?: return true
                    PsiUtils.getModule(psiFile) ?: return true
                    deploymentModel = domManager.getFileElement(psiFile as XmlFile, Model::class.java)
                        ?.rootElement
                        ?: return true

                    return false
                }
            }
        )
        return deploymentModel
            ?.databaseSchemas
            ?.find { database == it.database.stringValue }
    }

    companion object {
        val DATABASE_REGEX = "(jdbc[:\\\\]+)([a-z]+)".toRegex()
    }

}
