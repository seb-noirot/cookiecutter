package com.github.sebnoinot.cookiecutter.wizard

import com.github.sebnoinot.cookiecutter.CookiecutterBundle
import com.github.sebnoinot.cookiecutter.services.CookiecutterRunner
import com.github.sebnoinot.cookiecutter.settings.CookiecutterSettings
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.DirectoryProjectGeneratorBase
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import java.io.File
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel

class CookiecutterProjectGenerator : DirectoryProjectGeneratorBase<CookiecutterProjectSettings>(),
    CustomStepProjectGenerator<CookiecutterProjectSettings> {

    private val logger = Logger.getInstance(CookiecutterProjectGenerator::class.java)

    override fun getName(): String = CookiecutterBundle.message("wizard.name")

    override fun getDescription(): String = CookiecutterBundle.message("wizard.description")

    override fun getLogo(): Icon? = null

    override fun createPeer(): ProjectGeneratorPeer<CookiecutterProjectSettings> {
        return CookiecutterProjectGeneratorPeer()
    }

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: CookiecutterProjectSettings,
        module: Module
    ) {
        val runner = CookiecutterRunner.getInstance()
        
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            CookiecutterBundle.message("progress.title"),
            false
        ) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val template = settings.template
                    val outputDir = File(baseDir.parent.path)
                    
                    indicator.text = CookiecutterBundle.message("progress.running.cookiecutter")
                    indicator.text2 = template
                    
                    val output = runner.runCookiecutter(
                        template = template,
                        outputDir = outputDir,
                        noInput = false,
                        progressIndicator = indicator
                    )

                    if (output.exitCode == 0) {
                        // Delete the empty directory created by the wizard only on success
                        baseDir.delete(this)
                    } else {
                        logger.error("Cookiecutter failed: ${output.stderr}")
                    }
                    
                    // Refresh the file system
                    LocalFileSystem.getInstance().refresh(false)
                    
                } catch (e: Exception) {
                    logger.error("Failed to generate project from cookiecutter template", e)
                }
            }
        })
    }

    override fun createStep(
        projectGenerator: DirectoryProjectGenerator<CookiecutterProjectSettings>?,
        callback: AbstractNewProjectStep.AbstractCallback<CookiecutterProjectSettings>?
    ): AbstractActionWithPanel? {
        return null // Use default step
    }
}

data class CookiecutterProjectSettings(
    var template: String = ""
)

class CookiecutterProjectGeneratorPeer : ProjectGeneratorPeer<CookiecutterProjectSettings> {
    
    private val templateField: ComboBox<String> = ComboBox()
    private val settings = CookiecutterSettings.getInstance()
    private val projectSettings = CookiecutterProjectSettings()

    init {
        // Populate recent templates
        settings.recentTemplates.forEach { templateField.addItem(it) }
        templateField.isEditable = true
    }

    override fun getSettings(): CookiecutterProjectSettings {
        projectSettings.template = templateField.editor.item?.toString() ?: ""
        if (projectSettings.template.isNotEmpty()) {
            settings.addRecentTemplate(projectSettings.template)
        }
        return projectSettings
    }

    override fun getComponent(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(CookiecutterBundle.message("wizard.template.label")),
                templateField,
                true
            )
            .addComponent(JBLabel(CookiecutterBundle.message("wizard.template.examples")))
            .addComponent(JBLabel("  • gh:audreyfeldroy/cookiecutter-pypackage"))
            .addComponent(JBLabel("  • gl:gitlab-user/template-repo"))
            .addComponent(JBLabel("  • https://github.com/user/template.git"))
            .addComponent(JBLabel("  • /local/path/to/template"))
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        return panel
    }

    override fun buildUI(settingsStep: AbstractNewProjectStep.SettingsStep) {
        settingsStep.addSettingsComponent(component)
    }

    override fun validate(): com.intellij.openapi.ui.ValidationInfo? {
        val template = templateField.editor.item?.toString()
        return if (template.isNullOrEmpty()) {
            com.intellij.openapi.ui.ValidationInfo(
                CookiecutterBundle.message("wizard.validation.template.empty"),
                templateField
            )
        } else {
            null
        }
    }

    override fun isBackgroundJobRunning(): Boolean = false
}
