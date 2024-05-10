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

package com.intellij.idea.plugin.hybris.common.utils

import com.intellij.icons.AllIcons
import com.intellij.icons.ExpUiIcons
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ReflectionUtil
import icons.GradleIcons
import icons.OpenapiIcons
import org.jetbrains.kotlin.idea.KotlinIcons

/**
 * Please follow Intellij Platform UI style and naming convention for icons.
 *
 * @see <a href="https://jetbrains.design/intellij/principles/icons/#style">styleguide</a>
 */
object HybrisIcons {

    val IMPEX_FILE = getIcon("/icons/fileTypes/impexFile.svg")
    val FXS_FILE = getIcon("/icons/fileTypes/flexibleSearchFileIcon.svg")
    val PGQ_FILE = getIcon("icons/fileTypes/polyglotQueryFile.svg")
    val BEAN_FILE = getIcon("/icons/beanSystem/bean.svg")

    val PLUGIN_SETTINGS = getIcon("/icons/pluginSettings.svg")

    val FLEXIBLE_SEARCH = getIcon("/icons/flexibleSearch.svg")

    val DECLARATION = getIcon("/icons/declaration.svg")

    val Y_LICENCE = getIcon("/icons/hybrisLicenceIcon.svg")
    val Y_LOGO_BLUE = getIcon("/icons/hybrisIcon.svg")
    val Y_LOGO_ORANGE = getIcon("/icons/hybrisIconOrange.svg")
    val Y_LOGO_GREEN = getIcon("/icons/hybrisIconGreen.svg")
    val Y_REMOTE = getIcon("/icons/hybrisRemote.svg")
    val Y_REMOTE_GREEN = getIcon("/icons/hybrisRemoteGreen.svg")
    val Y_FACET = Y_LOGO_GREEN
    val EXTENSION_INFO = getIcon("/icons/extensionInfo.svg")
    val COCKPIT_NG_CONFIG = getIcon("/icons/cockpitNG/config.svg")
    val COCKPIT_NG_WIDGETS = getIcon("/icons/cockpitNG/widgets.svg")
    val COCKPIT_NG_WIDGET = getIcon("/icons/cockpitNG/widget.svg")
    val COCKPIT_NG_WIDGET_DEFINITION = getIcon("/icons/cockpitNG/widgetDefinition.svg")
    val COCKPIT_NG_ACTION_DEFINITION = getIcon("/icons/cockpitNG/actionDefinition.svg")
    val COCKPIT_NG_EDITOR_DEFINITION = getIcon("/icons/cockpitNG/editorDefinition.svg")
    val COCKPIT_NG_INITIALIZE_PROPERTY = AllIcons.Nodes.PropertyWrite
    val LOCAL_EXTENSIONS = getIcon("/icons/extensionInfo.svg")
    val BUSINESS_PROCESS = getIcon("/icons/businessProcess.svg")
    val MONITORING = getIcon("/icons/monitoring.svg")
    val BUILD_CALLBACKS = AllIcons.Toolwindows.ToolWindowBuild
    val EXTERNAL_DEPENDENCIES = getIcon("/icons/externalDependencies.svg")
    val UNMANAGED_DEPENDENCIES = getIcon("/icons/unmanagedDependencies.svg")
    val SETTINGS = getIcon("/icons/settings.svg")

    val MODULE_ECLIPSE = AllIcons.Providers.Eclipse
    val MODULE_MAVEN = OpenapiIcons.RepositoryLibraryLogo
    val MODULE_CONFLICT = AllIcons.Actions.Cancel
    val MODULE_GRADLE = if (PluginCommon.isPluginActive(PluginCommon.PLUGIN_GRADLE)) GradleIcons.Gradle else AllIcons.Nodes.Module
    val MODULE_CCV2 = getIcon("/icons/module/cloud.svg")
    val MODULE_CCV2_GROUP = getIcon("/icons/module/cloudGroup.svg")
    val MODULE_COMMERCE_GROUP = Y_LOGO_GREEN
    val MODULE_PLATFORM_GROUP = Y_LOGO_ORANGE
    val MODULE_CUSTOM_GROUP = Y_LOGO_BLUE
    val MODULE_EXTERNAL_GROUP = AllIcons.Nodes.ModuleGroup

