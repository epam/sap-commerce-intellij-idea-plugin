// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:keywordsElemType interface.
 */
public interface Keywords extends DomElement {

    /**
     * Returns the list of keyword children.
     *
     * @return the list of keyword children.
     */
    @NotNull
    @SubTagList("keyword")
    @Required
    java.util.List<GenericDomValue<String>> getKeywords();

    /**
     * Adds new child to the list of keyword children.
     *
     * @return created child
     */
    @SubTagList("keyword")
    GenericDomValue<String> addKeyword();


}
