package rut.miit.tech.summer_hackathon.controller.query;

import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractFilterTest {

    @Test
    void testWithJoin_AddsPropertyAndReturnsSameInstance() {
        AbstractFilter filter = new AbstractFilter();

        AbstractFilter result = filter.withJoin("department");

        assertThat(result).isSameAs(filter);
        assertThat(filter.fetchingProperties).containsExactly("department");
    }

    @Test
    void testApplyJoins_CallsRootFetchForEachProperty() {
        AbstractFilter filter = new AbstractFilter();
        filter.withJoin("user").withJoin("company");

        @SuppressWarnings("unchecked")
        Root<?> root = mock(Root.class);

        filter.applyJoins(root);

        verify(root, times(1)).fetch("user");
        verify(root, times(1)).fetch("company");
        verifyNoMoreInteractions(root);
    }
}
