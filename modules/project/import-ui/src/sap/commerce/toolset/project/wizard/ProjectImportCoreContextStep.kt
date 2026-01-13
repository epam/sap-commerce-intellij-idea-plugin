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

package sap.commerce.toolset.project.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.scale.JBUIScale
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.extensioninfo.EiConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.HybrisProjectImportBuilder
import sap.commerce.toolset.project.ProjectConstants
import sap.commerce.toolset.project.ProjectImportConstants
import sap.commerce.toolset.project.context.*
import sap.commerce.toolset.project.settings.ySettings
import sap.commerce.toolset.project.tasks.LookupModuleDescriptorsTask
import sap.commerce.toolset.project.tasks.LookupPlatformDirectoryTask
import sap.commerce.toolset.project.ui.uiCoreStep
import sap.commerce.toolset.settings.ApplicationSettings
import sap.commerce.toolset.util.directoryExists
import sap.commerce.toolset.util.fileExists
import java.awt.Dimension
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.swing.ScrollPaneConstants
import kotlin.io.path.*


/*
2026-01-13 15:51:33,912 [ 868663] SEVERE - #c.i.o.u.ObjectTree - Memory leak detected: 'newDisposable' (class com.intellij.openapi.util.Disposer$1) was registered in Disposer as a child of 'ROOT_DISPOSABLE' (class com.intellij.openapi.util.Disposer$2) but wasn't disposed.
Register it with a proper 'parentDisposable' or ensure that it's always disposed by direct Disposer.dispose() call.
See https://plugins.jetbrains.com/docs/intellij/disposers.html for more details.
The corresponding Disposer.register() stacktrace is shown as the cause:

java.lang.RuntimeException: Memory leak detected: 'newDisposable' (class com.intellij.openapi.util.Disposer$1) was registered in Disposer as a child of 'ROOT_DISPOSABLE' (class com.intellij.openapi.util.Disposer$2) but wasn't disposed.
Register it with a proper 'parentDisposable' or ensure that it's always disposed by direct Disposer.dispose() call.
See https://plugins.jetbrains.com/docs/intellij/disposers.html for more details.
The corresponding Disposer.register() stacktrace is shown as the cause:

	at com.intellij.openapi.util.ObjectNode.assertNoChildren(ObjectNode.java:47)
	at com.intellij.openapi.util.ObjectTree.assertIsEmpty(ObjectTree.java:227)
	at com.intellij.openapi.util.Disposer.assertIsEmpty(Disposer.java:228)
	at com.intellij.openapi.util.Disposer.assertIsEmpty(Disposer.java:222)
	at com.intellij.openapi.application.impl.ApplicationImpl.disposeContainer(ApplicationImpl.java:281)
	at com.intellij.openapi.application.impl.ApplicationImpl.destructApplication(ApplicationImpl.java:846)
	at com.intellij.openapi.application.impl.ApplicationImpl.doExit(ApplicationImpl.java:742)
	at com.intellij.openapi.application.impl.ApplicationImpl.exit(ApplicationImpl.java:727)
	at com.intellij.openapi.application.impl.ApplicationImpl.exit(ApplicationImpl.java:716)
	at com.intellij.openapi.application.ex.ApplicationEx.exit(ApplicationEx.java:65)
	at com.intellij.ui.mac.MacOSApplicationProviderKt$initMacApplication$3$1$1.invokeSuspend(MacOSApplicationProvider.kt:109)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith$$$capture(ContinuationImpl.kt:34)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt)
	at --- Async.Stack.Trace --- (captured by IntelliJ IDEA debugger)
	at kotlinx.coroutines.debug.internal.DebugProbesImpl$CoroutineOwner.<init>(DebugProbesImpl.kt:531)
	at kotlinx.coroutines.debug.internal.DebugProbesImpl.createOwner(DebugProbesImpl.kt:510)
	at kotlinx.coroutines.debug.internal.DebugProbesImpl.probeCoroutineCreated$kotlinx_coroutines_core(DebugProbesImpl.kt:497)
	at kotlin.coroutines.jvm.internal.DebugProbesKt.probeCoroutineCreated(DebugProbes.kt:7)
	at kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt.createCoroutineUnintercepted(IntrinsicsJvm.kt:161)
	at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable(Cancellable.kt:26)
	at kotlinx.coroutines.CoroutineStart.invoke(CoroutineStart.kt:358)
	at kotlinx.coroutines.AbstractCoroutine.start(AbstractCoroutine.kt:134)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch(Builders.common.kt:52)
	at kotlinx.coroutines.BuildersKt.launch(Unknown Source)
	at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch$default(Builders.common.kt:43)
	at kotlinx.coroutines.BuildersKt.launch$default(Unknown Source)
	at com.intellij.ui.mac.MacOSApplicationProviderKt.submit(MacOSApplicationProvider.kt:217)
	at com.intellij.ui.mac.MacOSApplicationProviderKt.initMacApplication$lambda$2(MacOSApplicationProvider.kt:106)
	at java.desktop/com.apple.eawt._AppEventHandler$_QuitDispatcher.performUsing(_AppEventHandler.java:447)
	at java.desktop/com.apple.eawt._AppEventHandler$_QuitDispatcher.performUsing(_AppEventHandler.java:436)
	at java.desktop/com.apple.eawt._AppEventHandler$_AppEventDispatcher$1.run(_AppEventHandler.java:568)
	at java.desktop/java.awt.event.InvocationEvent.dispatch$$$capture(InvocationEvent.java:318)
	at java.desktop/java.awt.event.InvocationEvent.dispatch(InvocationEvent.java)
	at --- Async.Stack.Trace --- (captured by IntelliJ IDEA debugger)
	at java.desktop/java.awt.event.InvocationEvent.<init>(InvocationEvent.java:291)
	at java.desktop/java.awt.event.InvocationEvent.<init>(InvocationEvent.java:217)
	at java.desktop/sun.awt.PeerEvent.<init>(PeerEvent.java:45)
	at java.desktop/sun.awt.PeerEvent.<init>(PeerEvent.java:40)
	at java.desktop/sun.awt.SunToolkit.invokeLaterOnAppContext(SunToolkit.java:611)
	at java.desktop/com.apple.eawt._AppEventHandler$_AppEventDispatcher.dispatch(_AppEventHandler.java:566)
	at java.desktop/com.apple.eawt._AppEventHandler.handleNativeNotification(_AppEventHandler.java:238)
Caused by: java.lang.Throwable
	at com.intellij.openapi.util.ObjectNode.<init>(ObjectNode.java:25)
	at com.intellij.openapi.util.ObjectNode.findOrCreateChildNode(ObjectNode.java:150)
	at com.intellij.openapi.util.ObjectTree.register(ObjectTree.java:52)
	at com.intellij.openapi.util.Disposer.register(Disposer.java:162)
	at com.intellij.util.containers.DisposableWrapperList.createDisposableWrapper(DisposableWrapperList.java:246)
	at com.intellij.util.containers.DisposableWrapperList.add(DisposableWrapperList.java:62)
	at com.intellij.openapi.ui.DialogPanelValidator.registerPanel(DialogPanelValidator.kt:32)
	at com.intellij.openapi.ui.DialogPanelValidator.<init>(DialogPanelValidator.kt:28)
	at com.intellij.openapi.ui.DialogPanel.registerValidators(DialogPanel.kt:54)
	at sap.commerce.toolset.project.wizard.ProjectImportCoreContextStep._ui_delegate$lambda$2(ProjectImportCoreContextStep.kt:65)
	at kotlin.SynchronizedLazyImpl.getValue(LazyJVM.kt:86)
	at sap.commerce.toolset.project.wizard.ProjectImportCoreContextStep.get_ui(ProjectImportCoreContextStep.kt:63)
	at sap.commerce.toolset.project.wizard.ProjectImportCoreContextStep.getComponent(ProjectImportCoreContextStep.kt:68)
	at sap.commerce.toolset.project.wizard.ProjectImportCoreContextStep.getComponent(ProjectImportCoreContextStep.kt:55)
	at com.intellij.ide.wizard.AbstractWizard.addStep(AbstractWizard.java:336)
	at com.intellij.ide.wizard.AbstractWizard.addStep(AbstractWizard.java:326)
	at com.intellij.ide.util.newProjectWizard.AddModuleWizard.initModuleWizard(AddModuleWizard.java:84)
	at com.intellij.ide.util.newProjectWizard.AddModuleWizard.<init>(AddModuleWizard.java:44)
	at sap.commerce.toolset.project.ProjectRefreshService$refresh$wizard$1.<init>(ProjectRefreshService.kt:69)
	at sap.commerce.toolset.project.ProjectRefreshService.refresh(ProjectRefreshService.kt:69)
	at sap.commerce.toolset.project.actionSystem.ProjectRefreshAction.actionPerformed(ProjectRefreshAction.kt:63)
	at com.intellij.openapi.actionSystem.ex.ActionUtil.doPerformActionOrShowPopup(ActionUtil.kt:437)
 */
