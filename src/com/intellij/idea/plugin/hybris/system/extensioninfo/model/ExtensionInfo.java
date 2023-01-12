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

// Generated on Mon Jan 09 16:31:21 CET 2023
// DTD/Schema  :    null
package com.intellij.idea.plugin.hybris.system.extensioninfo.model;

import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * null:extensioninfoElemType interface.
 * <pre>
 * <h3>Type null:extensioninfoElemType documentation</h3>
 * Configures the available modules of the extension.
 * </pre>
 */
@Stubbed
@StubbedOccurrence
public interface ExtensionInfo extends DomElement {

	/**
	 * Returns the value of the extension child.
	 * @return the value of the extension child.
	 */
	@NotNull
	@SubTag ("extension")
	@Required
	Extension getExtension();


}
