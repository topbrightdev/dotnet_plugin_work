/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.models

import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.annotation.OptBoolean
import com.fasterxml.jackson.dataformat.xml.annotation.*

/**
 * Represents csproj model.
 */
@JacksonXmlRootElement(localName = "Project", namespace = "http://schemas.microsoft.com/developer/msbuild/2003")
data class CsProject(
        @get:[
        JacksonXmlProperty(localName = "PropertyGroup")
        JacksonXmlElementWrapper(useWrapping = false)
        JsonMerge(OptBoolean.TRUE)]
        var propertyGroups: List<CsPropertyGroup>? = null,

        @get:[
        JacksonXmlProperty(localName = "ItemGroup")
        JacksonXmlElementWrapper(useWrapping = false)
        JsonMerge(OptBoolean.TRUE)]
        var itemGroups: List<CsItemGroup>? = null,

        var path: String? = null
)

/**
 * Property group.
 */
data class CsPropertyGroup(
        @get:JacksonXmlProperty(localName = "TargetFramework")
        var targetFramework: String? = null,

        @get:JacksonXmlProperty(localName = "TargetFrameworks")
        var targetFrameworks: String? = null,

        @get:JacksonXmlProperty(localName = "Condition")
        var condition: String? = null,

        @get:JacksonXmlProperty(localName = "RuntimeIdentifier")
        var runtimeIdentifier: String? = null,

        @get:JacksonXmlProperty(localName = "RuntimeIdentifiers")
        var runtimeIdentifiers: String? = null
)

/**
 * Item group.
 */
data class CsItemGroup(
        @get:[JacksonXmlProperty(localName = "PackageReference") JacksonXmlElementWrapper(useWrapping = false)]
        var packageReferences: List<CsPackageReference>? = null,

        @get:JacksonXmlProperty(localName = "Condition")
        var condition: String? = null
)

/**
 * Package reference.
 */
data class CsPackageReference(
        @get:JacksonXmlProperty(localName = "Include")
        var include: String? = null
)
