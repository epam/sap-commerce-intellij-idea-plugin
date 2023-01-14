// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:action interface.
 */
public interface Action extends DomElement, Positioned {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the action-id child.
     *
     * @return the value of the action-id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("action-id")
    GenericAttributeValue<String> getActionId();


    /**
     * Returns the value of the property child.
     *
     * @return the value of the property child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("property")
    GenericAttributeValue<String> getProperty();


    /**
     * Returns the value of the output-property child.
     *
     * @return the value of the output-property child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("output-property")
    GenericAttributeValue<String> getOutputProperty();


    /**
     * Returns the value of the triggerOnKeys child.
     *
     * @return the value of the triggerOnKeys child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("triggerOnKeys")
    GenericAttributeValue<String> getTriggerOnKeys();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();

    /**
     * Returns the list of parameter children.
     *
     * @return the list of parameter children.
     */
    @NotNull
    @SubTagList("parameter")
    java.util.List<Parameter> getParameters();

    /**
     * Adds new child to the list of parameter children.
     *
     * @return created child
     */
    @SubTagList("parameter")
    Parameter addParameter();


}
