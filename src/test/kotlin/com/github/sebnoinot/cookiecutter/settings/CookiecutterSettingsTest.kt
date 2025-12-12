package com.github.sebnoinot.cookiecutter.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CookiecutterSettingsTest : BasePlatformTestCase() {

    fun testDefaultSettings() {
        val settings = CookiecutterSettings.getInstance()
        
        assertNotNull(settings)
        assertEquals("cookiecutter", settings.cookiecutterExecutable)
        assertTrue(settings.recentTemplates.isEmpty())
    }

    fun testAddRecentTemplate() {
        val settings = CookiecutterSettings.getInstance()
        settings.recentTemplates.clear()
        
        settings.addRecentTemplate("gh:user/template1")
        assertEquals(1, settings.recentTemplates.size)
        assertEquals("gh:user/template1", settings.recentTemplates[0])
        
        settings.addRecentTemplate("gh:user/template2")
        assertEquals(2, settings.recentTemplates.size)
        assertEquals("gh:user/template2", settings.recentTemplates[0])
        assertEquals("gh:user/template1", settings.recentTemplates[1])
    }

    fun testRecentTemplatesDuplicateHandling() {
        val settings = CookiecutterSettings.getInstance()
        settings.recentTemplates.clear()
        
        settings.addRecentTemplate("gh:user/template1")
        settings.addRecentTemplate("gh:user/template2")
        settings.addRecentTemplate("gh:user/template1")
        
        assertEquals(2, settings.recentTemplates.size)
        assertEquals("gh:user/template1", settings.recentTemplates[0])
        assertEquals("gh:user/template2", settings.recentTemplates[1])
    }

    fun testRecentTemplatesMaxSize() {
        val settings = CookiecutterSettings.getInstance()
        settings.recentTemplates.clear()
        
        // Add 15 templates
        for (i in 1..15) {
            settings.addRecentTemplate("template$i")
        }
        
        // Should keep only the last 10
        assertEquals(10, settings.recentTemplates.size)
        assertEquals("template15", settings.recentTemplates[0])
        assertEquals("template6", settings.recentTemplates[9])
    }
}
