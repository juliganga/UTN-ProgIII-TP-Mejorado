package com.utn.ProgIII.service.implementations;

import com.utn.ProgIII.dto.EnumDTO;
import com.utn.ProgIII.mapper.EnumMapper;
import com.utn.ProgIII.model.Credential.Role;
import com.utn.ProgIII.model.Product.ProductStatus;
import com.utn.ProgIII.service.interfaces.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnumServiceImpl implements EnumService {
    @Autowired
    private EnumMapper enumMapper;

    @Override
    public List<EnumDTO> getAllStates() {
        return enumMapper.createListFromEnum(ProductStatus.class);
    }

    @Override
    public List<EnumDTO> getAllRoles() {
        return enumMapper.createListFromEnum(Role.class);
    }
}
