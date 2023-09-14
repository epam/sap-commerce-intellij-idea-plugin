/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.common.utils;

import com.intellij.icons.AllIcons;
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ReflectionUtil;
import icons.GradleIcons;
import icons.OpenapiIcons;

import javax.swing.*;

/**
 * Please follow Intellij Platform UI style and naming convention for icons.
 *
 * @see <a href="https://jetbrains.design/intellij/principles/icons/#style">styleguide</a>
 */
public final class HybrisIcons {

    private HybrisIcons() {
        // not allowed
    }

    public static final Icon IMPEX_FILE = getIcon("/icons/fileTypes/impexFile.svg");
    public static final Icon FXS_FILE = getIcon("/icons/fileTypes/flexibleSearchFileIcon.svg");
    public static final Icon PGQ_FILE = getIcon("icons/fileTypes/polyglotQueryFile.svg");
    public static final Icon BEAN_FILE = getIcon("/icons/beanSystem/bean.svg");

    public static final Icon PLUGIN_SETTINGS = getIcon("/icons/pluginSettings.svg");

    public static final Icon IMPEX = getIcon("/icons/impex.svg");
    public static final Icon FLEXIBLE_SEARCH = getIcon("/icons/flexibleSearch.svg");

    public static final Icon DECLARATION = getIcon("/icons/declaration.svg");

    public static final Icon Y_LOGO_BLUE = getIcon("/icons/hybrisIcon.svg");
    public static final Icon Y_LOGO_ORANGE = getIcon("/icons/hybrisIconOrange.svg");
    public static final Icon Y_LOGO_GREEN = getIcon("/icons/hybrisIconGreen.svg");
    public static final Icon Y_REMOTE = getIcon("/icons/hybrisRemote.svg");
    public static final Icon Y_REMOTE_GREEN = getIcon("/icons/hybrisRemoteGreen.svg");
    public static final Icon Y_FACET = Y_LOGO_GREEN;
    public static final Icon EXTENSION_INFO = getIcon("/icons/extensionInfo.svg");
    public static final Icon COCKPIT_NG_CONFIG = getIcon("/icons/cockpitNG/config.svg");
    public static final Icon COCKPIT_NG_WIDGETS = getIcon("/icons/cockpitNG/widgets.svg");
    public static final Icon COCKPIT_NG_WIDGET = getIcon("/icons/cockpitNG/widget.svg");
    public static final Icon COCKPIT_NG_WIDGET_DEFINITION = getIcon("/icons/cockpitNG/widgetDefinition.svg");
    public static final Icon COCKPIT_NG_ACTION_DEFINITION = getIcon("/icons/cockpitNG/actionDefinition.svg");
    public static final Icon COCKPIT_NG_EDITOR_DEFINITION = getIcon("/icons/cockpitNG/editorDefinition.svg");
    public static final Icon LOCAL_EXTENSIONS = getIcon("/icons/extensionInfo.svg");
    public static final Icon BUSINESS_PROCESS = getIcon("/icons/businessProcess.svg");
    public static final Icon MONITORING = getIcon("/icons/monitoring.svg");
    public static final Icon EXTERNAL_DEPENDENCIES = getIcon("/icons/externalDependencies.svg");
    public static final Icon UNMANAGED_DEPENDENCIES = getIcon("/icons/unmanagedDependencies.svg");
    public static final Icon SETTINGS = getIcon("/icons/settings.svg");

    public static final Icon MODULE_ECLIPSE = AllIcons.Providers.Eclipse;
    public static final Icon MODULE_MAVEN = OpenapiIcons.RepositoryLibraryLogo;
    public static final Icon MODULE_CONFLICT = AllIcons.Actions.Cancel;
    public static final Icon MODULE_GRADLE = PluginCommon.isPluginActive(PluginCommon.GRADLE_PLUGIN_ID)
        ? GradleIcons.Gradle
        : AllIcons.Nodes.Module;
    public static final Icon MODULE_CCV2 = getIcon("/icons/module/cloud.svg");
    public static final Icon MODULE_CCV2_GROUP = getIcon("/icons/module/cloudGroup.svg");
    public static final Icon MODULE_COMMERCE_GROUP = Y_LOGO_GREEN;
    public static final Icon MODULE_PLATFORM_GROUP = Y_LOGO_ORANGE;

