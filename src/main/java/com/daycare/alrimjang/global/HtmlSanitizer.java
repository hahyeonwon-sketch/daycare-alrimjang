package com.daycare.alrimjang.global;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Quill 에디터(공지사항/행사보고) 리치텍스트를 저장 전에 서버에서 정제.
 * 화면에서 th:utext로 렌더링하므로 저장 시점에 위험 태그를 제거함.
 * <script>, on*= 이벤트 핸들러, javascript: 링크 전부 제거.
 * Quill이 생성하는 서식 태그(bold/italic/color 등)만 허용.
 */
public class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.none()
            .addTags("p", "br", "strong", "em", "u", "s", "span")
            .addAttributes("span", "style", "class")
            .addAttributes("p", "style", "class");

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