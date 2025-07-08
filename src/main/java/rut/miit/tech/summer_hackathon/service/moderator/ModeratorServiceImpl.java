package rut.miit.tech.summer_hackathon.service.moderator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.model.Department;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.repository.ModeratorRepository;
import rut.miit.tech.summer_hackathon.service.department.DepartmentService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ModeratorServiceImpl implements ModeratorService{
    private final ModeratorRepository moderatorRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Moderator getById(Long id) {
        return moderatorRepository.findById(id).orElseThrow();
        //TODO: сделать кастомный ResourceNotFound
    }

    @Override
    public Moderator save(Moderator moderator) {

        List<Department> departments = departmentService.getAllByIds(moderator.getDepartments()
                .stream().map(Department::getId).toList());
        moderator.setPassword(passwordEncoder.encode(moderator.getPassword()));
        Moderator saved = moderatorRepository.save(moderator);
        for(Department department: departments){
            department.setModerator(moderator);
        }
        departmentService.saveAll(departments);
        return saved;
    }

    @Override
    public void delete(Long id) {
        moderatorRepository.deleteById(id);
    }

    @Override
    public Moderator update(Moderator moderator) {
        Moderator nonUpdated = getById(moderator.getId());
        moderator.setPassword(nonUpdated.getPassword());
        //Старые ид департаментов которые привязаны к модератору
        List<Department> oldDeps = departmentService.getAllByModeratorId(moderator.getId());
        //новые ид департаментов но не привязанные
        List<Department> newDeps = departmentService.getAllByIds(
                moderator.getDepartments().stream().map(Department::getId).toList()
        );
        // новые ид департаментов в виде сета
        Set<Long> newDepsIds = newDeps.stream().map(Department::getId).collect(Collectors.toSet());
        //удаляем из старых все которые пересекаются с новыми
        //остаются только те которые нужно отвязать
        oldDeps.removeIf((s) -> newDepsIds.contains(s.getId()));
        //отвязываем
        oldDeps.forEach(s -> s.setModerator(null));
        //привязываем модератора к новым
        newDeps.forEach(s -> s.setModerator(moderator));
        //сливаем обновление старые (отвязка) и новых (привязка) в одну коллекцию
        newDeps.addAll(oldDeps);
        moderator.setDepartments(departmentService.updateAll(newDeps));
        return moderatorRepository.save(moderator);
    }

    @Override
    public PageResult<Moderator> getAll(Specification<Moderator> filter, Pageable pageable) {
        return PageResult.of(moderatorRepository.findAll(filter, pageable),pageable);
    }

    @Override
    public Moderator getByLogin(String username) {
        return moderatorRepository.findByLogin(username).orElseThrow();
    }
}
