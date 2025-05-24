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

package com.intellij.idea.plugin.hybris.common.utils

import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.project.utils.Plugin
import com.intellij.openapi.util.IconLoader
import icons.AngularIcons
import icons.GradleIcons
import icons.OpenapiIcons
import org.jetbrains.kotlin.idea.KotlinIcons
import javax.swing.Icon

/**
 * Please follow Intellij Platform UI style and naming convention for icons.
 *
 * @see <a href="https://jetbrains.design/intellij/principles/icons/#style">styleguide</a>
 */
object HybrisIcons {

    val PLUGIN_SETTINGS = getIcon("/icons/pluginSettings.svg")
    val SETTINGS = getIcon("/icons/settings.svg")

    val MONITORING = getIcon("/icons/monitoring.svg")

    val CODE_NOT_GENERATED = AllIcons.General.ExclMark
    val SPRING_BEAN = getIcon("icons/springBean.svg")
    val GUTTER_POPULATOR = getIcon("/icons/gutter/populator.svg")

    object Y {
        val LICENCE = getIcon("/icons/hybrisLicenceIcon.svg")
        val LOGO_BLUE = getIcon("/icons/hybrisIcon.svg")
        val LOGO_ORANGE = getIcon("/icons/hybrisIconOrange.svg")
        val LOGO_GREEN = getIcon("/icons/hybrisIconGreen.svg")
        val REMOTE = getIcon("/icons/hybrisRemote.svg")
        val REMOTE_GREEN = getIcon("/icons/hybrisRemoteGreen.svg")
        val FACET = LOGO_GREEN
    }

    object UnmanagedDependencies {
        val FILE = getIcon("/icons/unmanagedDependencies.svg")
    }

    object ExternalDependencies {
        val FILE = getIcon("/icons/externalDependencies.svg")
    }

    object LocalExtensions {
        val FILE = getIcon("/icons/extensionInfo.svg")
    }

    object ExtensionInfo {
        val FILE = getIcon("/icons/extensionInfo.svg")
    }

    object BuildCallbacks {
        val FILE = AllIcons.Toolwindows.ToolWindowBuild
    }

    object Module {
        val ECLIPSE = AllIcons.Providers.Eclipse
        val MAVEN = OpenapiIcons.RepositoryLibraryLogo
        val CONFLICT = AllIcons.Actions.Cancel
        val GRADLE = Plugin.GRADLE.ifActive { GradleIcons.Gradle } ?: AllIcons.Nodes.Module
        val ANGULAR = Plugin.ANGULAR.ifActive { AngularIcons.Angular2 } ?: AllIcons.Nodes.Module
        val CCV2 = getIcon("/icons/module/cloud.svg")
        val CCV2_GROUP = getIcon("/icons/module/cloudGroup.svg")
        val COMMERCE_GROUP = Y.LOGO_GREEN
        val PLATFORM_GROUP = Y.LOGO_ORANGE
        val CUSTOM_GROUP = Y.LOGO_BLUE
        val EXTERNAL_GROUP = AllIcons.Nodes.ModuleGroup

        object Diagram {
            val PROPERTY = BeanSystem.PROPERTY
            val DESCRIPTION = AllIcons.Windows.Help
            val MAVEN_ENABLED = MAVEN
            val DEPRECATED = AllIcons.General.ExclMark
            val TEMPLATE = AllIcons.Nodes.Template
            val JALO_LOGIC_FREE = getIcon("/icons/flexibleSearch/star.svg")

            object Actions {
                val SHOW = AllIcons.Actions.DiagramDiff
            }
        }
    }

    object Extension {
        val CONFLICT = getIcon("/icons/extension/conflict.svg")
        val CONFIG = AllIcons.Nodes.ConfigFolder
        val CLOUD = getIcon("/icons/extension/cloud.svg")
        val CUSTOM = getIcon("/icons/extension/custom.svg")
        val PLATFORM = getIcon("/icons/extension/platform.svg")
        val EXT = getIcon("/icons/extension/ext.svg")
        val OOTB = getIcon("/icons/extension/ootb.svg")
        val WEB = AllIcons.Nodes.WebFolder
        val COMMON_WEB = AllIcons.Nodes.WebFolder
        val ADDON = AllIcons.Nodes.Plugin
        val BACKOFFICE = AllIcons.Nodes.Plugin
        val HMC = AllIcons.Nodes.Plugin
        val HAC = AllIcons.Nodes.Plugin
        val KOTLIN_NATURE = Plugin.KOTLIN.ifActive { KotlinIcons.SMALL_LOGO } ?: AllIcons.Nodes.Module
    }

