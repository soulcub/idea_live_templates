package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltbtmb extends BaseSpec {

    def "test model builder"() {
        given: "clipboard content"
            def _1 = "public class UserGamesPerConfig {\n" +
                    "\n" +
                    "    @NonNull\n" +
                    "    Long userId;\n" +
                    "    @NonNull\n" +
                    "    String gameConfigId;\n" +
                    "    @NonNull\n" +
                    "    GameType gameType;\n" +
                    "    @Singular\n" +
                    "    @NonNull\n" +
                    "    Map<String, UserGamesInfo> featuredGames;\n" +
                    "    long version;\n"
        and:
            def expected = "        UserGamesPerConfig.UserGamesPerConfigBuilder userGamesPerConfigBuilder() {\n" +
                    "        UserGamesPerConfig.builder()\n" +
                    "                .userId(userId)\n" +
                    "                .gameConfigId(gameConfigId)\n" +
                    "                .gameType(gameType)\n" +
                    "                .featuredGames(featuredGames)\n" +
                    "                .version(version)\n" +
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
                            return '.' + varName + '(' + varName + ')'
                        } else {
                            def tokens = it.split();
                            if (tokens.find { it == 'class' }) {
                                def className = tokens[2];
                                return className + '.' + className + 'Builder ' + className.uncapitalize() + 'Builder() {' + System.lineSeparator() + className + '.builder()'
                            } else {
                                def varName = tokens[1];
                                return '.' + varName + '(' + varName + ')'
                            }
                        }
                    }
                    .join(System.lineSeparator()) + System.lineSeparator() + '}'
        then:
            def resultStrings = result.split(System.lineSeparator())
            def expectedStrings = expected.split(System.lineSeparator())
            resultStrings.size() == expectedStrings.size()
            for (i in 0..<resultStrings.size()) {
                assert resultStrings[i].trim() == expectedStrings[i].trim()
            }
    }

}
