// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:editor-group interface.
 */
public interface EditorGroup extends DomElement {

    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    @Required
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the visible child.
     *
     * @return the value of the visible child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("visible")
    GenericAttributeValue<Boolean> getVisible();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @SubTag("label")
    @Required
    GenericDomValue<String> getLabel();


    /**
     * Returns the list of property children.
     *
     * @return the list of property children.
     */
    @NotNull
    @SubTagList("property")
    @Required
    java.util.List<EditorProperty> getProperties();

    /**
     * Adds new child to the list of property children.
     *
     * @return created child
     */
    @SubTagList("property")
    EditorProperty addProperty();


}
