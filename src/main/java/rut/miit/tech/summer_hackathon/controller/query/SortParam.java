package rut.miit.tech.summer_hackathon.controller.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortParam {

    private List<String> sort = new ArrayList<>();


    public Sort sort() {

        List<Sort.Order> orders = new ArrayList<>();


        sort.forEach(s -> {

            String[] parts = s.split(":", 2);


            String field = parts[0];


            String order = (parts.length > 1) ? parts[1] : "desc";

            orders.add(
                    Objects.equals(order, "asc") ?
                            Sort.Order.asc(field) :
                            Sort.Order.desc(field)
            );
        });


        return Sort.by(orders);
    }
}