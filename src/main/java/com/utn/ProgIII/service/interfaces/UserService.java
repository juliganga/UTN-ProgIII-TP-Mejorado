package com.utn.ProgIII.service.interfaces;

import com.utn.ProgIII.dto.CreateUserDTO;
import com.utn.ProgIII.dto.UserWithCredentialDTO;
import com.utn.ProgIII.dto.ViewSupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz que dicta los comportamientos que debe incorporar el servicio de usuarios
 */
public interface UserService {
    UserWithCredentialDTO createUserWithCredential(CreateUserDTO dto);
    UserWithCredentialDTO getUserById(Long id);
    Page<UserWithCredentialDTO> filterUsers(String role, String status,Pageable pageable);
    UserWithCredentialDTO updateUser(Long id, CreateUserDTO dto);
    void deleteOrRemoveUser(Long id, String method);
    Page<UserWithCredentialDTO> getUsersPage(Pageable pageable);

}
