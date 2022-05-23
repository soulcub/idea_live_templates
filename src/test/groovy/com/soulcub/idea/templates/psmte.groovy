package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class psmte extends BaseSpec {

    def "public static model to entity"() {
        given: "clipboard content"
            def _1 = "public class UserGamesPerConfigEntity implements Versioned<UserGamesPerConfigEntity> {\n" +
                    "\n" +
                    "    @Id\n" +
                    "    @NonNull\n" +
                    "    GameInfoKeyEntity id;\n" +
                    "    @NonNull\n" +
                    "    GameType gameType;\n" +
                    "    @NonNull\n" +
                    "    Collection<UserGamesEntity> byFeatureGames;\n" +
                    "    @With\n" +
                    "    @Version\n" +
                    "    @NonFinal\n" +
                    "    long version;" +
                    "    @Singular\n" +
                    "    @NonNull\n" +
                    "    Map<String, UserGamesInfo> featuredGames;\n"
        and:
            def expected = "         .id(model.getId())\n" +
                    "                .gameType(model.getGameType())\n" +
                    "                .byFeatureGames(model.getByFeatureGames())\n" +
                    "                .version(model.getVersion())\n" +
                    "                .featuredGames(model.getFeaturedGames())\n"
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .findAll { !it.contains('class') }
                    .collect { it.replace(';', '') }
                    .collect { if (it.contains('<')) return it.substring(0, it.indexOf('<')) + it.substring(it.lastIndexOf(' ')) else return it }
                    .collect { it ->
                        def tokens = it.split();
                        def varName = tokens[1];
                        return '.' + varName + '(model.get' + varName.capitalize() + '())'
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