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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;


/**
 * IntegrationTest ensuring that classindex is loadable in an OSGi environment.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class RunOSGiContainerIntegrationTest {

	@Inject
	private BundleContext bundleContext;

	@Configuration
	public Option[] configure() {

		final String clientBundleLocation = System.getProperty("client.bundle.artifact.location",
				"target/classindex-" + System.getProperty("project.version") + ".jar");

		final List<Option> allOptions = new ArrayList<>();
		allOptions.add(cleanCaches());
		allOptions.add(bundle("reference:file:" + clientBundleLocation));
		allOptions.add(junitBundles());
		allOptions.add(systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"));

		final Option[] options = allOptions.toArray(new Option[]{});
		return options(options);
	}

	@Test
	public void shouldBeAbleToResolveAsOSGiBundle() {
		assertNotNull(this.bundleContext);

		checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(ClassIndex.class));
		checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(IndexAnnotated.class));
		checkBundleIsPresentInstalledAndActive(FrameworkUtil.getBundle(IndexSubclasses.class));
	}

	private static void checkBundleIsPresentInstalledAndActive(final Bundle bundle) {
		assertNotNull(bundle);
		assertEquals("Expecting " + bundle.getSymbolicName() + " bundle to be active", Bundle.ACTIVE,
				bundle.getState());
	}

}
