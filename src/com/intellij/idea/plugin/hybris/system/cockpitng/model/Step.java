// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:StepType interface.
 */
public interface Step extends DomElement {

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
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the sublabel child.
     *
     * @return the value of the sublabel child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("sublabel")
    GenericAttributeValue<String> getSublabel();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the hide-breadcrumb child.
     *
     * @return the value of the hide-breadcrumb child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("hide-breadcrumb")
    GenericAttributeValue<Boolean> getHideBreadcrumb();


    /**
     * Returns the value of the info child.
     *
     * @return the value of the info child.
     */
    @NotNull
    @SubTag("info")
    Info getInfo();


    /**
     * Returns the value of the content child.
     *
     * @return the value of the content child.
     */
    @NotNull
    @SubTag("content")
    Content getContent();


    /**
     * Returns the value of the navigation child.
     *
     * @return the value of the navigation child.
     */
    @NotNull
    @SubTag("navigation")
    Navigation getNavigation();


}
