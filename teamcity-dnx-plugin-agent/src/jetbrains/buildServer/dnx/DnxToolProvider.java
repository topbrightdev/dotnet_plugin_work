/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lookups for DNX utilities.
 */
public class DnxToolProvider implements ToolProvider {

    public static final String DNU_TOOL = "dnu";
    private static final Logger LOG = Logger.getInstance(DnxToolProvider.class.getName());
    private static final Pattern DNU_TOOL_PATH = Pattern.compile("^.*dnu(\\.(cmd|bat))?$");

    public DnxToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry) {
        toolProvidersRegistry.registerToolProvider(this);
    }

    @Override
    public boolean supports(@NotNull String toolName) {
        return DNU_TOOL.equals(toolName);
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName) throws ToolCannotBeFoundException {
        final String pathVariable = System.getenv("PATH");

        LOG.debug(String.format("Looking for tool %s in path %s", toolName, pathVariable));

        final List<String> paths = StringUtil.splitHonorQuotes(pathVariable, File.pathSeparatorChar);
        for (String path : paths) {
            final File directory = new File(path);
            if (!directory.exists()) {
                LOG.debug("Ignoring non existing directory " + path);
                continue;
            }

            final File[] files = directory.listFiles();
            if (files == null) {
                LOG.debug("Ignoring empty directory " + path);
                continue;
            }

            for (File file : files) {
                final String absolutePath = file.getAbsolutePath();
                if (DNU_TOOL_PATH.matcher(absolutePath).find()) {
                    return absolutePath;
                }
            }
        }

        throw new ToolCannotBeFoundException(String.format("Unable to locate tool %s in system", toolName));
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName,
                          @NotNull AgentRunningBuild build,
                          @NotNull BuildRunnerContext runner) throws ToolCannotBeFoundException {
        return getPath(toolName);
    }
}
