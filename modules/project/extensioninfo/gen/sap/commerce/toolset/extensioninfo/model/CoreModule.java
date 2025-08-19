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

// Generated on Mon Jan 09 16:31:21 CET 2023
// DTD/Schema  :    null
package sap.commerce.toolset.extensioninfo.model;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.xml.FalseAttributeValue;
import sap.commerce.toolset.xml.TrueAttributeValue;

/**
 * null:coremoduleType interface.
 * <pre>
 * <h3>Type null:coremoduleType documentation</h3>
 * Configures a core module for the extension. A core module consists of an items.xml file (and therefore allows to add new types to the system), a manager class, classes for the JaLo Layer and the ServiceLayer and JUnit test classes. The following directories are required: /src, /resources, /testsrc.
 * </pre>
 */
public interface CoreModule extends DomElement {

	String ADDITIONALCLASSPATH = "additionalclasspath";
	String PACKAGEROOT = "packageroot";
	String MANAGER = "manager";
	String SOURCEAVAILABLE = "sourceavailable";
	String GENERATED = "generated";
	String JAVA_5 = "java5";

	/**
	 * Returns the value of the additionalclasspath child.
	 * <pre>
	 * <h3>Attribute null:additionalclasspath documentation</h3>
	 * Deprecated. Not used anymore.
	 * </pre>
	 * @return the value of the additionalclasspath child.
	 */
	@NotNull
	@Attribute(ADDITIONALCLASSPATH)
	@Deprecated(since = "ages")
	GenericAttributeValue<String> getAdditionalClasspath();

	/**
	 * Returns the value of the packageroot child.
	 * <pre>
	 * <h3>Attribute null:packageroot documentation</h3>
	 * Package root where extension and item classes will be generated to.
	 * </pre>
	 * @return the value of the packageroot child.
	 */
	@NotNull
	@Attribute(PACKAGEROOT)
	GenericAttributeValue<String> getPackageRoot();

	/**
	 * Returns the value of the manager child.
	 * <pre>
	 * <h3>Attribute null:manager documentation</h3>
	 * Fully qualified Java class name of the extension's manager.
	 * </pre>
	 * @return the value of the manager child.
	 */
	@NotNull
	@Attribute(MANAGER)
	GenericAttributeValue<String> getModuleManager();

	/**
	 * Returns the value of the sourceavailable child.
	 * <pre>
	 * <h3>Attribute null:sourceavailable documentation</h3>
	 * Deprecated. Has no effect and will be evaluated always to 'true' if a 'src' directory is available
	 * </pre>
	 * @return the value of the sourceavailable child.
	 */
	@NotNull
	@Attribute(SOURCEAVAILABLE)
	@Deprecated(since = "ages")
	GenericAttributeValue<Boolean> getSourceAvailable();

	/**
	 * Returns the value of the generated child.
	 * <pre>
	 * <h3>Attribute null:generated documentation</h3>
	 * If "true", item and extension classes will be generated. Only needed in case of "sourceavailable=true". Default is "false".
	 * </pre>
	 * @return the value of the generated child.
	 */
	@NotNull
	@Attribute(GENERATED)
	FalseAttributeValue getGenerated();

	/**
	 * Returns the value of the java5 child.
	 * <pre>
	 * <h3>Attribute null:java5 documentation</h3>
	 * Deprecated. Will always be evaluated to 'true'. Generated item and extension classes will use java generics and annotations.
	 * </pre>
	 * @return the value of the java5 child.
	 */
	@NotNull
	@Attribute(JAVA_5)
	@Deprecated(since = "ages")
	GenericAttributeValue<Boolean> getJava5();

	/**
	 * Returns the value of the generatePartOf child.
	 * <pre>
	 * <h3>Attribute null:generatePartOf documentation</h3>
	 * If "true", the generated item and extension classes will use the partOf handler, so partOf references will be removed if the holding item is removed. Default is "true".
	 * </pre>
	 * @return the value of the generatePartOf child.
	 */
	@NotNull
	@Attribute("generatePartOf")
	TrueAttributeValue getGeneratePartOf();

}
