package io.github.xanderstuff.questkit

import org.bukkit.Bukkit
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import javax.script.ScriptEngine
import java.io.File
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import javax.script.Bindings
import javax.script.ScriptContext

class KotlinScriptEngineFactory : KotlinJsr223JvmScriptEngineFactoryBase() {
    override fun getScriptEngine(): ScriptEngine {
        val thisJar = File(this::class.java.protectionDomain.codeSource.location.toURI())
        log("thisJarLocation: $thisJar")
        val bukkitApiJar = File(Bukkit::class.java.protectionDomain.codeSource.location.toURI())
        log("bukkitApiJar: $bukkitApiJar")
//        val pluginsDirectory = Bukkit.getPluginsFolder()
//        log("pluginsDirectory: $pluginsDirectory")
//        val currentClassPath = System.getProperty("java.class.path")
//        log("currentClassPath: $currentClassPath")
//        val currentClassPath = System.getProperty("java.class.path").split(File.pathSeparator).map{ File(it) }

        val customClasspath: MutableList<File> = mutableListOf(thisJar, bukkitApiJar)
//        customClasspath.addAll(currentClassPath)

        // based off of: https://stackoverflow.com/questions/44781462/kotlin-jsr-223-scriptenginefactory-within-the-fat-jar-cannot-find-kotlin-compi/44796842#44796842
        //TODO: compare with org.jetbrains.kotlin.script.jsr223.KotlinJsr223ScriptEngineFactoryExamples -> KotlinJsr223JvmLocalScriptEngineFactory
        //TODO: look at and determine usefulness of kotlin.script.experimental.jvm.util.jvmClasspathUtil -> classpathFromClassloader(), classpathFromClasspathProperty(), classpathFromClass(), scriptCompilationClasspathFromContextOrNull()
        return KotlinJsr223JvmLocalScriptEngine(
            this,
            customClasspath,
            KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
            { ctx, types ->
                ScriptArgsWithTypes(
                    arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)),
                    types ?: emptyArray()
                )
            },
            arrayOf(Bindings::class)
        )
    }
}