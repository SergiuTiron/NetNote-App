package client.utils;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkdownUtil {

	// This is taken from the -commonmark-java- library in the product backlog
	public static String parseToHtml(String markdownContent) {
		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdownContent);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);  // "<p>This is <em>Markdown</em></p>\n"
	}
}
