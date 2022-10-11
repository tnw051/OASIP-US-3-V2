package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CreateUserRequest;
import int221.oasip.backendus3.dtos.EditUserRequest;
import int221.oasip.backendus3.dtos.UserResponse;
import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.ValidationErrors;
import int221.oasip.backendus3.services.UserServive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public UserResponse create(@Valid @RequestBody CreateUserRequest request, BindingResult bindingResult) {
        try {
            return service.create(request);
        } catch (ValidationErrors e) {
            e.addFieldErrors(bindingResult);
            throw e;
        }
    }

    @GetMapping("/roles")
    public Role[] getRoles() {
        return Role.values();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        try {
            service.delete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Integer id, @Valid @RequestBody EditUserRequest editUserRequest) {
        if (editUserRequest.getName() == null &&
                editUserRequest.getEmail() == null &&
                editUserRequest.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of name, email or role must be provided");
        }

        return service.update(id, editUserRequest);
    }
}
