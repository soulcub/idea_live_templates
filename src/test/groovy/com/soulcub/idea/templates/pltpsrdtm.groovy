package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltpsrdtm extends BaseSpec {

    def "public static record dto to model"() {
        given: "clipboard content"
            def _1 = "public record RewardConfigAdminDto(\n" +
                    "\n" +
                    "   @NonNull WedgeType wedgeType,\n" +
                    "   @NonNull Integer wedgeIndex,\n" +
                    "   @NonNull Integer weight,\n" +
                    "   @NonNull Long bundleId,\n" +
                    "   @NonNull Map<String, UserGamesInfo> featuredGames,\n" +
                    "   @NonNull Collection<UserGamesEntity> byFeatureGames\n"
        and:
            def expected = "                .wedgeType(dto.getWedgeType())\n" +
                    "                .wedgeIndex(dto.getWedgeIndex())\n" +
                    "                .weight(dto.getWeight())\n" +
                    "                .bundleId(dto.getBundleId())\n" +
                    "                .featuredGames(dto.getFeaturedGames())\n" +
                    "                .byFeatureGames(dto.getByFeatureGames())\n"
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.replaceAll("@[^ ]+", "") }
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.contains('record') }
                    .collect { it.replace(',', '') }
                    .collect { it.trim() }
                    .collect { if (it.contains('<')) return it.substring(0, it.indexOf('<')) + it.substring(it.lastIndexOf(' ')) else return it }
                    .collect { it ->
                        def tokens = it.split();
                        def varName = tokens[1];
                        return '.' + varName + '(dto.get' + varName.capitalize() + '())'
                    }.join(System.lineSeparator())
        then:
            assertResult(expected, result)
    }

}
