package com.soulcub.idea

import spock.lang.Specification

class BaseSpec extends Specification {

    static String replacement = [
            '"': '&quot;',
            '>': '&gt;',
            '<': '&lt;',
    ]

}