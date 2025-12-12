package com.github.sebnoinot.cookiecutter.settings

import com.github.sebnoinot.cookiecutter.CookiecutterBundle
import com.github.sebnoinot.cookiecutter.services.CookiecutterInstaller
import com.github.sebnoinot.cookiecutter.services.CookiecutterRunner
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class CookiecutterConfigurable : Configurable {
    
    private var executableField: TextFieldWithBrowseButton? = null
    private var installButton: JButton? = null
    private var statusLabel: JBLabel? = null
    private val settings = CookiecutterSettings.getInstance()
    private val runner = CookiecutterRunner.getInstance()
    private val installer = CookiecutterInstaller.getInstance()

    override fun getDisplayName(): String = CookiecutterBundle.message("settings.displayName")

    override fun createComponent(): JComponent {
        executableField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                CookiecutterBundle.message("settings.executable.chooser.title"),
                CookiecutterBundle.message("settings.executable.chooser.description"),
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
            )
        }

        installButton = JButton(CookiecutterBundle.message("settings.install.button")).apply {
            addActionListener {
                installCookiecutter()
            }
        }

        statusLabel = JBLabel()
        updateStatus()

        val installPanel = JPanel(BorderLayout()).apply {
            add(installButton!!, BorderLayout.WEST)
            add(statusLabel!!, BorderLayout.CENTER)
        }

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(CookiecutterBundle.message("settings.executable.label")),
                executableField!!,
                true
            )
            .addComponent(installPanel)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun updateStatus() {
        val version = runner.getCookiecutterVersion()
        statusLabel?.text = if (version != null) {
            CookiecutterBundle.message("settings.status.installed", version)
        } else {
            CookiecutterBundle.message("settings.status.not.installed")
        }
    }

    private fun installCookiecutter() {
        val method = installer.detectInstallMethod()
        
        if (method == CookiecutterInstaller.InstallMethod.NONE) {
            Messages.showErrorDialog(
                CookiecutterBundle.message("settings.install.error.no.method"),
                CookiecutterBundle.message("error.title")
            )
            return
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            null,
            CookiecutterBundle.message("settings.install.progress.title"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                val output = installer.installCookiecutter(method, indicator)
                
                if (output.exitCode == 0) {
                    // Try to find the executable
                    val executable = installer.findCookiecutterExecutable()
                    
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        if (executable != null) {
                            settings.cookiecutterExecutable = executable
                            executableField?.text = executable
                            updateStatus()
                            Messages.showInfoMessage(
                                CookiecutterBundle.message("settings.install.success"),
                                CookiecutterBundle.message("success.title")
                            )
                        } else {
                            Messages.showWarningDialog(
                                CookiecutterBundle.message("settings.install.warning.not.found"),
                                CookiecutterBundle.message("success.title")
                            )
                        }
                    }
                } else {
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(
                            CookiecutterBundle.message("settings.install.error.failed", output.stderr),
                            CookiecutterBundle.message("error.title")
                        )
                    }
                }
            }
        })
    }

    override fun isModified(): Boolean {
        return executableField?.text != settings.cookiecutterExecutable
    }

    override fun apply() {
        executableField?.text?.let { settings.cookiecutterExecutable = it }
        updateStatus()
    }

    override fun reset() {
        executableField?.text = settings.cookiecutterExecutable
        updateStatus()
    }
}
