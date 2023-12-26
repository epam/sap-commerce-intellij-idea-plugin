/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.spellchecker.xml.NoSpellchecking;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:requires-extensionType interface.
 * <pre>
 * <h3>Type null:requires-extensionType documentation</h3>
 * Configures the set of extensions required by the extension at compile time.
 * </pre>
 */
public interface RequiresExtension extends DomElement {

	String NAME = "name";
	String VERSION = "version";

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Attribute null:name documentation</h3>
	 * Name of an extension which is required at compile time.
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	@Attribute(NAME)
	@Required
	@NoSpellchecking
	GenericAttributeValue<String> getName();

	/**
	 * Returns the value of the version child.
	 * <pre>
	 * <h3>Attribute null:version documentation</h3>
	 * Allowed range of versions of the required extension. Is used by the hybris package manager.
	 * </pre>
	 * @return the value of the version child.
	 */
	@NotNull
	@Attribute(VERSION)
	GenericAttributeValue<String> getVersion();

}
