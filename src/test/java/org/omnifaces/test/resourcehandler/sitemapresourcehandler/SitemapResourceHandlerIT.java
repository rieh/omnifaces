/*
 * Copyright 2020 OmniFaces
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.omnifaces.test.resourcehandler.sitemapresourcehandler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.omnifaces.test.OmniFacesIT.FacesConfig.withSitemapResourceHandler;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.apache.http.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.omnifaces.test.OmniFacesIT;

public class SitemapResourceHandlerIT extends OmniFacesIT {

	private static final String EXPEXTED_CONTENT_TYPE = "text/xml;charset=UTF-8";
	private static final String EXPECTED_XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String EXPECTED_XML_BODY = ""
		+ "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">"
			+ "<url><loc>http://localhost:8080/SitemapResourceHandlerIT/entity.xml?id=1</loc><lastmod>2020-12-22T19:20:10Z</lastmod><changefreq>weekly</changefreq><priority>1.0</priority></url>"
			+ "<url><loc>http://localhost:8080/SitemapResourceHandlerIT/entity.xml?id=2</loc><lastmod>2020-12-22</lastmod><changefreq>weekly</changefreq><priority>1.0</priority></url>"
			+ "<url><loc>http://localhost:8080/SitemapResourceHandlerIT/entity.xml?id=3</loc><lastmod>2020-12-22T15:20:10</lastmod><changefreq>weekly</changefreq><priority>1.0</priority></url>"
			+ "<url><loc>http://localhost:8080/SitemapResourceHandlerIT/entity.xml?id=4</loc><lastmod>2020-12-22T15:20:10-04:00</lastmod><changefreq>weekly</changefreq><priority>1.0</priority></url>"
		+ "</urlset>";

	@Deployment(testable=false)
	public static WebArchive createDeployment() {
		return buildWebArchive(SitemapResourceHandlerIT.class)
			.withFacesConfig(withSitemapResourceHandler)
			.createDeployment();
	}

	@Override
	@Before
	public void init() {
		// NOOP (there's no XHTML file in this test).
	}

	@Test
	public void test() {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(baseURL + "sitemap.xml").openConnection();
			assertEquals("Response code", HttpStatus.SC_OK, connection.getResponseCode());
			assertEquals("Content type", EXPEXTED_CONTENT_TYPE, connection.getHeaderField("Content-Type"));

			String actualPageSource;

			try (Scanner scanner = new Scanner(connection.getInputStream(), UTF_8.name())) {
				actualPageSource = scanner.useDelimiter("\\A").next();
			}

			String actualXmlProlog = actualPageSource
				.substring(0, actualPageSource.indexOf("<!--"))
				.trim();
			String actualXmlBody = actualPageSource
				.substring(actualPageSource.indexOf("-->") + 3)
				.trim()
			    .replace("127.0.0.1", "localhost"); // Depends on server used. We don't want to be dependent on that.

			assertEquals("XML prolog", EXPECTED_XML_PROLOG, actualXmlProlog);
			assertEquals("Page source", EXPECTED_XML_BODY, actualXmlBody);
		}
		catch (Exception e) {
			fail("Exception thrown: " + e);
		}
	}

}