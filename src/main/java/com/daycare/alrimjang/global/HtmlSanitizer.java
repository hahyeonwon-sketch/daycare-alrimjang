// HtmlSanitizer.java
// 변경: span·p 의 style 속성 제거 → XSS 우회 가능성 차단
package com.daycare.alrimjang.global;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.none()
            .addTags("p", "br", "strong", "em", "u", "s", "span")
            .addAttributes("span", "class")   // ✅ style 제거
            .addAttributes("p", "class");     // ✅ style 제거

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, "", SAFELIST,
                new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));
    }
}