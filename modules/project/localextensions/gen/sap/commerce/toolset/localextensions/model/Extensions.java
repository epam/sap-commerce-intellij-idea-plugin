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

// Generated on Thu Jan 12 19:15:30 CET 2023
// DTD/Schema  :    null

package sap.commerce.toolset.localextensions.model;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.xml.TrueAttributeValue;

import java.util.List;

/**
 * null:extensionsType interface.
 * <pre>
 * <h3>Type null:extensionsType documentation</h3>
 * Configures the installed extensions for the hybris platform..
 * </pre>
 */
public interface Extensions extends DomElement {

	String PATH = "path";
	String EXTENSION = "extension";
	String WEBAPP = "webapp";
	String AUTOLOAD = "autoload";

	/**
	 * Returns the list of path children.
	 * <pre>
	 * <h3>Element null:path documentation</h3>
	 * Adds a directory to scan for extensions in.
	 * </pre>
	 * @return the list of path children.
	 */
	@NotNull
	@SubTagList(PATH)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
	List<Scan> getPaths();

	/**
	 * Adds new child to the list of path children.
	 * @return created child
	 */
	@SubTagList(PATH)
	Scan addPath();

	/**
	 * Returns the list of extension children.
	 * <pre>
	 * <h3>Element null:extension documentation</h3>
	 * Adds an extension to the hybris platform.
	 * </pre>
	 * @return the list of extension children.
	 */
	@NotNull
	@SubTagList(EXTENSION)
	List<Extension> getExtensions();

	/**
	 * Adds new child to the list of extension children.
	 * @return created child
	 */
	@SubTagList(EXTENSION)
	Extension addExtension();

	/**
	 * Returns the list of webapp children.
	 * <pre>
	 * <h3>Element null:webapp documentation</h3>
	 * Adds external extensions to the hybris platform.
	 * </pre>
	 * @return the list of webapp children.
	 */
	@NotNull
	@SubTagList(WEBAPP)
	List<Webapp> getWebapps();

	/**
	 * Adds new child to the list of webapp children.
	 * @return created child
	 */
	@SubTagList(WEBAPP)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_2)
	Webapp addWebapp();

	/**
	 * Returns the value of the autoload child.
	 * <pre>
	 * <h3>Attribute null:autoload documentation</h3>
	 * Loads automatically all extensions of the platform. If set to 'false', you
	 * 				have to manually add the platform extensions in the (local)extensions.xml file.
	 * 				Default value is 'true'.
	 * </pre>
	 * @return the value of the autoload child.
	 */
	@NotNull
	@Attribute(AUTOLOAD)
	TrueAttributeValue getAutoload();

}
