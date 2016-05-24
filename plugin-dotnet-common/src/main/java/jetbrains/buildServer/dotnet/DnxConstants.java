/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

/**
 * Dnx runner constants.
 */
public interface DnxConstants {
    String RUNNER_TYPE = "dnx";
    String RUNNER_DISPLAY_NAME = ".NET Core (dnx) (retired)";
    String RUNNER_DESCRIPTION = "Provides DNX project execution";

    String DNX_PATH = "DNX_PATH";
    String CONFIG_PATH = RUNNER_TYPE + "_Path";

    String PARAM_COMMAND = "dnx-command";
    String PARAM_PATHS = "dnx-paths";
    String PARAM_FRAMEWORK = "dnx-framework";
    String PARAM_CONFIG = "dnx-config";
    String PARAM_APPBASE = "dnx-appbase";
    String PARAM_LIBS = "dnx-lib";
    String PARAM_PACKAGES = "dnx-packages";

    String PARAM_ARGUMENTS = "dnx-args";
    String PROJECT_JSON = "project.json";
}
