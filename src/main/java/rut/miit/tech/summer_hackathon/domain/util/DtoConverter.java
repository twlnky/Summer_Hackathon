package rut.miit.tech.summer_hackathon.domain.util;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DtoConverter {

    private final ModelMapper modelMapper;


    public <M, D> D toDto(M model, Class<D> destination) {
        return modelMapper.map(model, destination);
    }

    public <M, D> List<D> toDto(List<M> models, Class<D> destination) {
        return models.stream()
                .map(o -> modelMapper.map(o, destination)) // Маппинг каждого элемента
                .toList(); // Сбор в список (Java 16+ неизменяемый)
    }

    public <M, D> M toModel(D dto, Class<M> destination) {
        return modelMapper.map(dto, destination); // Прямой вызов
    }
}