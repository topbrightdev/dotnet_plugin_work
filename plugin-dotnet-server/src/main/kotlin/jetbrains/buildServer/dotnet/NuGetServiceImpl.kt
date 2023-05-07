package jetbrains.buildServer.dotnet

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.tools.ToolTypeAdapter
import org.springframework.cache.annotation.Cacheable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileFilter
import java.io.InputStreamReader
import java.net.URL
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
open class NuGetServiceImpl(
        private val _httpDownloader: HttpDownloader,
        private val _versionParser: NuGetPackageVersionParser)
    : NuGetService {

    @Cacheable("getPackagesById")
    open override fun getPackagesById(packageId: String, allowPrerelease: Boolean): Sequence<NuGetPackage> {
        return enumeratePackagesById(packageId, allowPrerelease).toList().asSequence()
    }

    private fun enumeratePackagesById(packageId: String, allowPrerelease: Boolean): Sequence<NuGetPackage> = buildSequence {
        LOG.info("Downloading list of packages for ${packageId}")
        var counter: Int = 0
        val listOfPackagesStream = ByteArrayOutputStream()
        _httpDownloader.download(URL("https://api-v2v3search-0.nuget.org/query?prerelease=${allowPrerelease};q=packageid:${packageId}"), listOfPackagesStream)
        val listOfPackagesJson = InputStreamReader(ByteArrayInputStream(listOfPackagesStream.toByteArray()))
        val listOfPackagesObj = JsonParser.parse(listOfPackagesJson).asJsonObject
        val dataArray = listOfPackagesObj.get("data").asJsonArray
        for (nuGetPackage in dataArray) {
            val nuGetPackageObj = nuGetPackage.getAsJsonObject()
            val versionItems = nuGetPackageObj.get("versions").getAsJsonArray()
            for (versionItem in versionItems) {
                val versionItemObj = versionItem.asJsonObject
                val version = versionItemObj.get("version").getAsString()
                val packageVersion = _versionParser.tryParse(version)
                if(packageVersion == null) {
                    continue
                }

                val packageInfoUrl = URL(versionItemObj.get("@id").getAsString());
                val packageInfoStream = ByteArrayOutputStream()
                _httpDownloader.download(packageInfoUrl, packageInfoStream)
                val packageInfoJson = InputStreamReader(ByteArrayInputStream(packageInfoStream.toByteArray()))
                val packageInfoObj = JsonParser.parse(packageInfoJson).asJsonObject
                val downloadUrl = URL(packageInfoObj.get("packageContent").asString);
                val isListed = packageInfoObj.get("listed").asBoolean;

                yield(NuGetPackage(packageId, packageVersion, downloadUrl, isListed))
                counter++;
            }
        }

        LOG.info("Downloaded list of ${counter} packages for ${packageId}")
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(NuGetServiceImpl::class.java.name)
        private val JsonParser = JsonParser()
    }
}