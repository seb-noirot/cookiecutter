package com.github.sebnoinot.cookiecutter.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator

class CookiecutterInstaller {

    private val logger = Logger.getInstance(CookiecutterInstaller::class.java)

    enum class InstallMethod {
        PIPX,
        PIP,
        NONE
    }

    /**
     * Detect the best installation method available
     * @return InstallMethod indicating which method to use
     */
    fun detectInstallMethod(): InstallMethod {
        return when {
            isCommandAvailable("pipx") -> InstallMethod.PIPX
            isCommandAvailable("pip") || isCommandAvailable("pip3") -> InstallMethod.PIP
            else -> InstallMethod.NONE
        }
    }

    /**
     * Check if a command is available in the system
     */
    private fun isCommandAvailable(command: String): Boolean {
        return try {
            val commandLine = GeneralCommandLine().apply {
                exePath = command
                addParameter("--version")
            }
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(5000)
            output.exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Install cookiecutter using the specified method
     * @param method The installation method to use
     * @param progressIndicator Progress indicator to show status
     * @return ProcessOutput containing the result of the installation
     */
    fun installCookiecutter(
        method: InstallMethod,
        progressIndicator: ProgressIndicator? = null
    ): ProcessOutput {
        progressIndicator?.text = "Installing cookiecutter..."

        val commandLine = when (method) {
            InstallMethod.PIPX -> {
                GeneralCommandLine().apply {
                    exePath = "pipx"
                    addParameter("install")
                    addParameter("cookiecutter")
                }
            }
            InstallMethod.PIP -> {
                val pipCommand = if (isCommandAvailable("pip3")) "pip3" else "pip"
                GeneralCommandLine().apply {
                    exePath = pipCommand
                    addParameter("install")
                    addParameter("--user")
                    addParameter("cookiecutter")
                }
            }
            InstallMethod.NONE -> {
                throw IllegalStateException("No suitable installation method available")
            }
        }

        logger.info("Installing cookiecutter: ${commandLine.commandLineString}")
        progressIndicator?.text2 = commandLine.commandLineString

        val handler = CapturingProcessHandler(commandLine)
        val output = handler.runProcess(300000) // 5 minute timeout

        if (output.exitCode != 0) {
            logger.warn("Cookiecutter installation failed with exit code ${output.exitCode}: ${output.stderr}")
        } else {
            logger.info("Cookiecutter installed successfully")
        }

        return output
    }

    /**
     * Try to find the cookiecutter executable after installation
     * @return Path to cookiecutter executable or null if not found
     */
    fun findCookiecutterExecutable(): String? {
        val possibleCommands = listOf("cookiecutter", "python -m cookiecutter", "python3 -m cookiecutter")
        
        for (command in possibleCommands) {
            try {
                val parts = command.split(" ")
                val commandLine = GeneralCommandLine().apply {
                    exePath = parts[0]
                    if (parts.size > 1) {
                        parts.drop(1).forEach { addParameter(it) }
                    }
                    addParameter("--version")
                }
                val handler = CapturingProcessHandler(commandLine)
                val output = handler.runProcess(5000)
                if (output.exitCode == 0) {
                    return command
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return null
    }

    companion object {
        fun getInstance(): CookiecutterInstaller {
            return ApplicationManager.getApplication().getService(CookiecutterInstaller::class.java)
        }
    }
}
