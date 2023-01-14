// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/summaryview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/summaryview:actions interface.
 */
public interface Actions extends DomElement {

    /**
     * Returns the value of the component-id child.
     *
     * @return the value of the component-id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("component-id")
    @Required
    GenericAttributeValue<String> getComponentId();


    /**
     * Returns the value of the group child.
     *
     * @return the value of the group child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("group")
    GenericAttributeValue<String> getGroup();


    /**
     * Returns the value of the renderer child.
     *
     * @return the value of the renderer child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("renderer")
    GenericAttributeValue<String> getRenderer();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the list of extended-group children.
     *
     * @return the list of extended-group children.
     */
    @NotNull
    @SubTagList("extended-group")
    java.util.List<ActionGroupExtended> getExtendedGroups();

    /**
     * Adds new child to the list of extended-group children.
     *
     * @return created child
     */
    @SubTagList("extended-group")
    ActionGroupExtended addExtendedGroup();


    /**
     * Returns the list of split-group children.
     *
     * @return the list of split-group children.
     */
    @NotNull
    @SubTagList("split-group")
    java.util.List<ActionGroupSplit> getSplitGroups();

    /**
     * Adds new child to the list of split-group children.
     *
     * @return created child
     */
    @SubTagList("split-group")
    ActionGroupSplit addSplitGroup();


    /**
     * Returns the list of three-dots-group children.
     *
     * @return the list of three-dots-group children.
     */
    @NotNull
    @SubTagList("three-dots-group")
    java.util.List<ActionGroupThreeDots> getThreeDotsGroups();

    /**
     * Adds new child to the list of three-dots-group children.
     *
     * @return created child
     */
    @SubTagList("three-dots-group")
    ActionGroupThreeDots addThreeDotsGroup();


    /**
     * Returns the list of group children.
     *
     * @return the list of group children.
     */
    @NotNull
    @SubTagList("group")
    java.util.List<ActionGroup> getGroups();

    /**
     * Adds new child to the list of group children.
     *
     * @return created child
     */
    @SubTagList("group")
    ActionGroup addGroup();


}
