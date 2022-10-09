/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.type.system.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.idea.plugin.hybris.type.system.inspections.fix.XmlAddAttributeQuickFix;
import com.intellij.idea.plugin.hybris.type.system.inspections.fix.XmlAddTagQuickFix;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.util.SortedMap;
import java.util.TreeMap;

public final class ItemsXmlQuickFixManager {

    private static final String DEPLOYMENT_TABLE_MUST_EXIST_FOR_ITEM_EXTENDING_GENERIC_ITEM = "DeploymentTableMustExistForItemExtendingGenericItem";
    private static final String MANDATORY_FIELD_MUST_HAVE_INITIAL_VALUE = "MandatoryFieldMustHaveInitialValue";
    private static final String IMMUTABLE_FIELD_MUST_HAVE_INITIAL_VALUE = "ImmutableFieldMustHaveInitialValue";
    private static final String BOOLEAN_FIELD_CANNOT_BE_OPTIONAL = "BooleanFieldCannotBeOptional";

    private ItemsXmlQuickFixManager() {
    }

    public static LocalQuickFix[] getQuickFixes(final Node problemNode, final @NotNull String id) {
        // ! No sense to generate more than 5 quick fixes
        final LocalQuickFix[] fixes = new LocalQuickFix[5];

        switch (id) {
            case MANDATORY_FIELD_MUST_HAVE_INITIAL_VALUE:
            case IMMUTABLE_FIELD_MUST_HAVE_INITIAL_VALUE:
                fixes[0] = new XmlAddTagQuickFix("defaultvalue", "", null, null);
                fixes[1] = new XmlAddAttributeQuickFix("modifiers", "initial", "true");
                break;
            case DEPLOYMENT_TABLE_MUST_EXIST_FOR_ITEM_EXTENDING_GENERIC_ITEM:
                final SortedMap<String, String> attributes = getAttributesForDeploymentTableFix(problemNode);
                fixes[0] = new XmlAddTagQuickFix("deployment", null, attributes, "description");
                break;
            case BOOLEAN_FIELD_CANNOT_BE_OPTIONAL:
                fixes[0] = new XmlAddTagQuickFix("defaultvalue", "", null, null);
                fixes[1] = new XmlAddAttributeQuickFix("modifiers", "optional", "false");
                break;
        }

        return fixes;
    }

    @NotNull
    private static SortedMap<String, String> getAttributesForDeploymentTableFix(final Node problemNode) {
        // @ToDo: look on max possible table name length and add truncate
        // ? Maybe there is a sense to create a separate check and fix for max possible table name?
        final String tableCode = problemNode.getAttributes().getNamedItem("code").getNodeValue();
        final SortedMap<String, String> attributes = new TreeMap<>();
        attributes.put("table", tableCode);
        // @ToDo: add autogeneration of typecode
        attributes.put("typecode", "");
        return attributes;
    }

}
