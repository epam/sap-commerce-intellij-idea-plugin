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

import com.intellij.idea.plugin.hybris.system.businessProcess.util.xml.BpNavigableElementConverter;
import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.de/xsd/processdefinition:processElemType interface.
 */
@Stubbed
@StubbedOccurrence
public interface Process extends DomElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("name")
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the start child.
	 * @return the value of the start child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("start")
	@Required
	@Convert(BpNavigableElementConverter.class)
	GenericAttributeValue<String> getStart();


	/**
	 * Returns the value of the onError child.
	 * @return the value of the onError child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("onError")
	GenericAttributeValue<String> getOnError();


	/**
	 * Returns the value of the processClass child.
	 * @return the value of the processClass child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("processClass")
	GenericAttributeValue<String> getProcessClass();


	/**
	 * Returns the value of the defaultNodeGroup child.
	 * @return the value of the defaultNodeGroup child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("defaultNodeGroup")
	GenericAttributeValue<String> getDefaultNodeGroup();


	/**
	 * Returns the list of contextParameter children.
	 * @return the list of contextParameter children.
	 */
	@NotNull
	@SubTagList ("contextParameter")
	java.util.List<ContextParameter> getContextParameters();
	/**
	 * Adds new child to the list of contextParameter children.
	 * @return created child
	 */
	@SubTagList ("contextParameter")
	ContextParameter addContextParameter();


	/**
	 * Returns the list of action children.
	 * @return the list of action children.
	 */
	@NotNull
	@SubTagList ("action")
	java.util.List<Action> getActions();
	/**
	 * Adds new child to the list of action children.
	 * @return created child
	 */
	@SubTagList ("action")
	Action addAction();


	/**
	 * Returns the list of scriptAction children.
	 * @return the list of scriptAction children.
	 */
	@NotNull
	@SubTagList ("scriptAction")
	java.util.List<ScriptAction> getScriptActions();
	/**
	 * Adds new child to the list of scriptAction children.
	 * @return created child
	 */
	@SubTagList ("scriptAction")
	ScriptAction addScriptAction();


	/**
	 * Returns the list of split children.
	 * @return the list of split children.
	 */
	@NotNull
	@SubTagList ("split")
	java.util.List<Split> getSplits();
	/**
	 * Adds new child to the list of split children.
	 * @return created child
	 */
	@SubTagList ("split")
	Split addSplit();


	/**
	 * Returns the list of wait children.
	 * @return the list of wait children.
	 */
	@NotNull
	@SubTagList ("wait")
	java.util.List<Wait> getWaits();
	/**
	 * Adds new child to the list of wait children.
	 * @return created child
	 */
	@SubTagList ("wait")
	Wait addWait();


	/**
	 * Returns the list of end children.
	 * @return the list of end children.
	 */
	@NotNull
	@SubTagList ("end")
	java.util.List<End> getEnds();
	/**
	 * Adds new child to the list of end children.
	 * @return created child
	 */
	@SubTagList ("end")
	End addEnd();


	/**
	 * Returns the list of join children.
	 * @return the list of join children.
	 */
	@NotNull
	@SubTagList ("join")
	java.util.List<Join> getJoins();
	/**
	 * Adds new child to the list of join children.
	 * @return created child
	 */
	@SubTagList ("join")
	Join addJoin();


	/**
	 * Returns the list of notify children.
	 * @return the list of notify children.
	 */
	@NotNull
	@SubTagList ("notify")
	java.util.List<Notify> getNotifies();
	/**
	 * Adds new child to the list of notify children.
	 * @return created child
	 */
	@SubTagList ("notify")
	Notify addNotify();


}
