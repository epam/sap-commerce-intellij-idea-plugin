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

package com.intellij.idea.plugin.hybris.psi.reference.contributor;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils;
import com.intellij.idea.plugin.hybris.psi.reference.provider.HybrisEnumItemReferenceProvider;
import com.intellij.idea.plugin.hybris.psi.reference.provider.HybrisEnumLiteralItemReferenceProvider;
import com.intellij.idea.plugin.hybris.psi.reference.provider.HybrisModelItemReferenceProvider;
import com.intellij.idea.plugin.hybris.system.type.psi.TSPatterns;
import com.intellij.idea.plugin.hybris.system.type.psi.provider.TSItemAttributeReferenceProvider;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nosov Aleksandr
 */
public class HybrisItemsXmlReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        final var inItemsXmlFile = PlatformPatterns.psiFile()
                                                   .withName(StandardPatterns.string().endsWith(HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING));
        registrar.registerReferenceProvider(
            PsiXmlUtils.INSTANCE.tagAttributeValuePattern("code", "enumtype")
                                .inFile(inItemsXmlFile),
            HybrisEnumItemReferenceProvider.Companion.getInstance()
        );
        registrar.registerReferenceProvider(
            PsiXmlUtils.INSTANCE.tagAttributeValuePattern("code", "value")
                                .inside(PsiXmlUtils.INSTANCE.insideTagPattern("enumtype"))
                                .inFile(inItemsXmlFile),
            HybrisEnumLiteralItemReferenceProvider.Companion.getInstance()
        );
        registrar.registerReferenceProvider(
            PsiXmlUtils.INSTANCE.tagAttributeValuePattern("code", "itemtype")
                                .inFile(inItemsXmlFile),
            HybrisModelItemReferenceProvider.Companion.getInstance()
        );
        registrar.registerReferenceProvider(
            TSPatterns.INSTANCE.getINDEX_KEY_ATTRIBUTE(),
            TSItemAttributeReferenceProvider.Companion.getInstance()
        );
    }


}
