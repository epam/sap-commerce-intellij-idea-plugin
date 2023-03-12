/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.settings;

import com.google.common.collect.Lists;
import com.intellij.ide.util.PropertyName;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class HybrisApplicationSettings {

    public static final List<String> DEFAULT_JUNK_FILE_NAMES = Lists.newArrayList(
        ".classpath",
        ".directory",
        ".externalToolBuilders",
        ".idea",
        ".pmd",
        ".project",
        ".ruleset",
        ".settings",
        ".springBeans",
        "beans.xsd",
        "classes",
        "eclipsebin",
        "extensioninfo.xsd",
        "items.xsd",
        "platformhome.properties",
        "ruleset.xml",
        "testclasses"
    );

    public static final List<String> DEFAULT_TSD_STOP_TYPE_NAMES = Lists.newArrayList(
        HybrisConstants.TS_TYPE_GENERIC_ITEM,
        HybrisConstants.TS_TYPE_ITEM,
        HybrisConstants.TS_TYPE_LOCALIZABLE_ITEM,
        HybrisConstants.TS_TYPE_EXTENSIBLE_ITEM,
        HybrisConstants.TS_TYPE_CRON_JOB
    );

    public static final List<String> DEFAULT_EXTENSIONS_RESOURCES_TO_EXCLUDE = Lists.newArrayList(
        "solrserver",
        "npmancillary"
    );

    public static final List<String> DEFAULT_EXCLUDED_FROM_INDEX = Lists.newArrayList(
        "smartedit-custom-build",
        "smartedit-build",
        "apps/**/node_modules",
        "common/temp/node_modules"
    );

    @PropertyName("foldingEnabled")
    private boolean foldingEnabled = true;

    @PropertyName("useSmartFolding")
    private boolean useSmartFolding = true;

    @PropertyName("groupModules")
    private boolean groupModules = true;

    @PropertyName("junkDirectoryList")
    private List<String> junkDirectoryList = DEFAULT_JUNK_FILE_NAMES;

    @PropertyName("typeSystemDiagramStopTypes")
    private List<String> tsdStopTypeList = DEFAULT_TSD_STOP_TYPE_NAMES;

    @PropertyName("extensionsResourcesToExclude")
    private List<String> extensionsResourcesToExcludeList = DEFAULT_EXTENSIONS_RESOURCES_TO_EXCLUDE;

    @PropertyName("groupHybris")
    private String groupHybris = "Hybris";

    @PropertyName("groupOtherHybris")
    private String groupOtherHybris = "Hybris/Unused";

    @PropertyName("groupCustom")
    private String groupCustom = "Custom";

    @PropertyName("groupNonHybris")
    private String groupNonHybris = "Others";

    @PropertyName("groupOtherCustom")
    private String groupOtherCustom = "Custom/Unused";

    @PropertyName("groupPlatform")
    private String groupPlatform = "Platform";

    @PropertyName("groupCCv2")
    private String groupCCv2 = "CCv2";

    @PropertyName("hideEmptyMiddleFolders")
    private boolean hideEmptyMiddleFolders = true;

    @PropertyName("defaultPlatformInReadOnly")
    private boolean defaultPlatformInReadOnly = true;

    @PropertyName("followSymlink")
    private boolean followSymlink = true;

    @PropertyName("withMavenSources")
    private boolean withMavenSources;

    @PropertyName("withMavenJavadocs")
    private boolean withMavenJavadocs;

    @PropertyName("withStandardProvidedSources")
    private boolean withStandardProvidedSources;

    @PropertyName("allowedSendingPlainStatistics")
    private boolean allowedSendingPlainStatistics = false;

    @PropertyName("disallowedSendingStatistics")
    private boolean disallowedSendingStatistics = false;

    @PropertyName("developmentMode")
    private boolean developmentMode = false;

    @PropertyName("externalDbDriversDirectory")
    private String externalDbDriversDirectory = "";

    @PropertyName("sourceCodeDirectory")
    private String sourceCodeDirectory = "";

    @PropertyName("sourceZipUsed")
    private boolean sourceZipUsed = true;

    @PropertyName("scanThroughExternalModule")
    private boolean scanThroughExternalModule = false;

    @PropertyName("excludeTestSources")
    private boolean excludeTestSources = false;

    @PropertyName("warnIfGeneratedItemsAreOutOfDate")
    private boolean warnIfGeneratedItemsAreOutOfDate = true;

    @PropertyName("excludedFromIndexList")
    private List<String> excludedFromIndexList = DEFAULT_EXCLUDED_FROM_INDEX;


    public HybrisApplicationSettings() {
    }

    public boolean isFoldingEnabled() {
        return foldingEnabled;
    }

    public void setFoldingEnabled(final boolean foldingEnabled) {
        this.foldingEnabled = foldingEnabled;
    }

    public boolean isUseSmartFolding() {
        return useSmartFolding;
    }

    public void setUseSmartFolding(final boolean foldingEnabled) {
        this.useSmartFolding = foldingEnabled;
    }

    public List<String> getJunkDirectoryList() {
        return junkDirectoryList;
    }

    public void setJunkDirectoryList(final List<String> junkDirectoryList) {
        this.junkDirectoryList = junkDirectoryList;
    }

    public List<String> getExcludedFromIndexList() {
        return excludedFromIndexList;
    }

    public void setExcludedFromIndexList(final List<String> excludedFromIndexList) {
        this.excludedFromIndexList = excludedFromIndexList;
    }

    public boolean isGroupModules() {
        return groupModules;
    }

    public void setGroupModules(final boolean groupModules) {
        this.groupModules = groupModules;
    }

    public String getGroupHybris() {
        return groupHybris;
    }

    public void setGroupHybris(final String groupHybris) {
        this.groupHybris = groupHybris;
    }

    public String getGroupOtherHybris() {
        return groupOtherHybris;
    }

    public void setGroupOtherHybris(final String groupOtherHybris) {
        this.groupOtherHybris = groupOtherHybris;
    }

    public String getGroupCustom() {
        return groupCustom;
    }

    public void setGroupCustom(final String groupCustom) {
        this.groupCustom = groupCustom;
    }

    public String getGroupOtherCustom() {
        return groupOtherCustom;
    }

    public void setGroupOtherCustom(final String groupOtherCustom) {
        this.groupOtherCustom = groupOtherCustom;
    }

    public String getGroupNonHybris() {
        return groupNonHybris;
    }

    public void setGroupNonHybris(final String groupNonHybris) {
        this.groupNonHybris = groupNonHybris;
    }

    public boolean isHideEmptyMiddleFolders() {
        return hideEmptyMiddleFolders;
    }

    public void setHideEmptyMiddleFolders(final boolean hideEmptyMiddleFolders) {
        this.hideEmptyMiddleFolders = hideEmptyMiddleFolders;
    }

    public boolean isDefaultPlatformInReadOnly() {
        return defaultPlatformInReadOnly;
    }

    public void setDefaultPlatformInReadOnly(final boolean defaultPlatformInReadOnly) {
        this.defaultPlatformInReadOnly = defaultPlatformInReadOnly;
    }

    public String getGroupPlatform() {
        return groupPlatform;
    }

    public void setGroupPlatform(final String groupPlatform) {
        this.groupPlatform = groupPlatform;
    }

    public String getGroupCCv2() {
        return groupCCv2;
    }

    public void setGroupCCv2(final String groupCCv2) {
        this.groupCCv2 = groupCCv2;
    }

    public boolean isWarnIfGeneratedItemsAreOutOfDate() {
        return warnIfGeneratedItemsAreOutOfDate;
    }

    public void setWarnIfGeneratedItemsAreOutOfDate(final boolean warnIfGeneratedItemsAreOutOfDate) {
        this.warnIfGeneratedItemsAreOutOfDate = warnIfGeneratedItemsAreOutOfDate;
    }

    public boolean isFollowSymlink() {
        return followSymlink;
    }

    public void setFollowSymlink(final boolean followSymlink) {
        this.followSymlink = followSymlink;
    }

    public boolean isWithMavenSources() {
        return withMavenSources;
    }

    public void setWithMavenSources(final boolean withMavenSources) {
        this.withMavenSources = withMavenSources;
    }

    public boolean isWithMavenJavadocs() {
        return withMavenJavadocs;
    }

    public void setWithMavenJavadocs(final boolean withMavenJavadocs) {
        this.withMavenJavadocs = withMavenJavadocs;
    }

    public boolean isWithStandardProvidedSources() {
        return withStandardProvidedSources;
    }

    public void setWithStandardProvidedSources(final boolean withStandardProvidedSources) {
        this.withStandardProvidedSources = withStandardProvidedSources;
    }

    public boolean isAllowedSendingPlainStatistics() {
        return allowedSendingPlainStatistics;
    }

    public void setAllowedSendingPlainStatistics(final boolean allowedSendingPlainStatistics) {
        this.allowedSendingPlainStatistics = allowedSendingPlainStatistics;
    }

    public boolean isDisallowedSendingStatistics() {
        return disallowedSendingStatistics;
    }

    public void setDisallowedSendingStatistics(final boolean disallowedSendingStatistics) {
        this.disallowedSendingStatistics = disallowedSendingStatistics;
    }

    public String getExternalDbDriversDirectory() {
        return externalDbDriversDirectory;
    }

    public void setExternalDbDriversDirectory(final String externalDbDriversDirectory) {
        this.externalDbDriversDirectory = externalDbDriversDirectory;
    }

    public String getSourceCodeDirectory() {
        return sourceCodeDirectory;
    }

    public void setSourceCodeDirectory(final String sourceCodeDirectory) {
        this.sourceCodeDirectory = sourceCodeDirectory;
    }

    public boolean isSourceZipUsed() {
        return sourceZipUsed;
    }

    public void setSourceZipUsed(final boolean sourceZipUsed) {
        this.sourceZipUsed = sourceZipUsed;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public void setDevelopmentMode(final boolean developmentMode) {
        this.developmentMode = developmentMode;
    }

    public List<String> getTsdStopTypeList() {
        return tsdStopTypeList;
    }

    public void setTsdStopTypeList(final List<String> tsdStopTypeList) {
        this.tsdStopTypeList = tsdStopTypeList;
    }

    public boolean isScanThroughExternalModule() {
        return scanThroughExternalModule;
    }

    public void setScanThroughExternalModule(final boolean scanThroughExternalModule) {
        this.scanThroughExternalModule = scanThroughExternalModule;
    }

    public boolean isExcludeTestSources() {
        return excludeTestSources;
    }

    public void setExcludeTestSources(final boolean excludeTestSources) {
        this.excludeTestSources = excludeTestSources;
    }

    public List<String> getExtensionsResourcesToExcludeList() {
        return extensionsResourcesToExcludeList;
    }

    public void setExtensionsResourcesToExcludeList(final List<String> extensionsResourcesToExcludeList) {
        this.extensionsResourcesToExcludeList = extensionsResourcesToExcludeList;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(foldingEnabled)
            .append(useSmartFolding)
            .append(groupModules)
            .append(junkDirectoryList)
            .append(tsdStopTypeList)
            .append(groupHybris)
            .append(groupOtherHybris)
            .append(groupCustom)
            .append(groupOtherCustom)
            .append(groupNonHybris)
            .append(groupPlatform)
            .append(groupCCv2)
            .append(hideEmptyMiddleFolders)
            .append(defaultPlatformInReadOnly)
            .append(followSymlink)
            .append(withMavenSources)
            .append(withMavenJavadocs)
            .append(withStandardProvidedSources)
            .append(scanThroughExternalModule)
            .append(allowedSendingPlainStatistics)
            .append(disallowedSendingStatistics)
            .append(externalDbDriversDirectory)
            .append(sourceCodeDirectory)
            .append(sourceZipUsed)
            .append(developmentMode)
            .append(excludeTestSources)
            .append(extensionsResourcesToExcludeList)
            .append(warnIfGeneratedItemsAreOutOfDate)
            .append(excludedFromIndexList)
            .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final HybrisApplicationSettings other = (HybrisApplicationSettings) obj;

        return new EqualsBuilder()
            .append(foldingEnabled, other.foldingEnabled)
            .append(useSmartFolding, other.useSmartFolding)
            .append(groupModules, other.groupModules)
            .append(junkDirectoryList, other.junkDirectoryList)
            .append(tsdStopTypeList, other.tsdStopTypeList)
            .append(groupHybris, other.groupHybris)
            .append(groupOtherHybris, other.groupOtherHybris)
            .append(groupCustom, other.groupCustom)
            .append(groupOtherCustom, other.groupOtherCustom)
            .append(groupNonHybris, other.groupNonHybris)
            .append(groupPlatform, other.groupPlatform)
            .append(groupCCv2, other.groupCCv2)
            .append(hideEmptyMiddleFolders, other.hideEmptyMiddleFolders)
            .append(defaultPlatformInReadOnly, other.defaultPlatformInReadOnly)
            .append(followSymlink, other.followSymlink)
            .append(withMavenSources, other.withMavenSources)
            .append(withMavenJavadocs, other.withMavenJavadocs)
            .append(withStandardProvidedSources, other.withStandardProvidedSources)
            .append(scanThroughExternalModule, other.scanThroughExternalModule)
            .append(allowedSendingPlainStatistics, other.allowedSendingPlainStatistics)
            .append(disallowedSendingStatistics, other.disallowedSendingStatistics)
            .append(externalDbDriversDirectory, other.externalDbDriversDirectory)
            .append(sourceCodeDirectory, other.sourceCodeDirectory)
            .append(sourceZipUsed, other.sourceZipUsed)
            .append(developmentMode, other.developmentMode)
            .append(excludeTestSources, other.excludeTestSources)
            .append(extensionsResourcesToExcludeList, other.extensionsResourcesToExcludeList)
            .append(warnIfGeneratedItemsAreOutOfDate, other.warnIfGeneratedItemsAreOutOfDate)
            .append(excludedFromIndexList, other.excludedFromIndexList)
            .isEquals();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HybrisApplicationSettings{");
        sb.append("foldingEnabled=").append(foldingEnabled);
        sb.append(", useSmartFolding=").append(useSmartFolding);
        sb.append(", groupModules=").append(groupModules);
        sb.append(", junkDirectoryList=").append(junkDirectoryList);
        sb.append(", tsdStopTypeList=").append(tsdStopTypeList);
        sb.append(", extensionsResourcesToExcludeList=").append(extensionsResourcesToExcludeList);
        sb.append(", groupHybris='").append(groupHybris).append('\'');
        sb.append(", groupOtherHybris='").append(groupOtherHybris).append('\'');
        sb.append(", groupCustom='").append(groupCustom).append('\'');
        sb.append(", groupOtherCustom='").append(groupOtherCustom).append('\'');
        sb.append(", groupNonHybris='").append(groupNonHybris).append('\'');
        sb.append(", groupPlatform='").append(groupPlatform).append('\'');
        sb.append(", groupCCv2='").append(groupCCv2).append('\'');
        sb.append(", hideEmptyMiddleFolders='").append(hideEmptyMiddleFolders).append('\'');
        sb.append(", defaultPlatformInReadOnly='").append(defaultPlatformInReadOnly).append('\'');
        sb.append(", followSymlink='").append(followSymlink).append('\'');
        sb.append(", withMavenSources='").append(withMavenSources).append('\'');
        sb.append(", withMavenJavadocs='").append(withMavenJavadocs).append('\'');
        sb.append(", withStandardProvidedSources='").append(withStandardProvidedSources).append('\'');
        sb.append(", scanThroughExternalModule='").append(scanThroughExternalModule).append('\'');
        sb.append(", allowedSendingPlainStatistics='").append(allowedSendingPlainStatistics).append('\'');
        sb.append(", disallowedSendingStatistics='").append(disallowedSendingStatistics).append('\'');
        sb.append(", externalDbDriversDirectory='").append(externalDbDriversDirectory).append('\'');
        sb.append(", sourceCodeDirectory='").append(sourceCodeDirectory).append('\'');
        sb.append(", sourceZipUsed='").append(sourceZipUsed).append('\'');
        sb.append(", developmentMode='").append(developmentMode).append('\'');
        sb.append(", excludeTestSources='").append(excludeTestSources).append('\'');
        sb.append(", warnIfGeneratedItemsAreOutOfDate='").append(warnIfGeneratedItemsAreOutOfDate).append('\'');
        sb.append(", excludedFromIndexList='").append(excludedFromIndexList).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static String[] toIdeaGroup(final String group) {
        if (group == null || group.trim().isEmpty()) {
            return null;
        }
        return StringUtils.split(group, " ,.;>/\\");
    }
}
