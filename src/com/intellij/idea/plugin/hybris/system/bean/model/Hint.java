// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.MutableGenericValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface Hint extends DomElement, MutableGenericValue<String> {

    String NAME = "name";

    @NotNull
    @Attribute(NAME)
    @Required
    GenericAttributeValue<String> getName();

}
