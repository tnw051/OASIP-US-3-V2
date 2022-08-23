package int221.oasip.backendus3.converters;

import int221.oasip.backendus3.entities.Role;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

//@Converter(autoApply = false)
//public class RoleConverter implements AttributeConverter<Role, String> {
//    @Override
//    public String convertToDatabaseColumn(Role role) {
//        if (role == null) {
//            return null;
//        }
//        return role.toString();
//    }
//
//    @Override
//    public Role convertToEntityAttribute(String role) {
//        if (role == null) {
//            return null;
//        }
//        return Role.fromString(role);
//    }
//}
