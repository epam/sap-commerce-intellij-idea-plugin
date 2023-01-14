// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/dashboard

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:grid interface.
 */
public interface Grid extends DomElement {

    /**
     * Returns the value of the minScreenWidth child.
     *
     * @return the value of the minScreenWidth child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("minScreenWidth")
    GenericAttributeValue<Integer> getMinScreenWidth();


    /**
     * Returns the value of the maxScreenWidth child.
     *
     * @return the value of the maxScreenWidth child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("maxScreenWidth")
    GenericAttributeValue<Integer> getMaxScreenWidth();


    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the unassigned child.
     *
     * @return the value of the unassigned child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("unassigned")
    GenericAttributeValue<UnassignedBehavior> getUnassigned();


    /**
     * Returns the value of the rowHeight child.
     *
     * @return the value of the rowHeight child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("rowHeight")
    GenericAttributeValue<Integer> getRowHeight();


    /**
     * Returns the value of the fluid child.
     *
     * @return the value of the fluid child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("fluid")
    GenericAttributeValue<Boolean> getFluid();


    /**
     * Returns the list of placement children.
     *
     * @return the list of placement children.
     */
    @NotNull
    @SubTagList("placement")
    java.util.List<Placement> getPlacements();

    /**
     * Adds new child to the list of placement children.
     *
     * @return created child
     */
    @SubTagList("placement")
    Placement addPlacement();


}