    public static final Icon EXTENSION_CONFIG = AllIcons.Nodes.ConfigFolder;
    public static final Icon EXTENSION_CLOUD = getIcon("/icons/extension/cloud.svg");
    public static final Icon EXTENSION_CUSTOM = getIcon("/icons/extension/custom.svg");
    public static final Icon EXTENSION_PLATFORM = getIcon("/icons/extension/platform.svg");
    public static final Icon EXTENSION_EXT = getIcon("/icons/extension/ext.svg");
    public static final Icon EXTENSION_OOTB = getIcon("/icons/extension/ootb.svg");
    public static final Icon EXTENSION_WEB = AllIcons.Nodes.WebFolder;
    public static final Icon EXTENSION_COMMON_WEB = AllIcons.Nodes.WebFolder;
    public static final Icon EXTENSION_ADDON = AllIcons.Nodes.WebFolder;
    public static final Icon EXTENSION_BACKOFFICE = AllIcons.Nodes.Folder;
    public static final Icon EXTENSION_HMC = AllIcons.Nodes.Folder;
    public static final Icon EXTENSION_HAC = AllIcons.Nodes.Folder;

    public static final Icon TYPE_SYSTEM = getIcon("/icons/typeSystem.svg");
    public static final Icon SPRING_BEAN = getIcon("icons/springBean.svg");
    public static final Icon LOCALIZED = getIcon("/icons/localized.svg");

    public static final Icon MACROS = getIcon("/icons/macros.svg");

    public static final Icon INTERCEPTOR = getIcon("/icons/interceptor.svg");

    public static final Icon TYPE_PRIMITIVE = getIcon("/icons/typePrimitive.svg");
    public static final Icon TYPE_BOXED = getIcon("/icons/typeBoxed.svg");
    public static final Icon TYPE_OBJECT = getIcon("/icons/typeObject.svg");
    public static final Icon TYPE_COLLECTION = getIcon("/icons/typeCollection.svg");
    public static final Icon TYPE_MAP = getIcon("/icons/typeMap.svg");
    public static final Icon TYPE_GENERIC = AllIcons.Nodes.AbstractClass;

    public static final Icon TS_GROUP_ATOMIC = getIcon("/icons/typeSystem/groupByAtomic.svg");
    public static final Icon TS_GROUP_ENUM = getIcon("/icons/typeSystem/groupByEnum.svg");
    public static final Icon TS_GROUP_COLLECTION = getIcon("/icons/typeSystem/groupByCollection.svg");
    public static final Icon TS_GROUP_ITEM = getIcon("/icons/typeSystem/groupByItem.svg");
    public static final Icon TS_GROUP_MAP = getIcon("/icons/typeSystem/groupByMap.svg");
    public static final Icon TS_GROUP_RELATION = getIcon("/icons/typeSystem/groupByRelation.svg");
    public static final Icon TS_ENUM = getIcon("/icons/typeSystem/enum.svg");
    public static final Icon TS_ENUM_VALUE = getIcon("/icons/typeSystem/enumValue.svg");
    public static final Icon TS_ATOMIC = getIcon("/icons/typeSystem/atomic.svg");
    public static final Icon TS_ITEM = getIcon("/icons/typeSystem/item.svg");
    public static final Icon TS_MAP = getIcon("/icons/typeSystem/map.svg");
    public static final Icon TS_RELATION = getIcon("/icons/typeSystem/relation.svg");
    public static final Icon TS_CUSTOM_PROPERTY = getIcon("/icons/typeSystem/customProperty.svg");
    public static final Icon TS_ATTRIBUTE = getIcon("/icons/typeSystem/attribute.svg");
    public static final Icon TS_ORDERING_ATTRIBUTE = getIcon("/icons/typeSystem/orderingAttribute.svg");
    public static final Icon TS_RELATION_SOURCE = getIcon("/icons/typeSystem/relationSource.svg");
    public static final Icon TS_RELATION_TARGET = getIcon("/icons/typeSystem/relationTarget.svg");
    public static final Icon TS_COLLECTION = getIcon("/icons/typeSystem/collection.svg");
    public static final Icon TS_INDEX = getIcon("/icons/typeSystem/index.svg");
    public static final Icon TS_INDEX_UNIQUE = getIcon("/icons/typeSystem/indexUnique.svg");
    public static final Icon TS_INDEX_REPLACE = getIcon("/icons/typeSystem/indexReplace.svg");
    public static final Icon TS_INDEX_REMOVE = getIcon("/icons/typeSystem/indexRemove.svg");
    public static final Icon TS_IMPORT = AllIcons.ToolbarDecorator.Import;
    public static final Icon TS_ANNOTATION = AllIcons.Nodes.Annotationtype;
    public static final Icon TS_HEADER_ABBREVIATION = getIcon("/icons/typeSystem/headerAbbreviation.svg");

