package int221.oasip.backendus3.services;

import int221.oasip.backendus3.entities.EventCategoryOwner;
import int221.oasip.backendus3.repository.EventCategoryOwnerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventCategoryOwnerService {
    private final EventCategoryOwnerRepository repository;

    public List<EventCategoryOwner> getAll() {
        return repository.findAll();
    }
}
