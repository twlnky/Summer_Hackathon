package rut.miit.tech.summer_hackathon.service.UserSearch;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rut.miit.tech.summer_hackathon.domain.model.User;
import rut.miit.tech.summer_hackathon.repository.UserRepository;
import rut.miit.tech.summer_hackathon.service.UserSearch.UserSearchService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import org.springframework.data.jpa.domain.Specification;

@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final UserRepository userRepository;

    @Override
    public PageResult<User> searchAllUsers(String query, Pageable pageable) {
        Specification<User> spec = (root, q, cb) -> {
            String pattern = "%" + query.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("middleName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("position")), pattern),
                    cb.like(cb.lower(root.get("note")), pattern)
            );
        };

        return PageResult.of(userRepository.findAll(spec, pageable), pageable);
    }
}
