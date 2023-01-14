// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/compareview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/compareview:section interface.
 */
public interface Section extends DomElement, AbstractSection {

    /**
     * Returns the value of the tooltipText child.
     *
     * @return the value of the tooltipText child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("tooltipText")
    GenericAttributeValue<String> getTooltipText();


    /**
     * Returns the value of the renderer child.
     *
     * @return the value of the renderer child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("renderer")
    GenericAttributeValue<String> getRenderer();


    /**
     * Returns the value of the initiallyOpened child.
     *
     * @return the value of the initiallyOpened child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("initiallyOpened")
    GenericAttributeValue<Boolean> getInitiallyOpenedAttr();


    /**
     * Returns the list of attribute children.
     *
     * @return the list of attribute children.
     */
    @NotNull
    @SubTagList("attribute")
    java.util.List<Attribute> getAttributes();

    /**
     * Adds new child to the list of attribute children.
     *
     * @return created child
     */
    @SubTagList("attribute")
    Attribute addAttribute();


    /**
     * Returns the value of the columns child.
     *
     * @return the value of the columns child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("columns")
    GenericAttributeValue<String> getColumns();


    /**
     * Returns the list of customPanel children.
     *
     * @return the list of customPanel children.
     */
    @NotNull
    @SubTagList("customPanel")
    java.util.List<CustomPanel> getCustomPanels();

    /**
     * Adds new child to the list of customPanel children.
     *
     * @return created child
     */
    @SubTagList("customPanel")
    CustomPanel addCustomPanel();


    /**
     * Returns the list of panel children.
     *
     * @return the list of panel children.
     */
    @NotNull
    @SubTagList("panel")
    java.util.List<Panel> getPanels();

    /**
     * Adds new child to the list of panel children.
     *
     * @return created child
     */
    @SubTagList("panel")
    Panel addPanel();


    /**
     * Returns the list of custom children.
     * <pre>
     * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:custom documentation</h3>
     * Allows to insert custom html into section. Html code may contain
     * 									SpEL expressions regarding edited object - SpEL expressions need to be in curly
     * 									braces
     * </pre>
     *
     * @return the list of custom children.
     */
    @NotNull
    @SubTagList("custom")
    java.util.List<CustomElement> getCustoms();

    /**
     * Adds new child to the list of custom children.
     *
     * @return created child
     */
    @SubTagList("custom")
    CustomElement addCustom();


    /**
     * Returns the list of custom-attribute children.
     *
     * @return the list of custom-attribute children.
     */
    @NotNull
    @SubTagList("custom-attribute")
    java.util.List<CustomAttribute> getCustomAttributes();

    /**
     * Adds new child to the list of custom-attribute children.
     *
     * @return created child
     */
    @SubTagList("custom-attribute")
    CustomAttribute addCustomAttribute();


    /**
     * Returns the list of actions children.
     *
     * @return the list of actions children.
     */
    @NotNull
    @SubTagList("actions")
    java.util.List<Actions> getActionses();

    /**
     * Adds new child to the list of actions children.
     *
     * @return created child
     */
    @SubTagList("actions")
    Actions addActions();


    /**
     * Returns the list of data-quality-group children.
     *
     * @return the list of data-quality-group children.
     */
    @NotNull
    @SubTagList("data-quality-group")
    java.util.List<DataQualityGroup> getDataQualityGroups();

    /**
     * Adds new child to the list of data-quality-group children.
     *
     * @return created child
     */
    @SubTagList("data-quality-group")
    DataQualityGroup addDataQualityGroup();


}
