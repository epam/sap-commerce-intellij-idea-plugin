/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.impex.lang.documentation

import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.lang.documentation.renderer.impexDoc
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.createSmartPointer

class ImpexDocumentationTarget(val element: PsiElement, private val originalElement: PsiElement?) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        val originalElementPtr = originalElement?.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            ImpexDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }

    override fun computePresentation(): TargetPresentation {
        val virtualFile = element.containingFile.virtualFile
        return TargetPresentation.builder(element.text)
            .locationText(virtualFile.name, virtualFile.fileType.icon)
            .presentation();
    }

    override fun computeDocumentationHint() = computeLocalDocumentation(element)
    override fun computeDocumentation() = computeLocalDocumentation(element)
        ?.let { DocumentationResult.documentation(it) }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    private fun computeLocalDocumentation(element: PsiElement) = when (element.elementType) {
        ImpexTypes.ATTRIBUTE_NAME -> {
            when (element.text) {

                AttributeModifier.ALIAS.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Export only")
                    allowedValues(
                        "A text such as myAttribute",
                        "Default: no alias is set"
                    )
                    content("With that modifier you can specify an alias name for an attribute, which is used in export case as attribute text in destination file.")
                    example("[alias=myAttribute]")
                }.build()

                AttributeModifier.ALLOW_NULL.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Import only")
                    booleanAllowedValues(false)
                    content(
                        "If set to true, this modifier explicitly allows null values for the column values.",
                        "If there is no business code that blocks null values, this modifier even allows null values in mandatory attributes, such as the catalogVersion attribute of the Media type, for example."
                    )
                    example("[allownull=true]")
                    tip(
                        "In the Service Layer mode, import may fail if allownull is set.",
                        "Import will switch dynamically to the legacy mode if it encounters this parameter.",
                        "After processing a given line, the import will switch back to the SL mode."
                    )
                }.build()

                AttributeModifier.CELL_DECORATOR.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Import only")
                    allowedValues(
                        "An AbstractImpExCSVCellDecorator class",
                        "Default: no decorator is applied."
                    )
                    content(
                        "Specifies a decorator class for modifying the cell value before interpreting.",
                        "See Impex API document, section Writing Own Cell Decorator for more information."
                    )
                    example("[cellDecorator=de.hybris.platform.catalog.jalo.classification.eclass.EClassSuperCategoryDecorator]")
                }.build()

                AttributeModifier.COLLECTION_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to attributes of type Collection.")
                    allowedValues(
                        "Any character",
                        "Default: comma ( , )"
                    )
                    content(
                        "Allows specifying a delimiter for separating values in a Collection.",
                        "For more information about Collections, see Type System Documentation."
                    )
                    example("INSERT_UPDATE Product;collectionAttribute[collection-delimiter = #];1#2#3#4;")
                }.build()

                AttributeModifier.DATE_FORMAT.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to Date and StandardDateRange types.")
                    allowedValues(
                        "A date format such as MM-dd-yyyy or dd.MM.yyyy",
                        "(See the Date and Time Patterns in Java API documentation)",
                        "Default: DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM, impexReader.getLocale())"
                    )
                    content("Specifies the format in which the column specifies Date values.")
                    example("UPDATE myProduct;myAttribute [dateformat=dd-MM-yyyy] ;17-01-1972")
                    content(
                        "A more complex example would be to implement a ISO 8601 compliant combined date and time representation.",
                        "The DateFormat class requires a syntax such as yyyy-MM-dd'T'HH:mm:ss.SSSZ.",
                        "The T in the date format representation must be delimited by single quotes by convention.",
                        "For use with ImpEx, the single quotes have to be escaped by doubling."
                    )
                    example("UPDATE myProduct;myAttribute[dateformat='yyyy-MM-dd''T''HH:mm:ss.SSSZ'] ;\"2009-04-03T04:14:00.235-0700\"")
                }.build()

                AttributeModifier.DEFAULT.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    allowedValues("A value that matches the current attribute column type.")
                    content("Sets the default for values of this column")
                    example("INSERT_UPDATE myProduct;myAttribute[default=MyName]")
                }.build()

                AttributeModifier.FORCE_WRITE.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Import only")
                    booleanAllowedValues(false)
                    content(
                        "If set, it tries to write into read-only columns.",
                        "Success depends on business logic of attribute setter.",
                        "If not set, writing of read-only attributes is not to be tried.",
                    )
                    example("[forceWrite=true]")
                    tip(
                        "In the Service Layer mode, import may fail if forceWrite is set.",
                        "Import will switch dynamically to the legacy mode if it encounters this parameter.",
                        "After processing a given line, the import will switch back to the SL mode.",
                    )
                }.build()

                AttributeModifier.IGNORE_KEY_CASE.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header(
                        "Import only",
                        "Restricted to attributes of type Item"
                    )
                    booleanAllowedValues(false)
                    content("If set, the case of the text that specifies attributes of items for resolving is ignored")

                }.build()

                AttributeModifier.IGNORE_NULL.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header(
                        "Import only",
                        "Restricted to attributes of type Item"
                    )
                    booleanAllowedValues(false)
                    content("For Collection-type attribute values, this allows ignoring of null values.")
                    list(
                        "If set to false as by default, a null value in a Collection-related column causes the null value to be written to the Collection instance. For example, if the value line contained the value myValue;null;myValue2, then setting the ignorenull modifier to false would cause the Collection to contain the null value, as in myValue;null;myValue2.",
                        "If set to true, a null value in a Collection-related column causes the null value to be skipped for the Collection instance. For example, if the value line contained the value myValue;null;myValue2, then setting the ignorenull modifier to true would cause the Collection not to contain the null value, as in myValue;myValue2."
                    )
                    example("INSERT_UPDATE myProduct;myAttribute[ignoreNull=true]")
                }.build()

                AttributeModifier.KEY_2_VALUE_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to attributes of type Map.")
                    allowedValues(
                        "any character",
                        "Default: '->'"
                    )
                    content("Specifies the assignment delimiter for a key-value pair.")
                    example("INSERT_UPDATE myProduct;myAttribute[key2value-delimiter=>>] ;myKey>>myValue;")
                }.build()

                AttributeModifier.LANG.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to localized attributes (Map<String, Language>)")
                    allowedValues(
                        "The ISO code or PK of a language defined for the SAP Commerce (such as de, en, or de_ch, for example)",
                        "Default: session language"
                    )
                    content("Specifies the language for a column containing localized attribute values.")
                    example("INSERT_UPDATE myProduct;myAttribute[lang=de]")
                }.build()

                AttributeModifier.MAP_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to attributes of type Map.")
                    allowedValues(
                        "Any character",
                        "Default: ';'"
                    )
                    content("Specifies the delimiter between two key-value pairs.")
                    example(
                        """
                        INSERT_UPDATE myProduct;myAttribute[map-delimiter=|] 
                        ;myKey->myValue|myKey2->myValue2;
                    """.trimIndent()
                    )
                }.build()

                AttributeModifier.MODE.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    externalLink(
                        "Header and Attribute Modifier",
                        "https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html#header-and-attribute-modifier"
                    )
                    header(
                        "Import only",
                        "Restricted to attributes of type Collection"
                    )
                    allowedValues(
                        "append, remove, merge",
                        "Default: replace (not explicitly specifiable) - an already existing Collection will be replaced"
                    )
                    content("Specifies the modification mode for Collection instances.")
                    list(
                        "mode=append: In the append mode, references to elements of the Collection value are added to the existing resolved Collection. You can also use (+) instead of mode=append.",
                        "mode=remove: In the remove mode, references to elements of the Collection value are removed from the resolved Collection. You can also use (-) instead of mode=remove",
                        "mode=merge: In the merge mode, references to elements of the Collection value are added to the existing resolved Collection only if they already aren't part of the Collection. You can also use (+?) instead of mode=merge",
                    )
                }.build()

                AttributeModifier.NUMBER_FORMAT.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to attributes of type Number")
                    allowedValues(
                        "A number format like #.###,## (For further information, refer to the NumberFormat class documentation by Sun Microsystems)",
                        "Default: NumberFormat.getNumberInstance( impexReader.getLocale() )"
                    )
                    content(
                        "Specifies the format the column uses to specify Number values.",
                        "Be aware that the delimiters for number values depend on the locale settings specified for the session, and you cannot specify the delimiters using the numberformat modifier only.",
                        "If you need to specify the delimiters explicitly, you have to set a locale for the reader instance explicitly that is related to the delimiters using the BeanShell code."
                    )
                    example("#% impex.setLocale( Locale.GERMAN );")
                    content("The following code snippet shows two settings for the numberformat parameter (typical for German-and English-related locales, respectively).")
                    example(
                        """
                        INSERT_UPDATE myProduct;myAttribute[numberformat=#.###,##] 
                        ;1.299,99 
                        INSERT_UPDATE myProduct;myAttribute[numberformat=#,###.##] 
                        ;1,299.99
                    """.trimIndent()
                    )
                }.build()

                AttributeModifier.PATH_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    header("Restricted to attribute of type ComposedType")
                    allowedValues(
                        "Any character",
                        "Default: ':'"
                    )
                    content("Defines the delimiter to separate values for attributes using an item expression, such as catalog versions, for example.")
                    example(
                        """
                        INSERT_UPDATE myProduct;myAttribute(code,id)[path-delimiter=:] 
                        ;myCode:myID
                    """.trimIndent()
                    )
                }.build()

                AttributeModifier.POS.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    allowedValues(
                        "A positive integer",
                        "Default: column number as positioned in header."
                    )
                    content(
                        "Specifies the column position where the values for this attribute can be found within the following value lines.",
                        "Attention: if this modifier is used for an attribute in a header description, it has to be used for all attributes in that header to avoid conflicts."
                    )
                    example("[pos=3]")
                }.build()

                AttributeModifier.TRANSLATOR.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    allowedValues(
                        "An AbstractValueTranslator or a SpecialValueTranslator",
                        "Default varies; each Type has a predefined default Translator."
                    )
                    content(
                        "A Translator class resolves a column of a value line into an attribute value for the item in the SAP Commerce.",
                        "The ImpEx extension processes all instances of out-of-the-box AtomicType and ComposedType instances, such as Java.lang.String and Product respectively, for example, into attribute values using the default Translator classes."
                    )
                    example("INSERT_UPDATE myProduct;myAttribute[translator=de.hybris.platform.impex.jalo.translators.ItemPKTranslator]")
                }.build()

                AttributeModifier.UNIQUE.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    booleanAllowedValues(false)
                    content(
                        "Marks this column as a key attribute - that is, the value is used to determine whether the item is unique or not.",
                        "ImpEx uses the combination of the values of all key attributes to check whether the item already exists.",
                        "This means that in the case of unique=true, each combination of the values of all key attributes in a row must be unique.",
                        "Each row must provide a unique set of values for them (since there may be multiple key attributes), which are used to check whether or not an item already exists."
                    )
                    example("INSERT_UPDATE myProduct;myAttribute[unique=true]")
                    list(
                        "If this modifier is set to false as by default, an insert is always performed, because there is no item with the same unique modifiers.",
                        "If set to true, the column contains key attribute values and an insert is only performed if no item with the same key attributes exist. Otherwise the existing item will be updated by the non-unique values."
                    )
                }.build()

                AttributeModifier.VIRTUAL.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    booleanAllowedValues(false)
                    content("If set, a related column within the following value lines is expected. This has to be used in conjunction with default modifier.")
                    example(
                        """
                        INSERT MyProduct;myAttribute1[virtual=true,default=myValue1];myAttribute2[unique=true] 
                        ;myValue2;
                    """.trimIndent()
                    )
                    content(
                        "Sets myValue1 to attribute myAttribute1 and myValue2 to attribute myAttribute2.",
                        "Be aware that the virtual column has no dedicated field in the value line.",
                        "This could lead to shiftings in the value line. For a better readability, it's advisable to add the virtual attributes at the end of the header line."
                    )
                }.build()

                else -> null
            }
        }

        else -> null
    }
}