@file:Suppress("UNCHECKED_CAST")

package com.greenbird.graalvmtest

import com.greenbird.graalvmtest.JsonMapGraalContextFactory.getContext
import com.greenbird.graalvmtest.JsonMapGraalContextFactory.jsLanguageId
import com.greenbird.graalvmtest.JvmGraalVmConverter.fromGraalVmToJvm
import com.greenbird.graalvmtest.JvmGraalVmConverter.fromJvmToGraalVm
import com.greenbird.graalvmtest.TestDataGenerator.listWithLists
import com.greenbird.graalvmtest.TestDataGenerator.listWithMaps
import org.apache.commons.lang3.RandomStringUtils
import org.graalvm.polyglot.*
import org.graalvm.polyglot.proxy.ProxyArray
import org.graalvm.polyglot.proxy.ProxyObject
import org.intellij.lang.annotations.Language
import java.util.function.Function
import kotlin.system.measureTimeMillis

private const val LIST_SIZE = 50_000
private const val NUMBER_OF_RUNS = 1
private const val WARMUP_RUNS = 3

object GraalVMPerformanceRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        // We can not use the Graal VM
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false")

        println()

        listOfMapsProcessingWithoutReturn()
        listOfMapsProcessingWithReturn()
        listOfListsProcessingWithoutReturn()
        listOfListsProcessingWithReturn()
        println("----------------------")
        listOfMapsInKotlinProcessing()
        listOfListsInKotlinProcessing()
        println("----------------------")
        listOfMapsWithIsolatedJSProcessing()
        listOfListsWithIsolatedJSProcessing()

        println()
    }
}

fun listOfMapsProcessingWithoutReturn() {
    val listWithMaps: List<Map<String, String>> = listWithMaps(LIST_SIZE)

    @Language("ECMAScript 6")
    val objectTransformationScript = """
                    let transformed = input.map(obj => {
                        return {
                             a : obj['1'],  b : obj['2'],  c : obj['3'],  d : obj['4'],  e : obj['5'],  f : obj['6'],
                             g : obj['7'],  h : obj['8'],  i : obj['9'],  j : obj['10'], k : obj['11'], l : obj['12'],
                             m : obj['13'], n : obj['14'], o : obj['15'], p : obj['16'], q : obj['17'], r : obj['18'],
                             s : obj['19'], t : obj['20']                        
                        }                    
                    })
                    return {}
                """.trimIndent()

    executeScript(listWithMaps, objectTransformationScript, "List of maps - no return value")
}

fun listOfMapsProcessingWithReturn() {
    val listWithMaps: List<Map<String, String>> = listWithMaps(LIST_SIZE)

    @Language("ECMAScript 6")
    val objectTransformationScript = """
                    let transformed = input.map(obj => {
                        return {
                             a : obj['1'],  b : obj['2'],  c : obj['3'],  d : obj['4'],  e : obj['5'],  f : obj['6'],
                             g : obj['7'],  h : obj['8'],  i : obj['9'],  j : obj['10'], k : obj['11'], l : obj['12'],
                             m : obj['13'], n : obj['14'], o : obj['15'], p : obj['16'], q : obj['17'], r : obj['18'],
                             s : obj['19'], t : obj['20']                        
                        }                    
                    })
                    return transformed
                """.trimIndent()

    executeScript(listWithMaps, objectTransformationScript, "List of maps - with return value")
}

fun listOfListsProcessingWithoutReturn() {
    val listWithLists: List<List<String>> = listWithLists(LIST_SIZE)

    @Language("ECMAScript 6")
    val listTransformationScript = """
                  let transformed = input.map(obj => {
                        return [
                             obj[0],  obj[1],  obj[2],  obj[3],  obj[4],  obj[5],
                             obj[6],  obj[7],  obj[8],  obj[9], obj[10], obj[11],
                             obj[12], obj[13], obj[14], obj[15], obj[16], obj[17],
                             obj[18], obj[19]                        
                        ]                    
                    })
                    return {}
                """.trimIndent()

    executeScript(listWithLists, listTransformationScript, "List of lists - no return value")
}

fun listOfListsProcessingWithReturn() {
    val listWithLists: List<List<String>> = listWithLists(LIST_SIZE)

    @Language("ECMAScript 6")
    val listTransformationScript = """
                    let transformed = input.map(obj => {
                        return [
                             obj[0],  obj[1],  obj[2],  obj[3],  obj[4],  obj[5],
                             obj[6],  obj[7],  obj[8],  obj[9], obj[10], obj[11],
                             obj[12], obj[13], obj[14], obj[15], obj[16], obj[17],
                             obj[18], obj[19]                        
                        ]                    
                    })
                    return transformed
                """.trimIndent()

    executeScript(listWithLists, listTransformationScript, "List of lists - with return value")
}

