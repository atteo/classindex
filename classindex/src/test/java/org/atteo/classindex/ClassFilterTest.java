/*
 * Copyright 2014 Atteo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atteo.classindex;

import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;

public class ClassFilterTest {
	// given
	private final List<Class<?>> list = Lists.<Class<?>>newArrayList(FirstComponent.class, SecondComponent.class,
			InnerClasses.InnerComponent.class, InnerClasses.InnerComponent.InnerInnerComponent.class,
			InnerClasses.InnerModule.class, Service.class);

	@Test
	public void shouldNotFilterWithAlwaysTruePredicate() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().satisfying(new ClassFilter.Predicate() {
			@Override
			public boolean matches(Class<?> klass) {
				return true;
			}
		}).from(list);

		// then
		assertThat(result).hasSize(6);
	}

	@Test
	public void shouldReturnTopLevelClasses() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().topLevel().from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, SecondComponent.class, Service.class);
	}

	@Test
	public void shouldReturnTopLevelOrStaticNestedClasses() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().topLevelOrStaticNested().from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, SecondComponent.class, InnerClasses.InnerComponent.class,
				Service.class);
	}

	@Test
	public void shouldReturnOnlyEnclosedInGivenClass() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().enclosedIn(InnerClasses.class).from(list);

		// then
		assertThat(result).containsOnly(InnerClasses.InnerComponent.class, InnerClasses.InnerModule.class,
				InnerClasses.InnerComponent.InnerInnerComponent.class);
	}

	@Test
	public void shouldReturnOnlyEnclosedDirectlyInGivenClass() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().enclosedDirectlyIn(InnerClasses.class).from(list);

		// then
		assertThat(result).containsOnly(InnerClasses.InnerComponent.class, InnerClasses.InnerModule.class);
	}

	@Test
	public void shouldReturnOnlyAnnotatedWith() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().annotatedWith(Component.class).from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, SecondComponent.class, InnerClasses.InnerComponent.class,
				InnerClasses.InnerComponent.InnerInnerComponent.class);
	}

	@Test
	public void shouldFailWhenUsingAnnotationWithoutRuntimeRetention() {
	    assertThrows(IllegalStateException.class, () -> {
			ClassFilter.only().annotatedWith(InheritedAnnotation.class);
		});
	}

	@Test
	public void shouldReturnOnlyWithModifiers() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().withModifiers(Modifier.STATIC).from(list);

		// then
		assertThat(result).containsOnly(InnerClasses.InnerComponent.class);
	}

	@Test
	public void shouldReturnOnlyWithoutModifiers() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().withoutModifiers(Modifier.STATIC).from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, SecondComponent.class,
			InnerClasses.InnerComponent.InnerInnerComponent.class, InnerClasses.InnerModule.class, Service.class);
	}

	@Test
	public void shouldReturnOnlyWithPublicDefaultConstructor() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().withPublicDefaultConstructor().from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, InnerClasses.InnerComponent.class);
	}

	@Test
	public void shouldSupportAlternatives() {
		// when
		Iterable<Class<?>> result = ClassFilter.any(
				ClassFilter.only().topLevel(),
				ClassFilter.only().enclosedIn(InnerClasses.class)
		).from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, SecondComponent.class, InnerClasses.InnerComponent.class,
				InnerClasses.InnerComponent.InnerInnerComponent.class, InnerClasses.InnerModule.class, Service.class);
	}

	@Test
	public void shouldReturnOnlyInterfaces() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().interfaces().from(list);

		// then
		assertThat(result).containsOnly(Service.class);
	}

	@Test
	public void shouldReturnOnlyClasses() {
		// when
		Iterable<Class<?>> result = ClassFilter.only().classes().from(list);

		// then
		assertThat(result).containsOnly(FirstComponent.class, SecondComponent.class, InnerClasses.InnerComponent.class,
				InnerClasses.InnerComponent.InnerInnerComponent.class, InnerClasses.InnerModule.class);
	}
}
