package rut.miit.tech.summer_hackathon.controller.query;

import lombok.AllArgsConstructor;
import lombok.Data;  // Автоматическая генерация геттеров/сеттеров
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;  // Основной класс для сортировки в Spring Data
import java.util.ArrayList;  // Для работы со списками
import java.util.List;
import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortParam {

    private List<String> sort = new ArrayList<>();





    public Sort sort() {

        List<Sort.Order> orders = new ArrayList<>();

        // Обработка каждого параметра сортировки
        sort.forEach(s -> {
            /*
             * Разбиение строки на части по двоеточию с лимитом 2:
             * - Решение: split(":", 2) гарантирует 2 части максимум
             * - Почему? Защита от значений с дополнительными ":"
             * - Пример: "user:profile:asc" → ["user", "profile:asc"] недопустимо
             */
            String[] parts = s.split(":", 2);  // Критически важный лимит

            // Извлечение имени поля (обязательная часть)
            String field = parts[0];

            /*
             * Обработка направления сортировки:
             * - По умолчанию: DESC (осознанное решение для безопасности)
             * - Если указан asc - сортировка по возрастанию
             * - Любое другое значение → DESC
             */
            String order = (parts.length > 1) ? parts[1] : "desc";  // Защита от отсутствия направления

            /*
             * Создание объекта Order:
             * - Objects.equals() вместо == для корректного сравнения строк
             * - Явное указание направления улучшает читаемость
             */
            orders.add(
                    Objects.equals(order, "asc") ?  // Безопасное сравнение
                            Sort.Order.asc(field) :         // Восходящая сортировка
                            Sort.Order.desc(field)           // Нисходящая сортировка (по умолчанию)
            );
        });

        // Сборка финального объекта Sort
        return Sort.by(orders);
    }
}