// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/summaryview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/summaryview:customSection interface.
 */
public interface CustomSection extends DomElement, Section {

    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("class")
    GenericAttributeValue<String> getClassAttr();


    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    GenericAttributeValue<String> getSpringBeanAttr();

    /**
     * Returns the list of render-parameter children.
     *
     * @return the list of render-parameter children.
     */
    @NotNull
    @SubTagList("render-parameter")
    java.util.List<Parameter> getRenderParameters();

    /**
     * Adds new child to the list of render-parameter children.
     *
     * @return created child
     */
    @SubTagList("render-parameter")
    Parameter addRenderParameter();

    /**
     * Returns the list of attribute children.
     *
     * @return the list of attribute children.
     */
    @Override
    @NotNull
    @SubTagList("attribute")
    java.util.List<Attribute> getAttributes();

    /**
     * Adds new child to the list of attribute children.
     *
     * @return created child
     */
    @Override
    @SubTagList("attribute")
    Attribute addAttribute();

    /**
     * Returns the list of customPanel children.
     *
     * @return the list of customPanel children.
     */
    @Override
    @NotNull
    @SubTagList("customPanel")
    java.util.List<CustomPanel> getCustomPanels();

    /**
     * Adds new child to the list of customPanel children.
     *
     * @return created child
     */
    @Override
    @SubTagList("customPanel")
    CustomPanel addCustomPanel();

    /**
     * Returns the list of panel children.
     *
     * @return the list of panel children.
     */
    @Override
    @NotNull
    @SubTagList("panel")
    java.util.List<Panel> getPanels();

    /**
     * Adds new child to the list of panel children.
     *
     * @return created child
     */
    @Override
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
    @Override
    @NotNull
    @SubTagList("custom")
    java.util.List<CustomElement> getCustoms();

    /**
     * Adds new child to the list of custom children.
     *
     * @return created child
     */
    @Override
    @SubTagList("custom")
    CustomElement addCustom();

    /**
     * Returns the list of custom-attribute children.
     *
     * @return the list of custom-attribute children.
     */
    @Override
    @NotNull
    @SubTagList("custom-attribute")
    java.util.List<CustomAttribute> getCustomAttributes();

    /**
     * Adds new child to the list of custom-attribute children.
     *
     * @return created child
     */
    @Override
    @SubTagList("custom-attribute")
    CustomAttribute addCustomAttribute();

    /**
     * Returns the list of actions children.
     *
     * @return the list of actions children.
     */
    @Override
    @NotNull
    @SubTagList("actions")
    java.util.List<Actions> getActionses();

    /**
     * Adds new child to the list of actions children.
     *
     * @return created child
     */
    @Override
    @SubTagList("actions")
    Actions addActions();

    /**
     * Returns the list of data-quality-group children.
     *
     * @return the list of data-quality-group children.
     */
    @Override
    @NotNull
    @SubTagList("data-quality-group")
    java.util.List<DataQualityGroup> getDataQualityGroups();

    /**
     * Adds new child to the list of data-quality-group children.
     *
     * @return created child
     */
    @Override
    @SubTagList("data-quality-group")
    DataQualityGroup addDataQualityGroup();

    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("class")
    GenericAttributeValue<String> getClazz();

    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    GenericAttributeValue<String> getSpringBean();

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @Override
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    GenericAttributeValue<String> getName();

    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @Override
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();

    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @Override
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


}
