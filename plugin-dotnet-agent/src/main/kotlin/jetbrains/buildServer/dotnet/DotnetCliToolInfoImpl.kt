package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import java.io.File

class DotnetCliToolInfoImpl(
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService,
        private val _sdkPathProvider: SdkPathProvider)
    : DotnetCliToolInfo {

    override fun getInfo(dotnetExecutable: File, path: File): DotnetInfo {
        val version = getVersion(dotnetExecutable, path)

        val sdks = if (version >= Version.VersionSupportingSdksList) {
            getSdks(dotnetExecutable, path)
        }  else {
            getSdks()
        }

        return DotnetInfo(version, sdks.toList())
    }

    private fun getVersion(dotnetExecutable: File, path: File): Version {
        LOG.info("Try getting the dotnet CLI version for directory \"$path\".")
        val versionResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        TargetType.Tool,
                        dotnetExecutable,
                        path,
                        versionArgs,
                        emptyList()))

        if (versionResult == null) {
            LOG.error("The error occurred getting the dotnet CLI version.")
        } else {
            val version = _versionParser.tryParse(versionResult.standardOutput)?.let {
                Version.parse(it)
            }

            if (version == null) {
                LOG.error("The error occurred parsing the dotnet CLI version.")
            } else {
                return version
            }
        }

        return Version.Empty
    }

    private fun getSdks(): Sequence<DotnetSdk> {
        val sdkPath = _sdkPathProvider.path;
        LOG.info("Try getting the dotnet SDKs from directory \"$sdkPath\".")
        val sdks = _fileSystemService.list(sdkPath)
                .filter { _fileSystemService.isDirectory(it) }
                .map { DotnetSdk(it, Version.parse(it.name)) }
                .filter { it.version != Version.Empty }
        return sdks
    }

    private fun getSdks(dotnetExecutable: File, path: File): Sequence<DotnetSdk> = sequence {
        LOG.info("Try getting the dotnet SDKs using the command \"dotnet ${listSdksArgs.joinToString(" ")}\".")
        val sdksResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        TargetType.Tool,
                        dotnetExecutable,
                        path,
                        listSdksArgs,
                        emptyList()))

        if (sdksResult == null) {
            LOG.error("The error occurred getting the dotnet SDKs.")
        } else {
            for (line in sdksResult.standardOutput) {
                val matchResult = sdkInfoRegex.find(line)
                if (matchResult != null) {
                    try {
                        val version = matchResult.groupValues[1].trim()
                        val baseSdkPath = matchResult.groupValues[2]
                        yield(DotnetSdk(File(baseSdkPath, version), Version.parse(version)))
                    }
                    catch (ex: Exception) {
                        LOG.error("The error occurred parsing the dotnet SDK version.", ex)
                    }
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetCliToolInfoImpl::class.java.name)
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        internal val versionArgs = listOf(CommandLineArgument("--version"))
        internal val listSdksArgs = listOf(CommandLineArgument("--list-sdks"))
    }
}