class ProjectImportCoreContextStep(context: WizardContext) : ProjectImportWizardStep(context), RefreshSupport {

    private val disposable = Disposer.newDisposable()
    private val importCoreContext by lazy {
        ProjectImportCoreContext(
            importSettings = ProjectImportSettings.of(ApplicationSettings.getInstance()).mutable()
        )
    }
    private val _ui by lazy {
        uiCoreStep(importCoreContext)
            .also { it.registerValidators(disposable) }
    }

    override fun getComponent() = JBScrollPane(_ui).apply {
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        preferredSize = Dimension(preferredSize.width, JBUIScale.scale(600))

        CCv2ProjectSettings.getInstance().loadDefaultCCv2Token { ccv2Token ->
            if (ccv2Token != null) importCoreContext.ccv2Token.set(ccv2Token)
        }
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        disposable.dispose()
    }

    override fun updateDataModel() {
        _ui.apply()

        val importSettings = importCoreContext.importSettings.immutable()
        val importContext = importBuilder().initContext(importSettings)

        wizardContext.projectName = importCoreContext.projectName.get()

        with(importContext) {
            this.platformVersion = importCoreContext.platformVersion.get()
            this.platformDistributionPath = importCoreContext.platformDistributionPath.get().toNioPathOrNull()
            this.javadocUrl = importCoreContext.javadocUrl.get()
            this.excludedFromScanning = importCoreContext.excludedFromScanningDirectories.get().toImmutableSet()

            this.externalExtensionsDirectory = importCoreContext.customDirectory.takeIf { importCoreContext.customDirectoryOverride.get() }
                ?.get()?.toNioPathOrNull()
            this.externalConfigDirectory = importCoreContext.configDirectory.takeIf { importCoreContext.configDirectoryOverride.get() }
                ?.get()?.toNioPathOrNull()
            this.externalDbDriversDirectory = importCoreContext.dbDriverDirectory.takeIf { importCoreContext.dbDriverDirectoryOverride.get() }
                ?.get()?.toNioPathOrNull()
            this.projectIconFile = importCoreContext.projectIconFile.takeIf { importCoreContext.projectIcon.get() }
                ?.get()?.toNioPathOrNull()
            this.modulesFilesDirectory = importCoreContext.moduleFilesStorageDirectory.takeIf { importCoreContext.moduleFilesStorage.get() }
                ?.get()?.toNioPathOrNull()

            this.sourceCodePath = importCoreContext.sourceCodePath.takeIf { importCoreContext.sourceCodePathOverride.get() }
                ?.get()
                ?.toNioPathOrNull()
                ?.takeIf { it.exists() }
            this.sourceCodeFile = importCoreContext.sourceCodeFile.takeIf { importCoreContext.sourceCodePathOverride.get() }
                ?.get()
                ?.toNioPathOrNull()
                ?.takeIf { it.fileExists }

            this.ccv2Token = importCoreContext.ccv2Token.get()

            thisLogger().info("importing a project with the following settings: $this")
        }

        searchModuleRoots(importContext)
    }

