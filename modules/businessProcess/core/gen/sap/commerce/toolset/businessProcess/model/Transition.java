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
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.de/xsd/processdefinition:transition interface.
 */
public interface Transition extends DomElement {

	String NAME = "name";
	String TO = "to";

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (NAME)
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the to child.
	 * @return the value of the to child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (TO)
	@Required
	@Convert(BpNavigableElementConverter.class)
	GenericAttributeValue<String> getTo();


}
