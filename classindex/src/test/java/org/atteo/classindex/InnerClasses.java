package org.atteo.classindex;

/**
 * Inner classes test.
 *
 * @author a.navrotskiy
 */
public class InnerClasses {
	// both static and non-static inner classes should be indexed
	public static class InnerService implements Service {
	}

	public static class InnerModule implements Module {
	}

	@Component
	static class InnerComponent {
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