    override fun updateStep() {
        if (importCoreContext.moduleFilesStorageDirectory.get().isBlank()) {
            importCoreContext.moduleFilesStorageDirectory.set(
                Path(builder.fileToImport)
                    .resolve(ProjectConstants.Paths.IDEA_MODULES)
                    .absolutePathString()
            )
        }
        if (importCoreContext.projectName.get().isBlank()) {
            importCoreContext.projectName.set(wizardContext.projectName)
        }

        if (importCoreContext.platformDistributionPath.get().isBlank()) {
            findPlatformDistributionPath()
                ?.let { importCoreContext.platformDistributionPath.set(it.normalize().pathString) }
        }

        val platformPath = importCoreContext.platformDistributionPath.get()
            .takeIf { it.isNotBlank() }
            ?.let { Path(it) }
            ?.takeIf { it.exists() }
            ?: return

        val platformApiVersion = getPlatformVersion(platformPath, true)
            ?.also {
                importCoreContext.platformApiVersion.set(it)
                importCoreContext.platformVersion.set(it)
            }
        val platformVersion = getPlatformVersion(platformPath, false)
            ?.also { importCoreContext.platformVersion.set(it) }

        if (importCoreContext.customDirectory.get().isBlank()) {
            importCoreContext.customDirectory.set(
                platformPath
                    .resolve(ProjectConstants.Paths.BIN_CUSTOM)
                    .absolutePathString()
            )
        }

        if (importCoreContext.configDirectory.get().isBlank()) {
            importCoreContext.configDirectory.set(
                platformPath
                    .resolve(EiConstants.Extension.CONFIG)
                    .absolutePathString()
            )
        }

        if (importCoreContext.dbDriverDirectory.get().isBlank()) {
            val dbDriversDirAbsolutePath = platformPath
                .resolve(ProjectConstants.Paths.BIN_PLATFORM)
                .resolve(ProjectConstants.Paths.LIB_DB_DRIVER)

            importCoreContext.dbDriverDirectory.set(dbDriversDirAbsolutePath.absolutePathString())
        }

        if (importCoreContext.javadocUrl.get().isBlank()) {
            getPlatformJavadocUrl(platformApiVersion)
                .takeIf { it.isNotBlank() }
                ?.let { importCoreContext.javadocUrl.set(it) }
        }

        if (importCoreContext.sourceCodePath.get().isBlank()) {
            val sourceCodeFile = platformPath.findSourceCodeFile(platformVersion, platformApiVersion)
                ?.pathString
                ?.also { importCoreContext.sourceCodeFile.set(it) }
                ?: platformPath.pathString

            importCoreContext.sourceCodePath.set(sourceCodeFile)
        }
    }

