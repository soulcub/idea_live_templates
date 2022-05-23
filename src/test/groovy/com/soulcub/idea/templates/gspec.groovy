package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class gspec extends BaseSpec {

    def "groovy spec"() {
        when:
            def parsedRawFieldsWithTypes = _1.split(System.lineSeparator())
                    .findAll { it.contains('private final') }
                    .collect { it.replace('private final', '') }
                    .collect { it.replace(';', '') }
                    .collect { it.trim() };
            def parsedRawFields = parsedRawFieldsWithTypes.collect { it.split()[1] };
            def parsedStringsWithClassName = _1.split(System.lineSeparator())
                    .find { it.contains('class') }
                    .split(' ');
            def className = parsedStringsWithClassName[parsedStringsWithClassName.findIndexOf { it == 'class' } + 1];
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .findAll { !it.contains('class') }
                    .findAll { !it.contains('TODO') }
                    .collect { it.replace(';', '') }
                    .collect { it ->
                        if (it.contains('{')) {
                            def returnType = it.split(' ')[1];
                            def methodName = it.split(' ')
                                    .find { it.contains('(') }
                                    .split('\\(')[0];
                            def methodParams = it.split(methodName)[1]
                                    .replace(' {', '')
                                    .split(', ')
                                    .collect { it.split(' ')[1] }
                                    .join(', ');
                            def mocks = (parsedRawFields.collect { '1 * ' + it } + '0 * _')
                                    .join(System.lineSeparator());
                            def resultPostfix = '';
                            if (returnType.contains('Optional')) { returnType = returnType.substring(returnType.indexOf('<') + 1, returnType.indexOf('>')); resultPostfix = '.get()'; }
                            return System.lineSeparator() +
                                    'def target = new ' + className + '(' + parsedRawFields.join(', ') + ')' + System.lineSeparator() +
                                    'def "test"() {' + System.lineSeparator() +
                                    'when:' + System.lineSeparator() +
                                    'def result = target.' + methodName + '(' + methodParams  + System.lineSeparator() +
                                    'then:' + System.lineSeparator() +
                                    mocks + System.lineSeparator() +
                                    'and:' + System.lineSeparator() +
                                    'result' + resultPostfix + ' == ' + returnType.uncapitalize() + '()' + System.lineSeparator() +
                                    '}'
                        } else if (it.contains('private final')) {
                            def fieldWithType = parsedRawFieldsWithTypes.find { field -> it.contains(field) };
                            def typeFieldArray = fieldWithType.split(' ')
                            return 'def ' + typeFieldArray[1] + ' = Mock(' + typeFieldArray[0] + ')'
                        }
                        throw new Exception('Cant process code line "' + it + '"')
                    }.join(System.lineSeparator())
        then:
            assertResult(expected, result)
        where: "clipboard content"
            _1 << [
                    "public class WheelGameInfoService {\n" +
                            "\n" +
                            "    private final UserGamesOperations userGamesOperations;\n" +
                            "    private final ActiveAndDeletedConfigsResolver activeAndDeletedConfigsResolver;\n" +
                            "\n" +
                            "    //TODO: Save config of active game to user state to avoid problems after config change\n" +
                            "    @Debug\n" +
                            "    public Optional<WheelGameInfoDto> getGameInfoDto(long userId, String featureType, String gameConfigId, String gameGuid) {\n",
                    "public class WheelGameInfoService {\n" +
                            "\n" +
                            "    private final UserGamesOperations userGamesOperations;\n" +
                            "    private final ActiveAndDeletedConfigsResolver activeAndDeletedConfigsResolver;\n" +
                            "\n" +
                            "    //TODO: Save config of active game to user state to avoid problems after config change\n" +
                            "    @Debug\n" +
                            "    public WheelGameInfoDto getGameInfoDto(long userId, String featureType, String gameConfigId, String gameGuid) {\n",
                    "public class RandomWedgeSelector {\n" +
                            "\n" +
                            "    private final RandomUtils randomUtils;\n" +
                            "\n" +
                            "    @Debug\n" +
                            "    public WedgeConfig select(DefaultWheelGameConfig wheelGameConfig) {\n"
            ]
            expected << [
                    "    def userGamesOperations = Mock(UserGamesOperations)\n" +
                            "    def activeAndDeletedConfigsResolver = Mock(ActiveAndDeletedConfigsResolver)\n" +
                            "\n" +
                            "    def target = new WheelGameInfoService(userGamesOperations, activeAndDeletedConfigsResolver)\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.getGameInfoDto(userId, featureType, gameConfigId, gameGuid)\n" +
                            "        then:\n" +
                            "            1 * userGamesOperations\n" +
                            "            1 * activeAndDeletedConfigsResolver\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result.get() == wheelGameInfoDto()\n" +
                            "    }\n",
                    "    def userGamesOperations = Mock(UserGamesOperations)\n" +
                            "    def activeAndDeletedConfigsResolver = Mock(ActiveAndDeletedConfigsResolver)\n" +
                            "\n" +
                            "    def target = new WheelGameInfoService(userGamesOperations, activeAndDeletedConfigsResolver)\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.getGameInfoDto(userId, featureType, gameConfigId, gameGuid)\n" +
                            "        then:\n" +
                            "            1 * userGamesOperations\n" +
                            "            1 * activeAndDeletedConfigsResolver\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result == wheelGameInfoDto()\n" +
                            "    }\n",
                    "    def randomUtils = Mock(RandomUtils)\n" +
                            "    \n" +
                            "    def target = new RandomWedgeSelector(randomUtils)\n" +
                            "    \n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.select(wheelGameConfig)\n" +
                            "        then:\n" +
                            "            1 * randomUtils\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result == wedgeConfig()\n" +
                            "    }"
            ]
    }

}