fun listOfMapsWithIsolatedJSProcessing() {
    @Language("ECMAScript 6")
    val isolatedJsScript = """
                    // borrowed from https://stackoverflow.com/a/1349426
                    function makeId() {
                        const length = 20;
                        var result           = '';
                        var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
                        var charactersLength = characters.length;
                        for ( var i = 0; i < length; i++ ) {
                          result += characters.charAt(Math.floor(Math.random() * 
                     charactersLength));
                       }
                       return result;
                    }

                    console.time('List of maps - JS only - instantiation');
                    let list = []
                    function createObject(){
                        return {
                          "1":  makeId(), "2": makeId(),  "3": makeId(), "4":   makeId(), "5":  makeId(), 
                          "6":  makeId(), "7":  makeId(), "8":  makeId(), "9":  makeId(), "10": makeId(), 
                          "11": makeId(), "12": makeId(), "13": makeId(), "14": makeId(), "15": makeId(),
                          "16": makeId(), "17": makeId(), "18": makeId(), "19": makeId(), "20": makeId()
                        }
                    } 
                    for (let i = 0; i < $LIST_SIZE; i++) {
                      list.push(createObject())
                    }
                    console.timeEnd('List of maps - JS only - instantiation');
                    
                    console.time('List of maps - JS only');
                    list.map(obj => {
                        return {
                             a : obj['1'],  b : obj['2'],  c : obj['3'],  d : obj['4'],  e : obj['5'],  f : obj['6'],
                             g : obj['7'],  h : obj['8'],  i : obj['9'],  j : obj['10'], k : obj['11'], l : obj['12'],
                             m : obj['13'], n : obj['14'], o : obj['15'], p : obj['16'], q : obj['17'], r : obj['18'],
                             s : obj['19'], t : obj['20']                        
                        }                    
                    })
                    console.timeEnd('List of maps - JS only');
                    return {};
                """.trimIndent()

    executeIsolatedJsTest(isolatedJsScript, "List of maps - JS only")
}

fun listOfListsWithIsolatedJSProcessing() {
    @Language("ECMAScript 6")
    val isolatedJsScript = """
                    // borrowed from https://stackoverflow.com/a/1349426
                    function makeId() {
                        const length = 20;
                        var result           = '';
                        var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
                        var charactersLength = characters.length;
                        for ( var i = 0; i < length; i++ ) {
                          result += characters.charAt(Math.floor(Math.random() * 
                     charactersLength));
                       }
                       return result;
                    }

                    console.time('List of lists - JS only - instantiation');
                    let list = []
                    function createList(){
                        return [
                          makeId(), makeId(), makeId(),  makeId(), makeId(), 
                          makeId(), makeId(), makeId(),  makeId(), makeId(), 
                          makeId(), makeId(), makeId(),  makeId(), makeId(),
                          makeId(), makeId(), makeId(),  makeId(), makeId()
                        ]
                    } 
                    for (let i = 0; i < $LIST_SIZE; i++) {
                      list.push(createList())
                    }
                    console.timeEnd('List of lists - JS only - instantiation');
                    
                    console.time('List of lists - JS only');
                    list.map(list => {
                        return [
                             list[1],  list[2],  list[3],  list[4],  list[5],  list[6],
                             list[7],  list[8],  list[9],  list[10], list[11], list[12],
                             list[13], list[14], list[15], list[16], list[17], list[18],
                             list[19], list[20]                        
                        ]                    
                    })
                    console.timeEnd('List of lists - JS only');
                    return {};
                """.trimIndent()

    executeIsolatedJsTest(isolatedJsScript, "List of lists - JS only")
}

fun listOfMapsInKotlinProcessing() {
    val listWithMaps: List<Map<String, String>> = listWithMaps(LIST_SIZE)
    val processingTimes = mutableListOf<Long>()

    // warmup
    repeat(WARMUP_RUNS) {
        processListOfMapsInKotlin(listWithMaps)
    }

    Runtime.getRuntime().gc()

    repeat(NUMBER_OF_RUNS) {
        val processingTime = measureTimeMillis {
            processListOfMapsInKotlin(listWithMaps)
        }
        processingTimes.add(processingTime)
    }

    println("List of maps - pure Kotlin: avg. ${processingTimes.average()} ms")
}

