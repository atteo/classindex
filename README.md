[![Build Status](https://travis-ci.org/atteo/classindex.svg)](https://travis-ci.org/atteo/classindex)
[![Coverage Status](https://img.shields.io/coveralls/atteo/classindex.svg)](https://coveralls.io/r/atteo/classindex)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.atteo.classindex/classindex/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.atteo.classindex/classindex)

About
=====
ClassIndex is a much quicker alternative to every run-time annotation scanning library like Reflections or Scannotations.

ClassIndex consist of two parts:
1. An annotation processor which at compile-time generates an index of classes implementing given interface, classes annotated by given annotation or placed in a common package. Thanks to [automatic discovery](http://www.jcp.org/en/jsr/detail?id=269) the processor will be automatically executed when classindex.jar is added to the classpath.

2. Run-time API to read the content of generated indexes.

Why ClassIndex?
===============

Class path scanning is very slow process. Replacing it with compile-time indexing speeds Java applications bootstrap considerably.

Here are the results of the [benchmark](https://github.com/atteo/classindex-benchmark) comparing ClassIndex with the various scanning solutions.

| Library                  | Application startup time |
| :----------------------- |-------------------------:|
| None - hardcoded list    |                  0:00.18 |
| [Scannotation](http://scannotation.sourceforge.net/)             |                  0:05.11 |
| [Reflections](https://github.com/ronmamo/reflections)            |                  0:05.37 |
| Reflections Maven plugin |                                                          0:00.52 |
| [Corn](https://sites.google.com/site/javacornproject/corn-cps)   |                  0:24.60 |
| ClassIndex               |                  0:00.18 |

Notes: benchmark was performed on Intel i5-2520M CPU @ 2.50GHz, classpath size was set to 121MB.

Changes
=======
Version 3.3
- New methods to return names of classes as a string

Version 3.2
- Better Java8 compatibility
- Better filtering

Version 3.1
- Class filtering - mechanism to filter classes based on various criteria

Version 3.0

- Non-local named nested classes are also indexed (both static and inner classes)
- Fix: incremental compilation in IntelliJ IDEA
- You can now restrict the results to specific class loader
- package name nad groupId have changed to org.atteo.classindex

Version 2.2

- Fix: jaxb.index was in incorrect format

Version 2.1

- Fix: custom processor with indexAnnotation() call resulted in javac throwing Error

Version 2.0

- You can now use [ClassIndex.getClassSummary()](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/ClassIndex.html#getClassSummary(java.lang.Class%29)) to retrieve first sentence of the Javadoc. For this to work specify storeJavadoc=true attribute when using IndexAnnotated or IndexSubclasses
- Requires Java 1.7

Version 1.4

- Fix FileNotFoundException when executed under Tomcat from Eclipse

Version 1.3

- Ignore classes which don't exist at runtime (#4).
    This fixes some issues in Eclipse.
- Allow to create custom processors which index subclasses and packages

Version 1.2

- Fix Eclipse support (#3)

Version 1.1

- Fix incremental compilation (#1)


Usage
=====

Class Indexing
--------------

There are two annotations which trigger compile-time indexing:

* [@IndexSubclasses](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/IndexSubclasses.html)
	* when placed on interface makes an index of all classes implementing the interface,
	* when placed on a class makes an index of its subclasses
	* and finally when placed in package-info.java it creates an index of all classes inside that package (directly - without subpackages).
* [@IndexAnnotated](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/IndexAnnotated.html) when placed on an annotation makes an index of all classes marked with that annotation.

To access the index at run-time use static methods of [ClassIndex](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/ClassIndex.html) class.

```java
@IndexAnnotated
public @interface Entity {
}
 
@Entity
public class Car {
}
 
...
 
for (Class<?> klass : ClassIndex.getAnnotated(Entity.class)) {
    System.out.println(klass.getName());
}
```

For subclasses of the given class the index file name and format is compatible with what [ServiceLoader](http://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) expects. Keep in mind that ServiceLoader also requires for the classes to have zero-argument default constructor.

For classes inside given package the index file is named "jaxb.index", it is located inside the package folder and it's format is compatible with what [JAXBContext.newInstance(String)](http://docs.oracle.com/javase/7/docs/api/javax/xml/bind/JAXBContext.html#newInstance(java.lang.String)) expects.

Javadoc storage
---------------

From version 2.0 [@IndexAnnotated](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/IndexAnnotated.html) and [@IndexSubclasses](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/IndexSubclasses.html) allow to specify storeJavadoc attribute. When set to true Javadoc comment for the indexed classes will be stored. You can retrieve first sentence of the Javadoc using [ClassIndex.getClassSummary()](http://www.atteo.org/static/classindex/apidocs/org/atteo/classindex/ClassIndex.html#getClassSummary(java.lang.Class)).

```java
@IndexAnnotated(storeJavadoc = true)
public @interface Entity {
}
 
/**
 * This is car.
 * Detailed car description follows.
 */
@Entity
public class Car {
}
 
...
 
assertEquals("This is car", ClassIndex.getClassSummary(Car.class));
```

Class filtering
---------------

Filtering allows you to select only classes with desired characteristics. Here are some basic samples:

* Selecting only top-level classes

```java
ClassFilter.only()
	.topLevel()
	.from(ClassIndex.getAnnotated(SomeAnnotation.class));
```

* Selecting only classes which are top level and public at the same time

```java
ClassFilter.only()
	.topLevel()
	.withModifiers(Modifier.PUBLIC)
	.from(ClassIndex.getAnnotated(SomeAnnotation.class));

```

* Selecting classes which are top-level or enclosed in given class:

```java
ClassFilter.any(
	ClassFilter.only().topLevel(),
	ClassFilter.only().enclosedIn(WithInnerClassesInside.class)
).from(ClassIndex.getAnnotated(SomeAnnotation.class);
```

Indexing when annotations cannot be used
----------------------------------------

Sometimes you cannot easily use annotations to trigger compile time indexing because you don't control the source code of the classes which should be annotated. For instance you cannot add @IndexAnnotated meta-annotation to [@Entity](http://docs.oracle.com/javaee/7/api/javax/persistence/Entity.html) annotation. Although not so straightforward, it is still possible to use ClassIndex in this case.

There are two steps necessary:

1.	First create a custom processor

	```java
	public class CustomProcessor extends ClassIndexProcessor {
		public ImportantClassIndexProcessor() {
			indexAnnotations(Entity.class);
		}
	}
	```
	In the constructor specify what indexes should be created by calling apriopriate methods:
	* indexAnnotations(...) - to create index of classes annotated with given annotations
	* indexSubclasses(...) - to create index of subclasses of given parent classes
	* indexPackages(...) - to create index of classes inside given packages.

2.	Register the processor by creating the file 'META-INF/services/javax.annotation.processing.Processor' in your classpath with the full class name of your processor, see the example [here](https://github.com/atteo/classindex/blob/master/classindex/src/test/resources/META-INF/services/javax.annotation.processing.Processor)

Important note: you also need to ensure that your custom processor is always available on the classpath when compiling indexed classes. When that is not the case there will not be any error - those classes will be missing in the index.

Eclipse
=======
Eclipse uses its own Java compiler which is not strictly standard compliant and requires extra configuration.
In Java Compiler -> Annotation Processing -> Factory Path you need to add ClassIndex jar file.
See the [screenshot](https://github.com/atteo/classindex/issues/5#issuecomment-15365420).

License
=======

ClassIndex is available under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Download
========

You can download the library from [here](http://search.maven.org/remotecontent?filepath=org/atteo/classindex/classindex/3.3/classindex-3.3.jar) or use the following Maven dependency:

```xml
<dependency>
    <groupId>org.atteo.classindex</groupId>
    <artifactId>classindex</artifactId>
    <version>3.3</version>
</dependency>
```