    public static final Icon BS_GROUP_BY_BEAN_DTO = getIcon("/icons/beanSystem/groupByDTO.svg");
    public static final Icon BS_GROUP_BY_BEAN_EVENT = getIcon("/icons/beanSystem/groupByEvent.svg");
    public static final Icon BS_GROUP_BY_BEAN_WS = getIcon("/icons/beanSystem/groupByWS.svg");
    public static final Icon BS_GROUP_BY_ENUM = getIcon("/icons/beanSystem/groupByEnum.svg");
    public static final Icon BS_BEAN = getIcon("/icons/beanSystem/bean.svg");
    public static final Icon BS_EVENT_BEAN = getIcon("/icons/beanSystem/eventBean.svg");
    public static final Icon BS_WS_BEAN = getIcon("/icons/beanSystem/wsBean.svg");
    public static final Icon BS_WS_HINT = AllIcons.Actions.QuickfixOffBulb;
    public static final Icon BS_PROPERTY = getIcon("/icons/beanSystem/property.svg");
    public static final Icon BS_ENUM = getIcon("/icons/beanSystem/enum.svg");
    public static final Icon BS_ENUM_VALUE = getIcon("/icons/beanSystem/enumValue.svg");
    public static final Icon BS_LEVEL_MAPPING = getIcon("/icons/beanSystem/levelMapping.svg");

    public static final Icon BP_DIAGRAM_WAIT = getIcon("/icons/businessProcess/diagram/wait.svg");
    public static final Icon BP_DIAGRAM_END = getIcon("/icons/businessProcess/diagram/end.svg");
    public static final Icon BP_DIAGRAM_NOTIFY = getIcon("/icons/businessProcess/diagram/notify.svg");
    public static final Icon BP_DIAGRAM_ACTION = getIcon("/icons/businessProcess/diagram/action.svg");
    public static final Icon BP_DIAGRAM_SPLIT = getIcon("/icons/businessProcess/diagram/split.svg");
    public static final Icon BP_DIAGRAM_JOIN = getIcon("/icons/businessProcess/diagram/join.svg");
    public static final Icon BP_DIAGRAM_SCRIPT = getIcon("/icons/businessProcess/diagram/script.svg");
    public static final Icon BP_DIAGRAM_PARAMETERS = AllIcons.Nodes.NewParameter;
    public static final Icon BP_DIAGRAM_PROPERTY = BS_PROPERTY;
    public static final Icon BP_DIAGRAM_SPRING_BEAN = SPRING_BEAN;
    public static final Icon BP_DIAGRAM_NODE = AllIcons.Nodes.FieldPK;
    public static final Icon BP_DIAGRAM_FIELD = AllIcons.Nodes.Field;
    public static final Icon BP_DIAGRAM_CLASS = AllIcons.Nodes.Class;
    public static final Icon BP_DIAGRAM_PARAMETER_REQUIRED = AllIcons.Nodes.Plugin;
    public static final Icon BP_DIAGRAM_PARAMETER_OPTIONAL = AllIcons.Nodes.Pluginobsolete;

