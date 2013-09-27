About
=====
Evo Class Index is a much quicker alternative to every run-time annotation scanning library like Reflections or Scannotations.

Evo Class Index is an annotation processor which at compile-time generates an index of classes implementing given interface, classes annotated by given annotation or placed in a common package. Java 6 will automatically [discover](http://www.jcp.org/en/jsr/detail?id=269) the processor from the classpath.

Changes
=======

Version 2.2
- Fix: jaxb.index was in incorrect format

Version 2.1

- Fix: custom processor with indexAnnotation() call resulted in javac throwing Error

Version 2.0

- You can now use [ClassIndex.getClassSummary()](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/ClassIndex.html#getClassSummary(java.lang.Class%29) to retrieve first sentence of the Javadoc. For this to work specify storeJavadoc=true attribute when using IndexAnnotated or IndexSubclasses
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


Basic usage
===========
There are two annotations which trigger the indexing:

* [@IndexSubclasses](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/IndexSubclasses.html) when placed on interface makes an index of all classes implementing the interface, when placed on class makes an index of its subclasses and finally when placed in package-info.java it creates an index of all classes in that package.
* [@IndexAnnotated](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/IndexAnnotated.html) when placed on an annotation makes an index of all classes marked with that annotation.
To access the index at run-time use static methods of [ClassIndex](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/ClassIndex.html) class.

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
===============
From version 2.0 [@IndexAnnotated](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/IndexAnnotated.html) and [@IndexSubclasses](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/IndexSubclasses.html) allow to specify storeJavadoc attribute. When set to true Javadoc comment for the indexed classes will be stored. You can retrieve first sentence of the Javadoc using [ClassIndex.getClassSummary()](http://www.atteo.org/static/evo-classindex/apidocs/org/atteo/evo/classindex/ClassIndex.html#getClassSummary(java.lang.Class%29).

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

Eclipse
=======
Eclipse uses its own Java compiler which is not strictly standard compliant and requires extra configuration.
In Java Compiler -> Annotation Processing -> Factory Path you need to add both evo-classindex and Guava jar files.
See the [screenshot](https://github.com/atteo/evo-classindex/issues/5#issuecomment-15365420).

License
=======

Evo Inflector is available under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Download
========

You can download the library from [here](http://search.maven.org/remotecontent?filepath=org/atteo/evo-classindex/2.0/evo-classindex-2.0.jar) or use the following Maven dependency:

```xml
<dependency>
    <groupId>org.atteo</groupId>
    <artifactId>evo-classindex</artifactId>
    <version>2.0</version>
</dependency>
```



