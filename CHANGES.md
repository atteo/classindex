Changes
=======
Version 3.12
- incremental processing in Gradle

Version 3.11
- do not crash on Java records

Version 3.10
- support relocations in classindex-transformer (#64)

Version 3.9
- also check for NoClassDefFoundError during class loading (#56)

Version 3.8
- make JAR file a valid OSGi bundle

Version 3.6
- remove dependency on JAXB API

Version 3.5
- make sure @Repeatable annotations are correctly indexed

Version 3.4
- TypeElement and PackageElement cannot be used reliably as a key for the map
- bring back compatibility with Android API < 19 by not depending on the availability of StandardCharsets class

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

