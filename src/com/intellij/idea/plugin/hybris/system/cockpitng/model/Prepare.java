// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:PrepareType interface.
 */
public interface Prepare extends DomElement {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the handler child.
     *
     * @return the value of the handler child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("handler")
    GenericAttributeValue<String> getHandler();


    /**
     * Returns the list of initialize children.
     *
     * @return the list of initialize children.
     */
    @NotNull
    @SubTagList("initialize")
    java.util.List<Initialize> getInitializes();

    /**
     * Adds new child to the list of initialize children.
     *
     * @return created child
     */
    @SubTagList("initialize")
    Initialize addInitialize();


    /**
     * Returns the list of assign children.
     *
     * @return the list of assign children.
     */
    @NotNull
    @SubTagList("assign")
    java.util.List<Assign> getAssigns();

    /**
     * Adds new child to the list of assign children.
     *
     * @return created child
     */
    @SubTagList("assign")
    Assign addAssign();


}
