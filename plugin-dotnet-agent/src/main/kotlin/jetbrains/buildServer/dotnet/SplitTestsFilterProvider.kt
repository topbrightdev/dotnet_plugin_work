package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesReader
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class SplitTestsFilterProvider(
    private val _settings: SplittedTestsFilterSettings,
    private val _fileSystem: FileSystemService,
    private val _testsNamesReader: SplitTestsNamesReader,
) : TestsFilterProvider {
    override val filterExpression: String get() =
        _settings.testsClassesFile
            ?.let { testsPartsFile ->
                LOG.debug("Tests group file is \"$testsPartsFile\".")
                if (!_fileSystem.isExists(testsPartsFile) || !_fileSystem.isFile(testsPartsFile)) {
                    LOG.warn("Cannot find file \"$testsPartsFile\".")
                    return@let null
                }

                var filter = buildFilter()

                LOG.debug("Tests group file filter: \"$filter\".")
                filter
            }
            ?: ""

    private fun File.readLinesFromFile(): List<String> =
        _fileSystem
            .read(this) { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    val tests: MutableList<String> = ArrayList()
                    while (reader.ready()) {
                        tests += reader.readLine()
                    }
                    tests
                }
            }

    private fun buildFilter() = when {
        _settings.useExactMatchFilter -> buildExactMatchFilter()
        else -> buildDefaultFilter()
    }

    private fun buildDefaultFilter(): String {
        val (filterOperation, filterCombineOperator) = when (_settings.filterType) {
            SplittedTestsFilterType.Includes -> Pair("~", " | ")
            SplittedTestsFilterType.Excludes -> Pair("!~", " & ")
        }

        return _settings.testClasses
            .map { it + "." }       // to avoid collisions with overlapping test class names prefixes
            .let { buildFilter("FullyQualifiedName", filterOperation, it, filterCombineOperator) }
    }

    private fun buildExactMatchFilter(): String {
        val (filterOperation, filterCombineOperator) = Pair("=", " | ")

        return _testsNamesReader.read()
            .toList()
            .let { buildFilter("FullyQualifiedName", filterOperation, it, filterCombineOperator) }
    }

    @Suppress("SameParameterValue")
    private fun buildFilter(filterProperty: String, filterOperation: String, filterValues: List<String>, filterCombineOperator: String) =
        // https://docs.microsoft.com/en-us/dotnet/core/testing/selective-unit-tests
        filterValues
            .map { filterValue -> "${filterProperty}${filterOperation}${filterValue}" }
            .let { filterElements ->
                if (filterElements.size > FilterExressionChunkSize)
                    // chunks in parentheses '(', ')' are necessery to avoid stack overflow in VSTest filter validator
                    // https://youtrack.jetbrains.com/issue/TW-76381
                    filterElements.chunked(FilterExressionChunkSize) { "(${it.joinToString(filterCombineOperator)})" }
                else
                    filterElements
            }
            .joinToString(filterCombineOperator)

    companion object {
        private val LOG = Logger.getLogger(SplitTestsFilterProvider::class.java)
        private const val FilterExressionChunkSize = 1000;
    }
}