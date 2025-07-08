package rut.miit.tech.summer_hackathon.controller.query;

import lombok.Data;  // Автоматически генерирует геттеры/сеттеры
import org.springframework.data.domain.PageRequest;  // Конкретная реализация Pageable
import org.springframework.data.domain.Pageable;  // Интерфейс для пагинации

/**
 * Класс для параметров пагинации с гибкой обработкой значений по умолчанию.
 * Обратите внимание на специальную обработку значения page = -1.
 */
@Data  // Генерирует геттеры/сеттеры для полей size и page
public class PageParam {
    // Размер страницы по умолчанию
    int size = 10;  // Осознанное решение: 10 элементов - баланс между производительностью и удобством

    // Номер страницы (-1 - специальное значение для отключения пагинации)
    int page = -1;  // Ключевое решение: использование -1 вместо null для обозначения "все данные"

    /**
     * Создает объект Pageable без сортировки.
     *
     * <p>Архитектурные решения:
     * 1. Возвращает Pageable.unpaged() при page=-1 - осознанный выбор для получения всех данных
     * 2. Для обычного режима - PageRequest с указанными параметрами
     */
    public Pageable toPageable() {
        if (page == -1) {
            // Режим "все данные": специальный объект без пагинации
            return Pageable.unpaged();  // Альтернатива: возвращать null, но это ломает API Spring Data
        }
        // Стандартная пагинация
        return PageRequest.of(page, size);  // PageRequest - неизменяемый потокобезопасный объект
    }

    /**
     * Создает объект Pageable с поддержкой сортировки.
     *
     * <p>Особенности реализации:
     * 1. Сохраняет поведение для page=-1, но добавляет сортировку
     * 2. Для обычного режима объединяет пагинацию и сортировку
     *
     * @param sortParam Параметры сортировки (может быть null)
     */
    public Pageable toPageable(SortParam sortParam) {
        if (page == -1) {
            // Режим "все данные" с сортировкой
            return Pageable.unpaged(sortParam.sort());  // Важно: сортировка сохраняется даже без пагинации
        }
        // Комбинирование пагинации и сортировки
        return PageRequest.of(page, size, sortParam.sort());  // Стандартный способ в Spring Data
    }
}