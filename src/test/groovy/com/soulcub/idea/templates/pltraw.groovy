package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltraw extends BaseSpec {

    def "test model raw"() {
        given: "clipboard content"
            def _1 = "@Value\n" +
                    "@Builder\n" +
                    "@Document(collection = 'smth', expiration = AerospikeConstants.NEVER_EXPIRE)\n" +
                    "public class BonusTriggerConfigEntity implements ConfigEntity {\n" +
                    "\n" +
                    "    @Id\n" +
                    "    @Builder.Default\n" +
                    "    String key = ConfigurationKey.BONUS_CONFIGURATION.name();\n" +
                    "    boolean enabled;\n" +
                    "    @NonNull\n" +
                    "    List<BonusRewardConfigEntity> rewardConfigs;"
        and:
            def expected = "    /**\n" +
                    "     * @see BonusTriggerConfigEntity\n" +
                    "     */\n" +
                    "    Map bonusTriggerConfigEntityRaw() {\n" +
                    "        [\n" +
                    "                key: key,\n" +
                    "                enabled: enabled,\n" +
                    "                rewardConfigs: rewardConfigs\n" +
                    "        ]\n" +
                    "    }\n"
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .collect { it.replace(';', '') }
                    .collect { it.replace('{', '') }
                    .collect { it.trim() }
                    .collect { String it ->
                        if (it.contains('<')) {
                            def varName = it.substring(it.indexOf('>') + 1).trim();
                            def result = varName + ': ' + varName;
                            if (!_1.split(System.lineSeparator()).last().contains(it.trim())) {
                                result = result + ','
                            }
                            return result
                        } else {
                            def tokens = it.split();
                            if (tokens.find { it == 'class' }) {
                                def className = tokens[2];
                                return '    /**' + System.lineSeparator() + '    * @see ' + className + System.lineSeparator() + '    */' + System.lineSeparator() + 'Map ' + className.uncapitalize() + 'Raw() {' + System.lineSeparator() + '['
                            } else {
                                def varName = tokens[1];
                                def result = varName + ': ' + varName;
                                if (!_1.split(System.lineSeparator()).last().contains(it.trim())) {
                                    result = result + ','
                                }
                                return result
                            }
                        }
                    }
                    .join(System.lineSeparator()) + System.lineSeparator() + ']' + System.lineSeparator() + '}'
        then:
            def resultStrings = result.split(System.lineSeparator())
            def expectedStrings = expected.split(System.lineSeparator())
            resultStrings.size() == expectedStrings.size()
            for (i in 0..<resultStrings.size()) {
                assert resultStrings[i].trim() == expectedStrings[i].trim()
            }
    }

}
