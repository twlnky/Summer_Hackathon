package rut.miit.tech.summer_hackathon.service.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private final List<T> queryResult;
    private final Integer pageCount;
    private final Integer pageSize;
    private final Long total;

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(queryResult.stream().map(mapper).toList(), pageCount, pageSize, total);
    }

    public static <T> PageResult<T> of(Page<T> page, Pageable pageable) {
        return new PageResult<T>(page.stream().toList(), page.getTotalPages(), pageable.isUnpaged() ? -1 : pageable.getPageSize(),
                page.getTotalElements());
    }
}
