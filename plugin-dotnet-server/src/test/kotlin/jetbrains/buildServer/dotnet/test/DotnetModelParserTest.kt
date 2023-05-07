/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.fetchers.DotnetConfigurationsFetcher
import jetbrains.buildServer.util.browser.Element
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

/**
 * @author Dmitry.Tretyakov
 *         Date: 06.12.2016
 *         Time: 18:17
 */
class DotnetModelParserTest {
    @Test(dataProvider = "getProjectFiles")
    fun getCsProjectModel(fullName: String, itemGroupsCount: Int) {
        val m = Mockery()
        val element = m.mock(Element::class.java)
        val parser = DotnetModelParser()
        val csproj = File(fullName)

        m.checking(object : Expectations() {
            init {
                one(element).isContentAvailable
                will(returnValue(true))

                one(element).inputStream
                will(returnValue(BufferedInputStream(FileInputStream(csproj))))

                one(element).fullName
                will(returnValue(fullName))
            }
        })

        val project = parser.getCsProjectModel(element)

        Assert.assertEquals(project!!.path, fullName)

        project.propertyGroups!!.let {
            Assert.assertEquals(it.size, 1)
            Assert.assertEquals(it[0].targetFramework, "netcoreapp1.0")
            Assert.assertNull(it[0].targetFrameworks)
        }

        project.itemGroups!!.let {
            Assert.assertEquals(it.size, itemGroupsCount)

            it[itemGroupsCount - 1].packageReferences!!.let {
                Assert.assertEquals(it.size, 5)
                Assert.assertEquals(it[4].include, "xunit.runner.visualstudio")
            }
        }

        val configurationsFetcher = DotnetConfigurationsFetcher(parser)
        Assert.assertEquals(configurationsFetcher.getDataItems(project), setOf("Core", "Debug", "Release"))
    }

    @Test
    fun getCsProjectRuntime() {
        val m = Mockery()
        val element = m.mock(Element::class.java)
        val parser = DotnetModelParser()
        val csproj = File("src/test/resources/project-runtime.csproj")

        m.checking(object : Expectations() {
            init {
                one(element).isContentAvailable
                will(returnValue(true))

                one(element).inputStream
                will(returnValue(BufferedInputStream(FileInputStream(csproj))))

                one(element).fullName
                will(returnValue(csproj.absolutePath))
            }
        })

        val project = parser.getCsProjectModel(element)
        Assert.assertNotNull(project)

        project?.propertyGroups!!.let {
            Assert.assertEquals(it.size, 1)
            Assert.assertEquals(it[0].runtimeIdentifier, "win7-x64")
            Assert.assertEquals(it[0].runtimeIdentifiers, "win7-x64;win-7x86;ubuntu.16.10-x64")
        }
    }

    @Test
    fun getCsProjectFrameworks() {
        val m = Mockery()
        val element = m.mock(Element::class.java)
        val parser = DotnetModelParser()
        val csproj = File("src/test/resources/project-frameworks.csproj")

        m.checking(object : Expectations() {
            init {
                one(element).isContentAvailable
                will(returnValue(true))

                one(element).inputStream
                will(returnValue(BufferedInputStream(FileInputStream(csproj))))

                one(element).fullName
                will(returnValue(csproj.absolutePath))
            }
        })

        val project = parser.getCsProjectModel(element)
        Assert.assertNotNull(project)

        project?.let {
            it.propertyGroups!!.let {
                Assert.assertEquals(it.size, 3)
                Assert.assertEquals(it[0].targetFrameworks, "net45;netstandard1.3")
            }
            it.itemGroups!!.let {
                Assert.assertEquals(it.size, 4)
            }
        }
    }

    @DataProvider
    fun getProjectFiles(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("src/test/resources/project.csproj", 2),
                arrayOf("src/test/resources/project-simplified.csproj", 1))
    }
}