    object Types {
        val PRIMITIVE = getIcon("/icons/types/primitive.svg")
        val BOXED = getIcon("/icons/types/boxed.svg")
        val OBJECT = getIcon("/icons/types/object.svg")
        val COLLECTION = getIcon("/icons/types/collection.svg")
        val MAP = getIcon("/icons/types/map.svg")
        val GENERIC = AllIcons.Nodes.AbstractClass
    }

    object ImpEx {
        val FILE = getIcon("/icons/fileTypes/impexFile.svg")
        val MACROS = getIcon("/icons/macros.svg")
        val MODE = AllIcons.Nodes.Function

        object Actions {
            val VALIDATE = getIcon("/icons/impex/validate.svg")

            val INSERT_COLUMN_LEFT = getIcon("/icons/impex/actions/columnInsertLeft.svg")
            val INSERT_COLUMN_RIGHT = getIcon("/icons/impex/actions/columnInsertRight.svg")
            val MOVE_COLUMN_LEFT = getIcon("/icons/impex/actions/columnMoveLeft.svg")
            val MOVE_COLUMN_RIGHT = getIcon("/icons/impex/actions/columnMoveRight.svg")
            val REMOVE_COLUMN = getIcon("/icons/impex/actions/columnRemove.svg")
            val REMOVE_TABLE = getIcon("/icons/impex/actions/tableRemove.svg")
            val SELECT_TABLE = getIcon("/icons/impex/actions/tableSelect.svg")
            val SPLIT_TABLE_VERTICALLY = getIcon("/icons/impex/actions/tableSplitVertically.svg")
        }
    }

    object FlexibleSearch {
        val FILE = getIcon("/icons/fileTypes/flexibleSearchFile.svg")

        val TABLE_ALIAS = getIcon("/icons/flexibleSearch/tableAlias.svg")
        val COLUMN_ALIAS = getIcon("/icons/flexibleSearch/columnAlias.svg")
        val OUTER_JOIN = getIcon("/icons/flexibleSearch/outerJoin.svg")
        val Y_COLUMN_PLACEHOLDER = AllIcons.Actions.PrettyPrint
        val Y_FROM_PLACEHOLDER = AllIcons.Actions.PrettyPrint
        val FROM_PARENS_PLACEHOLDER = getIcon("/icons/flexibleSearch/parens.svg")
        val KEYWORD = AllIcons.Nodes.Static
        val Y_COLUMN_ALL = getIcon("/icons/flexibleSearch/star.svg")
        val TABLE_SUFFIX = AllIcons.General.Filter
        val TABLE_ALIAS_SEPARATOR = getIcon("/icons/flexibleSearch/separator.svg")

        object Actions {
            val GROUP = getIcon("/icons/flexibleSearch/actions/group.svg")
        }
    }

    object PolyglotQuery {
        val FILE = getIcon("icons/fileTypes/polyglotQueryFile.svg")
    }

    object CockpitNG {
        val CONFIG = getIcon("/icons/cockpitNG/config.svg")
        val WIDGETS = getIcon("/icons/cockpitNG/widgets.svg")
        val WIDGET = getIcon("/icons/cockpitNG/widget.svg")
        val WIDGET_DEFINITION = getIcon("/icons/cockpitNG/widgetDefinition.svg")
        val ACTION_DEFINITION = getIcon("/icons/cockpitNG/actionDefinition.svg")
        val EDITOR_DEFINITION = getIcon("/icons/cockpitNG/editorDefinition.svg")
        val INITIALIZE_PROPERTY = AllIcons.Nodes.PropertyWrite

        object Molds {
            val NULL = AllIcons.Nodes.AnonymousClass
            val LIST = AllIcons.Actions.ListFiles
            val TREE = AllIcons.Actions.ShowAsTree
            val GRID = AllIcons.Graph.Grid
        }
    }

    object TypeSystem {
        val FILE = getIcon("/icons/typeSystem.svg")
        val DEPLOYMENT_MODEL = AllIcons.Nodes.Undeploy

