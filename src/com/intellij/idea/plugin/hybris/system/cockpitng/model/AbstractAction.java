// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:AbstractActionType interface.
 */
public interface AbstractAction extends DomElement {

    /**
     * Returns the value of the visible child.
     *
     * @return the value of the visible child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("visible")
    GenericAttributeValue<String> getVisible();


    /**
     * Returns the value of the default-target child.
     *
     * @return the value of the default-target child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("default-target")
    GenericAttributeValue<String> getDefaultTarget();


    /**
     * Returns the list of if children.
     *
     * @return the list of if children.
     */
    @NotNull
    @SubTagList("if")
    java.util.List<If> getIfs();

    /**
     * Adds new child to the list of if children.
     *
     * @return created child
     */
    @SubTagList("if")
    If addIf();


    /**
     * Returns the value of the save-all child.
     *
     * @return the value of the save-all child.
     */
    @NotNull
    @SubTag("save-all")
    @Required
    GenericDomValue<String> getSaveAll();


    /**
     * Returns the list of save children.
     *
     * @return the list of save children.
     */
    @NotNull
    @SubTagList("save")
    @Required
    java.util.List<Save> getSaves();

    /**
     * Adds new child to the list of save children.
     *
     * @return created child
     */
    @SubTagList("save")
    Save addSave();


}
