package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltgspec extends BaseSpec {

    def "groovy spec"() {
        when:
            Closure<String> removeGenerics
            removeGenerics = {
                def pattern = /<[^<>]*>/
                def result = it.replaceAll(pattern, '')
                if (result != it) {
                    return removeGenerics(result)
                } else {
                    return result
                }
            }
            def parsedRawFieldsWithTypes = _1.split(System.lineSeparator())
                    .findAll { it.contains('private final') }
                    .findAll { !it.contains('=') }
                    .collect { it.replace('private final', '') }
                    .collect { it.replace(';', '') }
                    .collect { removeGenerics(it) }
                    .collect { it.trim() }
            def parsedRawFields = parsedRawFieldsWithTypes.collect { it.split()[1] }
            def parsedStringsWithClassName = _1.split(System.lineSeparator())
                    .find { it.contains('class') }
                    .split(' ')
            def className = parsedStringsWithClassName[parsedStringsWithClassName.findIndexOf { it == 'class' } + 1]

            Closure<String> prepareBaseTarget = {
                System.lineSeparator() + 'def target = new ' + className + '(' + parsedRawFields.join(', ') + ')' + System.lineSeparator()
            }
            boolean targetWasAlreadyAdded = false

            def result = _1
                    .replaceAll(',\n', ", ")
                    .split(System.lineSeparator())
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .findAll { !it.startsWith('@') }
                    .findAll { !it.contains('class') }
                    .findAll { !it.contains('TODO') }
                    .findAll { !it.contains('static') }
                    .collect { it.replace(';', '') }
                    .collect { it ->
                        if (it.contains('{')) { /*method with all parameters*/
                            def returnType = it.split(' ')[1]
                            def methodName = it.split(' ')
                                               .find { it.contains('(') }
                                               .split('\\(')[0]
                            def methodParams = it.split(methodName + '\\(')[1]
                                                 .replace(') {', '')
                            def paramsForTarget = removeGenerics(methodParams)
                                    .split(', ')
                                    .collect { it.trim() }
                                    .collect { it.startsWith('(') ? it.substring(1) : it }
                                    .collect { it.trim() }
                                    .findAll { !it.isEmpty() }
                                    .collect {
                                        if (['boolean', 'char', 'byte', 'int', 'short', 'long', 'float',
                                             'double', 'Boolean', 'Character', 'Byte', 'Integer',
                                             'Short', 'Long', 'Float', 'Double', 'String'].any { defaultType -> it.contains(defaultType) }) {
                                            it.split(' ')[1]
                                        } else {
                                            '$' + it.split(' ')[0] + '()'
                                        }
                                    }
                                    .join(', ')

                            def mocks = (parsedRawFields.collect { '1 * ' + it } + '0 * _')
                                    .join(System.lineSeparator())
                            def resultPostfix = ''
                            if (returnType.contains('Optional')) {
                                returnType = returnType.substring(returnType.indexOf('<') + 1, returnType.lastIndexOf('>'))
                                resultPostfix = '.get()'
                            }

                            def result = prepareBaseTarget() +
                                    'def "test"() {' + System.lineSeparator() +
                                    'when:' + System.lineSeparator()
                            if (returnType.contains('void')) {
                                result += 'target.' + methodName + '(' + paramsForTarget + ')' + System.lineSeparator()
                            } else {
                                result += 'def result = target.' + methodName + '(' + paramsForTarget + ')' + System.lineSeparator()
                            }
                            result +=
                                    'then:' + System.lineSeparator() +
                                            mocks + System.lineSeparator()
                            if (!returnType.contains('void')) {
                                result +=
                                        'and:' + System.lineSeparator() +
                                                'result' + resultPostfix + ' == \$' + returnType.capitalize() + '()' + System.lineSeparator()
                            }
                            targetWasAlreadyAdded = true
                            return result + '}'
                        } else if (it.contains('private final')) { /*fields to mock*/
                            if (it.contains('=')) { /*constant field, no need to mock*/
                                return ''
                            }
                            def withoutTypes = it
                            if (it.contains('<')) {
                                withoutTypes = removeGenerics(it)
                            }
                            def fieldWithType = parsedRawFieldsWithTypes.find { field -> withoutTypes.contains(field) }
                            def typeFieldArray = fieldWithType.split(' ')
                            return 'def ' + typeFieldArray[1] + ' = Mock(' + typeFieldArray[0] + ')'
                        }
                        throw new Exception('Cant process code line "' + it + '"')
                    }
                    .collect { it.trim() }
                    .findAll { !it.isEmpty() }
                    .join(System.lineSeparator()) + (targetWasAlreadyAdded ? "" : System.lineSeparator() + prepareBaseTarget())
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
                            "    public Optional<BaseCoinsAddonConfigEntity> fromBoss(DefaultWheelGameConfigBossDto bossDto) {\n",
                    "public class BaseCoinsAddonEntityToModelMapper implements AddonEntityToModelMapper {\n" +
                            "\n" +
                            "    @Getter\n" +
                            "    private final AddonType addonType = BASE_COINS;\n" +
                            "\n" +
                            "    @Override\n" +
                            "    public Optional<Addon> fromEntity(WheelGameConfigEntityWithAddons<? extends DefaultWheelGameConfigEntity<? extends DefaultWheelGameConfigEntity<? extends DefaultWheelGameConfigEntity<?>>>> from) {\n",
                    "public class DefaultWheelGameCreator implements GameCreator {\n" +
                            "\n" +
                            "    private final UserGamesOperations userGamesOperations;\n" +
                            "    private final UserWheelGamesInfoMutators userWheelGamesInfoMutators;\n" +
                            "    private final GameCreatedKafkaEventFacade gameCreatedKafkaEventFacade;\n" +
                            "    private final SegmentationFacade segmentationFacade;\n" +
                            "    private final MessagingFacade messagingFacade;\n" +
                            "    private final GameGuidSupplier gameGuidSupplier;\n" +
                            "    private final GeneralFeatureStateService generalFeatureStateService;\n" +
                            "    private final FeatureStateCreator featureStateCreator;\n",
                    "public class GameConfigIdFromSkuDataExtractor {\n" +
                            "\n" +
                            "    public final static String SUB_SKU_DELIMITER = \"::\";\n" +
                            "\n" +
                            "    public String extractGameConfigId(RewardDto rewardDto) {\n"
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
                            "    }\n",
                    "    def target = new BaseCoinsAddonEntityToModelMapper()\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.fromEntity(\$WheelGameConfigEntityWithAddons())\n" +
                            "        then:\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result.get() == \$Addon()\n" +
                            "    }\n",
                    "    def userGamesOperations = Mock(UserGamesOperations)\n" +
                            "    def userWheelGamesInfoMutators = Mock(UserWheelGamesInfoMutators)\n" +
                            "    def gameCreatedKafkaEventFacade = Mock(GameCreatedKafkaEventFacade)\n" +
                            "    def segmentationFacade = Mock(SegmentationFacade)\n" +
                            "    def messagingFacade = Mock(MessagingFacade)\n" +
                            "    def gameGuidSupplier = Mock(GameGuidSupplier)\n" +
                            "    def generalFeatureStateService = Mock(GeneralFeatureStateService)\n" +
                            "    def featureStateCreator = Mock(FeatureStateCreator)\n" +
                            "\n" +
                            "    def target = new DefaultWheelGameCreator(userGamesOperations, userWheelGamesInfoMutators, gameCreatedKafkaEventFacade, segmentationFacade, messagingFacade, gameGuidSupplier, generalFeatureStateService, featureStateCreator)\n",
                    "    def target = new GameConfigIdFromSkuDataExtractor()\n" +
                            "\n" +
                            "    def \"test\"() {\n" +
                            "        when:\n" +
                            "            def result = target.extractGameConfigId(\$RewardDto())\n" +
                            "        then:\n" +
                            "            0 * _\n" +
                            "        and:\n" +
                            "            result == \$String()\n" +
                            "    }"
            ]
    }

}
