package com.github.sebnoinot.cookiecutter.ui

import com.github.sebnoinot.cookiecutter.CookiecutterBundle
import com.github.sebnoinot.cookiecutter.settings.CookiecutterSettings
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class CookiecutterTemplateDialog(
    private val defaultOutputDir: String? = null
) : DialogWrapper(true) {

    private val templateField: ComboBox<String> = ComboBox()
    private val outputDirField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val settings = CookiecutterSettings.getInstance()

    var template: String = ""
        private set
    var outputDirectory: String = ""
        private set

    init {
        title = CookiecutterBundle.message("dialog.title")
        init()
        
        // Populate recent templates
        settings.recentTemplates.forEach { templateField.addItem(it) }
        templateField.isEditable = true
        
        // Set default output directory
        defaultOutputDir?.let { outputDirField.text = it }
        
        // Add file chooser for output directory
        outputDirField.addBrowseFolderListener(
            CookiecutterBundle.message("dialog.output.chooser.title"),
            CookiecutterBundle.message("dialog.output.chooser.description"),
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
    }

    override fun createCenterPanel(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(CookiecutterBundle.message("dialog.template.label")),
                templateField,
                true
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(panel, BorderLayout.NORTH)
        
        val infoPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(CookiecutterBundle.message("dialog.template.examples")))
            .addComponent(JBLabel("  • gh:audreyfeldroy/cookiecutter-pypackage"))
            .addComponent(JBLabel("  • gl:gitlab-user/template-repo"))
            .addComponent(JBLabel("  • https://github.com/user/template.git"))
            .addComponent(JBLabel("  • /local/path/to/template"))
            .addVerticalGap(10)
            .addLabeledComponent(
                JBLabel(CookiecutterBundle.message("dialog.output.label")),
                outputDirField,
                true
            )
            .panel
        
        mainPanel.add(infoPanel, BorderLayout.CENTER)
        
        return mainPanel
    }

    override fun doOKAction() {
        template = templateField.editor.item?.toString() ?: ""
        outputDirectory = outputDirField.text
        
        if (template.isNotEmpty()) {
            settings.addRecentTemplate(template)
        }
        
        super.doOKAction()
    }

    override fun getPreferredFocusedComponent(): JComponent = templateField
}
