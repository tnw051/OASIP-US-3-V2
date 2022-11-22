package int221.oasip.backendus3.services;

import int221.oasip.backendus3.dtos.CategoryResponse;
import int221.oasip.backendus3.dtos.EditCategoryRequest;
import int221.oasip.backendus3.entities.EventCategory;
import int221.oasip.backendus3.entities.EventCategoryOwner;
import int221.oasip.backendus3.exceptions.EntityNotFoundException;
import int221.oasip.backendus3.exceptions.FieldNotValidException;
import int221.oasip.backendus3.exceptions.NotUniqueException;
import int221.oasip.backendus3.repository.EventCategoryOwnerRepository;
import int221.oasip.backendus3.repository.EventCategoryRepository;
import int221.oasip.backendus3.utils.ModelMapperUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventCategoryService {
    private EventCategoryRepository repository;
    private ModelMapper modelMapper;
    private ModelMapperUtils modelMapperUtils;
    private EventCategoryOwnerRepository eventCategoryOwnerRepository;

    public List<CategoryResponse> getAll() {
        return modelMapperUtils.mapList(repository.findAll(), CategoryResponse.class);
    }

    public CategoryResponse update(Integer id, EditCategoryRequest editCategory) {
        EventCategory category = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category with id" + id + " not found"));

        if (editCategory.getEventCategoryName() != null) {
            String strippedName = editCategory.getEventCategoryName().strip();
            if (strippedName.length() == 0) {
                throw new FieldNotValidException("eventCategoryName", "Category name cannot be empty");
            }
            EventCategory existingCategory = repository.findByEventCategoryNameIgnoreCase(strippedName);

            if (existingCategory != null && !existingCategory.getId().equals(category.getId())) {
                throw new NotUniqueException("Event category name is not unique");
            }

            category.setEventCategoryName(strippedName);
        }

        if (editCategory.getEventCategoryDescription() != null) {
            category.setEventCategoryDescription(editCategory.getEventCategoryDescription().strip());
        }

        if (editCategory.getEventDuration() != null) {
            category.setEventDuration(editCategory.getEventDuration());
        }

        return modelMapper.map(repository.saveAndFlush(category), CategoryResponse.class);
    }

    public List<CategoryResponse> getLecturerCategories(String email) {
        List<EventCategory> categories = eventCategoryOwnerRepository.findByOwnerEmail(email).stream().map(EventCategoryOwner::getEventCategory).collect(Collectors.toList());
        return modelMapperUtils.mapList(categories, CategoryResponse.class);
    }
}