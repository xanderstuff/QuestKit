package io.github.xanderstuff.questkit

import java.io.File
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class Script(
    val file: File
) {
    fun load(engine: ScriptEngine) {
        try {
            engine.eval(file.reader())
        } catch (e: ScriptException) {
            error(
                """
                An error has occured in a script!
                At: ${e.fileName} ${e.lineNumber}:${e.columnNumber}
                Stack Trace:
                
                ${e.stackTraceToString()}
            """.trimIndent()
            )
        }
    }
}

/**
 * Reloads all scripts in the folder.
 */
fun reloadAllScripts() {
    val scriptEngineManager = ScriptEngineManager()
    val kotlinEngine = scriptEngineManager.getEngineByExtension("kts")

    val directory = File(SCRIPT_PATH)
    directory.walkTopDown().forEach {
        if (it.isFile && it.extension == "kts") {
            Script(it).load(kotlinEngine)
        }
    }
}
