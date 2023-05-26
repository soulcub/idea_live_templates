package com.soulcub.idea.templates

import com.soulcub.idea.BaseSpec

class plttrb extends BaseSpec {

    def "test record builder"() {
        given: "clipboard content"
            def _1 = "public record ConfigAdminDto(\n" +
                    "        @NonNull String name,\n" +
                    "        @Nullable String configId,\n" +
                    "        int priority,\n" +
                    "        boolean enabled,\n" +
                    "        @NonNull String assetId,\n" +
                    "        long from,\n" +
                    "        long to,\n" +
                    "        long segment,\n" +
                    "        long initialPoints,\n" +
                    "        @NonNull Set<RewardConfigAdminDto> rewards"
        and:
            def expected = "    ConfigAdminDto \$ConfigAdminDto(@DelegatesTo(ConfigAdminDto.ConfigAdminDtoBuilder) Closure closure = {}) {\n" +
                    "        ConfigAdminDto.builder()\n" +
                    "                      .name(name)\n" +
                    "                      .configId(configId)\n" +
                    "                      .priority(priority)\n" +
                    "                      .enabled(enabled)\n" +
                    "                      .assetId(assetId)\n" +
                    "                      .from(from)\n" +
                    "                      .to(to)\n" +
                    "                      .segment(segment)\n" +
                    "                      .initialPoints(initialPoints)\n" +
                    "                      .rewards(rewards)\n" +
                    "                      .tap(closure)\n" +
                    "                      .build()\n" +
                    "    }"
        when:
            def result = _1
                    .split(System.lineSeparator())
                    .collect { it.replaceAll("@[^ ]+", "") }
                    .collect { it.trim() }
                    .collect { it.replaceAll(",(?=[^,]*\$)", "") }
                    .findAll { !it.isEmpty() }
                    .collect { it.replace('{', '') }
                    .collect { it.trim() }
                    .collect { String it ->
                        if (it.contains('<')) {
                            def varName = it.substring(it.indexOf('>') + 1).trim();
                            return '.' + varName + '(' + varName + ')'
                        } else {
                            def tokens = it.split();
                            if (tokens.find { it == 'record' }) {
                                def className = tokens[2].replace('(', '');
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
    }

}
