// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationReference interface.
 */
public interface NotificationReference extends DomElement {

    /**
     * Returns the value of the index child.
     *
     * @return the value of the index child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("index")
    GenericAttributeValue<Integer> getIndex();


    /**
     * Returns the value of the placeholder child.
     *
     * @return the value of the placeholder child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("placeholder")
    @Required
    GenericAttributeValue<Integer> getPlaceholder();


    /**
     * Returns the value of the link child.
     *
     * @return the value of the link child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("link")
    GenericAttributeValue<String> getLink();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the message child.
     *
     * @return the value of the message child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("message")
    GenericAttributeValue<String> getMessage();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


    /**
     * Returns the list of context children.
     *
     * @return the list of context children.
     */
    @NotNull
    @SubTagList("context")
    java.util.List<NotificationLinkReferenceContext> getContexts();

    /**
     * Adds new child to the list of context children.
     *
     * @return created child
     */
    @SubTagList("context")
    NotificationLinkReferenceContext addContext();


}