    val EXTENSION_CONFIG = AllIcons.Nodes.ConfigFolder
    val EXTENSION_CLOUD = getIcon("/icons/extension/cloud.svg")
    val EXTENSION_CUSTOM = getIcon("/icons/extension/custom.svg")
    val EXTENSION_PLATFORM = getIcon("/icons/extension/platform.svg")
    val EXTENSION_EXT = getIcon("/icons/extension/ext.svg")
    val EXTENSION_OOTB = getIcon("/icons/extension/ootb.svg")
    val EXTENSION_WEB = AllIcons.Nodes.WebFolder
    val EXTENSION_COMMON_WEB = AllIcons.Nodes.WebFolder
    val EXTENSION_ADDON = AllIcons.Nodes.WebFolder
    val EXTENSION_BACKOFFICE = AllIcons.Nodes.Folder
    val EXTENSION_HMC = AllIcons.Nodes.Folder
    val EXTENSION_HAC = AllIcons.Nodes.Folder
    val EXTENSION_KOTLIN_NATURE = if (PluginCommon.isPluginActive(PluginCommon.PLUGIN_KOTLIN)) KotlinIcons.SMALL_LOGO else AllIcons.Nodes.Module

    val TYPE_SYSTEM = getIcon("/icons/typeSystem.svg")
    val SPRING_BEAN = getIcon("icons/springBean.svg")
    val LOCALIZED = getIcon("/icons/localized.svg")

    val MACROS = getIcon("/icons/macros.svg")

    val INTERCEPTOR = getIcon("/icons/interceptor.svg")

    val JAVA_CLASS = AllIcons.Nodes.Class

    val TYPE_PRIMITIVE = getIcon("/icons/typePrimitive.svg")
    val TYPE_BOXED = getIcon("/icons/typeBoxed.svg")
    val TYPE_OBJECT = getIcon("/icons/typeObject.svg")
    val TYPE_COLLECTION = getIcon("/icons/typeCollection.svg")
    val TYPE_MAP = getIcon("/icons/typeMap.svg")
    val TYPE_GENERIC = AllIcons.Nodes.AbstractClass

    val TS_GROUP_ATOMIC = getIcon("/icons/typeSystem/groupByAtomic.svg")
    val TS_GROUP_ENUM = getIcon("/icons/typeSystem/groupByEnum.svg")
    val TS_GROUP_COLLECTION = getIcon("/icons/typeSystem/groupByCollection.svg")
    val TS_GROUP_ITEM = getIcon("/icons/typeSystem/groupByItem.svg")
    val TS_GROUP_MAP = getIcon("/icons/typeSystem/groupByMap.svg")
    val TS_GROUP_RELATION = getIcon("/icons/typeSystem/groupByRelation.svg")
    val TS_DESCRIPTION = AllIcons.Windows.Help
    val TS_ENUM = getIcon("/icons/typeSystem/enum.svg")
    val TS_ENUM_VALUE = getIcon("/icons/typeSystem/enumValue.svg")
    val TS_ATOMIC = getIcon("/icons/typeSystem/atomic.svg")
    val TS_ITEM = getIcon("/icons/typeSystem/item.svg")
    val TS_MAP = getIcon("/icons/typeSystem/map.svg")
    val TS_RELATION = getIcon("/icons/typeSystem/relation.svg")
    val TS_CUSTOM_PROPERTY = getIcon("/icons/typeSystem/customProperty.svg")
    val TS_ATTRIBUTE = getIcon("/icons/typeSystem/attribute.svg")
    val TS_ORDERING_ATTRIBUTE = getIcon("/icons/typeSystem/orderingAttribute.svg")
    val TS_RELATION_SOURCE = getIcon("/icons/typeSystem/relationSource.svg")
    val TS_RELATION_TARGET = getIcon("/icons/typeSystem/relationTarget.svg")
    val TS_COLLECTION = getIcon("/icons/typeSystem/collection.svg")
    val TS_INDEX = getIcon("/icons/typeSystem/index.svg")
    val TS_INDEX_UNIQUE = getIcon("/icons/typeSystem/indexUnique.svg")
    val TS_INDEX_REPLACE = getIcon("/icons/typeSystem/indexReplace.svg")
    val TS_INDEX_REMOVE = getIcon("/icons/typeSystem/indexRemove.svg")
    val TS_IMPORT = AllIcons.ToolbarDecorator.Import
    val TS_ANNOTATION = AllIcons.Nodes.Annotationtype
    val TS_HEADER_ABBREVIATION = getIcon("/icons/typeSystem/headerAbbreviation.svg")
    val TS_ALTERNATIVE_DECLARATION = AllIcons.Actions.Forward
    val TS_SIBLING = AllIcons.Gutter.OverridenMethod
    val TS_PREVIEW_SHOW = AllIcons.Actions.Show
    val TS_PREVIEW_SHOW_COLLECTIONS = AllIcons.Actions.GroupByPrefix
    val TS_PREVIEW_SHOW_ENUMS = AllIcons.Actions.GroupByTestProduction
    val TS_PREVIEW_SHOW_MAPS = AllIcons.Actions.GroupByPackage
    val TS_PREVIEW_SHOW_ITEMS = AllIcons.Actions.GroupByClass

