// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/fulltextsearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/fulltextsearch:fulltext-searchElemType interface.
 */
public interface FulltextSearch extends DomElement {

    /**
     * Returns the value of the field-list child.
     *
     * @return the value of the field-list child.
     */
    @NotNull
    @SubTag("field-list")
    @Required
    FieldList getFieldList();


    /**
     * Returns the value of the preferred-search-strategy child.
     *
     * @return the value of the preferred-search-strategy child.
     */
    @NotNull
    @SubTag("preferred-search-strategy")
    GenericDomValue<String> getPreferredSearchStrategy();


    /**
     * Returns the value of the operator child.
     *
     * @return the value of the operator child.
     */
    @NotNull
    @SubTag("operator")
    GenericDomValue<Operator> getOperator();


}