        val LOCALIZED = getIcon("/icons/typeSystem/localized.svg")
        val INTERCEPTOR = getIcon("/icons/typeSystem/interceptor.svg")

        val GROUP_ATOMIC = getIcon("/icons/typeSystem/groupByAtomic.svg")
        val GROUP_ENUM = getIcon("/icons/typeSystem/groupByEnum.svg")
        val GROUP_COLLECTION = getIcon("/icons/typeSystem/groupByCollection.svg")
        val GROUP_ITEM = getIcon("/icons/typeSystem/groupByItem.svg")
        val GROUP_MAP = getIcon("/icons/typeSystem/groupByMap.svg")
        val GROUP_RELATION = getIcon("/icons/typeSystem/groupByRelation.svg")
        val DESCRIPTION = AllIcons.Windows.Help
        val ENUM_VALUE = getIcon("/icons/typeSystem/enumValue.svg")
        val CUSTOM_PROPERTY = getIcon("/icons/typeSystem/customProperty.svg")
        val ATTRIBUTE = getIcon("/icons/typeSystem/attribute.svg")
        val ORDERING_ATTRIBUTE = getIcon("/icons/typeSystem/orderingAttribute.svg")
        val RELATION_SOURCE = getIcon("/icons/typeSystem/relationSource.svg")
        val RELATION_TARGET = getIcon("/icons/typeSystem/relationTarget.svg")
        val INDEX = getIcon("/icons/typeSystem/index.svg")
        val INDEX_UNIQUE = getIcon("/icons/typeSystem/indexUnique.svg")
        val INDEX_REPLACE = getIcon("/icons/typeSystem/indexReplace.svg")
        val INDEX_REMOVE = getIcon("/icons/typeSystem/indexRemove.svg")
        val HEADER_ABBREVIATION = getIcon("/icons/typeSystem/headerAbbreviation.svg")
        val ALTERNATIVE_DECLARATION = AllIcons.Actions.Forward
        val SIBLING = AllIcons.Gutter.OverridenMethod

        object Types {
            val ITEM = getIcon("/icons/typeSystem/item.svg")
            val ATOMIC = getIcon("/icons/typeSystem/atomic.svg")
            val MAP = getIcon("/icons/typeSystem/map.svg")
            val RELATION = getIcon("/icons/typeSystem/relation.svg")
            val ENUM = getIcon("/icons/typeSystem/enum.svg")
            val COLLECTION = getIcon("/icons/typeSystem/collection.svg")
            val PRIMITIVE = HybrisIcons.Types.PRIMITIVE
            val OBJECT = HybrisIcons.Types.OBJECT
            val SPECIAL = AllIcons.FileTypes.Yaml
        }

        object Preview {
            object Actions {
                val SHOW = AllIcons.Actions.Show
                val SHOW_COLLECTIONS = AllIcons.Actions.GroupByPrefix
                val SHOW_ENUMS = AllIcons.Actions.GroupByTestProduction
                val SHOW_MAPS = AllIcons.Actions.GroupByPackage
                val SHOW_ITEMS = AllIcons.Actions.GroupByClass
            }
        }

        object Diagram {
            val PROPERTY = AllIcons.Nodes.Property
            val DEPLOYMENT = AllIcons.Debugger.Db_db_object

            object Actions {
                val RESET_VIEW = getIcon("/icons/typeSystem/diagram/resetView.svg")
                val OPEN_SETTINGS = HybrisIcons.SETTINGS
            }
        }
    }

    object BeanSystem {
        val FILE = getIcon("/icons/beanSystem/bean.svg")

        val GROUP_BY_BEAN_DTO = getIcon("/icons/beanSystem/groupByDTO.svg")
        val GROUP_BY_BEAN_EVENT = getIcon("/icons/beanSystem/groupByEvent.svg")
        val GROUP_BY_BEAN_WS = getIcon("/icons/beanSystem/groupByWS.svg")
        val GROUP_BY_ENUM = getIcon("/icons/beanSystem/groupByEnum.svg")
        val BEAN = getIcon("/icons/beanSystem/bean.svg")
        val IMPORT = AllIcons.ToolbarDecorator.Import
        val ANNOTATION = AllIcons.Nodes.Annotationtype
        val EVENT_BEAN = getIcon("/icons/beanSystem/eventBean.svg")
        val WS_BEAN = getIcon("/icons/beanSystem/wsBean.svg")
        val WS_HINT = AllIcons.Actions.QuickfixOffBulb
        val PROPERTY = getIcon("/icons/beanSystem/property.svg")
        val ENUM = getIcon("/icons/beanSystem/enum.svg")
        val ENUM_VALUE = getIcon("/icons/beanSystem/enumValue.svg")
        val LEVEL_MAPPING = getIcon("/icons/beanSystem/levelMapping.svg")
        val ALTERNATIVE_DECLARATION = AllIcons.Actions.Forward
        val SIBLING = AllIcons.Gutter.OverridenMethod

