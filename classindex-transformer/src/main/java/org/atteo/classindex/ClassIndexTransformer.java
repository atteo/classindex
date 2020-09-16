/*
 * Copyright 2011 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.classindex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.codehaus.plexus.util.IOUtil;

public class ClassIndexTransformer implements ResourceTransformer {
	public static final String SUBCLASS_INDEX_PREFIX = "META-INF/services/";
	public static final String ANNOTATED_INDEX_PREFIX = "META-INF/annotations/";
	public static final String PACKAGE_INDEX_NAME = "jaxb.index";
	private final Map<String, ByteArrayOutputStream> serviceEntries = new HashMap<>();
	private List<Relocator> relocators;

	@Override
	public boolean canTransformResource(String resource) {
		return resource.startsWith(SUBCLASS_INDEX_PREFIX)
				|| resource.startsWith(ANNOTATED_INDEX_PREFIX)
				|| resource.endsWith("/" + PACKAGE_INDEX_NAME);
	}

	@Override
	public void processResource(String resource, InputStream is, List<Relocator> relocators)
			throws IOException {
		if (this.relocators == null) {
			this.relocators = relocators;
		}

		ByteArrayOutputStream data = serviceEntries.get(resource);
		if (data == null) {
			data = new ByteArrayOutputStream();
			serviceEntries.put(resource, data);
		}

		try {
			String content = IOUtil.toString(is, StandardCharsets.UTF_8.name());
			StringReader reader = new StringReader(content);
			BufferedReader lineReader = new BufferedReader(reader);
			String line;
			while ((line = lineReader.readLine()) != null) {
				String qualifiedClassName = relocateIfNeeded(line);
				data.write(qualifiedClassName.getBytes(StandardCharsets.UTF_8));
				data.write("\n".getBytes(StandardCharsets.UTF_8));
			}
		} finally {
			is.close();
		}
	}

	@Override
	public boolean hasTransformedResource() {
		return serviceEntries.size() > 0;
	}

	@Override
	public void modifyOutputStream(JarOutputStream jos)
			throws IOException {
		for (Map.Entry<String, ByteArrayOutputStream> entry : serviceEntries.entrySet()) {
			String key = entry.getKey();
			ByteArrayOutputStream stream = entry.getValue();
			jos.putNextEntry(new JarEntry(relocateFileName(key)));
			IOUtil.copy(new ByteArrayInputStream(stream.toByteArray()), jos);
			stream.reset();
		}
	}

	private String relocateFileName(String key) {
		String prefix = "";
		if (key.startsWith(SUBCLASS_INDEX_PREFIX)) {
			prefix = SUBCLASS_INDEX_PREFIX;
		} else if (key.startsWith(ANNOTATED_INDEX_PREFIX)) {
			prefix = ANNOTATED_INDEX_PREFIX;
		}
		return prefix + relocateIfNeeded(key.substring(prefix.length()));
	}

	private String relocateIfNeeded(String key) {
		if (relocators == null) {
			return key;
		}

		for (Relocator relocator : relocators) {
			if (relocator.canRelocateClass(key)) {
				return relocator.relocateClass(key);
			}
		}

		return key;
	}
}
