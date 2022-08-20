package int221.oasip.backendus3.converters;

import int221.oasip.backendus3.entities.Role;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }

        return role.toString();
    }

    @Override
    public Role convertToEntityAttribute(String rawRole) {
        if (rawRole == null) {
            return null;
        }

        Stream.of(Role.values())
                .map(Role::toString).forEach(System.out::println);

        return Stream.of(Role.values())
                .filter(r -> r.toString().equals(rawRole))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
