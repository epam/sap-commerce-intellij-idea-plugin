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

// Generated on Thu Jan 12 19:15:30 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.localextensions.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:hybrisconfigElemType interface.
 * <pre>
 * <h3>Type null:hybrisconfigElemType documentation</h3>
 * Configures the installed extensions for the hybris platform.
 * </pre>
 */
public interface Hybrisconfig extends DomElement {

	String EXTENSIONS = "extensions";

	/**
	 * Returns the value of the extensions child.
	 * <pre>
	 * <h3>Element null:extensions documentation</h3>
	 * Configures the installed extensions for the hybris platform..
	 * </pre>
	 * @return the value of the extensions child.
	 */
	@NotNull
	@SubTag(EXTENSIONS)
	@Required
	Extensions getExtensions();


}
