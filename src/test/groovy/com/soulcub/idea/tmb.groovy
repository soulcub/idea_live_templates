package com.soulcub.idea

import spock.lang.Specification

class psmte extends Specification {

    static String replacement = [
            '"': '&quot;',
            '>': '&gt;',
            '<': '&lt;',
    ]

    def "psmte"() {
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
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .collect { it.replace(';', '') }
                    .collect { it.replace('{', '') }
                    .collect { it.trim() }
                    .collect { it ->
                        def tokens = it.split()
                        if (tokens.find { it == 'class' }) {
                            def className = tokens[2]
                            return className + ' ' + className.uncapitalize() + '() {' + System.lineSeparator() + className + '.builder()'
                        } else if (tokens.find { it.contains('<') }) {
                            def varName = tokens[1]
                            return '.' + varName + '(' + varName + ')'
                        } else {
                            def varName = tokens[1]
                            return '.' + varName + '(' + varName + ')'
                        }
                    }
                    .join(System.lineSeparator())
            + System.lineSeparator()
            + '.build()'
            + System.lineSeparator()
            + '}'
        then:
            result == "    public static UserGamesPerConfigEntity of(UserGamesPerConfig model) {\n" +
                    "        return builder()\n" +
                    "                .id(GameInfoKeyEntity.of(model.getUserId(), model.getGameConfigId()))\n" +
                    "                .gameType(model.getGameType())\n" +
                    "                .byFeatureGames(remapToList(model.getFeaturedGames().values(), UserGamesEntity::of))\n" +
                    "                .version(model.getVersion())\n" +
                    "                .build();\n" +
                    "    }\n"
    }

}