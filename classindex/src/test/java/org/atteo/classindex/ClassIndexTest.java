/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atteo.classindex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Documented;
import java.net.URL;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.classindex.processor.Important;
import org.atteo.classindex.processor.Plugin;
import org.atteo.classindex.second.ExtraPlugin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

public class ClassIndexTest {
	@Test
	@SuppressWarnings("unchecked")
	public void shouldIndexSubclasses() {
		// when
		Iterable<Class<? extends Service>> services = ClassIndex.getSubclasses(Service.class);
		// then
		assertThat(services).containsOnly(FirstService.class, SecondService.class, InnerClasses.InnerService.class);
	}

	@Test
	public void shouldReturnNamesOfSubclasses() {
		// when
		Iterable<String> services = ClassIndex.getSubclassesNames(Service.class);

		// then
		assertThat(services).containsOnly(FirstService.class.getCanonicalName(), SecondService.class.getCanonicalName(),
				InnerClasses.class.getCanonicalName() + "$" + InnerClasses.InnerService.class.getSimpleName());
	}

	@Test
	public void shouldIndexAnnotated() {
		// when
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(Component.class);
		// then
		assertThat(annotated).containsOnly(FirstComponent.class, SecondComponent.class,
				InnerClasses.InnerComponent.class, InnerClasses.InnerComponent.InnerInnerComponent.class);
	}

	@Test
	public void shouldReturnNamesOfAnnotated() {
		// when
		Iterable<String> annotated = ClassIndex.getAnnotatedNames(Component.class);
		// then
		assertThat(annotated).containsOnly(
				FirstComponent.class.getCanonicalName(),
				SecondComponent.class.getCanonicalName(), InnerClasses.class.getCanonicalName() + "$"
					+ InnerClasses.InnerComponent.class.getSimpleName(), InnerClasses.class.getCanonicalName() + "$"
					+ InnerClasses.InnerComponent.class.getSimpleName() + "$"
					+ InnerClasses.InnerComponent.InnerInnerComponent.class.getSimpleName(),
				// list of names contains also a deleted component
				"org.atteo.classindex.DeletedComponent");
	}

	@Test
	public void shouldIndexWhenAnnotationIsInherited() {
		// when
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(InheritedAnnotation.class);
		// then
		assertThat(annotated).containsOnly(Service.class, FirstService.class, SecondService.class, InnerClasses.InnerService.class);
	}

	@Test
	public void shouldIndexClassesInsidePackage() {
		// when
		Iterable<Class<?>> packageSubclasses = ClassIndex.getPackageClasses(
				ClassIndexTest.class.getPackage().getName());

		// then
		assertThat(packageSubclasses).contains(FirstComponent.class, Component.class);
		assertThat(packageSubclasses).doesNotContain(Important.class);
	}

	@Test
	public void shouldReturnNamesOfClassesInsidePackage() {
		// when
		Iterable<String> packageSubclasses = ClassIndex.getPackageClassesNames(
				ClassIndexTest.class.getPackage().getName());

		// then
		assertThat(packageSubclasses).contains(FirstComponent.class.getCanonicalName(),
				Component.class.getCanonicalName());
	}

	@Test
	public void shouldCreateConformingJaxbIndex() throws IOException {
		// when
		String resourceName = ClassIndexTest.class.getPackage().getName().replace('.', '/')
				+ '/' + ClassIndex.PACKAGE_INDEX_NAME;
		URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		assertNotNull(resource);
		InputStream stream = resource.openStream();
		String content = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

		// then
		assertThat(content).contains("\nFirstComponent\n");
		assertThat(content).contains("\nComponent\n");
		assertThat(content).contains("\nTomcatFromEclipseTest$WeirdClassLoader\n");
	}

	// https://github.com/atteo/classindex/issues/8
	@Test
	public void shouldReadJaxbIndexCreatedByOlderVersions() {
		// when
		Iterable<Class<?>> packageSubclasses = ClassIndex.getPackageClasses(ExtraPlugin.class.getPackage().getName());

		// then
		assertThat(packageSubclasses).contains(ExtraPlugin.class);
	}

	@Test
	public void shouldSuportServiceLoader() {
		// when
		ServiceLoader<Service> loader = ServiceLoader.load(Service.class);
		// then
		assertThat(loader).hasSize(3);
	}

	@Test
	public void customProcessorShouldIndexAnnotated() {
		// when
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(Important.class);
		// then
		assertThat(annotated).containsOnly(FirstComponent.class, FirstService.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void customProcessorShouldIndexSubclasses() {
		// when
		Iterable<Class<? extends Plugin>> plugins = ClassIndex.getSubclasses(Plugin.class);
		// then
		assertThat(plugins).containsOnly(FirstComponent.class, ExtraPlugin.class);
	}

	@Test
	public void customProcessorShouldIndexPackages() {
		// when
		Iterable<Class<?>> classes = ClassIndex.getPackageClasses(ExtraPlugin.class.getPackage().getName());
		// then
		assertThat(classes).containsOnly(ExtraPlugin.class);
	}

	@Test
	public void shouldNotIndexNotAnnotated() {
		// when
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(Documented.class);
		// then
		assertThat(annotated).isEmpty();
	}

	@Test
	public void shouldNotStoreSummaryByDefault() {
		assertThat(ClassIndex.getClassSummary(FirstService.class)).isNull();
	}

	@Test
	public void shouldStoreSummaryForAnnotated() {
		assertEquals("First component", ClassIndex.getClassSummary(FirstComponent.class));
	}

	@Test
	public void shouldStoreSummaryForSubclasses() {
		assertEquals("First module", ClassIndex.getClassSummary(FirstModule.class));
	}

	@Test
	public void shouldIndexClassesWithRepeatedAnnotations() {
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(RepeatableAnnotation.class);
		assertThat(annotated).contains(StandardRepeated.class, Java8Repeated.class);
	}
}
