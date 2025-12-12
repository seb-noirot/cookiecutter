package com.github.sebnoinot.cookiecutter.actions

import com.github.sebnoinot.cookiecutter.CookiecutterBundle
import com.github.sebnoinot.cookiecutter.services.CookiecutterRunner
import com.github.sebnoinot.cookiecutter.ui.CookiecutterTemplateDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class GenerateFromTemplateAction : AnAction() {

    private val logger = Logger.getInstance(GenerateFromTemplateAction::class.java)
    private val runner = CookiecutterRunner.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        
        // Determine default output directory
        val defaultOutputDir = when {
            selectedFile != null && selectedFile.isDirectory -> selectedFile.path
            selectedFile != null -> selectedFile.parent?.path
            project != null -> project.basePath
            else -> System.getProperty("user.home")
        }

        // Show dialog to get template information
        val dialog = CookiecutterTemplateDialog(defaultOutputDir)
        if (!dialog.showAndGet()) {
            return
        }

        val template = dialog.template
        val outputDir = dialog.outputDirectory

        if (template.isEmpty() || outputDir.isEmpty()) {
            Messages.showErrorDialog(
                project,
                CookiecutterBundle.message("error.template.or.output.empty"),
                CookiecutterBundle.message("error.title")
            )
            return
        }

        // Check if cookiecutter is available
        if (!runner.isCookiecutterAvailable()) {
            val result = Messages.showYesNoDialog(
                project,
                CookiecutterBundle.message("error.cookiecutter.not.available.offer.install"),
                CookiecutterBundle.message("error.title"),
                "Install Cookiecutter",
                "Cancel",
                Messages.getQuestionIcon()
            )
            
            if (result == Messages.YES) {
                // Open settings to trigger installation
                com.intellij.openapi.options.ShowSettingsUtil.getInstance().showSettingsDialog(
                    project,
                    CookiecutterBundle.message("settings.displayName")
                )
            }
            return
        }

        // Run cookiecutter in background task
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            CookiecutterBundle.message("progress.title"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = CookiecutterBundle.message("progress.running.cookiecutter")
                    indicator.text2 = template
                    
                    val output = runner.runCookiecutter(
                        template = template,
                        outputDir = File(outputDir),
                        noInput = false,
                        progressIndicator = indicator
                    )

                    ApplicationManager.getApplication().invokeLater {
                        if (output.exitCode == 0) {
                            // Refresh file system
                            val refreshedDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(outputDir)
                            refreshedDir?.refresh(false, true)
                            
                            Messages.showInfoMessage(
                                project,
                                CookiecutterBundle.message("success.message", template),
                                CookiecutterBundle.message("success.title")
                            )
                        } else {
                            Messages.showErrorDialog(
                                project,
                                CookiecutterBundle.message("error.execution.failed", output.stderr),
                                CookiecutterBundle.message("error.title")
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to run cookiecutter", e)
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            project,
                            CookiecutterBundle.message("error.execution.exception", e.message ?: "Unknown error"),
                            CookiecutterBundle.message("error.title")
                        )
                    }
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        // Action is always available
        e.presentation.isEnabledAndVisible = true
    }
}
