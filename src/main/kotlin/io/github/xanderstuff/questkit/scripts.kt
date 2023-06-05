package io.github.xanderstuff.questkit

import java.io.File
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class Script(
    val file: File
) {
    /**
     * Loads this [Script] using the provided [engine]
     *
     * @return true if the script was loaded successfully
     */
    fun load(engine: ScriptEngine): Boolean {
        try {
            engine.eval(file.reader())
        } catch (e: ScriptException) {
            error(
                """
                An error has occurred in a script!
                At: ${e.fileName}, line: ${e.lineNumber} column:${e.columnNumber}
                Stack Trace:
                
                ${e.stackTraceToString()}
                """.trimIndent()
            )
            return false
        }
        return true
    }
}

/**
 * Reloads all scripts in the folder.
 *
 * @return a [Pair] of Ints, corresponding to the number of successfully loaded scripts and the number of scripts that were attempted to load, respectively
 */
fun reloadAllScripts(): Pair<Int, Int> {
    val kotlinEngine = ScriptEngineManager().getEngineByExtension("kts")!!

    val directory = File(SCRIPT_PATH)

    var numScriptsLoaded = 0
    var numScriptsAvailable = 0
    directory.walkTopDown().forEach {
        if (it.isFile && it.extension == "kts") {
            numScriptsAvailable++
            if (Script(it).load(kotlinEngine)) numScriptsLoaded++
        }
    }

    log("Finished loading $numScriptsLoaded out of $numScriptsAvailable scripts")
    return Pair(numScriptsLoaded, numScriptsAvailable)
}
