// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:action-group interface.
 */
public interface ActionGroup extends DomElement, Positioned {

    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the show-group-header child.
     *
     * @return the value of the show-group-header child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("show-group-header")
    GenericAttributeValue<Boolean> getShowGroupHeader();


    /**
     * Returns the value of the show-separator child.
     *
     * @return the value of the show-separator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("show-separator")
    GenericAttributeValue<Boolean> getShowSeparator();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the list of label children.
     *
     * @return the list of label children.
     */
    @NotNull
    @SubTagList("label")
    java.util.List<GenericDomValue<String>> getLabels();

    /**
     * Adds new child to the list of label children.
     *
     * @return created child
     */
    @SubTagList("label")
    GenericDomValue<String> addLabel();


    /**
     * Returns the list of action children.
     *
     * @return the list of action children.
     */
    @NotNull
    @SubTagList("action")
    java.util.List<Action> getActions();

    /**
     * Adds new child to the list of action children.
     *
     * @return created child
     */
    @SubTagList("action")
    Action addAction();


}
