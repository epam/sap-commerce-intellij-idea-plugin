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

// Generated on Wed Jan 18 00:35:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package sap.commerce.toolset.cockpitNG.model.widgets;

import com.intellij.util.xml.Namespace;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/config/notifications:ImportanceLevel enumeration.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public enum ImportanceLevel {
    FAILURE,
    INFO,
    SUCCESS,
    WARNING
}
