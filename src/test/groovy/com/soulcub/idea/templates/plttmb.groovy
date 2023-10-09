package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class plttmb extends BaseSpec {

    def "test model builder"() {
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .findAll { !it.startsWith('/*') }
                    .findAll { !it.startsWith('*') }
                    .findAll { !it.contains('=') }
                    .collect { it.replace(';', '') }
                    .collect { it.replace('{', '') }
                    .collect { it.trim() }
                    .collect { String it ->
                        if (it.contains('<') && !it.contains('class')) { /*if we have found smth with generic, like List<Object> or Map<String, String>*/
                            def varName = it.substring(it.indexOf('>') + 1).trim();
                            return '.' + varName + '(' + varName + ')'
                        } else {
                            def tokens = it.split();
                            if (tokens.find { it == 'class' }) {
                                def className = tokens[2];
                                return className + ' \$' + className + '(@DelegatesTo(' + className + '.' + className + 'Builder) Closure closure = {}) {' + System.lineSeparator() + className + '.builder()'
                            } else {
                                def varName = tokens[1];
                                return '.' + varName + '(' + varName + ')'
                            }
                        }
                    }
                    .join(System.lineSeparator()) + System.lineSeparator() + '.tap(closure)' + System.lineSeparator() + '.build()' + System.lineSeparator() + '}'
        then:
            def resultStrings = result.split(System.lineSeparator())
            def expectedStrings = expected.split(System.lineSeparator())
            resultStrings.size() == expectedStrings.size()
            for (i in 0..<resultStrings.size()) {
                assert resultStrings[i].trim() == expectedStrings[i].trim()
            }
        where: "clipboard content"
            _1 << [
                    "public class UserGamesPerConfig {\n" +
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
                            "    long version;\n",
                    "public class LoginBonusGemsMultipliersAddon implements Addon {\n" +
                            "\n" +
                            "    @Getter\n" +
                            "    AddonType addonType = AddonType.LOGIN_BONUS_GEM_MULTIPLIERS;\n" +
                            "    @NonNull\n" +
                            "    String innerConfigId;\n",
                    "public class DefaultWheelGameConfig implements WeightedConfig<WedgeConfig> {\n" +
                            "\n" +
                            "    @NonNull\n" +
                            "    Long assetId;\n" +
                            "    @NonNull\n" +
                            "    String bossGuid;\n" +
                            "    /**\n" +
                            "     * Key - weight value for current wedge.\n" +
                            "     */\n" +
                            "    @NonNull\n" +
                            "    NavigableMap<Integer, WedgeConfig> weightedWedges;\n" +
                            "\n" +
                            "    @NonNull\n" +
                            "    List<WedgeConfig> allWedges;\n" +
                            "\n" +
                            "    @Singular\n" +
                            "    Map<AddonType, Addon> addons;"
            ]
            expected << [
                    "        UserGamesPerConfig \$UserGamesPerConfig(@DelegatesTo(UserGamesPerConfig.UserGamesPerConfigBuilder) Closure closure = {}) {\n" +
                            "        UserGamesPerConfig.builder()\n" +
                            "                .userId(userId)\n" +
                            "                .gameConfigId(gameConfigId)\n" +
                            "                .gameType(gameType)\n" +
                            "                .featuredGames(featuredGames)\n" +
                            "                .version(version)\n" +
                            "                .tap(closure)\n" +
                            "                .build()\n" +
                            "    }\n",
                    "    LoginBonusGemsMultipliersAddon \$LoginBonusGemsMultipliersAddon(@DelegatesTo(LoginBonusGemsMultipliersAddon.LoginBonusGemsMultipliersAddonBuilder) Closure closure = {}) {\n" +
                            "        LoginBonusGemsMultipliersAddon.builder()\n" +
                            "                .innerConfigId(innerConfigId)\n" +
                            "                .tap(closure)\n" +
                            "                .build()\n" +
                            "    }\n",
                    "    DefaultWheelGameConfig \$DefaultWheelGameConfig(@DelegatesTo(DefaultWheelGameConfig.DefaultWheelGameConfigBuilder) Closure closure = {}) {\n" +
                            "        DefaultWheelGameConfig.builder()\n" +
                            "                .assetId(assetId)\n" +
                            "                .bossGuid(bossGuid)\n" +
                            "                .weightedWedges(weightedWedges)\n" +
                            "                .allWedges(allWedges)\n" +
                            "                .addons(addons)\n" +
                            "                .tap(closure)\n" +
                            "                .build()\n" +
                            "    }\n"
            ]
    }

}
