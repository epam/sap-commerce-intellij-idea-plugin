// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:editorAreaElemType interface.
 */
public interface EditorArea extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the viewMode child.
     *
     * @return the value of the viewMode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("viewMode")
    GenericAttributeValue<String> getViewMode();


    /**
     * Returns the value of the hideTabNameIfOnlyOneVisible child.
     *
     * @return the value of the hideTabNameIfOnlyOneVisible child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("hideTabNameIfOnlyOneVisible")
    GenericAttributeValue<Boolean> getHideTabNameIfOnlyOneVisible();


    /**
     * Returns the value of the logic-handler child.
     *
     * @return the value of the logic-handler child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("logic-handler")
    GenericAttributeValue<String> getLogicHandler();


    /**
     * Returns the value of the essentials child.
     *
     * @return the value of the essentials child.
     */
    @NotNull
    @SubTag("essentials")
    Essentials getEssentials();


    /**
     * Returns the list of customTab children.
     *
     * @return the list of customTab children.
     */
    @NotNull
    @SubTagList("customTab")
    java.util.List<CustomTab> getCustomTabs();

    /**
     * Adds new child to the list of customTab children.
     *
     * @return created child
     */
    @SubTagList("customTab")
    CustomTab addCustomTab();


    /**
     * Returns the list of tab children.
     *
     * @return the list of tab children.
     */
    @NotNull
    @SubTagList("tab")
    java.util.List<Tab> getTabs();

    /**
     * Adds new child to the list of tab children.
     *
     * @return created child
     */
    @SubTagList("tab")
    Tab addTab();


}
