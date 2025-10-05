package rut.miit.tech.summer_hackathon.controller.query;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@Data
public class PageParam {

    int size = 10;
    int page = -1;

    public Pageable toPageable() {
        if (page == -1) {
            return Pageable.unpaged();
        }
        return PageRequest.of(page, size);
    }


    public Pageable toPageable(SortParam sortParam) {
        if (page == -1) {
            return Pageable.unpaged(sortParam.sort());
        }
        return PageRequest.of(page, size, sortParam.sort());
    }
}