    val BS_GROUP_BY_BEAN_DTO = getIcon("/icons/beanSystem/groupByDTO.svg")
    val BS_GROUP_BY_BEAN_EVENT = getIcon("/icons/beanSystem/groupByEvent.svg")
    val BS_GROUP_BY_BEAN_WS = getIcon("/icons/beanSystem/groupByWS.svg")
    val BS_GROUP_BY_ENUM = getIcon("/icons/beanSystem/groupByEnum.svg")
    val BS_BEAN = getIcon("/icons/beanSystem/bean.svg")
    val BS_EVENT_BEAN = getIcon("/icons/beanSystem/eventBean.svg")
    val BS_WS_BEAN = getIcon("/icons/beanSystem/wsBean.svg")
    val BS_WS_HINT = AllIcons.Actions.QuickfixOffBulb
    val BS_PROPERTY = getIcon("/icons/beanSystem/property.svg")
    val BS_ENUM = getIcon("/icons/beanSystem/enum.svg")
    val BS_ENUM_VALUE = getIcon("/icons/beanSystem/enumValue.svg")
    val BS_LEVEL_MAPPING = getIcon("/icons/beanSystem/levelMapping.svg")
    val BS_ALTERNATIVE_DECLARATION = AllIcons.Actions.Forward
    val BS_SIBLING = AllIcons.Gutter.OverridenMethod
    val BS_PREVIEW_SHOW = AllIcons.Actions.Show

    val CODE_NOT_GENERATED = AllIcons.General.ExclMark

    val DIAGRAM = AllIcons.FileTypes.Diagram
    val DIAGRAM_DIFF = AllIcons.Actions.DiagramDiff

    val BP_DIAGRAM_WAIT = getIcon("/icons/businessProcess/diagram/wait.svg")
    val BP_DIAGRAM_END = getIcon("/icons/businessProcess/diagram/end.svg")
    val BP_DIAGRAM_NOTIFY = getIcon("/icons/businessProcess/diagram/notify.svg")
    val BP_DIAGRAM_ACTION = getIcon("/icons/businessProcess/diagram/action.svg")
    val BP_DIAGRAM_SPLIT = getIcon("/icons/businessProcess/diagram/split.svg")
    val BP_DIAGRAM_JOIN = getIcon("/icons/businessProcess/diagram/join.svg")
    val BP_DIAGRAM_SCRIPT = getIcon("/icons/businessProcess/diagram/script.svg")
    val BP_DIAGRAM_PARAMETERS = AllIcons.Nodes.NewParameter
    val BP_DIAGRAM_PROPERTY = BS_PROPERTY
    val BP_DIAGRAM_SPRING_BEAN = SPRING_BEAN
    val BP_DIAGRAM_NODE = AllIcons.Nodes.FieldPK
    val BP_DIAGRAM_FIELD = AllIcons.Nodes.Field
    val BP_DIAGRAM_CLASS = AllIcons.Nodes.Class
    val BP_DIAGRAM_PARAMETER_REQUIRED = AllIcons.Nodes.Plugin
    val BP_DIAGRAM_PARAMETER_OPTIONAL = AllIcons.Nodes.Pluginobsolete
    val BP_DIAGRAM_PROPERTIES = AllIcons.Nodes.Property

    val MODULE_DEP_DIAGRAM_PROPERTY = BS_PROPERTY
    val MODULE_DEP_DIAGRAM_DESCRIPTION = AllIcons.Windows.Help
    val MODULE_DEP_DIAGRAM_MAVEN_ENABLED = MODULE_MAVEN
    val MODULE_DEP_DIAGRAM_DEPRECATED = AllIcons.General.ExclMark
    val MODULE_DEP_DIAGRAM_TEMPLATE = AllIcons.Nodes.Template
    val MODULE_DEP_DIAGRAM_JALO_LOGIC_FREE = getIcon("/icons/flexibleSearch/star.svg")

