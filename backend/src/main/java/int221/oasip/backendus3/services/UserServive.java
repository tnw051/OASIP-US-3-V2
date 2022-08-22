package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CreateUserRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.ValidationErrors;
import int221.oasip.backendus3.repository.UserRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServive {
    private UserRepository repository;
    private ModelMapper modelMapper;
    private ModelMapperUtils modelMapperUtils;

    public List<UserResponse> getAll() {
        List<User> users = repository.findAll(Sort.by("name"));
        return modelMapperUtils.mapList(users, UserResponse.class);
    }

    public UserResponse create(CreateUserRequest request) {
        String strippedName = request.getName().strip();
        String strippedEmail = request.getEmail().strip();
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
            errors.addFieldError("role", "Invalid role");
        }
        if (errors.hasErrors()) {
            throw errors;
        }

        User user = new User();
        user.setName(strippedName);
        user.setEmail(strippedEmail);
        user.setRole(parsedRole);

        return modelMapper.map(repository.saveAndFlush(user), UserResponse.class);
    }
}
