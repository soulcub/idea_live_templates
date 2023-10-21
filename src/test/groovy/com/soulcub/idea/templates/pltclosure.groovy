package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltclosure extends BaseSpec {

    def "extend mock to closure"() {
        when:
            def nonPrimitiveObjectMutator = { String it ->
                it.replace('\$', '').replace('()', '').uncapitalize()
            }
            def parameterNameMutator = { String it ->
                def result = 'p_' + nonPrimitiveObjectMutator(it.trim().replaceAll(/[\[\]]/, ''))
                if (it.contains('[')) {
                    result += 's'
                }
                result
            }
            def methodCallAndOther = _1.split('\\(', 2)
            def paramsAndMockReturn = methodCallAndOther[1].split('>>')
            def parameters = paramsAndMockReturn[0].replace('))', ')')
                                                   .replace('()', 'parenthesesplaceholder')
                                                   .replace(')', '')
                                                   .replace('parenthesesplaceholder', '()')
                                                   .split(', ')
            def result = methodCallAndOther[0] + '(' +
                    parameters.collect { '_' }.join(', ') +
                    ') >> { ' +
                    parameters.collect { 'def ' + parameterNameMutator(it) }.join(', ') + ' ->' +
                    System.lineSeparator() +
                    parameters.collect { 'assert ' + parameterNameMutator(it) + ' == ' + it }.join(System.lineSeparator()) +
                    System.lineSeparator()
            if (paramsAndMockReturn.size() > 1) {
                result += paramsAndMockReturn[1].trim() +
                        System.lineSeparator()
            }
            result += '}'
        then:
            def resultStrings = result.split(System.lineSeparator())
            def expectedStrings = expected.split(System.lineSeparator())
            for (i in 0..<resultStrings.size()) {
                assert resultStrings[i].trim() == expectedStrings[i].trim()
            }
            resultStrings.size() == expectedStrings.size()
        where: "clipboard content"
            _1 << [
                    "            1 * addonRewardCreator.create(\$PlayGameRequestParams(), \$WinWedgeResult(), \$DefaultWheelGameConfig()) >> \$SkuReward()\n",
                    "            1 * goodsFacade.rewardMultipleSkus(\$PlayGameRequestParams(), [\$SkuReward()], bossGuid, \$WheelGameConfigs()) >> \$RewardResponse()",
                    "1 * gameConfigsRepository.save(gameConfigId, entity1)"
            ]
            expected << [
                    "1 * addonRewardCreator.create(_, _, _) >> { def p_playGameRequestParams, def p_winWedgeResult, def p_defaultWheelGameConfig ->\n" +
                            "                assert p_playGameRequestParams == \$PlayGameRequestParams()\n" +
                            "                assert p_winWedgeResult == \$WinWedgeResult()\n" +
                            "                assert p_defaultWheelGameConfig == \$DefaultWheelGameConfig()\n" +
                            "                \$SkuReward()\n" +
                            "            }",
                    "            1 * goodsFacade.rewardMultipleSkus(_, _, _, _) >> { def p_playGameRequestParams, def p_skuRewards, def p_bossGuid, def p_wheelGameConfigs ->\n" +
                            "                assert p_playGameRequestParams == \$PlayGameRequestParams()\n" +
                            "                assert p_skuRewards == [\$SkuReward()]\n" +
                            "                assert p_bossGuid == bossGuid\n" +
                            "                assert p_wheelGameConfigs == \$WheelGameConfigs()\n" +
                            "                \$RewardResponse()\n" +
                            "            }",
                    "1 * gameConfigsRepository.save(_, _) >> { def p_gameConfigId, def p_entity1 ->\n" +
                            "                assert p_gameConfigId == gameConfigId\n" +
                            "                assert p_entity1 == entity1\n" +
                            "            }"
            ]

    }

}
