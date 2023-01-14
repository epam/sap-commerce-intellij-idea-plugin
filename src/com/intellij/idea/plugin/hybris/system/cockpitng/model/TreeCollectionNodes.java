// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/treeCollection

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/treeCollection:tree-collection-nodesElemType interface.
 */
public interface TreeCollectionNodes extends DomElement {

    /**
     * Returns the value of the skip-only-attribute child.
     *
     * @return the value of the skip-only-attribute child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("skip-only-attribute")
    GenericAttributeValue<Boolean> getSkipOnlyAttribute();


    /**
     * Returns the list of node children.
     *
     * @return the list of node children.
     */
    @NotNull
    @SubTagList("node")
    java.util.List<TreeNode> getNodes();

    /**
     * Adds new child to the list of node children.
     *
     * @return created child
     */
    @SubTagList("node")
    TreeNode addNode();


}
