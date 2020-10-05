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

import java.util.Iterator;
import java.util.ServiceLoader;

public class ClassIndexTest {
	public static void main(String[] args) {
		Iterable<Class<? extends Service>> services = ClassIndex.getSubclasses(Service.class);
		assertEquals(2, size(services));
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(Component.class);
		assertEquals(3, size(annotated));
		Iterable<Class<?>> packageSubclasses = ClassIndex.getPackageClasses(
				ClassIndexTest.class.getPackage().getName());
		assertEquals(9, size(packageSubclasses));
		ServiceLoader<Service> loader = ServiceLoader.load(Service.class);
		assertEquals(2, size(loader));

		Iterable<Class<? extends ParentService>> parentServices = ClassIndex.getSubclasses(ParentService.class);
		assertEquals(2, size(parentServices));

		Iterable<Class<?>> customAnnotationClasses = ClassIndex.getAnnotated(CustomAnnotation.class);
		assertEquals(2, size(parentServices));
	}

	private static void assertEquals(int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException("Test has failed. Expected: " + expected + ", actual: " + actual);
		}
	}

	private static int size(Iterable<?> iterable) {
		Iterator<?> iterator = iterable.iterator();
		int size = 0;
		while (iterator.hasNext()) {
			size++;
			iterator.next();
		}
		return size;
	}
}
