package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltgspec extends BaseSpec {

    def "groovy spec"() {
        when:
            def parsedRawFieldsWithTypes = _1.split(System.lineSeparator())
                    .findAll { it.contains('private final') }
                    .collect { it.replace('private final', '') }
                    .collect { it.replace(';', '') }
                    .collect { if (!it.contains('<')) return it else it.substring(0, it.indexOf('<')) + it.substring(it.indexOf('>') + 1, it.length()) }
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
                        if (it.contains('{')) { /*method to run full string*/
                            def returnType = it.split(' ')[1];
                            def methodName = it.split(' ')
                                    .find { it.contains('(') }
                                    .split('\\(')[0];
                            def methodParams = it.split(methodName)[1]
                                    .replace(') {', '')
                                    .split(', ')
                                    .collect { it.trim() }
                                    .findAll { !it.isEmpty() }
                                    .findAll { it != '(' } /*in case we don't have parameters*/
                                    .collect { it.split(' ')[1] }
                                    .join(', ');
                            def mocks = (parsedRawFields.collect { '1 * ' + it } + '0 * _')
                                    .join(System.lineSeparator());
                            def resultPostfix = '';
                            if (returnType.contains('Optional')) { returnType = returnType.substring(returnType.indexOf('<') + 1, returnType.indexOf('>')); resultPostfix = '.get()'; }

                            def withoutResultAssertion = System.lineSeparator() +
                                    'def target = new ' + className + '(' + parsedRawFields.join(', ') + ')' + System.lineSeparator() +
                                    'def "test"() {' + System.lineSeparator() +
                                    'when:' + System.lineSeparator();
                            if (returnType.contains('void')) {
                                withoutResultAssertion += 'target.' + methodName + '(' + methodParams + ')' + System.lineSeparator()
                            } else {
                                withoutResultAssertion += 'def result = target.' + methodName + '(' + methodParams + ')' + System.lineSeparator()
                            }
                            withoutResultAssertion +=
                                    'then:' + System.lineSeparator() +
                                            mocks + System.lineSeparator()
                            if (!returnType.contains('void')) {
                                withoutResultAssertion +=
                                        'and:' + System.lineSeparator() +
                                                'result' + resultPostfix + ' == ' + returnType.uncapitalize() + '()' + System.lineSeparator()
                            }
                            return withoutResultAssertion + '}'
                        } else if (it.contains('private final')) { /*fields*/
                            def withoutTypes = it;
                            if (it.contains('<')) {
                                withoutTypes = it.substring(0, it.indexOf('<')) + it.substring(it.indexOf('>') + 1, it.length())
                            }
                            def fieldWithType = parsedRawFieldsWithTypes.find { field -> withoutTypes.contains(field) };
                            def typeFieldArray = fieldWithType.split(' ');
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
                            "    public WedgeConfig select(DefaultWheelGameConfig wheelGameConfig) {\n",
                    "public class MessagingFacade {\n" +
                            "\n" +
                            "    private final Duration messageTtl;\n" +
                            "    private final KafkaTemplate<String, UserMessage> messagingKafkaTemplate;\n" +
                            "    private final String taskCompletedMessageType;\n" +
                            "\n" +
                            "    @Debug\n" +
                            "    @Metered(name = \"task_finished_client_messaging\")\n" +
                            "    public void sendAchievedRuleMessage(Long userId, GameCreatedMessagingDto gameCreatedMessagingDto) {\n",
                    "public class AdminConfigsController {\n" +
                            "\n" +
                            "    private final AdminConfigsService adminConfigsService;\n" +
                            "\n" +
                            "    public List<JackpotConfigDto> getAllJackpotConfigs() {\n"
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
                            "    }",
                    "    def messageTtl = Mock(Duration)\n" +
                            "    def messagingKafkaTemplate = Mock(KafkaTemplate)\n" +
                            "    def taskCompletedMessageType = Mock(String)\n" +
                            "\n" +
                            "    def target = new MessagingFacade(messageTtl, messagingKafkaTemplate, taskCompletedMessageType)\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            target.sendAchievedRuleMessage(userId, gameCreatedMessagingDto)\n" +
                            "        then:\n" +
                            "            1 * messageTtl\n" +
                            "            1 * messagingKafkaTemplate\n" +
                            "            1 * taskCompletedMessageType\n" +
                            "            0 * _\n" +
                            "    }",
                    "    def adminConfigsService = Mock(AdminConfigsService)\n" +
                            "\n" +
                            "    def target = new AdminConfigsController(adminConfigsService)\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.getAllJackpotConfigs()\n" +
                            "        then:\n" +
                            "            1 * adminConfigsService\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result == list<JackpotConfigDto>()\n" +
                            "    }"
            ]
    }

}
