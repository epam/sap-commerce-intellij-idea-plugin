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

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.spellchecker.xml.NoSpellchecking;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:extensionType interface.
 * <pre>
 * <h3>Type null:extensionType documentation</h3>
 * Configures the available modules of the extension.
 * </pre>
 */
public interface Extension extends DomElement {

	String REQUIRES_EXTENSION = "requires-extension";
	String COREMODULE = "coremodule";
	String WEBMODULE = "webmodule";
	String HMCMODULE = "hmcmodule";
	String META = "meta";
	String NAME = "name";
	String VERSION = "version";
	String CLASSPREFIX = "classprefix";
	String ABSTRACTCLASSPREFIX = "abstractclassprefix";
	String ISOLDSTYLEEXTENSION = "isoldstyleextension";
	String REQUIREDBYALL = "requiredbyall";
	String MANAGERNAME = "managername";
	String MANAGERSUPERCLASS = "managersuperclass";
	String DESCRIPTION = "description";
	String USEMAVEN = "usemaven";
	String JALO_LOGIC_FREE = "jaloLogicFree";

	/**
	 * Returns the list of requires-extension children.
	 * <pre>
	 * <h3>Element null:requires-extension documentation</h3>
	 * Configures the set of extensions required by the extension at compile time. If you set 'autoload=true' in the localextensions.xml file, you will not need to reference any core extensions here.
	 * </pre>
	 * @return the list of requires-extension children.
	 */
	@NotNull
	@SubTagList(REQUIRES_EXTENSION)
	List<RequiresExtension> getRequiresExtensions();

	/**
	 * Adds new child to the list of requires-extension children.
	 * @return created child
	 */
	@SubTagList(REQUIRES_EXTENSION)
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
	@SubTag(COREMODULE)
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
	@SubTag(WEBMODULE)
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
	@SubTag(HMCMODULE)
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
	@SubTagList(META)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
	List<Meta> getMetas();

	/**
	 * Adds new child to the list of meta children.
	 * @return created child
	 */
	@SubTagList(META)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
	Meta addMeta();

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Attribute null:name documentation</h3>
	 * Name of the extension. Do not use special characters or spaces.
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
	 * Optionally defines the version of this extension. If not defined the build process assumes it being the same version as the platform.
	 * </pre>
	 * @return the value of the version child.
	 */
	@NotNull
	@Attribute(VERSION)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
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
	@Attribute(CLASSPREFIX)
	@NoSpellchecking
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
	@Attribute(ABSTRACTCLASSPREFIX)
	@NoSpellchecking
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
	@Attribute(ISOLDSTYLEEXTENSION)
	@Deprecated(since = "ages")
	FalseAttributeValue getIsOldStyleExtension();

	/**
	 * Returns the value of the requiredbyall child.
	 * <pre>
	 * <h3>Attribute null:requiredbyall documentation</h3>
	 * If 'true' this extension is treated like platform/ext core extensions and is automtically added to all other extension dependencies.
	 * </pre>
	 * @return the value of the requiredbyall child.
	 */
	@NotNull
	@Attribute(REQUIREDBYALL)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
	FalseAttributeValue getRequiredByAll();

	/**
	 * Returns the value of the managername child.
	 * <pre>
	 * <h3>Attribute null:managername documentation</h3>
	 * Class name of the manager class. Default is "[classprefix]Manager"
	 * </pre>
	 * @return the value of the managername child.
	 */
	@NotNull
	@Attribute(MANAGERNAME)
	@NoSpellchecking
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
	@Attribute(MANAGERSUPERCLASS)
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
	@Attribute(DESCRIPTION)
	GenericAttributeValue<String> getDescription();

	/**
	 * Returns the value of the usemaven child.
	 * Originally, for some reason the schema defines this attribute as a string
	 * <pre>
	 * <h3>Attribute null:usemaven documentation</h3>
	 * If 'true' uses maven and external-dependencies.xml file for fetching required libraries into \lib and \web\webroot\WEB-INF\lib.
	 * </pre>
	 * @return the value of the usemaven child.
	 */
	@NotNull
	@Attribute(USEMAVEN)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_2)
	FalseAttributeValue getUseMaven();

	/**
	 * Returns the value of the jaloLogicFree child.
	 * <pre>
	 * <h3>Attribute null:jaloLogicFree documentation</h3>
	 * If 'true' types introduced by this extension are SLD safe by default and contains no JALO logic.
	 * </pre>
	 * @return the value of the jaloLogicFree child.
	 */
	@NotNull
	@Attribute(JALO_LOGIC_FREE)
	@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_1811)
	FalseAttributeValue getJaloLogicFree();

}
