/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.impex.constants.modifier.AttributeModifier
import com.intellij.idea.plugin.hybris.impex.constants.modifier.TypeModifier
import com.intellij.idea.plugin.hybris.impex.lang.documentation.renderer.impexDoc
import com.intellij.idea.plugin.hybris.impex.psi.ImpexTypes
import com.intellij.idea.plugin.hybris.impex.psi.references.ImpExHeaderAbbreviationReference
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.model.Pointer
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely

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
            .presentation()
    }

    override fun computeDocumentationHint() = computeLocalDocumentation(element)
    override fun computeDocumentation() = computeLocalDocumentation(element)
        ?.let { DocumentationResult.documentation(it) }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    private fun computeLocalDocumentation(element: PsiElement) = when (element.elementType) {
        ImpexTypes.HEADER_TYPE,
        ImpexTypes.VALUE_SUBTYPE -> {
            try {
                TSMetaModelAccess.getInstance(element.project).findMetaClassifierByName(element.text)
                    ?.documentation()
            } catch (_: ProcessCanceledException) {
                message("hybris.documentation.not.available.during.indexing")
            }
        }

        ImpexTypes.ATTRIBUTE_NAME -> {
            when (element.text) {
                AttributeModifier.EXPR.modifierName -> impexDoc {
                    typeModifier(element.text)
                    subHeader("VelocityTranslator")
                    externalLink(
                        "Special Value Translators",
                        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/4ce7b82cbc574456ac197393f88e5cc6.html?locale=en-US#special-value-translators"
                    )
                    texts(
                        "The VelocityTranslator is used only for export. It can be used for exporting a value for an item using a velocity expression.",
                        "With that, you can export a constant value or can aggregate different attributes.",
                        "Using the following header at an export of an order item, the code of the order is exported as well as the id and name of the user owning the order and its payment address, street name, and country.",
                    )
                    example("""
                        INSERT_UPDATE Order; code[unique=true]; \
                                            @template1[translator=de.hybris.jakarta.ext.impex.jalo.translators.VelocityTranslator, expr='${"$"}item.user.getUID()']; \
                                            @template2[translator=de.hybris.jakarta.ext.impex.jalo.translators.VelocityTranslator, expr='${"$"}item.user.name'];
                    """.trimIndent())
                }.build()

                AttributeModifier.CLASSIFICATION_CLASS.modifierName,
                AttributeModifier.SYSTEM.modifierName,
                AttributeModifier.VERSION.modifierName -> impexDoc {
                    typeModifier(element.text)
                    subHeader("ClassificationAttributeTranslator")
                    externalLink(
                        "Special Value Translators",
                        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/4ce7b82cbc574456ac197393f88e5cc6.html?locale=en-US#special-value-translators"
                    )
                    texts(
                        "Instead of importing each product feature one by one you can assign all features of a product with one value line using this translator.",
                        "Therefore you have to declare a special attribute for each feature to import.",
                        "Assuming you want to set a value for feature type at your product a suitable header could be as follows:",
                    )
                    example("""
                        UPDATE Product; code[unique=true]; @type[system='SampleClassification',version='1.0',translator=de.hybris.platform.catalog.jalo.classification.impex.ClassificationAttributeTranslator;]
                    """.trimIndent())
                    texts("In this example, the modifiers system and version, which are both mandatory, specify the classification system version of the product feature.")
                    texts("One more optional modifier class can be used with this translator.")
                }.build()

                TypeModifier.DISABLE_UNIQUE_ATTRIBUTES_VALIDATOR_FOR_TYPES.modifierName -> impexDoc {
                    typeModifier(element.text)
                    externalLink(
                        "Disable Interceptors Programmatically",
                        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex"
                    )
                    texts(
                        "disable.UniqueAttributesValidator.for.types (InterceptorExecutionPolicy#DISABLED_UNIQUE_ATTRIBUTE_VALIDATOR_FOR_ITEM_TYPES).",
                        "This attribute takes a set of item types for which you want to disable UniqueAttributesValidator.",
                        "To disable UniqueAttributesValidator, specify a comma-separated item types for which you want to disable it:",
                        "The following impex specifies only one such interceptor:"
                    )
                    example("""
                        INSERT_UPDATE Currency[disable.UniqueAttributesValidator.for.types='Currency'];isocode[unique=true];digits;
                        ;EUR_Test;-2;
                    """.trimIndent())
                    texts("If you want to specify more than one ID, put the bean IDs in apostrophes: <'beanID'>.")
                }.build()

                TypeModifier.DISABLE_INTERCEPTOR_BEANS.modifierName -> impexDoc {
                    typeModifier(element.text)
                    externalLink(
                        "Disable Interceptors Programmatically",
                        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex"
                    )
                    texts(
                        "disable.interceptor.beans (InterceptorExecutionPolicy#DISABLED_INTERCEPTOR_BEANS constant).",
                        "This attribute takes a set of Spring bean IDs.",
                        "To disable specific interceptors, specify a comma-separated bean-IDs list for the disable.interceptor.beans header attribute.",
                        "The following impex specifies only one such interceptor:"
                    )
                    example("""
                        INSERT_UPDATE Currency[disable.interceptor.beans='validateCurrencyDataInterceptor'];isocode[unique=true];digits;
                        ;EUR_Test;-2;
                    """.trimIndent())
                }.build()

                TypeModifier.DISABLE_INTERCEPTOR_TYPES.modifierName -> impexDoc {
                    typeModifier(element.text)
                    externalLink(
                        "Disable Interceptors Programmatically",
                        "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/9ce1b60e12714a7dba6ea7e66b4f7acd.html?locale=en-US#disable-interceptors-via-impex"
                    )
                    texts(
                        "disable.interceptor.types (InterceptorExecutionPolicy#DISABLED_INTERCEPTOR_TYPES constant)",
                        "This attribute takes a set of interceptor types (de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy.InterceptorType) that you want to disable."
                    )
                    example("""
                        INSERT_UPDATE Currency[disable.interceptor.types=validate];isocode[unique=true];digits;
                        ;EUR_Test2;-2;
                    """.trimIndent())
                }.build()

                TypeModifier.SLD_ENABLED.modifierName -> impexDoc {
                    typeModifier(element.text)
                    externalLink(
                        "Using ServiceLayer Direct",
                        "https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/ccf4dd14636b4f7eac2416846ffd5a70.html?locale=en-US"
                    )
                    texts("Setting the modifier to sld.enabled=true means that both titles are to be imported using the SLD mode, even if the global switch, or the flag in ImportConfig is set to the legacy mode.")
                    example("[sld.enabled=true]")
                }.build()

                TypeModifier.BATCH_MODE.modifierName -> impexDoc {
                    typeModifier(element.text)
                    subHeader("Import only")
                    booleanAllowedValues(false)
                    texts(
                        "In UPDATE or REMOVE mode, the batch mode allows modifying more than one item that matches for a combination of all unique attributes of a value line.",
                        "So, if for a value line more than one item is found that matches the combination of unique attributes, the attributes specified as non-unique are updated at all found items."
                    )
                    example("[batchmode=true]")
                    list(
                        "If the batch mode is not enabled (as by default), the hybris ImpEx extension throws an exception if more than one item matches for a value line.",
                        "If the batch mode is enabled, the hybris ImpEx extension modifies any platform item that matches the value line."
                    )
                }.build()

                TypeModifier.CACHE_UNIQUE.modifierName -> impexDoc {
                    typeModifier(element.text)
                    subHeader("Import only")
                    booleanAllowedValues(false)
                    texts(
                        "If this option is enabled, the CachingExistingItemResolver is used for existing item resolving (in case of update or remove mode) which caches by the combination of unique keys already resolved items.",
                        "So when processing a value line first, it is tried to find the related item by searching the cache using the unique keys.",
                        "The usage is only meaningful if an item has to be processed more than one time within a header scope.",
                        "The maximum size of the cache is not restricted at the moment."
                    )
                    example("[cacheUnique=true]")
                }.build()

                TypeModifier.PROCESSOR.modifierName -> impexDoc {
                    typeModifier(element.text)
                    subHeader("Import only")
                    allowedValues(
                        "A ImportProcessor class",
                        "Default is the DefaultImportProcessor"
                    )
                    texts(
                        "Unlike a Translator, which handles a certain column of a value line, a Processor handles an entire value line.",
                        "It contains all business logic to transform a value line into values for attributes of an item in SAP Commerce (such as calls for translator classes, for example) and performs the real setting of the values.",
                        "In other words: a Processor is passed a value line as input, and it creates or updates an item in SAP Commerce as its output."
                    )
                    example("[processor=de.hybris.platform.impex.jalo.imp.DefaultImportProcessor]")
                    tip("SAP Commerce doesn't support the Processor modifier in Distributed ImpEx.")
                }.build()

                TypeModifier.IMPEX_LEGACY_MODE.modifierName -> impexDoc {
                    typeModifier(element.text)
                    subHeader("(since SAP Commerce version 5.1.1)")
                    booleanAllowedValues(false)
                    texts(
                        "This modifier allows enabling the legacy mode (jalo) per header.",
                        "This way - in case of service layer mode enabled globally, ImpEx will switch dynamically to legacy mode, just like for existing 'allowNull' or 'forceWrite' modifiers.",
                        "The only difference is - it's set for the type, not for the column."
                    )
                    example("INSERT_UPDATE myProduct[impex.legacy.mode=true];myAttribute")
                }.build()

                AttributeModifier.ALIAS.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Export only")
                    allowedValues(
                        "A text such as myAttribute",
                        "Default: no alias is set"
                    )
                    texts("With that modifier you can specify an alias name for an attribute, which is used in export case as attribute text in destination file.")
                    example("[alias=myAttribute]")
                }.build()

                AttributeModifier.ALLOW_NULL.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Import only")
                    booleanAllowedValues(false)
                    texts(
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
                    subHeader("Import only")
                    allowedValues(
                        "An AbstractImpExCSVCellDecorator class",
                        "Default: no decorator is applied."
                    )
                    texts(
                        "Specifies a decorator class for modifying the cell value before interpreting.",
                        "See Impex API document, section Writing Own Cell Decorator for more information."
                    )
                    example("[cellDecorator=de.hybris.platform.catalog.jalo.classification.eclass.EClassSuperCategoryDecorator]")
                }.build()

                AttributeModifier.COLLECTION_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Restricted to attributes of type Collection.")
                    allowedValues(
                        "Any character",
                        "Default: comma ( , )"
                    )
                    texts(
                        "Allows specifying a delimiter for separating values in a Collection.",
                        "For more information about Collections, see Type System Documentation."
                    )
                    example("INSERT_UPDATE Product;collectionAttribute[collection-delimiter = #];1#2#3#4;")
                }.build()

                AttributeModifier.DATE_FORMAT.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Restricted to Date and StandardDateRange types.")
                    allowedValues(
                        "A date format such as MM-dd-yyyy or dd.MM.yyyy",
                        "(See the Date and Time Patterns in Java API documentation)",
                        "Default: DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM, impexReader.getLocale())"
                    )
                    texts("Specifies the format in which the column specifies Date values.")
                    example("UPDATE myProduct;myAttribute [dateformat=dd-MM-yyyy] ;17-01-1972")
                    texts(
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
                    texts("Sets the default for values of this column")
                    example("INSERT_UPDATE myProduct;myAttribute[default=MyName]")
                }.build()

                AttributeModifier.FORCE_WRITE.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Import only")
                    booleanAllowedValues(false)
                    texts(
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
                    subHeader(
                        "Import only",
                        "Restricted to attributes of type Item"
                    )
                    booleanAllowedValues(false)
                    texts("If set, the case of the text that specifies attributes of items for resolving is ignored")

                }.build()

                AttributeModifier.IGNORE_NULL.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader(
                        "Import only",
                        "Restricted to attributes of type Item"
                    )
                    booleanAllowedValues(false)
                    texts("For Collection-type attribute values, this allows ignoring of null values.")
                    list(
                        "If set to false as by default, a null value in a Collection-related column causes the null value to be written to the Collection instance. For example, if the value line contained the value myValue;null;myValue2, then setting the ignorenull modifier to false would cause the Collection to contain the null value, as in myValue;null;myValue2.",
                        "If set to true, a null value in a Collection-related column causes the null value to be skipped for the Collection instance. For example, if the value line contained the value myValue;null;myValue2, then setting the ignorenull modifier to true would cause the Collection not to contain the null value, as in myValue;myValue2."
                    )
                    example("INSERT_UPDATE myProduct;myAttribute[ignoreNull=true]")
                }.build()

                AttributeModifier.KEY_2_VALUE_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Restricted to attributes of type Map.")
                    allowedValues(
                        "any character",
                        "Default: '->'"
                    )
                    texts("Specifies the assignment delimiter for a key-value pair.")
                    example("INSERT_UPDATE myProduct;myAttribute[key2value-delimiter=>>] ;myKey>>myValue;")
                }.build()

                AttributeModifier.LANG.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Restricted to localized attributes (Map<String, Language>)")
                    allowedValues(
                        "The ISO code or PK of a language defined for the SAP Commerce (such as de, en, or de_ch, for example)",
                        "Default: session language"
                    )
                    texts("Specifies the language for a column containing localized attribute values.")
                    example("INSERT_UPDATE myProduct;myAttribute[lang=de]")
                }.build()

                AttributeModifier.MAP_DELIMITER.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Restricted to attributes of type Map.")
                    allowedValues(
                        "Any character",
                        "Default: ';'"
                    )
                    texts("Specifies the delimiter between two key-value pairs.")
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
                    subHeader(
                        "Import only",
                        "Restricted to attributes of type Collection"
                    )
                    allowedValues(
                        "append, remove, merge",
                        "Default: replace (not explicitly specifiable) - an already existing Collection will be replaced"
                    )
                    texts("Specifies the modification mode for Collection instances.")
                    list(
                        "mode=append: In the append mode, references to elements of the Collection value are added to the existing resolved Collection.<br>You can also use <strong>(+)</strong instead of mode=append.",
                        "mode=remove: In the remove mode, references to elements of the Collection value are removed from the resolved Collection.<br>You can also use <strong>(-)</strong? instead of mode=remove",
                        "mode=merge: In the merge mode, references to elements of the Collection value are added to the existing resolved Collection only if they already aren't part of the Collection.<br>You can also use <strong>(+?)</strong> instead of mode=merge",
                    )
                }.build()

                AttributeModifier.NUMBER_FORMAT.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    subHeader("Restricted to attributes of type Number")
                    allowedValues(
                        "A number format like #.###,## (For further information, refer to the NumberFormat class documentation by Sun Microsystems)",
                        "Default: NumberFormat.getNumberInstance( impexReader.getLocale() )"
                    )
                    texts(
                        "Specifies the format the column uses to specify Number values.",
                        "Be aware that the delimiters for number values depend on the locale settings specified for the session, and you cannot specify the delimiters using the numberformat modifier only.",
                        "If you need to specify the delimiters explicitly, you have to set a locale for the reader instance explicitly that is related to the delimiters using the BeanShell code."
                    )
                    example("#% impex.setLocale( Locale.GERMAN );")
                    texts("The following code snippet shows two settings for the numberformat parameter (typical for German-and English-related locales, respectively).")
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
                    subHeader("Restricted to attribute of type ComposedType")
                    allowedValues(
                        "Any character",
                        "Default: ':'"
                    )
                    texts("Defines the delimiter to separate values for attributes using an item expression, such as catalog versions, for example.")
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
                    texts(
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
                    texts(
                        "A Translator class resolves a column of a value line into an attribute value for the item in the SAP Commerce.",
                        "The ImpEx extension processes all instances of out-of-the-box AtomicType and ComposedType instances, such as Java.lang.String and Product respectively, for example, into attribute values using the default Translator classes."
                    )
                    example("INSERT_UPDATE myProduct;myAttribute[translator=de.hybris.platform.impex.jalo.translators.ItemPKTranslator]")
                }.build()

                AttributeModifier.UNIQUE.modifierName -> impexDoc {
                    attributeModifier(element.text)
                    booleanAllowedValues(false)
                    texts(
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
                    texts("If set, a related column within the following value lines is expected. This has to be used in conjunction with default modifier.")
                    example(
                        """
                        INSERT MyProduct;myAttribute1[virtual=true,default=myValue1];myAttribute2[unique=true] 
                        ;myValue2;
                    """.trimIndent()
                    )
                    texts(
                        "Sets myValue1 to attribute myAttribute1 and myValue2 to attribute myAttribute2.",
                        "Be aware that the virtual column has no dedicated field in the value line.",
                        "This could lead to shiftings in the value line. For a better readability, it's advisable to add the virtual attributes at the end of the header line."
                    )
                }.build()

                else -> null
            }
        }

        ImpexTypes.HEADER_PARAMETER_NAME -> {
            element.parent.reference
                ?.asSafely<ImpExHeaderAbbreviationReference>()
                ?.let {
                    impexDoc {
                        headerAbbreviation(element.text)
                        externalLink(
                            "Using Header Abbreviations",
                            "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/2fb5a2a780c94325b4a48ff62b36ab23.html#using-header-abbreviations"
                        )
                        texts(
                            "ImpEx provides a way to shorten length column declarations by using regexp patterns and replacements.",
                            "Although the ImpEx header definition language provides a most flexible way of using custom column translators, their declaration can grow long.",
                            "You can shorten them using the ImpEx alias syntax.",
                            "The following example shows how to use this syntax to shorten classification columns declaration by giving them a new syntax.",
                            "Put this into your SAP Commerce Cloud platform local.properties file:"
                        )
                        example("impex.header.replacement.1 = C@(\\\\w+) ... @$1[system='\\\\${'$'}systemName', version='\\\\${'$'}systemVersion', translator='de...ClassificationAttributeTranslator']")
                        texts(
                            "Note the Java string notation has to be used, that's why there are double '\\'s.",
                            "All parameters starting with impex.header.replacement are parsed as ImpEx column replacement rules.",
                            "The parameter has to end with a number that defines the priority of the rule. This way ambiguous rules can be sorted.",
                            "So what's this for? The first part of the property C@(\\\\w+) defines the new abbreviation pattern to be used to declare classification attribute columns with.",
                            "The second part is the replacement text including the attribute qualifier match group ${'$'}1.",
                            "In fact, it contains the original special column declaration. Both parts are to be separated by '...'."
                        )
                    }.build()
                }

        }

        else -> null
    }
}