package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltpsrdte extends BaseSpec {

    def "public static record dto to entity"() {
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
            def expected = "                .wedgeType(dto.wedgeType())\n" +
                    "                .wedgeIndex(dto.wedgeIndex())\n" +
                    "                .weight(dto.weight())\n" +
                    "                .bundleId(dto.bundleId())\n" +
                    "                .featuredGames(dto.featuredGames())\n" +
                    "                .byFeatureGames(dto.byFeatureGames())\n"
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
                        return '.' + varName + '(dto.' + varName + '())'
                    }.join(System.lineSeparator())
        then:
            assertResult(expected, result)
    }

}
