// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/explorertree

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/explorertree:dynamic-node interface.
 */
public interface DynamicNode extends DomElement, ExplorerNode {

    /**
     * Returns the value of the populator-bean-id child.
     *
     * @return the value of the populator-bean-id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("populator-bean-id")
    GenericAttributeValue<String> getPopulatorBeanId();


    /**
     * Returns the value of the populator-class-name child.
     *
     * @return the value of the populator-class-name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("populator-class-name")
    GenericAttributeValue<String> getPopulatorClassName();


    /**
     * Returns the value of the indexing-depth child.
     *
     * @return the value of the indexing-depth child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("indexing-depth")
    GenericAttributeValue<Integer> getIndexingDepth();


    /**
     * Returns the value of the hide-root-node child.
     *
     * @return the value of the hide-root-node child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("hide-root-node")
    GenericAttributeValue<Boolean> getHideRootNode();


    /**
     * Returns the list of dynamic-node-parameter children.
     *
     * @return the list of dynamic-node-parameter children.
     */
    @NotNull
    @SubTagList("dynamic-node-parameter")
    java.util.List<Parameter> getDynamicNodeParameters();

    /**
     * Adds new child to the list of dynamic-node-parameter children.
     *
     * @return created child
     */
    @SubTagList("dynamic-node-parameter")
    Parameter addDynamicNodeParameter();


}