    override fun refresh(refreshContext: ProjectRefreshContext) {
        val importSettings = refreshContext.importSettings
        val projectSettings = refreshContext.project.ySettings
        val importContext = importBuilder().initContext(importSettings)

        with(importContext) {
            val resolvedPlatformDistributionPath = (projectSettings.platformRelativePath
                ?.let { refreshContext.projectPath.resolve(it) }
            // refreshing a project which was never imported by this plugin
                ?: findPlatformDistributionPath())
            this.platformDistributionPath = resolvedPlatformDistributionPath

            val platformApiVersion = resolvedPlatformDistributionPath?.let { getPlatformVersion(it, true) }
            val platformVersion = resolvedPlatformDistributionPath?.let { getPlatformVersion(it, false) }

            this.platformVersion = platformVersion
                ?: projectSettings.hybrisVersion
            this.javadocUrl = platformApiVersion
                ?.let { getPlatformJavadocUrl(it) }
                ?.takeIf { it.isNotBlank() }
                ?: projectSettings.javadocUrl
            this.sourceCodePath = projectSettings.sourceCodePath?.toNioPathOrNull()
            this.sourceCodeFile = this.sourceCodePath?.findSourceCodeFile(platformVersion, platformApiVersion)

            this.externalExtensionsDirectory = projectSettings.externalExtensionsDirectory?.toNioPathOrNull()
            this.externalConfigDirectory = projectSettings.externalConfigDirectory?.toNioPathOrNull()
            this.externalDbDriversDirectory = projectSettings.externalDbDriversDirectory?.toNioPathOrNull()

            this.modulesFilesDirectory = projectSettings.ideModulesFilesDirectory?.toNioPathOrNull()
                ?: refreshContext.projectPath.resolve(ProjectConstants.Paths.IDEA_MODULES)

            this.ccv2Token = CCv2ProjectSettings.getInstance().getCCv2Token()
            this.excludedFromScanning = projectSettings.excludedFromScanning
        }

        thisLogger().info("Refreshing a project with the following settings: $importContext")
        searchModuleRoots(importContext)
    }