    public static final Icon TS_DIAGRAM_PROPERTY = AllIcons.Nodes.Property;
    public static final Icon TS_DIAGRAM_DEPLOYMENT = AllIcons.Debugger.Db_db_object;
    public static final Icon TS_DIAGRAM_RESET_VIEW = getIcon("/icons/typeSystem/diagram/resetView.svg");
    public static final Icon TS_DIAGRAM_SETTINGS = SETTINGS;

    public static final Icon FXS_TABLE_ALIAS = getIcon("/icons/flexibleSearch/tableAlias.svg");
    public static final Icon FXS_COLUMN_ALIAS = getIcon("/icons/flexibleSearch/columnAlias.svg");
    public static final Icon FXS_OUTER_JOIN = getIcon("/icons/flexibleSearch/outerJoin.svg");
    public static final Icon FXS_Y_COLUMN_PLACEHOLDER = AllIcons.Actions.PrettyPrint;
    public static final Icon FXS_Y_FROM_PLACEHOLDER = AllIcons.Actions.PrettyPrint;
    public static final Icon FXS_FROM_PARENS_PLACEHOLDER = getIcon("/icons/flexibleSearch/parens.svg");
    public static final Icon FXS_KEYWORD = AllIcons.Nodes.Static;
    public static final Icon FXS_Y_COLUMN_ALL = getIcon("/icons/flexibleSearch/star.svg");
    public static final Icon FXS_TABLE_SUFFIX = AllIcons.General.Filter;
    public static final Icon FXS_TABLE_ALIAS_SEPARATOR = getIcon("/icons/flexibleSearch/separator.svg");

    public static final Icon IMPEX_VALIDATE = getIcon("/icons/impex/validate.svg");
    public static final Icon IMPEX_MODE = AllIcons.Nodes.Function;

    public static final Icon GUTTER_POPULATOR = getIcon("/icons/gutter/populator.svg");

    public static final Icon CONSOLE_SOLR = getIcon("/icons/console/solr.svg");
    public static final Icon CONSOLE_OPEN = getIcon("/icons/console/open.svg");
    public static final Icon CONSOLE_EXECUTE = AllIcons.Actions.Execute;
    public static final Icon CONSOLE_EXECUTE_COMMIT_MODE_OFF = getIcon("/icons/console/executeWithCommitModeOff.svg");

    public static final Icon TABLE_COLUMN_INSERT_LEFT = getIcon("/icons/table/columnInsertLeft.svg");
    public static final Icon TABLE_COLUMN_INSERT_RIGHT = getIcon("/icons/table/columnInsertRight.svg");
    public static final Icon TABLE_COLUMN_MOVE_LEFT = getIcon("/icons/table/columnMoveLeft.svg");
    public static final Icon TABLE_COLUMN_MOVE_RIGHT = getIcon("/icons/table/columnMoveRight.svg");
    public static final Icon TABLE_COLUMN_REMOVE = getIcon("/icons/table/columnRemove.svg");
    public static final Icon TABLE_REMOVE = getIcon("/icons/table/tableRemove.svg");
    public static final Icon TABLE_SELECT = getIcon("/icons/table/tableSelect.svg");
    public static final Icon TABLE_SPLIT_VERTICALLY = getIcon("/icons/table/tableSplitVertically.svg");

    static Icon getIcon(final String path) {
        final Class<?> callerClass = ReflectionUtil.getGrandCallerClass();
        assert callerClass != null : path;
        return IconLoader.getIcon(path, callerClass);
    }
}
