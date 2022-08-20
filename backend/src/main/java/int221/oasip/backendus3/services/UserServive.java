package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CreateUserRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.User;
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
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        return modelMapper.map(repository.saveAndFlush(user), UserResponse.class);
    }
}