fun listOfListsInKotlinProcessing() {
    val listWithMaps = listWithLists(LIST_SIZE)
    val processingTimes = mutableListOf<Long>()

    // warmup
    repeat(WARMUP_RUNS) {
        processListOfListsInKotlin(listWithMaps)
    }

    Runtime.getRuntime().gc()

    repeat(NUMBER_OF_RUNS) {
        val processingTime = measureTimeMillis {
            processListOfListsInKotlin(listWithMaps)
        }
        processingTimes.add(processingTime)
    }

    println("List of lists - pure Kotlin: avg. ${processingTimes.average()} ms")
}

fun executeScript(testData: Any, scriptText: String, testCase: String, numberOfRuns: Int = NUMBER_OF_RUNS) {
    val processingTimes = mutableListOf<Long>()
    val scriptExecutor = JavaScriptExecutor(scriptText, testCase)

    // warmup
    repeat(WARMUP_RUNS) {
        scriptExecutor.execute(testData)
    }

    Runtime.getRuntime().gc()

    repeat(numberOfRuns) {
        val processingTime = measureTimeMillis {
            scriptExecutor.execute(testData)
        }
        processingTimes.add(processingTime)
    }

    println("$testCase: avg. ${processingTimes.average()} ms")
    scriptExecutor.closeContext()
}

private fun executeIsolatedJsTest(jsScript: String, testCase: String) {
    val scriptExecutor = JavaScriptExecutor(jsScript, testCase, scriptWithInput = false)

    // warmup
    println()
    println("==== WARMUP: '${testCase}' ====")
    repeat(WARMUP_RUNS) {
        scriptExecutor.execute(null)
    }

    println("==== WARMUP END: '${testCase}'  ====")
    println()

    Runtime.getRuntime().gc()

    repeat(NUMBER_OF_RUNS) {
        val processingTime = measureTimeMillis {
            scriptExecutor.execute(null)
        }
    }
    scriptExecutor.closeContext()
}

private fun processListOfMapsInKotlin(listOfMaps: List<Map<String, String>>): List<Map<String, String>> =
    listOfMaps.map {
        mapOf(
            "a" to it["1"]!!, "b" to it["2"]!!, "c" to it["3"]!!, "d" to it["4"]!!, "e" to it["5"]!!,
            "f" to it["6"]!!, "g" to it["7"]!!, "h" to it["8"]!!, "i" to it["9"]!!, "j" to it["10"]!!,
            "k" to it["11"]!!, "l" to it["12"]!!, "m" to it["13"]!!, "n" to it["14"]!!, "o" to it["15"]!!,
            "p" to it["16"]!!, "q" to it["17"]!!, "r" to it["18"]!!, "s" to it["19"]!!, "t" to it["20"]!!
        )
    }

private fun processListOfListsInKotlin(listOfLists: List<List<Any>>): List<List<Any>> =
    listOfLists.map {
        listOf(
            it[0], it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8], it[9], it[10], it[11], it[12], it[13],
            it[14], it[15], it[16], it[17], it[18], it[19]
        )
    }

class JavaScriptExecutor(script: String, name: String, val scriptWithInput: Boolean = true) {
    private val context = getContext()
    private val processingFunction: Function<Array<Any?>, Any?>

    @Language("ECMAScript 6")
    private val wrapperFunctionText = when {
        scriptWithInput -> """
            (function (input) {
                $script
            })
        """
        else -> """
            (function () {
                $script
            })
        """
    }

    init {
        val compiledWrapperScript = Source.newBuilder(
            jsLanguageId,
            wrapperFunctionText,
            name
        ).buildLiteral()

        @Suppress("UNCHECKED_CAST")
        processingFunction = context.eval(compiledWrapperScript).`as`(Any::class.java) as Function<Array<Any?>, Any?>
    }

    fun execute(input: Any?): Any? {
        val inputProxy = fromJvmToGraalVm(input)

        val resultValue = processingFunction.apply(arrayOf(inputProxy))

        return if (resultValue != null) {
            fromGraalVmToJvm(resultValue)
        } else {
            throw IllegalStateException("Script did not produced any result, missing return statement in the script definition.")
        }
    }

    fun closeContext() = context.close()
}

