package rut.miit.tech.summer_hackathon.controller.query;

import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

public class AbstractFilter {
    protected List<String> fetchingProperties = new ArrayList<>();

    public <R extends AbstractFilter> R withJoin(String property){
        fetchingProperties.add(property);
        return (R)this;
    }

    protected final void applyJoins(Root<?> root){
        fetchingProperties.forEach(
                root::fetch
        );
    }


}
