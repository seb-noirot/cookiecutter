package com.github.sebnoinot.cookiecutter.settings

import com.github.sebnoinot.cookiecutter.CookiecutterBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class CookiecutterConfigurable : Configurable {
    
    private var executableField: TextFieldWithBrowseButton? = null
    private val settings = CookiecutterSettings.getInstance()

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

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(CookiecutterBundle.message("settings.executable.label")),
                executableField!!,
                true
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        return executableField?.text != settings.cookiecutterExecutable
    }

    override fun apply() {
        executableField?.text?.let { settings.cookiecutterExecutable = it }
    }

    override fun reset() {
        executableField?.text = settings.cookiecutterExecutable
    }
}
