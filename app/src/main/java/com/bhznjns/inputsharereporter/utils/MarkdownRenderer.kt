package com.bhznjns.inputsharereporter.utils

import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer


class MarkdownRenderer {
    fun renderMarkdown(markdown: String?): String {
        val extensions: List<Extension> = listOf(TablesExtension.create())
        val parser = Parser.builder().extensions(extensions).build()
        val document = parser.parse(markdown)
        val renderer = HtmlRenderer.builder().extensions(extensions).build()
        val html = "<html><head><style>" +
                "body { margin: 2rem 1rem; user-select: none; }" +
                "p { font-size: 1.125rem; }" +
                "div { width: fit-content; margin: 0 auto; }" +
                "table { min-width: 60vw; border-collapse: collapse;" +
                        "border: 1px solid rgba(0, 0, 0, .15);" +
                        "box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08); }" +
                "tbody { border-top: 1px solid rgb(231, 231, 231); }" +
                "th, td { padding: 12px 24px; text-align: left; }" +
                "th { color: rgb(107, 114, 128); background-color: rgb(250, 250, 250); text-transform: uppercase; }" +
                "td:first-child { width: 10rem; } " +
                // dark mode styles
                "@media (prefers-color-scheme: dark) {" +
                "body { color: white; }" +
                "p { color: rgb(247, 247, 247); }" +
                "table { border-color: rgba(68, 68, 68); }" +
                "tbody { border-color: transparent; }" +
                "th { background-color: rgb(51, 51, 51); color: rgb(221, 221, 221) }" +
                "td { color: rgb(241, 241, 241) } }" +
                "</style></head><body><div>" + renderer.render(document) + "</div></body></html>"
        return html
    }
}