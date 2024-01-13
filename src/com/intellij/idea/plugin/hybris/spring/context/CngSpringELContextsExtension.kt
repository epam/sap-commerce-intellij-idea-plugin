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
package com.intellij.idea.plugin.hybris.spring.context

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.idea.plugin.hybris.system.cockpitng.CngConfigDomFileDescription.Companion.NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS
import com.intellij.idea.plugin.hybris.system.cockpitng.CngConfigDomFileDescription.Companion.NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Labels
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.AbstractAction
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPsiHelper
import com.intellij.javaee.el.util.ELImplicitVariable
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.light.DefiniteLightVariable
import com.intellij.psi.scope.processor.MethodResolveProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.PropertyUtilBase
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.spring.el.contextProviders.SpringElContextsExtension

class CngSpringELContextsExtension : SpringElContextsExtension() {

    override fun getContextVariables(spelFile: PsiElement): MutableCollection<out PsiVariable> {
        if (!PluginCommon.isPluginActive(PluginCommon.JAVAEE_EL_PLUGIN_ID)) return mutableListOf()

        val context = spelFile.context ?: return mutableListOf()
        val project = spelFile.project

        val tag = context.parentOfType<XmlTag>() ?: return mutableListOf()

        return when {
            context is XmlText
                && tag.localName == Labels.LABEL
                && tag.namespace == NAMESPACE_COCKPIT_NG_CONFIG_HYBRIS -> process(context, project)

            context is XmlAttributeValue
                && context.parentOfType<XmlAttribute>()?.localName == AbstractAction.VISIBLE
                && tag.namespace == NAMESPACE_COCKPIT_NG_CONFIG_WIZARD_CONFIG -> process(context, project)

            else -> mutableListOf()
        }
    }

    private fun process(context: XmlText, project: Project) = context
        .let { CngPsiHelper.resolveContextType(it) }
        ?.let { findClassByHybrisTypeName(project, it) }
        ?.let { process(it) }
        ?.toMutableList()
        ?: mutableListOf()

    private fun process(context: XmlAttributeValue, project: Project) = context
        .let { CngPsiHelper.resolveNamedContextTypeForNewItemInWizardFlow(it) }
        ?.let {
            val name = it.first ?: return@let null
            val type = it.second ?: return@let null

            val psiClass = if (type.contains(".") && type != HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE) findClassByFQN(project, type)
            else findClassByHybrisTypeName(project, type)
            if (psiClass == null) return@let null

            name to psiClass
        }
        ?.let { DefiniteLightVariable(it.first, PsiTypesUtil.getClassType(it.second), it.second) }
        ?.let { mutableListOf(it) }
        ?: mutableListOf()

    private fun findClassByHybrisTypeName(project: Project, typeName: String) = PsiShortNamesCache.getInstance(project)
        .getClassesByName(typeName + HybrisConstants.MODEL_SUFFIX, GlobalSearchScope.allScope(project))
        .firstOrNull { psiClass -> psiClass.containingFile.virtualFile.path.contains("/platform/bootstrap") }

    private fun findClassByFQN(project: Project, className: String) = JavaPsiFacade.getInstance(project)
        .findClass(className, GlobalSearchScope.allScope(project))

    private fun process(psiClass: PsiClass) = psiClass
        .let { MethodResolveProcessor.getAllMethods(it) }
        .filter { it.hasModifierProperty("public") && !it.isConstructor }
        .filter { it.returnTypeElement?.type != PsiTypes.voidType() }
        .mapNotNull { method ->
            val name = PropertyUtilBase.getPropertyName(method) ?: return@mapNotNull null
            val type = PropertyUtilBase.getPropertyType(method) ?: return@mapNotNull null
            val nameIdentifier = PropertyUtilBase.getPropertyNameIdentifier(method) ?: return@mapNotNull null

            ELImplicitVariable(method, name, type, nameIdentifier, ELImplicitVariable.NESTED_RANGE)
        }
        .toMutableList()
}
