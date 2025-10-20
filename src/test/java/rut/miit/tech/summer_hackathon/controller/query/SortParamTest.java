package rut.miit.tech.summer_hackathon.controller.query;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SortParamTest {

    @Test
    void testEmptySortList_ShouldReturnUnsorted() {
        SortParam sortParam = new SortParam(List.of());

        Sort sort = sortParam.sort();

        assertThat(sort.isSorted()).isFalse();
    }

    @Test
    void testSingleAscField() {
        SortParam sortParam = new SortParam(List.of("name:asc"));

        Sort sort = sortParam.sort();

        Sort.Order order = sort.getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void testSingleDescField() {
        SortParam sortParam = new SortParam(List.of("id:desc"));

        Sort sort = sortParam.sort();

        Sort.Order order = sort.getOrderFor("id");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testFieldWithoutDirection_ShouldDefaultToDesc() {
        SortParam sortParam = new SortParam(List.of("createdAt"));

        Sort sort = sortParam.sort();

        Sort.Order order = sort.getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testInvalidDirection_ShouldDefaultToDesc() {
        SortParam sortParam = new SortParam(List.of("username:up"));

        Sort sort = sortParam.sort();

        Sort.Order order = sort.getOrderFor("username");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testMultipleSortFields() {
        SortParam sortParam = new SortParam(List.of("name:asc", "id:desc", "email:asc"));

        Sort sort = sortParam.sort();

        assertThat(sort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(sort.getOrderFor("email").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void testSplitBehaviorWithColonInsideFieldName_ShouldSplitOnlyOnce() {
        SortParam sortParam = new SortParam(List.of("user:profile:asc"));

        Sort sort = sortParam.sort();

        // Должно разбиться как ["user", "profile:asc"], то есть field = "user", order = "profile:asc"
        // Поскольку order != "asc", должно примениться DESC
        Sort.Order order = sort.getOrderFor("user");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testNoArgsConstructorAndSetter() {
        SortParam sortParam = new SortParam();
        sortParam.setSort(List.of("id:asc"));

        Sort sort = sortParam.sort();
        assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void testAllArgsConstructor() {
        SortParam sortParam = new SortParam(List.of("updatedAt:desc"));
        Sort sort = sortParam.sort();

        Sort.Order order = sort.getOrderFor("updatedAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
