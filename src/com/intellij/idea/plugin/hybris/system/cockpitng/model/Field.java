// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/fulltextsearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/fulltextsearch:FieldType interface.
 */
public interface Field extends DomElement, Positioned {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getNameAttr();


    /**
     * Returns the value of the operator child.
     *
     * @return the value of the operator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("operator")
    GenericAttributeValue<String> getOperator();


    /**
     * Returns the value of the selected child.
     *
     * @return the value of the selected child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("selected")
    GenericAttributeValue<Boolean> getSelectedAttr();


    /**
     * Returns the value of the editor child.
     *
     * @return the value of the editor child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("editor")
    GenericAttributeValue<String> getEditorAttr();


    /**
     * Returns the value of the sortable child.
     *
     * @return the value of the sortable child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("sortable")
    GenericAttributeValue<Boolean> getSortable();


    /**
     * Returns the value of the disabled child.
     *
     * @return the value of the disabled child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disabled")
    GenericAttributeValue<Boolean> getDisabled();


    /**
     * Returns the value of the mandatory child.
     *
     * @return the value of the mandatory child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("mandatory")
    GenericAttributeValue<Boolean> getMandatory();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeModeAttr();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPositionAttr();


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


    /**
     * Returns the value of the displayName child.
     *
     * @return the value of the displayName child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("displayName")
    GenericAttributeValue<String> getDisplayName();


    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the selected child.
     *
     * @return the value of the selected child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("selected")
    GenericAttributeValue<Boolean> getSelected();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    GenericAttributeValue<String> getLabel();


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


}
