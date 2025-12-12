package com.github.sebnoinot.cookiecutter.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "CookiecutterSettings",
    storages = [Storage("cookiecutter.xml")]
)
class CookiecutterSettings : PersistentStateComponent<CookiecutterSettings> {
    
    var cookiecutterExecutable: String = "cookiecutter"
    var recentTemplates: MutableList<String> = mutableListOf()

    override fun getState(): CookiecutterSettings = this

    override fun loadState(state: CookiecutterSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        private const val MAX_RECENT_TEMPLATES = 10
        
        fun getInstance(): CookiecutterSettings {
            return ApplicationManager.getApplication().getService(CookiecutterSettings::class.java)
        }
    }
    
    fun addRecentTemplate(template: String) {
        recentTemplates.remove(template)
        recentTemplates.add(0, template)
        if (recentTemplates.size > MAX_RECENT_TEMPLATES) {
            recentTemplates = recentTemplates.take(MAX_RECENT_TEMPLATES).toMutableList()
        }
    }
}
