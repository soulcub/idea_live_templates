package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class pltclosure extends BaseSpec {

    def "extend mock to closure"() {
        when:
            def nonPrimitiveObjectMutator = { String it ->
                it.replace('\$', '').replace('()', '').uncapitalize()
            }
            def parameterNameMutator = { String it ->
                def result = nonPrimitiveObjectMutator(it.trim().replaceAll(/[\[\]]/, ''))
                if (it.contains('[')) {
                    result += 's'
                }
                result
            }
            def methodCallAndOther = _1.split('\\(', 2)
            def paramsAndMockReturn = methodCallAndOther[1].split('>>')
            def parameters = paramsAndMockReturn[0].replace('))', ')').split(', ')
            def result = methodCallAndOther[0] + '(' +
                    parameters.collect { '_' }.join(', ') +
                    ') >> { ' +
                    parameters.collect { 'def ' + parameterNameMutator(it) }.join(', ') + ' ->' +
                    System.lineSeparator() +
                    parameters.collect { 'assert ' + parameterNameMutator(it) + ' == ' + it }.join(System.lineSeparator()) +
                    System.lineSeparator() +
                    paramsAndMockReturn[1].trim() +
                    System.lineSeparator() +
                    '}'
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
                    "            1 * goodsFacade.rewardMultipleSkus(\$PlayGameRequestParams(), [\$SkuReward()], bossGuid, \$WheelGameConfigs()) >> \$RewardResponse()"
            ]
            expected << [
                    "1 * addonRewardCreator.create(_, _, _) >> { def playGameRequestParams, def winWedgeResult, def defaultWheelGameConfig ->\n" +
                            "                assert playGameRequestParams == \$PlayGameRequestParams()\n" +
                            "                assert winWedgeResult == \$WinWedgeResult()\n" +
                            "                assert defaultWheelGameConfig == \$DefaultWheelGameConfig()\n" +
                            "                \$SkuReward()\n" +
                            "            }",
                    "            1 * goodsFacade.rewardMultipleSkus(_, _, _, _) >> { def playGameRequestParams, def skuRewards, def bossGuid, def wheelGameConfigs ->\n" +
                            "                assert playGameRequestParams == \$PlayGameRequestParams()\n" +
                            "                assert skuRewards == [\$SkuReward()]\n" +
                            "                assert bossGuid == bossGuid\n" +
                            "                assert wheelGameConfigs == \$WheelGameConfigs()\n" +
                            "                \$RewardResponse()\n" +
                            "            }"
            ]
    }

}
