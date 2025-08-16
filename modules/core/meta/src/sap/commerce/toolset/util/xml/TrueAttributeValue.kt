package sap.commerce.toolset.util.xml

import com.intellij.util.xml.Convert
import com.intellij.util.xml.GenericAttributeValue
import org.jetbrains.annotations.NotNull
import sap.commerce.toolset.util.xml.converter.TrueBooleanConverter

interface TrueAttributeValue : GenericAttributeValue<Boolean> {

    @NotNull
    @Convert(TrueBooleanConverter::class)
    override fun getValue(): Boolean
}

fun TrueAttributeValue.toBoolean() = stringValue?.toBoolean() ?: true