    val TS_DIAGRAM_PROPERTY = AllIcons.Nodes.Property
    val TS_DIAGRAM_DEPLOYMENT = AllIcons.Debugger.Db_db_object
    val TS_DIAGRAM_RESET_VIEW = getIcon("/icons/typeSystem/diagram/resetView.svg")
    val TS_DIAGRAM_SETTINGS = SETTINGS

    val FXS_TABLE_ALIAS = getIcon("/icons/flexibleSearch/tableAlias.svg")
    val FXS_COLUMN_ALIAS = getIcon("/icons/flexibleSearch/columnAlias.svg")
    val FXS_OUTER_JOIN = getIcon("/icons/flexibleSearch/outerJoin.svg")
    val FXS_Y_COLUMN_PLACEHOLDER = AllIcons.Actions.PrettyPrint
    val FXS_Y_FROM_PLACEHOLDER = AllIcons.Actions.PrettyPrint
    val FXS_FROM_PARENS_PLACEHOLDER = getIcon("/icons/flexibleSearch/parens.svg")
    val FXS_KEYWORD = AllIcons.Nodes.Static
    val FXS_Y_COLUMN_ALL = getIcon("/icons/flexibleSearch/star.svg")
    val FXS_TABLE_SUFFIX = AllIcons.General.Filter
    val FXS_TABLE_ALIAS_SEPARATOR = getIcon("/icons/flexibleSearch/separator.svg")

    val IMPEX_VALIDATE = getIcon("/icons/impex/validate.svg")
    val IMPEX_MODE = AllIcons.Nodes.Function

    val GUTTER_POPULATOR = getIcon("/icons/gutter/populator.svg")

    val CONSOLE = AllIcons.Debugger.Console
    val CONSOLE_SOLR = getIcon("/icons/console/solr.svg")
    val CONSOLE_OPEN = getIcon("/icons/console/open.svg")
    val CONSOLE_EXECUTE = AllIcons.Actions.Execute
    val CONSOLE_SUSPEND = AllIcons.Actions.Suspend
    val CONSOLE_EXECUTE_COMMIT_MODE_OFF = getIcon("/icons/console/executeWithCommitModeOff.svg")

    val DYNATRACE = getIcon("/icons/dynatrace.svg")
    val OPENSEARCH = getIcon("/icons/opensearch.svg")

    val CCV2 = getIcon("/icons/ccv2/ccv2.svg")
    val CCV2_FETCH = AllIcons.Vcs.Fetch
    val CCV2_ENVIRONMENTS = EXTENSION_CLOUD
    val CCV2_BUILDS = BUILD_CALLBACKS
    val CCV2_DEPLOYMENTS = AllIcons.Nodes.Deploy
    val CCV2_BACKUPS = AllIcons.Nodes.Undeploy
    val CCV2_ENDPOINTS = AllIcons.General.Web
    val CCV2_SHOW = AllIcons.Actions.Show

    val CCV2_BUILD_BRANCH = AllIcons.Vcs.Branch
    val CCV2_BUILD_CREATED_BY = AllIcons.General.User
    val CCV2_BUILD_STATUS_UNKNOWN = ExpUiIcons.Run.TestUnknown
    val CCV2_BUILD_STATUS_SCHEDULED = ExpUiIcons.Run.Profile
    val CCV2_BUILD_STATUS_BUILDING = ExpUiIcons.Run.TestCustom
    val CCV2_BUILD_STATUS_SUCCESS = ExpUiIcons.Run.TestPassed
    val CCV2_BUILD_STATUS_FAIL = ExpUiIcons.Run.TestFailed
    val CCV2_BUILD_STATUS_DELETED = ExpUiIcons.Run.KillProcess
    val CCV2_BUILD_CREATE = AllIcons.Actions.Execute
    val CCV2_BUILD_REDO = AllIcons.Actions.BuildAutoReloadChanges
    val CCV2_BUILD_DEPLOY = AllIcons.Nodes.Deploy
    val CCV2_BUILD_DELETE = ExpUiIcons.General.Delete
    val CCV2_BUILD_LOGS = ExpUiIcons.General.Download

