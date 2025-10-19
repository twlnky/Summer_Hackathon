package rut.miit.tech.summer_hackathon.controller.query;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageParamTest {

    @Test
    void testDefaultValues() {
        PageParam param = new PageParam();

        assertThat(param.getSize()).isEqualTo(10);
        assertThat(param.getPage()).isEqualTo(-1);
    }

    @Test
    void testToPageable_UnpagedWhenPageIsMinusOne() {
        PageParam param = new PageParam();
        param.setPage(-1);

        Pageable pageable = param.toPageable();

        assertThat(pageable.isUnpaged()).isTrue();
    }

    @Test
    void testToPageable_PagedWhenPageIsNonNegative() {
        PageParam param = new PageParam();
        param.setPage(2);
        param.setSize(20);

        Pageable pageable = param.toPageable();

        assertThat(pageable).isInstanceOf(PageRequest.class);
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.isUnpaged()).isFalse();
    }

    @Test
    void testToPageable_WithSortParamAndPageIsMinusOne() {
        PageParam param = new PageParam();
        param.setPage(-1);

        SortParam sortParam = new SortParam(java.util.List.of("name:asc"));
        Pageable pageable = param.toPageable(sortParam);

        assertThat(pageable.isUnpaged()).isTrue();
        assertThat(pageable.getSort()).isEqualTo(sortParam.sort());
        assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void testToPageable_WithSortParamAndNormalPage() {
        PageParam param = new PageParam();
        param.setPage(1);
        param.setSize(5);

        SortParam sortParam = new SortParam(java.util.List.of("id:desc"));
        Pageable pageable = param.toPageable(sortParam);

        assertThat(pageable).isInstanceOf(PageRequest.class);
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(sortParam.sort());
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void testChangingSizeAndPageValues() {
        PageParam param = new PageParam();

        param.setPage(3);
        param.setSize(50);

        assertThat(param.getPage()).isEqualTo(3);
        assertThat(param.getSize()).isEqualTo(50);

        Pageable pageable = param.toPageable();
        assertThat(pageable.getPageNumber()).isEqualTo(3);
        assertThat(pageable.getPageSize()).isEqualTo(50);
    }
}
