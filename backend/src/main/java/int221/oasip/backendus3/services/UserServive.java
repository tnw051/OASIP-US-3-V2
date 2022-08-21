package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CreateUserRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.exceptions.NotUniqueException;
import int221.oasip.backendus3.repository.UserRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

        Optional<User> existingUser = repository.findByName(strippedName);
        if (existingUser.isPresent()) {
            throw new NotUniqueException("Name is not unique");
        }
        existingUser = repository.findByEmail(strippedEmail);
        if (existingUser.isPresent()) {
            throw new NotUniqueException("Email is not unique");
        }

        User user = new User();
        user.setName(strippedName);
        user.setEmail(strippedEmail);
        user.setRole(request.getRole());

        return modelMapper.map(repository.saveAndFlush(user), UserResponse.class);
    }
}
