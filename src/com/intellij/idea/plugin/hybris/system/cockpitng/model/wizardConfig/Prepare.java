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
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:PrepareType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Prepare extends DomElement {

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
	 * Returns the value of the handler child.
	 * @return the value of the handler child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("handler")
	GenericAttributeValue<String> getHandler();


	/**
	 * Returns the list of initialize children.
	 * @return the list of initialize children.
	 */
	@NotNull
	@SubTagList ("initialize")
	java.util.List<Initialize> getInitializes();
	/**
	 * Adds new child to the list of initialize children.
	 * @return created child
	 */
	@SubTagList ("initialize")
	Initialize addInitialize();


	/**
	 * Returns the list of assign children.
	 * @return the list of assign children.
	 */
	@NotNull
	@SubTagList ("assign")
	java.util.List<Assign> getAssigns();
	/**
	 * Adds new child to the list of assign children.
	 * @return created child
	 */
	@SubTagList ("assign")
	Assign addAssign();


}
