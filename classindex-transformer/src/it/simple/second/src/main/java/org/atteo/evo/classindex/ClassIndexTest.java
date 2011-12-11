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

import java.util.ServiceLoader;
import com.google.common.collect.Iterables;

public class ClassIndexTest {
	public static void main(String[] args) {
		Iterable<Class<? extends Service>> services = ClassIndex.getSubclasses(Service.class);
		assertEquals(2, Iterables.size(services));
		Iterable<Class<?>> annotated = ClassIndex.getAnnotated(Component.class);
		assertEquals(2, Iterables.size(annotated));
		Iterable<Class<?>> packageSubclasses = ClassIndex.getPackageClasses(
				ClassIndexTest.class.getPackage().getName());
		assertEquals(7, Iterables.size(packageSubclasses));
		ServiceLoader<Service> loader = ServiceLoader.load(Service.class);
		assertEquals(2, Iterables.size(loader));
	}
	
	private static void assertEquals(int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException("Test has failed. Expected: " + expected + ", actual: " + actual);
		}
	}
}
