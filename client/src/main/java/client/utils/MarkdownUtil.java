package client.utils;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.net.MalformedURLException;
import java.net.URL;

public class MarkdownUtil {

	// This is taken from the -commonmark-java- library in the product backlog
	public static String parseToHtml(String markdownContent) {
		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdownContent);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);  // "<p>This is <em>Markdown</em></p>\n"
	}

	// Method to render the HTML using a CSS file
	public static void renderMarkdownInWebView(String markdownContent, WebView webView) throws MalformedURLException {

		String htmlContent = parseToHtml(markdownContent);

		WebEngine webEngine = webView.getEngine();
		webEngine.loadContent(htmlContent);

		URL cssFileUrl = MarkdownUtil.class.getResource("/css/markdown-style.css"); // Adjust path based on location
		if (cssFileUrl != null) {
			// Add the CSS link to the HTML content
			String cssLink = "<link rel='stylesheet' type='text/css' href='" + cssFileUrl + "' />";

			// Load the CSS into the WebView
			webEngine.loadContent(cssLink + htmlContent);
		}
		else {
			System.err.println("CSS file not found.");
		}
	}
}
