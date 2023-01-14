// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/valuechooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/valuechooser:option interface.
 */
public interface Option extends DomElement, Positioned {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    @Required
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the selected child.
     *
     * @return the value of the selected child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("selected")
    GenericAttributeValue<Boolean> getSelected();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();

    /**
     * Returns the list of value children.
     *
     * @return the list of value children.
     */
    @NotNull
    @SubTagList("value")
    @Required
    java.util.List<Value> getValues();

    /**
     * Adds new child to the list of value children.
     *
     * @return created child
     */
    @SubTagList("value")
    Value addValue();


}
