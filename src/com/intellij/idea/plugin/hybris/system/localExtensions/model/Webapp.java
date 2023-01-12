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

package com.intellij.idea.plugin.hybris.system.localExtensions.model;

import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * null:webappType interface.
 * <pre>
 * <h3>Type null:webappType documentation</h3>
 * Adds external extension to the hybris platform.
 * </pre>
 */
public interface Webapp extends DomElement {

	/**
	 * Returns the value of the contextroot child.
	 * <pre>
	 * <h3>Attribute null:contextroot documentation</h3>
	 * External extension's webroot.
	 * </pre>
	 * @return the value of the contextroot child.
	 */
	@NotNull
	@Attribute ("contextroot")
	GenericAttributeValue<String> getContextroot();


	/**
	 * Returns the value of the context child.
	 * <pre>
	 * <h3>Attribute null:context documentation</h3>
	 * External extension's context.xml file.
	 * </pre>
	 * @return the value of the context child.
	 */
	@NotNull
	@Attribute ("context")
	GenericAttributeValue<String> getContext();


	/**
	 * Returns the value of the path child.
	 * <pre>
	 * <h3>Attribute null:path documentation</h3>
	 * Path to external extension's war file (or exploded directory).
	 * </pre>
	 * @return the value of the path child.
	 */
	@NotNull
	@Attribute ("path")
	GenericAttributeValue<String> getPath();


}
