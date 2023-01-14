// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/test

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/test:property interface.
 */
public interface Property extends DomElement {

    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @SubTag(value = "qualifier", index = 0)
    GenericDomValue<String> getQualifier1();


    /**
     * Returns the value of the visible child.
     *
     * @return the value of the visible child.
     */
    @NotNull
    @SubTag("visible")
    GenericDomValue<Boolean> getVisible();


    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    @Required
    GenericAttributeValue<String> getQualifier2();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description")
    GenericAttributeValue<String> getDescription();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the validate child.
     *
     * @return the value of the validate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("validate")
    GenericAttributeValue<Boolean> getValidate();


    /**
     * Returns the value of the readonly child.
     *
     * @return the value of the readonly child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("readonly")
    GenericAttributeValue<Boolean> getReadonly();


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
     * Returns the value of the editor child.
     *
     * @return the value of the editor child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("editor")
    GenericAttributeValue<String> getEditor();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the exclude child.
     *
     * @return the value of the exclude child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("exclude")
    GenericAttributeValue<Boolean> getExclude();


    /**
     * Returns the list of editor-parameter children.
     *
     * @return the list of editor-parameter children.
     */
    @NotNull
    @SubTagList("editor-parameter")
    java.util.List<Parameter> getEditorParameters();

    /**
     * Adds new child to the list of editor-parameter children.
     *
     * @return created child
     */
    @SubTagList("editor-parameter")
    Parameter addEditorParameter();


}