    val CCV2_DEPLOYMENT_CREATED_BY = AllIcons.General.User
    val CCV2_DEPLOYMENT_UPDATE_MODE_NONE = AllIcons.Diff.GutterCheckBox
    val CCV2_DEPLOYMENT_UPDATE_MODE_UPDATE = ExpUiIcons.General.PluginUpdate
    val CCV2_DEPLOYMENT_UPDATE_MODE_INIT = AllIcons.General.ExclMark
    val CCV2_DEPLOYMENT_UPDATE_MODE_UNKNOWN = ExpUiIcons.Run.TestUnknown
    val CCV2_DEPLOYMENT_STRATEGY_ROLLING_UPDATE = AllIcons.Gutter.RecursiveMethod
    val CCV2_DEPLOYMENT_STRATEGY_RECREATE = AllIcons.Gutter.WriteAccess
    val CCV2_DEPLOYMENT_STRATEGY_GREEN = AllIcons.Gutter.ReadAccess
    val CCV2_DEPLOYMENT_STRATEGY_UNKNOWN = ExpUiIcons.Run.TestUnknown

    val CCV2_DEPLOYMENT_STATUS_SCHEDULED = ExpUiIcons.Run.Profile
    val CCV2_DEPLOYMENT_STATUS_DEPLOYING = ExpUiIcons.Run.TestCustom
    val CCV2_DEPLOYMENT_STATUS_DEPLOYED = ExpUiIcons.Run.TestPassed
    val CCV2_DEPLOYMENT_STATUS_UNDEPLOYED = ExpUiIcons.Run.TestSkipped
    val CCV2_DEPLOYMENT_STATUS_FAIL = ExpUiIcons.Run.TestError
    val CCV2_DEPLOYMENT_STATUS_UNKNOWN = ExpUiIcons.Run.TestUnknown

    val CCV2_ENV_STATUS_PROVISIONING = AllIcons.Actions.ProfileYellow
    val CCV2_ENV_STATUS_AVAILABLE = AllIcons.Actions.ProjectWideAnalysisOn
    val CCV2_ENV_STATUS_TERMINATING = AllIcons.Actions.ProfileRed
    val CCV2_ENV_STATUS_TERMINATED = AllIcons.Actions.ProjectWideAnalysisOff
    val CCV2_ENV_STATUS_READY_FOR_DEPLOYMENT = AllIcons.Actions.ShowReadAccess
    val CCV2_ENV_STATUS_UNKNOWN = ExpUiIcons.Run.TestUnknown

    val CCV2_ENV_ENVIRONMENT_TYPE_DEV = AllIcons.Nodes.AnonymousClass
    val CCV2_ENV_ENVIRONMENT_TYPE_STG = AllIcons.Nodes.Type
    val CCV2_ENV_ENVIRONMENT_TYPE_PROD = AllIcons.Nodes.AbstractException
    val CCV2_ENV_ENVIRONMENT_TYPE_UNKNOWN = AllIcons.Nodes.ErrorIntroduction

    val CONNECTION_ADD = AllIcons.General.Add
    val CONNECTION_EDIT = AllIcons.Actions.Edit

    val TABLE_COLUMN_INSERT_LEFT = getIcon("/icons/table/columnInsertLeft.svg")
    val TABLE_COLUMN_INSERT_RIGHT = getIcon("/icons/table/columnInsertRight.svg")
    val TABLE_COLUMN_MOVE_LEFT = getIcon("/icons/table/columnMoveLeft.svg")
    val TABLE_COLUMN_MOVE_RIGHT = getIcon("/icons/table/columnMoveRight.svg")
    val TABLE_COLUMN_REMOVE = getIcon("/icons/table/columnRemove.svg")
    val TABLE_REMOVE = getIcon("/icons/table/tableRemove.svg")
    val TABLE_SELECT = getIcon("/icons/table/tableSelect.svg")
    val TABLE_SPLIT_VERTICALLY = getIcon("/icons/table/tableSplitVertically.svg")

    val SAVE_ALL = getIcon("/icons/menu-saveall.svg")
    val UPLOAD = getIcon("/icons/upload.svg")
    val DELETE = getIcon("/icons/delete.svg")
    val CANCEL = DELETE

    val ACTION_COLLAPSE_ALL = AllIcons.Actions.Collapseall
    val ACTION_EXPAND_ALL = AllIcons.Actions.Expandall
    val ACTION_CLEAR_ALL = AllIcons.Actions.GC
    val ACTION_REMOVE = AllIcons.General.Remove
    val ACTION_FORCE_REFRESH = AllIcons.Actions.ForceRefresh
    val ACTION_FORWARD = AllIcons.Actions.Forward

    val SCOPE_LOCAL = AllIcons.Ide.LocalScope

    val NODE_JUNK = AllIcons.Modules.ExcludedGeneratedRoot

    private fun getIcon(path: String) = ReflectionUtil.getGrandCallerClass()
        ?.let { IconLoader.getIcon(path, it) }
        ?: error(path)
}