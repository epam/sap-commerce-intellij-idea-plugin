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

// Generated on Tue Jan 10 21:54:19 CET 2023
// DTD/Schema  :    http://www.hybris.de/xsd/processdefinition

package sap.commerce.toolset.businessProcess.model;

import sap.commerce.toolset.businessProcess.util.xml.BpNavigableElementConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.de/xsd/processdefinition:notify interface.
 */
public interface Notify extends NavigableElement {

	/**
	 * Returns the value of the then child.
	 * @return the value of the then child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("then")
	@Convert(BpNavigableElementConverter.class)
	GenericAttributeValue<String> getThen();


	/**
	 * Returns the list of userGroup children.
	 * @return the list of userGroup children.
	 */
	@NotNull
	@SubTagList ("userGroup")
	@Required
	java.util.List<UserGroup> getUserGroups();
	/**
	 * Adds new child to the list of userGroup children.
	 * @return created child
	 */
	@SubTagList ("userGroup")
	UserGroup addUserGroup();


}
