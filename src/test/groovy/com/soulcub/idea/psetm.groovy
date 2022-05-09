package com.soulcub.idea

class psetm extends BaseSpec {

    def "test"() {
        given: "clipboard content"
            def _1 = "public class WedgeConfig implements Weighted {\n" +
                    "\n" +
                    "    @NonNull\n" +
                    "    WedgeType wedgeType;\n" +
                    "    @NonNull\n" +
                    "    Integer wedgeIndex;\n" +
                    "    @NonNull\n" +
                    "    Integer weight;\n" +
                    "    @NonNull\n" +
                    "    Long bundleId;\n"
        and:
            def expected = "                .wedgeType(entity.getWedgeType())\n" +
                    "                .wedgeIndex(entity.getWedgeIndex())\n" +
                    "                .weight(entity.getWeight())\n" +
                    "                .bundleId(entity.getBundleId())\n"
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .findAll { !it.contains('class') }
                    .collect { it.replace(';', '') }
                    .collect { it ->
                        def tokens = it.split()
                        def varType = tokens[0]
                        def varName = tokens[1]
                        if (tokens.find { typeString -> ['Collection', 'Set', 'List'].any { typeString.contains(it) } }) {
                            return '.' + varName + '(remapToSet(entity.get' + varName.capitalize() + '(), ' + varType.substring(varType.indexOf('<') + 1, varType.indexOf('>')) + '::of))'
                        } else {
                            return '.' + varName + '(entity.get' + varName.capitalize() + '())'
                        }
                    }.join(System.lineSeparator())
        then:
            def resultStrings = result.split(System.lineSeparator())
            def expectedStrings = expected.split(System.lineSeparator())
            resultStrings.size() == expectedStrings.size()
            for (i in 0..<resultStrings.size()) {
                assert resultStrings[i].trim() == expectedStrings[i].trim()
            }
    }

}