package com.github.sebnoinot.cookiecutter.services

import com.github.sebnoinot.cookiecutter.CookiecutterBundle
import com.github.sebnoinot.cookiecutter.settings.CookiecutterSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class CookiecutterRunner {

    private val logger = Logger.getInstance(CookiecutterRunner::class.java)
    private val settings = CookiecutterSettings.getInstance()

    /**
     * Run cookiecutter with the given template and output directory
     * @param template The template path (local path, GitHub repo, GitLab repo, or Git URL)
     * @param outputDir The output directory where the project will be created
     * @param noInput If true, use default values without prompting
     * @param extraContext Additional context variables to pass to cookiecutter
     * @param progressIndicator Progress indicator to show status
     * @return ProcessOutput containing the result of the command
     */
    fun runCookiecutter(
        template: String,
        outputDir: File,
        noInput: Boolean = false,
        extraContext: Map<String, String> = emptyMap(),
        progressIndicator: ProgressIndicator? = null
    ): ProcessOutput {
        progressIndicator?.text = CookiecutterBundle.message("progress.running.cookiecutter")
        
        val commandLine = GeneralCommandLine().apply {
            exePath = settings.cookiecutterExecutable
            addParameter(template)
            addParameter("--output-dir")
            addParameter(outputDir.absolutePath)
            
            if (noInput) {
                addParameter("--no-input")
            }
            
            extraContext.forEach { (key, value) ->
                addParameter("$key=$value")
            }
            
            withWorkingDirectory(outputDir)
        }

        logger.info("Running cookiecutter: ${commandLine.commandLineString}")
        progressIndicator?.text2 = commandLine.commandLineString

        val handler = CapturingProcessHandler(commandLine)
        val output = handler.runProcess(300000) // 5 minute timeout

        if (output.exitCode != 0) {
            logger.warn("Cookiecutter failed with exit code ${output.exitCode}: ${output.stderr}")
        } else {
            logger.info("Cookiecutter completed successfully")
        }

        return output
    }

    /**
     * Check if cookiecutter is available in the system
     * @return true if cookiecutter is available, false otherwise
     */
    fun isCookiecutterAvailable(): Boolean {
        return try {
            val commandLine = GeneralCommandLine().apply {
                exePath = settings.cookiecutterExecutable
                addParameter("--version")
            }
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(5000)
            output.exitCode == 0
        } catch (e: Exception) {
            logger.warn("Failed to check cookiecutter availability", e)
            false
        }
    }

    /**
     * Get the cookiecutter version
     * @return Version string or null if not available
     */
    fun getCookiecutterVersion(): String? {
        return try {
            val commandLine = GeneralCommandLine().apply {
                exePath = settings.cookiecutterExecutable
                addParameter("--version")
            }
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(5000)
            if (output.exitCode == 0) {
                output.stdout.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("Failed to get cookiecutter version", e)
            null
        }
    }

    companion object {
        fun getInstance(): CookiecutterRunner {
            return ApplicationManager.getApplication().getService(CookiecutterRunner::class.java)
        }
    }
}
