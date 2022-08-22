package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CreateUserRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.services.UserServive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserServive service;

    @GetMapping("")
    public List<UserResponse> getUsers() {
        return service.getAll();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return service.create(request);
    }

    @GetMapping("/roles")
    public Role[] getRoles() {
        return Role.values();
    }

}
