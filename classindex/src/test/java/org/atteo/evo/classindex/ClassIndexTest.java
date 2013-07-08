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
package org.atteo.evo.classindex;

import java.lang.annotation.Documented;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import org.atteo.evo.classindex.processor.Important;
import org.atteo.evo.classindex.processor.Plugin;
import org.atteo.evo.classindex.second.ExtraPlugin;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ClassIndexTest {
	@Test
	@SuppressWarnings("unchecked")
	public void shouldIndexSubclasses() {
		// when
		Iterable<Class<? extends Service>> services = ClassIndex.getSubclasses(Service.class);
		// then
		assertThat(services).containsOnly(FirstService.class, SecondService.class);
	}

	@Test
	public void shouldIndexAnnotated() {
		// when
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(Component.class);
		// then
		assertThat(annotated).containsOnly(FirstComponent.class, SecondComponent.class);
	}

	@Test
	public void shouldIndexWhenAnnotationIsInherited() {
		// when
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(InheritedAnnotation.class);
		// then
		assertThat(annotated).containsOnly(Service.class, FirstService.class, SecondService.class);
	}

	@Test
	public void shouldIndexClassesInsidePackage() {
		// when
		Iterable<Class<?>> packageSubclasses = ClassIndex.getPackageClasses(
				ClassIndexTest.class.getPackage().getName());
		// then
		assertThat(packageSubclasses).contains(FirstComponent.class, Component.class);
	}

	@Test
	public void shouldSuportServiceLoader() {
		// when
		ServiceLoader<Service> loader = ServiceLoader.load(Service.class);
		// then
		assertThat(loader).hasSize(2);
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
}