    override fun validate(): Boolean {
        if (!super.validate()) return false

        val firstValidationInfo = _ui.validateAll()
            .filter { it.component?.isVisible ?: false }
            .onEach { it.okEnabled = true }
            .firstOrNull()

        if (firstValidationInfo != null) {
            throw ConfigurationException(firstValidationInfo.message)
        }

        importCoreContext.projectIconFile.takeIf { importCoreContext.projectIcon.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.fileExists ?: false }
            ?.let { throw ConfigurationException("Custom project icon should point to an existing SVG file.") }

        importCoreContext.customDirectory.takeIf { importCoreContext.customDirectoryOverride.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.directoryExists ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.custom.extensions.directory.does.not.exist")) }

        importCoreContext.configDirectory.takeIf { importCoreContext.configDirectoryOverride.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.directoryExists ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.config.directory.does.not.exist")) }

        importCoreContext.dbDriverDirectory.takeIf { importCoreContext.dbDriverDirectoryOverride.get() }
            ?.takeUnless { it.get().toNioPathOrNull()?.directoryExists ?: false }
            ?.let { throw ConfigurationException(i18n("hybris.import.wizard.validation.dbdriver.directory.does.not.exist")) }

        val distributionPath = importCoreContext.platformDistributionPath.get()
        if (distributionPath.isBlank()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.empty"))
        if (!Path(distributionPath).isDirectory()) throw ConfigurationException(i18n("hybris.import.wizard.validation.hybris.distribution.directory.does.not.exist"))

        return true
    }

    private fun getPlatformVersion(platformPath: Path, apiOnly: Boolean): String? {
        val buildInfoFile = platformPath.resolve(ProjectConstants.Paths.BIN_PLATFORM_BUILD_NUMBER)
        val buildProperties = Properties()

        try {
            Files.newBufferedReader(buildInfoFile, Charsets.UTF_8).use { fis ->
                buildProperties.load(fis)
            }
        } catch (_: IOException) {
            return null
        }

        if (!apiOnly) {
            val version = buildProperties.getProperty(ProjectImportConstants.HYBRIS_VERSION_KEY)
            if (version != null) return version
        }

        return buildProperties.getProperty(ProjectImportConstants.HYBRIS_API_VERSION_KEY)
    }

    private fun getPlatformJavadocUrl(platformApiVersion: String?) =
        if (platformApiVersion?.isNotEmpty() == true) String.format(HybrisConstants.URL_HELP_JAVADOC, platformApiVersion)
        else HybrisConstants.URL_HELP_JAVADOC_FALLBACK

    private fun searchModuleRoots(importContext: ProjectImportContext.Mutable) {
        try {
            thisLogger().info("Setting RootProjectDirectory to ${importContext.rootDirectory}")

            LookupModuleDescriptorsTask.getInstance().execute(importContext)
        } catch (e: Exception) {
            importContext.clear()
            thisLogger().error(e.message, e)

            Messages.showErrorDialog(
                e.message,
                "Project Import"
            )
        }
    }

    private fun findPlatformDistributionPath(): Path? {
        try {
            val rootProjectDirectory = Path(builder.fileToImport)

            return LookupPlatformDirectoryTask.getInstance().execute(rootProjectDirectory)
        } catch (_: CancellationException) {
            // noop
        } catch (e: Exception) {
            thisLogger().error(e)
        }
        return null
    }

    private fun importBuilder() = builder as HybrisProjectImportBuilder

}