package com.soulcub.idea

import spock.lang.Specification

class BaseSpec extends Specification {

    static String pathToSaveScript = "/Users/SStorozhev/IdeaProjects/ideaTemplates"

    static String replacement = [
            '"': '&quot;',
            '>': '&gt;',
            '<': '&lt;',
    ]

    def cleanupSpec() {
        def pathToTestFileClass = this.class.classLoader.getResource(this.class.name.replace('.', '/') + ".class").getPath()
        def pathToTestFile = pathToTestFileClass.replace("target/test-classes", "src/test/groovy").replace(".class", ".groovy")
        def templateName = this.class.simpleName
        def codeLines = new File(pathToTestFile).readLines()
        def firstScriptLine = codeLines.findIndexOf { it.trim() == "when:" } + 1
        def lastScriptLine = codeLines.findIndexOf { it.trim() == "then:" } - 1
        def rawGroovyScript = codeLines.subList(firstScriptLine, lastScriptLine + 1)
                .collect { it.trim() }
                .collect { if (it == "def result = _1") return "return _1" else return it }
        def fileName = pathToSaveScript + '/' + templateName + '.groovy'
        println 'file name ' + fileName
        println templateName

        writeToFile(fileName, rawGroovyScript)

        println rawGroovyScript
        println "---------=========RESULT=========---------"
        println "groovyScript(\"" + fileName + "\", clipboard())"
        println "---------========================---------"
    }

    private void writeToFile(String fileName, rawGroovyScript) {
        def file = new File(fileName)
        if (file.getParentFile() != null && !file.getParentFile().mkdirs()) {
            // handle permission problems here if needed
        }
        file.createNewFile()
        file.newWriter().withWriter { out -> rawGroovyScript.each { out.println(it) } }
    }

    def assertResult(String expected, String result) {
        def resultStrings = splitAndTrim(result)
        def expectedStrings = splitAndTrim(expected)
        resultStrings.size() == expectedStrings.size()
        for (i in 0..<resultStrings.size()) {
            assert resultStrings[i] == expectedStrings[i]
        }
        true
    }

    private splitAndTrim(String input) {
        input.split(System.lineSeparator())
                .collect { it.trim() }
                .findAll { !it.empty }
    }

}