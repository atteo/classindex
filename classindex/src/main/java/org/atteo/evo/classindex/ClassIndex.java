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

package org.atteo.evo.classindex;

import java.lang.annotation.Annotation;

/**
 * Just for v2.0 compatibility.
 * @deprecated please use {@link org.atteo.classindex.ClassIndex} instead.
 */
@Deprecated
public class ClassIndex {
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> Iterable<Class<? extends T>> getSubclasses(Class<T> superClass) {
		return org.atteo.classindex.ClassIndex.getSubclasses(superClass);
	}

	@Deprecated
	public static Iterable<Class<?>> getPackageClasses(String packageName) {
		return org.atteo.classindex.ClassIndex.getPackageClasses(packageName);
	}

	@Deprecated
	public static Iterable<Class<?>> getAnnotated(Class<? extends Annotation> annotation) {
		return org.atteo.classindex.ClassIndex.getAnnotated(annotation);
	}

	@Deprecated
	public static String getClassSummary(Class<?> klass) {
		return org.atteo.classindex.ClassIndex.getClassSummary(klass);
	}
}
