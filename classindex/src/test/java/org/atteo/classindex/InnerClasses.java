package org.atteo.classindex;

/**
 * Inner classes test.
 */
public class InnerClasses {
	// both static and non-static inner classes should be indexed
	public static class InnerService implements Service {
	}

	public class InnerModule implements Module {
	}

	@Component
	public static class InnerComponent {
		@Component
		class InnerInnerComponent {
		}
	}

	// anonymous inner classes should not be indexed
	private Module module = new Module() {

	};

	// local classes should not be indexed
	public void testMethod() {
		class NotIndexedClass {

		}
	}
}
