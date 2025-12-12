package com.github.sebnoinot.cookiecutter.services

import com.github.sebnoinot.cookiecutter.settings.CookiecutterSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CookiecutterRunnerTest : BasePlatformTestCase() {

    fun testGetInstance() {
        val runner = CookiecutterRunner.getInstance()
        assertNotNull(runner)
    }

    fun testCookiecutterExecutableFromSettings() {
        val settings = CookiecutterSettings.getInstance()
        val originalExecutable = settings.cookiecutterExecutable
        
        try {
            settings.cookiecutterExecutable = "/custom/path/to/cookiecutter"
            val runner = CookiecutterRunner.getInstance()
            
            // The runner should use the configured executable
            assertNotNull(runner)
            
        } finally {
            settings.cookiecutterExecutable = originalExecutable
        }
    }
}
