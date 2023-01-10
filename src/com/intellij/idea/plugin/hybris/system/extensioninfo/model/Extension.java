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

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:extensionType interface.
 * <pre>
 * <h3>Type null:extensionType documentation</h3>
 * Configures the available modules of the extension.
 * </pre>
 */
public interface Extension extends DomElement {

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Attribute null:name documentation</h3>
	 * Name of the extension. Do not use special characters or spaces.
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	@Attribute ("name")
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the version child.
	 * <pre>
	 * <h3>Attribute null:version documentation</h3>
	 * Optionally defines the version of this extension. If not defined the build process assumes it being the same version as the platform.
	 * </pre>
	 * @return the value of the version child.
	 */
	@NotNull
	@Attribute ("version")
	GenericAttributeValue<String> getVersion();


	/**
	 * Returns the value of the classprefix child.
	 * <pre>
	 * <h3>Attribute null:classprefix documentation</h3>
	 * Prefix used for generated extension classes, such as the classes for Constants. Default is "[extensionname]".
	 * </pre>
	 * @return the value of the classprefix child.
	 */
	@NotNull
	@Attribute ("classprefix")
	GenericAttributeValue<String> getClassPrefix();


	/**
	 * Returns the value of the abstractclassprefix child.
	 * <pre>
	 * <h3>Attribute null:abstractclassprefix documentation</h3>
	 * Prefix for generated Java classes, such as the abstract classes for getter and setter methods. Default is "Generated".
	 * </pre>
	 * @return the value of the abstractclassprefix child.
	 */
	@NotNull
	@Attribute ("abstractclassprefix")
	GenericAttributeValue<String> getAbstractClassPrefix();


	/**
	 * Returns the value of the isoldstyleextension child.
	 * <pre>
	 * <h3>Attribute null:isoldstyleextension documentation</h3>
	 * Deprecated. Default is "false".
	 * </pre>
	 * @return the value of the isoldstyleextension child.
	 */
	@NotNull
	@Attribute ("isoldstyleextension")
	GenericAttributeValue<Boolean> getIsOldStyleExtension();


	/**
	 * Returns the value of the requiredbyall child.
	 * <pre>
	 * <h3>Attribute null:requiredbyall documentation</h3>
	 * If 'true' this extension is treated like platform/ext core extensions and is automtically added to all other extension dependencies.
	 * </pre>
	 * @return the value of the requiredbyall child.
	 */
	@NotNull
	@Attribute ("requiredbyall")
	GenericAttributeValue<Boolean> getRequiredByAll();


	/**
	 * Returns the value of the managername child.
	 * <pre>
	 * <h3>Attribute null:managername documentation</h3>
	 * Class name of the manager class. Default is "[classprefix]Manager"
	 * </pre>
	 * @return the value of the managername child.
	 */
	@NotNull
	@Attribute ("managername")
	GenericAttributeValue<String> getManagerName();


	/**
	 * Returns the value of the managersuperclass child.
	 * <pre>
	 * <h3>Attribute null:managersuperclass documentation</h3>
	 * Class name of the manager's superclass. Default is de.hybris.platform.jalo.extension.Extension.
	 * </pre>
	 * @return the value of the managersuperclass child.
	 */
	@NotNull
	@Attribute ("managersuperclass")
	GenericAttributeValue<String> getManagerSuperClass();


	/**
	 * Returns the value of the description child.
	 * <pre>
	 * <h3>Attribute null:description documentation</h3>
	 * Short description of this extension. Is used by the hybris package manager.
	 * </pre>
	 * @return the value of the description child.
	 */
	@NotNull
	@Attribute ("description")
	GenericAttributeValue<String> getDescription();


	/**
	 * Returns the value of the usemaven child.
	 * <pre>
	 * <h3>Attribute null:usemaven documentation</h3>
	 * If 'true' uses maven and external-dependencies.xml file for fetching required libraries into \lib and \web\webroot\WEB-INF\lib.
	 * </pre>
	 * @return the value of the usemaven child.
	 */
	@NotNull
	@Attribute ("usemaven")
	GenericAttributeValue<Boolean> getUseMaven();


	/**
	 * Returns the value of the jaloLogicFree child.
	 * <pre>
	 * <h3>Attribute null:jaloLogicFree documentation</h3>
	 * If 'true' types introduced by this extension are SLD safe by default and contains no JALO logic.
	 * </pre>
	 * @return the value of the jaloLogicFree child.
	 */
	@NotNull
	@Attribute ("jaloLogicFree")
	GenericAttributeValue<Boolean> getJaloLogicFree();


	/**
	 * Returns the list of requires-extension children.
	 * <pre>
	 * <h3>Element null:requires-extension documentation</h3>
	 * Configures the set of extensions required by the extension at compile time. If you set 'autoload=true' in the localextensions.xml file, you will not need to reference any core extensions here.
	 * </pre>
	 * @return the list of requires-extension children.
	 */
	@NotNull
	@SubTagList ("requires-extension")
	java.util.List<RequiresExtension> getRequiresExtensions();
	/**
	 * Adds new child to the list of requires-extension children.
	 * @return created child
	 */
	@SubTagList ("requires-extension")
	RequiresExtension addRequiresExtension();


	/**
	 * Returns the value of the coremodule child.
	 * <pre>
	 * <h3>Element null:coremodule documentation</h3>
	 * Configures a core module for the extension. A core module consists of an items.xml file (and therefore allows to add new types to the system), a manager class, classes for the JaLo Layer and the ServiceLayer and JUnit test classes. The following directories are required: /src, /resources, /testsrc.
	 * </pre>
	 * @return the value of the coremodule child.
	 */
	@NotNull
	@SubTag ("coremodule")
	CoreModule getCoremodule();


	/**
	 * Returns the value of the webmodule child.
	 * <pre>
	 * <h3>Element null:webmodule documentation</h3>
	 * Configures a web module for the extension. Required directory: /web.
	 * </pre>
	 * @return the value of the webmodule child.
	 */
	@NotNull
	@SubTag ("webmodule")
	WebModule getWebmodule();


	/**
	 * Returns the value of the hmcmodule child.
	 * <pre>
	 * <h3>Element null:hmcmodule documentation</h3>
	 * Configures an hMC module for the extension. Required directory: /hmc.
	 * </pre>
	 * @return the value of the hmcmodule child.
	 */
	@NotNull
	@SubTag ("hmcmodule")
	HmcModule getHmcmodule();


	/**
	 * Returns the list of meta children.
	 * <pre>
	 * <h3>Element null:meta documentation</h3>
	 * Configures metadata.
	 * </pre>
	 * @return the list of meta children.
	 */
	@NotNull
	@SubTagList ("meta")
	java.util.List<Meta> getMetas();
	/**
	 * Adds new child to the list of meta children.
	 * @return created child
	 */
	@SubTagList ("meta")
	Meta addMeta();


}
