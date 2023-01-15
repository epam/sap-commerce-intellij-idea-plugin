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
package com.intellij.idea.plugin.hybris.psi.reference.contributor

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils.tagAttributeValuePattern
import com.intellij.idea.plugin.hybris.psi.reference.provider.TSItemReferenceProvider
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class HybrisCockpitngReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val cockpitngConfigFile = PlatformPatterns.psiFile()
            .withName(StandardPatterns.string().endsWith(HybrisConstants.COCKPIT_NG_CONFIG_XML))

        // classes resolved by JavaXmlClassListReferenceContributor
        registrar.registerReferenceProvider(
            tagAttributeValuePattern("context", "type")
                .inFile(cockpitngConfigFile),
            TSItemReferenceProvider.instance
        )
    }
}