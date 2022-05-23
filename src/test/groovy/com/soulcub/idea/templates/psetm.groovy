package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class psetm extends BaseSpec {

    def "public static entity to model"() {
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
                    "    Long bundleId;\n" +
                    "    @Singular\n" +
                    "    @NonNull\n" +
                    "    Map<String, UserGamesInfo> featuredGames;\n" +
                    "    @NonNull\n" +
                    "    Collection<UserGamesEntity> byFeatureGames;\n"
        and:
            def expected = "                .wedgeType(entity.getWedgeType())\n" +
                    "                .wedgeIndex(entity.getWedgeIndex())\n" +
                    "                .weight(entity.getWeight())\n" +
                    "                .bundleId(entity.getBundleId())\n" +
                    "                .featuredGames(entity.getFeaturedGames())\n" +
                    "                .byFeatureGames(entity.getByFeatureGames())\n"
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
                        return '.' + varName + '(entity.get' + varName.capitalize() + '())'
                    }.join(System.lineSeparator())
        then:
            assertResult(expected, result)
    }

}