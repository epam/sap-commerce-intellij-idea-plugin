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

// Generated on Thu Jan 19 16:23:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:ContentType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Content extends DomElement {

	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("id")
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the merge-mode child.
	 * @return the value of the merge-mode child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("merge-mode")
	GenericAttributeValue<String> getMergeMode();


	/**
	 * Returns the value of the position child.
	 * @return the value of the position child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("position")
	GenericAttributeValue<Integer> getPosition();


	/**
	 * Returns the list of column children.
	 * @return the list of column children.
	 */
	@NotNull
	@SubTagList ("column")
	java.util.List<Content> getColumns();
	/**
	 * Adds new child to the list of column children.
	 * @return created child
	 */
	@SubTagList ("column")
	Content addColumn();


	/**
	 * Returns the list of property children.
	 * @return the list of property children.
	 */
	@NotNull
	@SubTagList ("property")
	java.util.List<Property> getProperties();
	/**
	 * Adds new child to the list of property children.
	 * @return created child
	 */
	@SubTagList ("property")
	Property addProperty();


	/**
	 * Returns the list of property-list children.
	 * @return the list of property-list children.
	 */
	@NotNull
	@SubTagList ("property-list")
	java.util.List<PropertyList> getPropertyLists();
	/**
	 * Adds new child to the list of property-list children.
	 * @return created child
	 */
	@SubTagList ("property-list")
	PropertyList addPropertyList();


	/**
	 * Returns the list of custom-view children.
	 * @return the list of custom-view children.
	 */
	@NotNull
	@SubTagList ("custom-view")
	java.util.List<View> getCustomViews();
	/**
	 * Adds new child to the list of custom-view children.
	 * @return created child
	 */
	@SubTagList ("custom-view")
	View addCustomView();


}
