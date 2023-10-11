package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltgspec extends BaseSpec {

    def "groovy spec"() {
        when:
            def parsedRawFieldsWithTypes = _1.split(System.lineSeparator())
                    .findAll { it.contains('private final') }
                    .findAll { !it.contains('=') }
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
                    .replaceAll(',\n', ", ")
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .findAll { !it.contains('class') }
                    .findAll { !it.contains('TODO') }
                    .findAll { !it.contains('public static') }
                    .collect { it.replace(';', '') }
                    .collect { it ->
                        if (it.contains('{')) { /*method with all parameters*/
                            def returnType = it.split(' ')[1];
                            def methodName = it.split(' ')
                                    .find { it.contains('(') }
                                    .split('\\(')[0];
                            def methodParams = it.split(methodName)[1]
                                                 .replace(') {', '')
                                                 .split(', ')
                                                 .collect { it.trim() }
                                                 .collect { it.startsWith('(') ? it.substring(1) : it }
                                                 .collect { it.trim() }
                                                 .findAll { !it.isEmpty() }
                                                 .collect {
                                                     if (['boolean', 'char', 'byte', 'int', 'short', 'long', 'float',
                                                          'double', 'Boolean', 'Character', 'Byte', 'Integer',
                                                          'Short', 'Long', 'Float', 'Double', 'String'].any { defaultType ->
                                                         it.contains(defaultType)}) {
                                                         it.split(' ')[1]
                                                     } else {
                                                         '$' + it.split(' ')[0] + '()'
                                                     }
                                                 }
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
                                                'result' + resultPostfix + ' == \$' + returnType.capitalize() + '()' + System.lineSeparator()
                            }
                            return withoutResultAssertion + '}'
                        } else if (it.contains('private final')) { /*fields to mock*/
                            if (it.contains('=')) { /*constant field, no need to mock*/
                                return ''
                            }
                            def withoutTypes = it;
                            if (it.contains('<')) {
                                withoutTypes = it.substring(0, it.indexOf('<')) + it.substring(it.indexOf('>') + 1, it.length())
                            }
                            def fieldWithType = parsedRawFieldsWithTypes.find { field -> withoutTypes.contains(field) };
                            def typeFieldArray = fieldWithType.split(' ');
                            return 'def ' + typeFieldArray[1] + ' = Mock(' + typeFieldArray[0] + ')'
                        }
                        throw new Exception('Cant process code line "' + it + '"')
                    }
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .join(System.lineSeparator())
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
                            "    public List<JackpotConfigDto> getAllJackpotConfigs() {\n",
                    "public class LoginBonusStateCreator implements InitialFeatureStateExtractor {\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public FeatureState create(Long userId,\n" +
                            "                               String userGameGuid,\n" +
                            "                               RewardDto rewardDto,\n" +
                            "                               DefaultWheelGameConfig defaultWheelGameConfig) {\n",
                    "public class BaseCoinsAddonBossAndEntityMapper implements AddonBossAndEntityMapper<BaseCoinsAddonConfigEntity> {\n" +
                            "\n" +
                            "    @Getter\n" +
                            "    private final AddonType addonType = LOGIN_BONUS_BASE_COINS;\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public Optional<BaseCoinsAddonConfigEntity> fromBoss(DefaultWheelGameConfigBossDto bossDto) {\n"
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
                            "            result.get() == \$WheelGameInfoDto()\n" +
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
                            "            result == \$WheelGameInfoDto()\n" +
                            "    }\n",
                    "    def randomUtils = Mock(RandomUtils)\n" +
                            "    \n" +
                            "    def target = new RandomWedgeSelector(randomUtils)\n" +
                            "    \n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.select(\$DefaultWheelGameConfig())\n" +
                            "        then:\n" +
                            "            1 * randomUtils\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result == \$WedgeConfig()\n" +
                            "    }",
                    "    def messageTtl = Mock(Duration)\n" +
                            "    def messagingKafkaTemplate = Mock(KafkaTemplate)\n" +
                            "    def taskCompletedMessageType = Mock(String)\n" +
                            "\n" +
                            "    def target = new MessagingFacade(messageTtl, messagingKafkaTemplate, taskCompletedMessageType)\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            target.sendAchievedRuleMessage(userId, \$GameCreatedMessagingDto())\n" +
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
                            "            result == \$List<JackpotConfigDto>()\n" +
                            "    }",
                    "    def target = new LoginBonusStateCreator()\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.create(userId, userGameGuid, \$RewardDto(), \$DefaultWheelGameConfig())\n" +
                            "        then:\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result == \$FeatureState()\n" +
                            "    }\n",
                    "    def target = new BaseCoinsAddonBossAndEntityMapper()\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.fromBoss(\$DefaultWheelGameConfigBossDto())\n" +
                            "        then:\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result.get() == \$BaseCoinsAddonConfigEntity()\n" +
                            "    }\n"
            ]
    }

}
