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

// Generated on Thu Jan 19 16:23:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package sap.commerce.toolset.cockpitNG.model.wizardConfig;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:NavigationType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Navigation extends DomElement, Mergeable {

	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("id")
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the cancel child.
	 * @return the value of the cancel child.
	 */
	@NotNull
	@SubTag ("cancel")
	Cancel getCancel();


	/**
	 * Returns the value of the back child.
	 * @return the value of the back child.
	 */
	@NotNull
	@SubTag ("back")
	Back getBack();


	/**
	 * Returns the value of the next child.
	 * @return the value of the next child.
	 */
	@NotNull
	@SubTag ("next")
	Next getNext();


	/**
	 * Returns the value of the done child.
	 * @return the value of the done child.
	 */
	@NotNull
	@SubTag ("done")
	Done getDone();


	/**
	 * Returns the value of the custom child.
	 * @return the value of the custom child.
	 */
	@NotNull
	@SubTag ("custom")
	Custom getCustom();


}
