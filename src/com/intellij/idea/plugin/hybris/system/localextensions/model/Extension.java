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

// Generated on Thu Jan 12 19:15:30 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.localextensions.model;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.spellchecker.xml.NoSpellchecking;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * null:extensionType interface.
 * <pre>
 * <h3>Type null:extensionType documentation</h3>
 * Adds an extension to the hybris platform.
 * </pre>
 */
public interface Extension extends DomElement {

	String DIR = "dir";
	String NAME = "name";

	/**
	 * Returns the value of the dir child.
	 * <pre>
	 * <h3>Attribute null:dir documentation</h3>
	 * Path to the extension folder relative to the platform home.
	 * </pre>
	 * @return the value of the dir child.
	 */
	@NotNull
	@Attribute(DIR)
	GenericAttributeValue<String> getDir();

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Attribute null:name documentation</h3>
	 * The name of the extension. Requires at least one &lt;scan&gt; entry to be able to look up the extension location.
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	@Attribute(NAME)
	@NoSpellchecking
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
	GenericAttributeValue<String> getName();

}