        object Preview {
            val SHOW = AllIcons.Actions.Show
        }
    }

    object BusinessProcess {
        val FILE = getIcon("/icons/businessProcess.svg")

        object Diagram {
            val PROCESS = getIcon("/icons/businessProcess.svg")
            val WAIT = getIcon("/icons/businessProcess/diagram/wait.svg")
            val END = getIcon("/icons/businessProcess/diagram/end.svg")
            val NOTIFY = getIcon("/icons/businessProcess/diagram/notify.svg")
            val ACTION = getIcon("/icons/businessProcess/diagram/action.svg")
            val SPLIT = getIcon("/icons/businessProcess/diagram/split.svg")
            val JOIN = getIcon("/icons/businessProcess/diagram/join.svg")
            val SCRIPT = getIcon("/icons/businessProcess/diagram/script.svg")
            val PARAMETERS = AllIcons.Nodes.NewParameter
            val PROPERTY = BeanSystem.PROPERTY
            val SPRING_BEAN = HybrisIcons.SPRING_BEAN
            val NODE = AllIcons.Nodes.FieldPK
            val FIELD = AllIcons.Nodes.Field
            val CLASS = AllIcons.Nodes.Class
            val PARAMETER_REQUIRED = AllIcons.Nodes.Plugin
            val PARAMETER_OPTIONAL = AllIcons.Nodes.Pluginobsolete
            val PROPERTIES = AllIcons.Nodes.Property
        }
    }

    object Console {
        val DESCRIPTOR = AllIcons.Debugger.Console
        val SOLR = getIcon("/icons/console/solr.svg")

        object Actions {
            val OPEN = getIcon("/icons/console/open.svg")
            val EXECUTE = AllIcons.Actions.Execute
            val EXECUTE_COMMIT_MODE_OFF = getIcon("/icons/console/executeWithCommitModeOff.svg")
            val SUSPEND = AllIcons.Actions.Suspend
        }
    }

    object CCv2 {
        val DESCRIPTOR = getIcon("/icons/ccv2/ccv2.svg")
        val DYNATRACE = getIcon("/icons/dynatrace.svg")
        val OPENSEARCH = getIcon("/icons/opensearch.svg")

        val ENVIRONMENTS = Extension.CLOUD
        val BUILDS = BuildCallbacks.FILE
        val DEPLOYMENTS = AllIcons.Nodes.Deploy
        val BACKUPS = AllIcons.Nodes.Undeploy
        val ENDPOINTS = AllIcons.General.Web
        val SHOW = AllIcons.Actions.Show

        object Actions {
            val FETCH = AllIcons.Vcs.Fetch
        }

        object Build {
            val BRANCH = AllIcons.Vcs.Branch
            val REVISION = AllIcons.Vcs.CommitNode
            val CREATED_BY = AllIcons.General.User

            object Status {
                val UNKNOWN = AllIcons.RunConfigurations.TestUnknown
                val SCHEDULED = AllIcons.Actions.Profile
                val BUILDING = AllIcons.RunConfigurations.TestCustom
                val SUCCESS = AllIcons.RunConfigurations.TestPassed
                val FAIL = AllIcons.RunConfigurations.TestFailed
                val DELETED = AllIcons.Debugger.KillProcess
            }

            object Actions {
                val CREATE = AllIcons.Actions.Execute
                val REDO = AllIcons.Actions.BuildAutoReloadChanges
                val DEPLOY = AllIcons.Nodes.Deploy
                val DELETE = AllIcons.General.Delete
                val DOWNLOAD_LOGS = AllIcons.Actions.Download
                val SHOW_DETAILS = BUILDS
                val WATCH = AllIcons.Debugger.Watch
            }
        }

