package rut.miit.tech.summer_hackathon.controller.moderator;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ModeratorFilterTest {

    @Test
    @DisplayName("toPredicate — создаёт предикат LIKE, если login задан")
    void toPredicate_shouldCreateLikePredicate_whenLoginIsSet() {
        // given
        ModeratorFilter filter = new ModeratorFilter();
        filter.setLogin("admin");

        Root<Moderator> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Predicate likePredicate = mock(Predicate.class);

        // настройка поведения моков
        when(root.get("login")).thenReturn(null);
        when(cb.like(any(), anyString())).thenReturn(likePredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(likePredicate);

        // when
        Predicate result = filter.toPredicate(root, query, cb);

        // then
        assertThat(result).isNotNull();
        verify(cb).like(any(), eq("%admin%"));
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("toPredicate — возвращает пустой предикат, если login == null")
    void toPredicate_shouldReturnEmptyPredicate_whenLoginIsNull() {
        // given
        ModeratorFilter filter = new ModeratorFilter(); // login не задан
        Root<Moderator> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Predicate andPredicate = mock(Predicate.class);

        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        // when
        Predicate result = filter.toPredicate(root, query, cb);

        // then
        assertThat(result).isEqualTo(andPredicate);
        // при этом метод like не должен вызываться
        verify(cb, never()).like(any(), anyString());
    }

    @Test
    @DisplayName("copy — создаёт независимую копию фильтра с тем же login")
    void copy_shouldReturnIndependentCopy() {
        // given
        ModeratorFilter original = new ModeratorFilter();
        original.setLogin("testUser");

        // when
        ModeratorFilter copy = original.copy();

        // then
        assertThat(copy).isNotSameAs(original);
        assertThat(copy.getLogin()).isEqualTo("testUser");

        // изменение копии не влияет на оригинал
        copy.setLogin("changed");
        assertThat(original.getLogin()).isEqualTo("testUser");
    }
}
