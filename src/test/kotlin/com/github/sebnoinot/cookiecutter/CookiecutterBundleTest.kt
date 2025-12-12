package com.github.sebnoinot.cookiecutter

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CookiecutterBundleTest : BasePlatformTestCase() {

    fun testBundleMessages() {
        // Test that bundle can load messages
        val displayName = CookiecutterBundle.message("settings.displayName")
        assertNotNull(displayName)
        assertEquals("Cookiecutter", displayName)
        
        val wizardName = CookiecutterBundle.message("wizard.name")
        assertNotNull(wizardName)
        assertEquals("Cookiecutter", wizardName)
    }

    fun testBundleMessageWithParameters() {
        val message = CookiecutterBundle.message("success.message", "test-template")
        assertNotNull(message)
        assertTrue(message.contains("test-template"))
    }
}
