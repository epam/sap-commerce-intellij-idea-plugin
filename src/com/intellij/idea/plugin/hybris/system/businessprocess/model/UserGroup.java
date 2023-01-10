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

// Generated on Tue Jan 10 21:54:19 CET 2023
// DTD/Schema  :    http://www.hybris.de/xsd/processdefinition

package com.intellij.idea.plugin.hybris.system.businessprocess.model;

import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.de/xsd/processdefinition:userGroupType interface.
 * <pre>
 * <h3>Type http://www.hybris.de/xsd/processdefinition:userGroupType documentation</h3>
 * Define userGroup for notification.
 * </pre>
 */
public interface UserGroup extends DomElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("name")
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the message child.
	 * @return the value of the message child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("message")
	GenericAttributeValue<String> getMessage();


	/**
	 * Returns the list of locmessage children.
	 * @return the list of locmessage children.
	 */
	@NotNull
	@SubTagList ("locmessage")
	@Required
	java.util.List<Localizedmessage> getLocmessages();
	/**
	 * Adds new child to the list of locmessage children.
	 * @return created child
	 */
	@SubTagList ("locmessage")
	Localizedmessage addLocmessage();


}
