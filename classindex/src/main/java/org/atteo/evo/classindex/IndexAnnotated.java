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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ServiceLoader;

import org.atteo.evo.classindex.processor.ClassIndexProcessor;

/**
 * Index all classes annotated by annotation annotated by this meta-annotation.
 *
 * <p>
 * During compilation {@link ClassIndexProcessor} creates a resource file listing all classes
 * annotated by annotation annotated by this meta-annotation.
 * </p>
 * <p>
 * You can retrieve the list at runtime using {@link ClassIndex#getAnnotated(Class)}.
 * If the classes also have a zero-argument constructor you can use {@link ServiceLoader} facility.
 * </p>
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface IndexAnnotated {
}
