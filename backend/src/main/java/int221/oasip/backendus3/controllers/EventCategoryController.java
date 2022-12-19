package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CategoryResponse;
import int221.oasip.backendus3.dtos.EditCategoryRequest;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.exceptions.NotUniqueException;
import int221.oasip.backendus3.services.EventCategoryService;
import int221.oasip.backendus3.services.auth.AuthStatus;
import int221.oasip.backendus3.services.auth.AuthUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class EventCategoryController {
    private EventCategoryService service;
    private AuthUtil authUtil;

    @GetMapping("")
    public List<CategoryResponse> getCategories() {
        return service.getAll();
    }

    @PatchMapping("/{id}")
    public CategoryResponse update(@PathVariable Integer id, @Valid @RequestBody EditCategoryRequest editCategory) {
        if (editCategory.getEventCategoryName() == null &&
                editCategory.getEventCategoryDescription() == null &&
                editCategory.getEventDuration() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one of eventCategoryName, eventCategoryDescription or eventDuration must be provided");
        }

        try {
            return service.update(id, editCategory);
        } catch (NotUniqueException e) {
            throw new FieldNotValidException("eventCategoryName", e.getMessage());
        }
    }

    // TODO: merge with getCategories()?
    @GetMapping("/lecturer")
    public List<CategoryResponse> getLecturerCategories() {
        AuthStatus authStatus = authUtil.getAuthStatus();
        if (!authStatus.isLecturer) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only lecturer can access this resource");
        }

        return service.getLecturerCategories(authStatus.getEmail());
    }
}
