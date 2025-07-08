package rut.miit.tech.summer_hackathon.controller.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class SortParam {
    //sort=id:asc&sort=login:desc
    private List<String> sort = new ArrayList<>();

    public Sort sort() {
        List<Sort.Order> orders = new ArrayList<>();
        sort.forEach(s -> {
            String[] parts = s.split(":", 2);
            String field = parts[0];
            String order = parts[1];
            orders.add(Objects.equals(order, "asc") ?
                    Sort.Order.asc(field) :
                    Sort.Order.desc(field));
        });
        return Sort.by(orders);
    }
}
