package rut.miit.tech.summer_hackathon.domain.util;

import lombok.RequiredArgsConstructor; // Генерирует конструктор с final полями
import org.modelmapper.ModelMapper; // Основной класс для маппинга
import org.springframework.stereotype.Component; // Помечает класс как Spring-компонент

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

    /**
     * Алгоритм toModel (DTO → сущность):
     * 1. Принимает DTO-объект `dto` и класс целевой модели `destination`
     * 2. Делегирует преобразование ModelMapper
     * 3. Возвращает новый экземпляр сущности
     *
     * Особенности:
     * - Обратная операция к toDto()
     * - Не заполняет связи и сложные поля без дополнительной конфигурации
     *
     * Пример:
     *   UserDTO dto = new UserDTO(...);
     *   User user = converter.toModel(dto, User.class);
     */
    public <M, D> M toModel(D dto, Class<M> destination) {
        return modelMapper.map(dto, destination); // Прямой вызов
    }
}