object JvmGraalVmConverter {
    private val primitiveWrapperTypes = setOf(
        java.lang.Boolean::class.java,
        java.lang.Character::class.java,
        java.lang.Byte::class.java,
        java.lang.Short::class.java,
        java.lang.Integer::class.java,
        java.lang.Long::class.java,
        java.lang.Float::class.java,
        java.lang.Double::class.java,
        java.lang.Void::class.java
    )

    private fun Class<*>.isPrimitiveOrWrapper(): Boolean = this.isPrimitive || primitiveWrapperTypes.contains(this)

    fun fromJvmToGraalVm(input: Any?): Any? = when {
        input == null -> null
        String::class.java.isAssignableFrom(input::class.java) || input::class.java.isPrimitiveOrWrapper() -> input
        List::class.java.isAssignableFrom(input::class.java) -> ListProxyObject(input as MutableList<Any?>)
        Map::class.java.isAssignableFrom(input::class.java) -> MapProxyObject(input as MutableMap<String, Any?>)
        else -> throw IllegalArgumentException("Unsupported input type")
    }

    fun fromGraalVmToJvm(scriptOutput: Any?): Any? = if (scriptOutput is Value) {
        scriptOutput.`as`(Any::class.java)
    } else {
        scriptOutput
    }

    private class ListProxyObject(private val list: MutableList<Any?>) : ProxyArray {
        override fun get(index: Long): Any? = when (val v = list[index.toInt()]) {
            is MutableMap<*, *> -> MapProxyObject(v as MutableMap<String, Any?>)
            else -> v
        }

        override fun set(index: Long, value: Value) {
            list.add(index.toInt(), if (value.isHostObject) value.asHostObject<Any>() else value)
        }

        override fun getSize(): Long = list.size.toLong()

    }

    private class MapProxyObject(private val values: MutableMap<String, Any?>) : ProxyObject {

        override fun putMember(key: String, value: Value) {
            values[key] = if (value.isHostObject) value.asHostObject<Any>() else value
        }

        override fun hasMember(key: String): Boolean = values.containsKey(key)

        override fun getMember(key: String): Any? = when (val v = values[key]) {
            is MutableMap<*, *> -> MapProxyObject(v as MutableMap<String, Any?>)
            is MutableList<*> -> ListProxyObject(v as MutableList<Any?>)
            else -> v
        }

        override fun removeMember(key: String?): Boolean = when {
            values.containsKey(key) -> {
                values.remove(key)
                true
            }
            else -> false
        }

        override fun getMemberKeys(): Any = values.keys.toList()
    }
}

object JsonMapGraalContextFactory {
    const val jsLanguageId = "js"

    fun getContext(): Context =
        Context
            .newBuilder()
            .engine(engine)
            .allowHostAccess(hostAccess)
            .build()

    private val hostAccess = HostAccess
        .newBuilder()
        .allowListAccess(true)
        .targetTypeMapping(
            Value::class.java, Any::class.java,
            { v -> v.hasArrayElements() },
            { v -> transformArray(v) }
        ).build()

    private val engine = Engine
        .newBuilder()
        .option("js.experimental-foreign-object-prototype", "true")
        .allowExperimentalOptions(true)
        .build()

    private fun transformArray(v: Value): MutableList<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until v.arraySize) {
            val element = v.getArrayElement(i)
            if (element.hasArrayElements() && !element.isHostObject) {
                list.add(transformArray(element))
            } else if (element.hasMembers() && !element.isHostObject) {
                list.add(transformMembers(element))
            } else {
                list.add(element.`as`(Any::class.java))
            }
        }
        return list
    }

    private fun transformMembers(v: Value): Map<*, *> {
        val map: MutableMap<String, Any?> = mutableMapOf()
        for (key in v.memberKeys) {
            val member = v.getMember(key)
            if (member.hasArrayElements() && !member.isHostObject) {
                map[key] = transformArray(member)
            } else if (member.hasMembers() && !member.isHostObject) {
                map[key] = transformMembers(member)
            } else {
                map[key] = member.`as`(Any::class.java)
            }
        }
        return map
    }
}

object TestDataGenerator {
    private const val STRING_LENGTH = 20
    private const val ELEMENT_SIZE = 20

    fun listWithMaps(size: Int): List<Map<String, String>> = (0 until size).map {
        (1..ELEMENT_SIZE).associate {
            "$it" to RandomStringUtils.randomAlphanumeric(STRING_LENGTH)
        }
    }

    fun listWithLists(size: Int): List<List<String>> = (0 until size).map {
        (1..ELEMENT_SIZE).map {
            RandomStringUtils.randomAlphanumeric(STRING_LENGTH)
        }
    }
}
