package com.utn.ProgIII.mapper;

import com.utn.ProgIII.dto.EnumDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EnumMapper {
    public <E extends Enum<E>> List<EnumDTO> createListFromEnum(Class<E> klass) {
        List<EnumDTO> list = new ArrayList<>();
        for (E c : klass.getEnumConstants()) {
            list.add(new EnumDTO(c.name(),c.ordinal()));
        }

        return list;
    }
}
