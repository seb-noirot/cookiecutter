package com.github.sebnoinot.cookiecutter.services

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CookiecutterInstallerTest : BasePlatformTestCase() {

    fun testGetInstance() {
        val installer = CookiecutterInstaller.getInstance()
        assertNotNull(installer)
    }

    fun testDetectInstallMethod() {
        val installer = CookiecutterInstaller.getInstance()
        val method = installer.detectInstallMethod()
        
        // The method should return one of the enum values
        assertNotNull(method)
        assertTrue(
            method == CookiecutterInstaller.InstallMethod.PIPX ||
            method == CookiecutterInstaller.InstallMethod.PIP ||
            method == CookiecutterInstaller.InstallMethod.NONE
        )
    }

    fun testFindCookiecutterExecutable() {
        val installer = CookiecutterInstaller.getInstance()
        
        // This may return null if cookiecutter is not installed, which is fine
        val executable = installer.findCookiecutterExecutable()
        
        // If found, it should be one of the known commands
        if (executable != null) {
            assertTrue(
                executable.contains("cookiecutter") ||
                executable.contains("python")
            )
        }
    }
}
