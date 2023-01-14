// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/treeCollection

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/treeCollection:tree-node interface.
 */
public interface TreeNode extends DomElement {

    /**
     * Returns the value of the attribute child.
     *
     * @return the value of the attribute child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("attribute")
    @Required
    GenericAttributeValue<String> getAttribute();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


}
