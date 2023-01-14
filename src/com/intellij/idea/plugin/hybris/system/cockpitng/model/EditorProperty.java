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
 * http://www.hybris.com/cockpit/config/hybris:editor-property interface.
 */
public interface EditorProperty extends DomElement {

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
     * Returns the value of the readOnly child.
     *
     * @return the value of the readOnly child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("readOnly")
    GenericAttributeValue<Boolean> getReadOnly();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the editor child.
     *
     * @return the value of the editor child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("editor")
    GenericAttributeValue<String> getEditor();


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
    GenericDomValue<String> getLabel();


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
