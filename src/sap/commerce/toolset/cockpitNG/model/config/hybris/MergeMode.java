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

// Generated on Wed Jan 18 00:35:54 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/common

package sap.commerce.toolset.cockpitNG.model.config.hybris;

/**
 * http://www.hybris.com/cockpitng/config/common:merge-mode enumeration.
 * <pre>
 * <h3>Enumeration http://www.hybris.com/cockpitng/config/common:merge-mode documentation</h3>
 * A general purpose type defining merge mode of configuration element
 * </pre>
 */
public enum MergeMode implements com.intellij.util.xml.NamedEnum {
    MERGE("MERGE"),
    REMOVE("REMOVE"),
    REPLACE("REPLACE");

    private final String value;

    MergeMode(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
