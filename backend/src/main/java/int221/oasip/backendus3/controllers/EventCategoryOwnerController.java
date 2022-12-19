package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.dtos.CategoryOwnerResponse;
import int221.oasip.backendus3.entities.EventCategoryOwner;
import int221.oasip.backendus3.services.EventCategoryOwnerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/category-owners")
@AllArgsConstructor
public class EventCategoryOwnerController {
    private final EventCategoryOwnerService service;

    @GetMapping
    public List<CategoryOwnerResponse> getAll() {
        List<EventCategoryOwner> ownerships = service.getAll();
        return ownerships.stream()
                .map(o ->
                        new CategoryOwnerResponse(o.getId(), o.getOwnerEmail(), o.getEventCategory().getId()))
                .collect(Collectors.toList());
    }
}
