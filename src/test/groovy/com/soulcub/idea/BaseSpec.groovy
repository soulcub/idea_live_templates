package com.soulcub.idea

import spock.lang.Specification

class BaseSpec extends Specification {

    static String replacement = [
            '"': '&quot;',
            '>': '&gt;',
            '<': '&lt;',
    ]

    def cleanup() {
        def pathToTestFileClass = this.class.classLoader.getResource(this.class.name.replace('.', '/') + ".class").getPath()
        def pathToTestFile = pathToTestFileClass.replace("target/test-classes", "src/test/groovy").replace(".class", ".groovy")
        println this.class.simpleName
        def codeLines = new File(pathToTestFile).readLines()
        def firstScriptLine = codeLines.findIndexOf { it.trim() == "_1" }
        def lastScriptLine = codeLines.findIndexOf { it.contains("join(System.lineSeparator())") }
        def rawGroovyScript = codeLines.subList(firstScriptLine, lastScriptLine + 1)
                .collect { it.trim() }
                .join(" ")
        println rawGroovyScript
        println "---------=========RESULT=========---------"
        println "groovyScript(\"" + rawGroovyScript + "\", clipboard())"
        println "---------========================---------"
    }

    def assertResult(String expected, String result) {
        def resultStrings = result.split(System.lineSeparator())
        def expectedStrings = expected.split(System.lineSeparator())
        resultStrings.size() == expectedStrings.size()
        for (i in 0..<resultStrings.size()) {
            assert resultStrings[i].trim() == expectedStrings[i].trim()
        }
        true
    }

}