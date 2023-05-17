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

package com.intellij.idea.plugin.hybris.system.businessProcess.model;

import com.intellij.idea.plugin.hybris.util.xml.SpringBeanReferenceConverter;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.de/xsd/processdefinition:action interface.
 */
public interface Action extends NavigableElement {

	String CAN_JOIN_PREVIOUS_NODE = "canJoinPreviousNode";
	String NODE_GROUP = "nodeGroup";
	String NODE = "node";
	String BEAN = "bean";

	/**
	 * Returns the value of the bean child.
	 * @return the value of the bean child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (BEAN)
	@Required
	@Referencing(SpringBeanReferenceConverter.class)
	GenericAttributeValue<String> getBean();


	/**
	 * Returns the value of the node child.
	 * @return the value of the node child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (NODE)
	GenericAttributeValue<Integer> getNode();


	/**
	 * Returns the value of the nodeGroup child.
	 * @return the value of the nodeGroup child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (NODE_GROUP)
	GenericAttributeValue<String> getNodeGroup();


	/**
	 * Returns the value of the canJoinPreviousNode child.
	 * @return the value of the canJoinPreviousNode child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (CAN_JOIN_PREVIOUS_NODE)
	GenericAttributeValue<Boolean> getCanJoinPreviousNode();


	/**
	 * Returns the list of parameter children.
	 * @return the list of parameter children.
	 */
	@NotNull
	@SubTagList ("parameter")
	java.util.List<Parameter> getParameters();
	/**
	 * Adds new child to the list of parameter children.
	 * @return created child
	 */
	@SubTagList ("parameter")
	Parameter addParameter();


	/**
	 * Returns the list of transition children.
	 * @return the list of transition children.
	 */
	@NotNull
	@SubTagList ("transition")
	@Required
	java.util.List<Transition> getTransitions();
	/**
	 * Adds new child to the list of transition children.
	 * @return created child
	 */
	@SubTagList ("transition")
	Transition addTransition();


}
