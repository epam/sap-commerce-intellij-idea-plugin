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

import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.businessProcess.util.xml.BpNavigableElementConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.de/xsd/processdefinition:processElemType interface.
 */
@Stubbed
@StubbedOccurrence
@Namespace(HybrisConstants.SCHEMA_BUSINESS_PROCESS)
public interface Process extends DomElement {

	String DEFAULT_NODE_GROUP = "defaultNodeGroup";
	String PROCESS_CLASS = "processClass";
	String END = "end";
	String NAME = "name";
	String START = "start";
	String ON_ERROR = "onError";
	String ACTION = "action";
	String SCRIPT_ACTION = "scriptAction";
	String SPLIT = "split";
	String WAIT = "wait";

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (NAME)
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the start child.
	 * @return the value of the start child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (START)
	@Required
	@Convert(BpNavigableElementConverter.class)
	GenericAttributeValue<String> getStart();


	/**
	 * Returns the value of the onError child.
	 * @return the value of the onError child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (ON_ERROR)
	@Convert(BpNavigableElementConverter.class)
	GenericAttributeValue<String> getOnError();


	/**
	 * Returns the value of the processClass child.
	 * @return the value of the processClass child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (PROCESS_CLASS)
	GenericAttributeValue<String> getProcessClass();


	/**
	 * Returns the value of the defaultNodeGroup child.
	 * @return the value of the defaultNodeGroup child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (DEFAULT_NODE_GROUP)
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
	@SubTagList (ACTION)
	java.util.List<Action> getActions();
	/**
	 * Adds new child to the list of action children.
	 * @return created child
	 */
	@SubTagList (ACTION)
	Action addAction();


	/**
	 * Returns the list of scriptAction children.
	 * @return the list of scriptAction children.
	 */
	@NotNull
	@SubTagList (SCRIPT_ACTION)
	java.util.List<ScriptAction> getScriptActions();
	/**
	 * Adds new child to the list of scriptAction children.
	 * @return created child
	 */
	@SubTagList (SCRIPT_ACTION)
	ScriptAction addScriptAction();


	/**
	 * Returns the list of split children.
	 * @return the list of split children.
	 */
	@NotNull
	@SubTagList (SPLIT)
	java.util.List<Split> getSplits();
	/**
	 * Adds new child to the list of split children.
	 * @return created child
	 */
	@SubTagList (SPLIT)
	Split addSplit();


	/**
	 * Returns the list of wait children.
	 * @return the list of wait children.
	 */
	@NotNull
	@SubTagList (WAIT)
	java.util.List<Wait> getWaits();
	/**
	 * Adds new child to the list of wait children.
	 * @return created child
	 */
	@SubTagList (WAIT)
	Wait addWait();


	/**
	 * Returns the list of end children.
	 * @return the list of end children.
	 */
	@NotNull
	@SubTagList (END)
	java.util.List<End> getEnds();
	/**
	 * Adds new child to the list of end children.
	 * @return created child
	 */
	@SubTagList (END)
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
