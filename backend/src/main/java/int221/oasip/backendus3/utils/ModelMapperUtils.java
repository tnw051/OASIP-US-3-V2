package int221.oasip.backendus3.utils;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ModelMapperUtils {
    private ModelMapper modelMapper;

    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream().map(s -> modelMapper.map(s, targetClass)).collect(Collectors.toList());
    }
}
