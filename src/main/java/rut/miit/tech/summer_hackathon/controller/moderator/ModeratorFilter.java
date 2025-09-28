package rut.miit.tech.summer_hackathon.controller.moderator;

import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import rut.miit.tech.summer_hackathon.controller.query.AbstractFilter;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

import java.util.ArrayList;
import java.util.List;

/**
 * Фильтр для поиска модераторов с возможностью динамического построения запросов.
 * Реализует интерфейс Specification для интеграции с Spring Data JPA.
 *
 * <p>Особенности реализации:
 * 1. Динамическое построение предикатов на основе переданных параметров
 * 2. Жадная загрузка связанных сущностей (departments)
 * 3. Поддержка частичного совпадения для текстовых полей
 * </p>
 */
@Data  // Автоматически генерирует boilerplate-код (геттеры, сеттеры, equals, hashCode)
public class ModeratorFilter
        extends AbstractFilter
        implements Specification<Moderator> {
    private String login;  // Поле для фильтрации по логину (частичное совпадение)

    /**
     * Преобразует параметры фильтра в предикат для Criteria API.
     *
     * <p>Архитектурные решения:
     * 1. Использование CriteriaBuilder вместо JPQL/HQL: типобезопасность, гибкость
     * 2. Динамическое построение предикатов: фильтр работает даже при частичном заполнении
     * 3. Жадная загрузка departments: предотвращает N+1 проблему при последующих обращениях
     *
     * @param root    Корневая сущность (Moderator)
     * @param query   Объект запроса (не используется, но требуется интерфейсом)
     * @param cb      CriteriaBuilder для построения предикатов
     * @return Комбинированный предикат на основе всех условий фильтра
     */
    @Override
    public Predicate toPredicate(Root<Moderator> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // Список для накопления условий фильтрации
        List<Predicate> predicates = new ArrayList<>();

        // Фильтрация по логину (если параметр задан)
        if(login != null) {
            // Создание предиката для частичного совпадения (LIKE %login%)
            // Решение:
            //   - Используем "%" + login + "%" для поиска по части строки
            //   - cb.like() чувствителен к регистру - осознанное решение для логинов
            predicates.add(cb.like(root.get("login"), "%" + login + "%"));
        }
        applyJoins(root);
        /**
         * Жадная загрузка связанных сущностей departments.
         *
         * Почему здесь, а не в репозитории:
         * 1. Централизация логики загрузки связей в фильтре
         * 2. Гарантирует загрузку departments при любом использовании спецификации
         * 3. Избегает дублирования кода в разных запросах
         *
         * Особенности:
         * - JoinType.LEFT: чтобы включать модераторов без отделов
         * - fetch() влияет на форму результата, но не на условия WHERE
         */
       // Class<?> queryType = query.getSelection().getJavaType();
        //if(queryType != Long.class && queryType != Integer.class) {
        //    root.fetch("departments", JoinType.LEFT);
        //}


        // Комбинирование всех условий через AND
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    public ModeratorFilter copy(){
        ModeratorFilter moderatorFilter = new ModeratorFilter();
        moderatorFilter.login = this.login;
        return moderatorFilter;
    }
}