        object Deployment {
            val CREATED_BY = AllIcons.General.User

            object UpdateMode {
                val NONE = AllIcons.Diff.GutterCheckBox
                val UPDATE = AllIcons.Ide.Notification.PluginUpdate
                val INIT = AllIcons.General.ExclMark
                val UNKNOWN = AllIcons.RunConfigurations.TestUnknown
            }

            object Strategy {
                val ROLLING_UPDATE = AllIcons.Gutter.RecursiveMethod
                val RECREATE = AllIcons.Gutter.WriteAccess
                val GREEN = AllIcons.Gutter.ReadAccess
                val UNKNOWN = AllIcons.RunConfigurations.TestUnknown
            }

            object Status {
                val SCHEDULED = AllIcons.Actions.Profile
                val DEPLOYING = AllIcons.RunConfigurations.TestCustom
                val DEPLOYED = AllIcons.RunConfigurations.TestPassed
                val UNDEPLOYED = AllIcons.RunConfigurations.TestSkipped
                val FAIL = AllIcons.RunConfigurations.TestError
                val UNKNOWN = AllIcons.RunConfigurations.TestUnknown
            }

            object Actions {
                val WATCH = AllIcons.Debugger.Watch
            }
        }

        object Environment {
            val DATA_BACKUP_CREATED_BY = AllIcons.General.User

            object Actions {
                val SHOW_DETAILS = getIcon("/icons/module/cloud.svg")
            }

            object Status {
                val PROVISIONING = AllIcons.Actions.ProfileYellow
                val AVAILABLE = AllIcons.Actions.ProjectWideAnalysisOn
                val TERMINATING = AllIcons.Actions.ProfileRed
                val TERMINATED = AllIcons.Actions.ProjectWideAnalysisOff
                val READY_FOR_DEPLOYMENT = AllIcons.Actions.ShowReadAccess
                val UNKNOWN = AllIcons.RunConfigurations.TestUnknown
            }

            object Type {
                val DEV = AllIcons.Nodes.AnonymousClass
                val STG = AllIcons.Nodes.Type
                val PROD = AllIcons.Nodes.AbstractException
                val UNKNOWN = AllIcons.Nodes.ErrorIntroduction
            }
        }

        object Service {
            val MODIFIED_BY = AllIcons.General.User

            object Actions {
                val SHOW_DETAILS = AllIcons.Nodes.Services
                val RESTART_POD = AllIcons.Actions.RestartFrame
            }
        }
    }

    object Connection {
        val ADD = AllIcons.General.Add
        val EDIT = AllIcons.Actions.Edit
    }

    object Scope {
        val LOCAL = AllIcons.Ide.LocalScope
        val PLATFORM = Extension.PLATFORM
        val COMMERCE_GROUP = Module.COMMERCE_GROUP
        val PLATFORM_GROUP = Module.PLATFORM_GROUP
    }

    object ProjectView {
        val NODE_JUNK = AllIcons.Modules.ExcludedGeneratedRoot
    }

    object Actions {
        val COLLAPSE_ALL = AllIcons.Actions.Collapseall
        val EXPAND_ALL = AllIcons.Actions.Expandall
        val CLEAR_ALL = AllIcons.Actions.GC
        val REMOVE = AllIcons.General.Remove
        val FORCE_REFRESH = AllIcons.Actions.ForceRefresh
        val FORWARD = AllIcons.Actions.Forward
        val GO_TO_DECLARATION = getIcon("/icons/actions/declaration.svg")
        val SHOW_DIAGRAM = AllIcons.FileTypes.Diagram
    }

    object Log {
        object Level {
            val OFF = getIcon("/icons/logging/level/off.svg")
            val ALL = getIcon("/icons/logging/level/all.svg")
            val TRACE = getIcon("/icons/logging/level/trace.svg")
            val DEBUG = getIcon("/icons/logging/level/debug.svg")
            val INFO = getIcon("/icons/logging/level/info.svg")
            val WARN = getIcon("/icons/logging/level/warn.svg")
            val ERROR = getIcon("/icons/logging/level/error.svg")
            val FATAL = getIcon("/icons/logging/level/fatal.svg")
            val SEVERE = getIcon("/icons/logging/level/severe.svg")
        }
    }

    private fun getIcon(path: String): Icon = IconLoader.getIcon(path, HybrisIcons::class.java.classLoader)
}