package rut.miit.tech.summer_hackathon.controller.moderator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rut.miit.tech.summer_hackathon.controller.query.PageParam;
import rut.miit.tech.summer_hackathon.controller.query.SortParam;
import rut.miit.tech.summer_hackathon.domain.dto.ModeratorDTO;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.service.moderator.ModeratorService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;


@RestController
@RequestMapping("/api/v1/moderators")
@RequiredArgsConstructor
public class ModeratorController {
    private final ModeratorService moderatorService;


    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping()
    public PageResult<ModeratorDTO> getAll(
            @ModelAttribute ModeratorFilter filter,
            @ModelAttribute PageParam pageParam,
            @ModelAttribute SortParam sortParam) {


        return moderatorService.getAll(filter, pageParam.toPageable(sortParam))
                .map(Moderator::toDto);
    }


    @PreAuthorize("hasAuthority('ADMIN') or @security.checkAccessToModer(#id)")
    @GetMapping("/{id}")
    public ModeratorDTO getById(@PathVariable Long id) {

        return moderatorService.getById(id).toDto();
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ModeratorDTO create(@Valid @RequestBody ModeratorDTO moderator) {

        return moderatorService.save(moderator.toModel()).toDto();
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ModeratorDTO update(@Valid @RequestBody ModeratorDTO moderator) {
        return moderatorService.update(moderator.toModel()).toDto();
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        moderatorService.getById(id);
        moderatorService.delete(id);
    }
}