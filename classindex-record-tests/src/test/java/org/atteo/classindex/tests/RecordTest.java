package org.atteo.classindex.tests;

import org.atteo.classindex.ClassIndex;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordTest {
    @Test
    public void shouldIndexAnnotatedRecords() {
        // given
        // when
        var annotated = ClassIndex.getAnnotated(Blue.class);

        // then
        assertThat(annotated).hasSize(1);
    }

    @Test
    public void shouldIndexSubclassedRecords() {
        // given
        // when
        var annotated = ClassIndex.getSubclasses(Animal.class);

        // then
        assertThat(annotated).hasSize(1);
    }
}
