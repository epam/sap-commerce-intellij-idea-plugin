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

// Generated on Thu Jan 19 16:23:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:PropertyListType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface PropertyList extends DomElement {

	String ROOT = "root";
	String READONLY = "readonly";
	String VALIDATE = "validate";
	String ID = "id";
	String MERGE_MODE = "merge-mode";
	String POSITION = "position";
	String INCLUDE_NON_DECLARED_MANDATORY = "include-non-declared-mandatory";
	String INCLUDE_NON_DECLARED_UNIQUE = "include-non-declared-unique";
	String INCLUDE_NON_DECLARED_WRITABLE_ON_CREATION = "include-non-declared-writable-on-creation";
	String ENABLE_NON_DECLARED_INCLUDES = "enable-non-declared-includes";
	String PROPERTY = "property";

	/**
	 * Returns the value of the root child.
	 * @return the value of the root child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (ROOT)
	GenericAttributeValue<String> getRoot();


	/**
	 * Returns the value of the readonly child.
	 * @return the value of the readonly child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (READONLY)
	GenericAttributeValue<Boolean> getReadonly();


	/**
	 * Returns the value of the validate child.
	 * @return the value of the validate child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (VALIDATE)
	GenericAttributeValue<Boolean> getValidate();


	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (ID)
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the merge-mode child.
	 * @return the value of the merge-mode child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (MERGE_MODE)
	GenericAttributeValue<String> getMergeMode();


	/**
	 * Returns the value of the position child.
	 * @return the value of the position child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (POSITION)
	GenericAttributeValue<Integer> getPosition();


	/**
	 * Returns the value of the include-non-declared-mandatory child.
	 * @return the value of the include-non-declared-mandatory child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (INCLUDE_NON_DECLARED_MANDATORY)
	GenericAttributeValue<Boolean> getIncludeNonDeclaredMandatory();


	/**
	 * Returns the value of the include-non-declared-unique child.
	 * @return the value of the include-non-declared-unique child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (INCLUDE_NON_DECLARED_UNIQUE)
	GenericAttributeValue<Boolean> getIncludeNonDeclaredUnique();


	/**
	 * Returns the value of the include-non-declared-writable-on-creation child.
	 * @return the value of the include-non-declared-writable-on-creation child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (INCLUDE_NON_DECLARED_WRITABLE_ON_CREATION)
	GenericAttributeValue<Boolean> getIncludeNonDeclaredWritableOnCreation();


	/**
	 * Returns the value of the enable-non-declared-includes child.
	 * @return the value of the enable-non-declared-includes child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (ENABLE_NON_DECLARED_INCLUDES)
	GenericAttributeValue<Boolean> getEnableNonDeclaredIncludes();


	/**
	 * Returns the list of property children.
	 * @return the list of property children.
	 */
	@NotNull
	@SubTagList (PROPERTY)
	java.util.List<Property> getProperties();
	/**
	 * Adds new child to the list of property children.
	 * @return created child
	 */
	@SubTagList (PROPERTY)
	Property addProperty();


}
