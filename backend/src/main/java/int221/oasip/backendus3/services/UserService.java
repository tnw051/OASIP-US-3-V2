package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CreateUserRequest;
import int221.oasip.backendus3.dtos.EditUserRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.EventCategoryOwner;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.ForbiddenException;
import int221.oasip.backendus3.exceptions.ValidationErrors;
import int221.oasip.backendus3.repository.EventCategoryOwnerRepository;
import int221.oasip.backendus3.repository.EventCategoryRepository;
import int221.oasip.backendus3.repository.UserRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository repository;
    private EventCategoryRepository categoryRepository;
    private EventCategoryOwnerRepository categoryOwnerRepository;
    private ModelMapper modelMapper;
    private ModelMapperUtils modelMapperUtils;
    private Argon2PasswordEncoder argon2PasswordEncoder;

    public List<UserResponse> getAll() {
        List<User> users = repository.findAll(Sort.by("name"));
        return modelMapperUtils.mapList(users, UserResponse.class);
    }

    public UserResponse create(CreateUserRequest request) {
        String strippedName = request.getName().strip();
        String strippedEmail = request.getEmail().strip();
        String password = request.getPassword();
        String strippedRoleRaw = request.getRole().strip();
        Role parsedRole = null;

        ValidationErrors errors = new ValidationErrors();

        if (repository.findByName(strippedName).isPresent()) {
            errors.addFieldError("name", "Name is not unique");
        }
        if (repository.findByEmail(strippedEmail).isPresent()) {
            errors.addFieldError("email", "Email is not unique");
        }
        try {
            parsedRole = Role.fromString(strippedRoleRaw);
        } catch (IllegalArgumentException e) {
            errors.addFieldError("role", "Role must be either student, admin, or lecturer");
        }
        if (errors.hasErrors()) {
            throw errors;
        }

        User user = new User();
        user.setName(strippedName);
        user.setEmail(strippedEmail);
        user.setPassword(argon2PasswordEncoder.encode(password));
        user.setRole(parsedRole);

        return modelMapper.map(repository.saveAndFlush(user), UserResponse.class);
    }

    public void delete(Integer id) {
        User user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));

        // if the user is lecturer, check if there are any lecturers left to take over the categories (owners of each category must be > 0)
        // if there are none, throw an error
        if (user.getRole().equals(Role.LECTURER)) {
            List<EventCategory> ownCategories = this.categoryRepository.findByOwners_OwnerEmail(user.getEmail());
            List<EventCategory> ownCategoriesWithOneOwner = ownCategories.stream().filter(category -> category.getOwners().size() == 1).collect(Collectors.toList());
            if (ownCategoriesWithOneOwner.size() > 0) {
                String message = user.getName() + " is the owner of " +
                        getCategoriesString(ownCategories) +
                        ". You cannot delete this user account since " + user.getName() + " is the only owner of " +
                        getCategoriesString(ownCategoriesWithOneOwner) +
                        ". Another owner must be added to the event category(s) before this lecturer can be deleted.";

                throw new ForbiddenException(message);
            }

            Stream<EventCategoryOwner> lecturerCategoryOwnerships = ownCategories.stream()
                    .flatMap(category ->
                            category.getOwners().stream().filter(owner -> owner.getOwnerEmail().equals(user.getEmail())));
            lecturerCategoryOwnerships.forEach(owner -> this.categoryOwnerRepository.delete(owner));
        }

        repository.deleteById(id);
    }

    private StringBuilder getCategoriesString(List<EventCategory> categories) {
        return getListString(categories, EventCategory::getEventCategoryName);
    }

    private <T, R> StringBuilder getListString(List<T> list, Function<T, R> mapper) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(mapper.apply(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb;
    }

    public UserResponse update(Integer id, EditUserRequest request) {
        User user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));

        ValidationErrors errors = new ValidationErrors();
        if (request.getName() != null) {
            String strippedName = request.getName().strip();
            if (!user.getName().equals(strippedName)) {
                if (!repository.existsByName(strippedName)) {
                    user.setName(strippedName);
                } else {
                    errors.addFieldError("name", "Name is not unique");
                }
            }
        }

        if (request.getEmail() != null) {
            String strippedEmail = request.getEmail().strip();
            if (!user.getEmail().equals(strippedEmail)) {
                if (!repository.existsByEmail(strippedEmail)) {
                    user.setEmail(strippedEmail);
                } else {
                    errors.addFieldError("email", "Email is not unique");
                }
            }
        }

        if (request.getRole() != null) {
            String strippedRoleRaw = request.getRole().strip();
            try {
                Role parsedRole = Role.fromString(strippedRoleRaw);
                user.setRole(parsedRole);
            } catch (IllegalArgumentException e) {
                errors.addFieldError("role", "Invalid role, must be either student, admin, or lecturer");
            }
        }

        if (errors.hasErrors()) {
            throw errors;
        }

        return modelMapper.map(repository.saveAndFlush(user), UserResponse.class);
    }

    public UserResponse getById(Integer id) {
        User user = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
        return modelMapper.map(user, UserResponse.class);
    }
}
