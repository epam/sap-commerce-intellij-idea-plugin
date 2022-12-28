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

import com.intellij.idea.plugin.hybris.psi.reference.provider.HybrisEnumItemReferenceProvider;
import com.intellij.idea.plugin.hybris.psi.reference.provider.HybrisEnumLiteralItemReferenceProvider;
import com.intellij.idea.plugin.hybris.psi.reference.provider.HybrisModelItemReferenceProvider;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import static com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils.insideTagPattern;
import static com.intellij.idea.plugin.hybris.common.utils.PsiXmlUtils.tagAttributeValuePattern;

/**
 * @author Nosov Aleksandr
 */
public class HybrisItemXmlReferenceContributor extends PsiReferenceContributor {

    public static final String ITEMS_TYPE_FILE_NAME = "-items";

    @Override
    public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            tagAttributeValuePattern("enumtype", "code", ITEMS_TYPE_FILE_NAME),
            new HybrisEnumItemReferenceProvider()
        );
        registrar.registerReferenceProvider(
            tagAttributeValuePattern("value", "code", ITEMS_TYPE_FILE_NAME).inside(insideTagPattern("enumtype")),
            new HybrisEnumLiteralItemReferenceProvider()
        );
        registrar.registerReferenceProvider(
            tagAttributeValuePattern("itemtype", "code", ITEMS_TYPE_FILE_NAME),
            new HybrisModelItemReferenceProvider()
        );
    }


}
