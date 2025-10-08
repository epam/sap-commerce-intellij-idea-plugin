/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.debugger

internal object DebuggerConstants {

    private val QUALIFIER = toString("getQualifier()")
    private val UID = toString("getUid()")
    private val ID = toString("getId()")
    private val IDENTIFIER = toString("getIdentifier()")
    private val CODE = toString("getCode()")
    private val DESCRIPTION = toString("getDescription()")
    private val NAME = toString("getName()")

    val TO_STRING_MAPPING = mapOf(
        "de.hybris.platform.core.model.type.ComposedTypeModel" to CODE,
        "de.hybris.platform.core.model.AbstractDynamicContentModel" to CODE,
        "de.hybris.platform.core.model.flexiblesearch.SavedQueryModel" to CODE,
        "de.hybris.platform.processengine.model.BusinessProcessModel" to CODE,
        "de.hybris.platform.cronjob.model.CronJobModel" to CODE,
        "de.hybris.platform.cronjob.model.JobModel" to CODE,
        "de.hybris.platform.workflow.model.AbstractWorkflowActionModel" to CODE,
        "de.hybris.platform.workflow.model.WorkflowItemAttachmentModel" to CODE,
        "de.hybris.platform.core.model.order.delivery.DeliveryModeModel" to CODE,
        "de.hybris.platform.core.model.order.payment.PaymentModeModel" to CODE,
        "de.hybris.platform.deliveryzone.model.ZoneModel" to CODE,
        "de.hybris.platform.processing.model.AbstractRetentionRuleModel" to CODE,
        "de.hybris.platform.workflow.model.AbstractWorkflowDecisionModel" to CODE,
        "de.hybris.platform.comments.model.CommentTypeModel" to CODE,
        "de.hybris.platform.cms2.model.ComponentTypeGroupModel" to CODE,
        "de.hybris.platform.core.model.user.TitleModel" to CODE,
        "de.hybris.platform.core.model.order.price.TaxModel" to CODE,
        "com.hybris.backoffice.model.ThemeModel" to CODE,
        "de.hybris.platform.comments.model.ComponentModel" to CODE,
        "de.hybris.platform.core.model.type.TypeModel" to CODE,
        "de.hybris.platform.integrationservices.model.IntegrationObjectItemModel" to CODE,
        "de.hybris.platform.ticket.model.CsTicketEventEmailConfigurationModel" to CODE,
        "de.hybris.platform.commons.model.renderer.RendererTemplateModel" to CODE,
        "de.hybris.platform.core.model.type.SearchRestrictionModel" to CODE,
        "de.hybris.platform.ordersplitting.model.VendorModel" to CODE,
        "de.hybris.platform.core.model.product.UnitModel" to CODE,
        "de.hybris.platform.comments.model.DomainModel" to CODE,
        "de.hybris.platform.core.model.security.UserRightModel" to CODE,

        "de.hybris.platform.core.model.security.PrincipalModel" to UID,
        "de.hybris.platform.basecommerce.model.site.BaseSiteModel" to UID,
        "de.hybris.platform.store.BaseStoreModel" to UID,
        "de.hybris.platform.core.model.user.AbstractUserAuditModel" to UID,

        "de.hybris.platform.apiregistryservices.model.events.EventConfigurationModel" to DESCRIPTION,
        "de.hybris.platform.solrfacetsearch.model.config.SolrSearchConfigModel" to DESCRIPTION,

        "de.hybris.platform.validation.model.constraints.AbstractConstraintModel" to ID,
        "de.hybris.platform.validation.model.constraints.ConstraintGroupModel" to ID,
        "de.hybris.platform.catalog.model.CatalogModel" to ID,
        "de.hybris.platform.apiregistryservices.model.DestinationTargetModel" to ID,
        "de.hybris.platform.apiregistryservices.model.AbstractDestinationModel" to ID,
        "de.hybris.platform.apiregistryservices.model.EndpointModel" to ID,

        "de.hybris.platform.core.model.media.MediaContainerModel" to QUALIFIER,
        "de.hybris.platform.core.model.media.MediaFormatModel" to QUALIFIER,
        "de.hybris.platform.core.model.media.MediaContextModel" to QUALIFIER,
        "de.hybris.platform.core.model.media.MediaFolderModel" to QUALIFIER,

        "de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel" to IDENTIFIER,
        "de.hybris.platform.promotions.model.PromotionGroupModel" to IDENTIFIER,

        "de.hybris.platform.solrfacetsearch.model.config.SolrServerConfigModel" to NAME,
        "de.hybris.platform.solrfacetsearch.model.config.SolrIndexConfigModel" to NAME,

        "de.hybris.platform.cms2.model.contents.CMSItemModel" to CMS_ITEM,
        "de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel" to SOLR_INDEXED_PROPERTY,
        "de.hybris.platform.core.model.link.LinkModel" to LINK,
        "de.hybris.platform.core.model.product.ProductModel" to PRODUCT,
        "de.hybris.platform.core.model.order.AbstractOrderModel" to ABSTRACT_ORDER,
        "de.hybris.platform.core.model.order.AbstractOrderEntryModel" to ABSTRACT_ORDER_ENTRY,
        "de.hybris.platform.core.model.media.MediaModel" to MEDIA,
        "de.hybris.platform.core.model.type.AttributeDescriptorModel" to ATTRIBUTE_DESCRIPTOR,
        "de.hybris.platform.catalog.model.CatalogVersionModel" to CATALOG_VERSION,

        "de.hybris.platform.solrfacetsearch.model.config.SolrEndpointUrlModel" to toString("getUrl()"),
        "de.hybris.platform.apiregistryservices.model.events.EventPropertyConfigurationModel" to toString("getPropertyName()"),
        "de.hybris.platform.core.model.c2l.C2LItemModel" to toString("getIsocode()"),
        "de.hybris.platform.core.model.web.StoredHttpSessionModel" to toString("getSessionId()"),
        "de.hybris.platform.core.model.enumeration.EnumerationValueModel" to toString("getItemtype()", "getCode()"),
        "de.hybris.platform.cmsfacades.model.CMSItemTypeAttributeFilterConfigModel" to toString("getTypeCode()", "getMode()"),
        "de.hybris.platform.basecommerce.model.externaltax.ProductTaxCodeModel" to toString("getTaxArea()", "getProductCode()", "getTaxCode()"),
        "de.hybris.platform.integrationservices.model.AbstractIntegrationObjectItemAttributeModel" to toString("getAttributeName()"),
        "de.hybris.platform.cronjob.model.CronJobHistoryModel" to toString("getCronJobCode()", "getUserUid()"),
        "de.hybris.platform.cronjob.model.TriggerModel" to toString("getCronExpression()"),
    )

