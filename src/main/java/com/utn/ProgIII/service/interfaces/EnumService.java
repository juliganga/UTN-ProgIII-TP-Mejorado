package com.utn.ProgIII.service.interfaces;

import com.utn.ProgIII.dto.EnumDTO;
import java.util.List;

public interface EnumService {
    List<EnumDTO> getAllStates();
    List<EnumDTO> getAllRoles();
    List<EnumDTO> getAllUserStates();
}
