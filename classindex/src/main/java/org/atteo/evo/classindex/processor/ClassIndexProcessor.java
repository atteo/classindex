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
package org.atteo.evo.classindex.processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Inherited;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.atteo.evo.classindex.ClassIndex;
import org.atteo.evo.classindex.IndexAnnotated;
import org.atteo.evo.classindex.IndexSubclasses;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Generates index files for {@link ClassIndex}.
 */
public class ClassIndexProcessor extends AbstractProcessor {
	private Multimap<TypeElement, TypeElement> subclassMap = HashMultimap.create();
	private Multimap<TypeElement, TypeElement> annotatedMap = HashMultimap.create();
	private Multimap<PackageElement, TypeElement> packageMap = HashMultimap.create();

	private Set<String> indexedAnnotations;

	private Types types;
	private Filer filer;

	public ClassIndexProcessor() {
		indexedAnnotations = Collections.emptySet();
	}

	/**
	 * Used when creating subclasses of the processor which will index some annotations
	 * which cannot be itself annotated with {@link IndexAnnotated} or {@link IndexSubclasses}.
	 * @param classes list of classes which the processor will be indexing
	 */
	protected ClassIndexProcessor(Class<?>... classes) {
		indexedAnnotations = new HashSet<String>();
		for (Class<?> klass : classes) {
			indexedAnnotations.add(klass.getCanonicalName());
		}
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Sets.newHashSet("*");
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		types = processingEnv.getTypeUtils();
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			for (Element element : roundEnv.getRootElements()) {
				if (!(element instanceof TypeElement)) {
					continue;
				}

				TypeElement typeElement = (TypeElement) element;

				if (!indexedAnnotations.isEmpty()) {
					for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
						TypeElement annotationElement = (TypeElement) mirror.getAnnotationType()
								.asElement();

						if (indexedAnnotations.contains(annotationElement.getQualifiedName().toString())) {
							annotatedMap.put(annotationElement, typeElement);
						}
					}

					continue;
				}

				for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
					TypeElement annotationElement = (TypeElement) mirror.getAnnotationType()
							.asElement();

					if (annotationElement.getAnnotation(IndexAnnotated.class) != null) {
						annotatedMap.put(annotationElement, typeElement);
					}
				}
				indexSupertypes(typeElement, typeElement, annotatedMap);

				// root elements are enclosed by packages
				PackageElement packageElement = (PackageElement) element.getEnclosingElement();
				if (packageElement.getAnnotation(IndexSubclasses.class) != null) {
					packageMap.put(packageElement, typeElement);
				}
			}

			if (!roundEnv.processingOver()) {
				return false;
			}

			for (TypeElement element : subclassMap.keySet()) {
				writeIndexFile(subclassMap.get(element), ClassIndex.SUBCLASS_INDEX_PREFIX
						+ element.getQualifiedName().toString());
			}
			for (TypeElement element : annotatedMap.keySet()) {
				writeIndexFile(annotatedMap.get(element), ClassIndex.ANNOTATED_INDEX_PREFIX
						+ element.getQualifiedName().toString());
			}
			for (PackageElement element : packageMap.keySet()) {
				writeIndexFile(packageMap.get(element), element.getQualifiedName().toString()
						.replace(".", "/")
						+ "/" + ClassIndex.PACKAGE_INDEX_NAME);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return false;
	}

	private void readOldIndexFile(Set<String> entries, String resourceName) throws IOException {
		BufferedReader reader = null;
		try {
			FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
			Reader resourceReader = resource.openReader(true);
			reader = new BufferedReader(resourceReader);

			String line = reader.readLine();
			while (line != null) {
				entries.add(line);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			// Thrown by Eclipse JDT when not found
		} catch (UnsupportedOperationException e) {
			// Java6 does not support reading old index files
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void writeIndexFile(Iterable<TypeElement> elementList, String resourceName)
			throws IOException {
		Set<String> entries = new HashSet<String>();
		for (TypeElement element : elementList) {
			entries.add(element.getQualifiedName().toString());
		}

		readOldIndexFile(entries, resourceName);

		FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
		Writer writer = file.openWriter();
		for (String entry : entries) {
			writer.write(entry);
			writer.write("\n");
		}
		writer.close();
	}

	/**
	 * Index super types for {@link IndexSubclasses} and any {@link IndexAnnotated}
	 * additionally accompanied by {@link Inherited}.
	 */
	private void indexSupertypes(TypeElement rootElement, TypeElement element,
			Multimap<TypeElement, TypeElement> annotatedMap) {

		for (TypeMirror mirror : types.directSupertypes(element.asType())) {
			if (mirror.getKind() != TypeKind.DECLARED) {
				continue;
			}

			DeclaredType superType = (DeclaredType) mirror;
			TypeElement superTypeElement = (TypeElement) superType.asElement();
			if (superTypeElement.getAnnotation(IndexSubclasses.class) != null) {
				subclassMap.put(superTypeElement, rootElement);
			}

			for (AnnotationMirror annotationMirror : superTypeElement.getAnnotationMirrors()) {
				TypeElement annotationElement = (TypeElement) annotationMirror.getAnnotationType()
						.asElement();
				if (annotationElement.getAnnotation(IndexAnnotated.class) != null
						&& annotationElement.getAnnotation(Inherited.class) != null) {
					annotatedMap.put(annotationElement, rootElement);
				}
			}

			indexSupertypes(rootElement, superTypeElement, annotatedMap);
		}
	}
}