    private const val PK = """
        de.hybris.platform.core.PK _pk = getPk();
        String pk = "(<unsaved>)"
        
        if (_pk != null) {
            pk = _pk + " | rev: " + getPersistenceContext().getPersistenceVersion()
        }
    """

    const val ITEM = """
        $PK
        pk
    """

    private const val ATTRIBUTE_DESCRIPTOR = """
        $PK
        String qualifier = getQualifier() == null ? "?" : getQualifier();
        String enclosingType = getEnclosingType() == null ? "?" : getEnclosingType().getCode();
        
        pk + " | " + enclosingType + " | " + qualifier
    """
    private const val PRODUCT = """
        $PK
        String code = getCode() == null ? "?" : getCode();
    
        if (getCatalogVersion() == null) return pk + " | " + code;
    
        String catalogId = getCatalogVersion().getCatalog() == null ? "?" : getCatalogVersion().getCatalog().getId();
        String version = getCatalogVersion().getVersion() == null ? "?" : getCatalogVersion().getVersion();
        
        pk + " | " + code + " | " + catalogId + " ("+ version + ")"
    """
    private const val CATALOG_VERSION = """
        $PK
        String catalogId = getCatalog() == null ? "?" : getCatalog().getId();
        String version = getVersion() == null ? "?" : getVersion();
    
        pk + " | " + catalogId + " ("+ version + ")"
    """
    private const val ABSTRACT_ORDER = """
        $PK
        String code = getCode() == null ? "?" : getCode();
        String userUid = getUser() == null ? "?" : getUser().getUid();

        pk + " | " + code + " | " + userUid
    """
    private const val ABSTRACT_ORDER_ENTRY = """
        $PK
        Long quantity = getQuantity() == null ? "?" : getQuantity();
        de.hybris.platform.core.model.product.ProductModel product = getProduct()

        if (product == null) return pk + " | " + quantity + " ?";

        String productCode = product.getCode() == null ? "?" : product.getCode();
        String catalogId = product.getCatalogVersion().getCatalog() == null ? "?" : product.getCatalogVersion().getCatalog().getId();
        String version = product.getCatalogVersion().getVersion() == null ? "?" : product.getCatalogVersion().getVersion();
        
        pk + " | " + quantity + " " + productCode + " | " + catalogId + " ("+ version + ")"
    """
    private const val MEDIA = """
        $PK
        String code = getCode() == null ? "?" : getCode();

        if (getCatalogVersion() == null) return pk + " | " + code;

        String catalogId = getCatalogVersion().getCatalog() == null ? "?" : getCatalogVersion().getCatalog().getId();
        String version = getCatalogVersion().getVersion() == null ? "?" : getCatalogVersion().getVersion();

        pk + " | " + code + " | " + catalogId + " ("+ version + ")"
    """
    private const val CMS_ITEM = """
        $PK
        String uid = getUid() == null ? "?" : getUid();

        if (getCatalogVersion() == null) return pk + " | " + uid;

        String catalogId = getCatalogVersion().getCatalog() == null ? "?" : getCatalogVersion().getCatalog().getId();
        String version = getCatalogVersion().getVersion() == null ? "?" : getCatalogVersion().getVersion();

        pk + " | " + uid + " | " + catalogId + " ("+ version + ")"
    """
    private const val SOLR_INDEXED_PROPERTY = """
        $PK
        String name = getName() == null ? "?" : getName();
        String solrIndexedType = getSolrIndexedType() == null ? "?" : getSolrIndexedType().getIdentifier();
        String type = getType() == null ? "?" : getType().getCode();

        pk + " | " + name + " | " + solrIndexedType + " | " + type
    """
    private const val LINK = """
        $PK
        String source = getSource() == null ? "?" : getSource().toString();
        String target = getTarget() == null ? "?" : getTarget().toString();

        pk + " | " + source + " <-> " + target
    """

    private fun toString(vararg getters: String) = """
        $PK
        ${getters.mapIndexed { index, getter -> "String fieldValue$index = $getter == null ? \"?\" : $getter;" }.joinToString("\n")}

        pk + " | " + ${(0..<getters.size).joinToString(" + \" | \" + ") { "fieldValue$it" }}
    """
}