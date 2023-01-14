// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/explorertree

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/explorertree:explorer-node interface.
 */
public interface ExplorerNode extends DomElement, Positioned {

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
     * Returns the value of the expanded-by-default child.
     *
     * @return the value of the expanded-by-default child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("expanded-by-default")
    GenericAttributeValue<Boolean> getExpandedByDefault();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the list of navigation-node children.
     *
     * @return the list of navigation-node children.
     */
    @NotNull
    @SubTagList("navigation-node")
    java.util.List<NavigationNode> getNavigationNodes();

    /**
     * Adds new child to the list of navigation-node children.
     *
     * @return created child
     */
    @SubTagList("navigation-node")
    NavigationNode addNavigationNode();


    /**
     * Returns the list of type-node children.
     *
     * @return the list of type-node children.
     */
    @NotNull
    @SubTagList("type-node")
    java.util.List<TypeNode> getTypeNodes();

    /**
     * Adds new child to the list of type-node children.
     *
     * @return created child
     */
    @SubTagList("type-node")
    TypeNode addTypeNode();


    /**
     * Returns the list of dynamic-node children.
     *
     * @return the list of dynamic-node children.
     */
    @NotNull
    @SubTagList("dynamic-node")
    java.util.List<DynamicNode> getDynamicNodes();

    /**
     * Adds new child to the list of dynamic-node children.
     *
     * @return created child
     */
    @SubTagList("dynamic-node")
    DynamicNode addDynamicNode();


}
