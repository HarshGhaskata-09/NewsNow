package com.example.newsnow.utils;

public class ArticleCleaner {

    // Enhanced CSS selectors for Indian news sites and global patterns
    private static final String[] CLUTTER_SELECTORS = {
            "header", "footer", "nav", "aside", ".sidebar", ".ads", ".advertisement", 
            "#ad-container", ".social-share", ".newsletter-signup", ".related-posts",
            ".comments-section", ".navbar", ".menu", ".top-nav", ".bottom-nav",
            ".sticky-footer", ".bottom-sticky", ".n18logo", ".header-logo",
            ".footer-sticky", ".app-download-banner", ".share-icons", ".trending-title",
            ".outbrain", ".taboola", "[id^='div-gpt-ad']", "[class*='ad-unit']",
            ".ad-banner", ".ad-wrapper", ".ad-label", ".rhs-ads", ".m-ad"
    };

    public static String getInjectionJs(boolean isDarkMode) {
        StringBuilder js = new StringBuilder();
        js.append("(function() {");
        
        js.append("  var cleanPage = function() {");
        
        // 1. Hide by selectors
        js.append("    var selectors = [");
        for (int i = 0; i < CLUTTER_SELECTORS.length; i++) {
            js.append("'").append(CLUTTER_SELECTORS[i]).append("'");
            if (i < CLUTTER_SELECTORS.length - 1) js.append(",");
        }
        js.append("    ];");
        js.append("    selectors.forEach(function(s) {");
        js.append("      document.querySelectorAll(s).forEach(function(el) { el.style.display = 'none'; });");
        js.append("    });");

        // 2. Hide elements containing "ADVERTISEMENT" text
        js.append("    document.querySelectorAll('div, section, p, span').forEach(function(el) {");
        js.append("      if (el.innerText && el.innerText.toUpperCase() === 'ADVERTISEMENT') {");
        js.append("        if (el.parentElement) el.parentElement.style.display = 'none';");
        js.append("      }");
        js.append("    });");
        
        js.append("  };");

        // Run once immediately
        js.append("  cleanPage();");

        // Observe for late loading ads
        js.append("  var observer = new MutationObserver(cleanPage);");
        js.append("  observer.observe(document.body, { childList: true, subtree: true });");

        String bgColor = isDarkMode ? "#0A0E12" : "#F4F6F9";
        String textColor = isDarkMode ? "#F0F6FC" : "#0D1117";
        String headingColor = isDarkMode ? "#FFFFFF" : "#101010";

        js.append("  var style = document.createElement('style');");
        js.append("  style.innerHTML = '");
        js.append("    body { background-color: " + bgColor + " !important; color: " + textColor + " !important; line-height: 1.6 !important; padding: 16px !important; } ");
        js.append("    p { margin-bottom: 24px !important; font-size: 19px !important; } ");
        js.append("    h1 { color: " + headingColor + " !important; margin-top: 10px !important; font-weight: bold !important; } ");
        js.append("    img { max-width: 100% !important; height: auto !important; border-radius: 12px !important; margin: 16px 0 !important; } ");
        js.append("    .news-content, article { background-color: transparent !important; } ");
        js.append("  ';");
        js.append("  document.head.appendChild(style);");

        js.append("})();");
        return js.toString();
    }
}

