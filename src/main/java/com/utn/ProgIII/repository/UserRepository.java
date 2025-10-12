package com.utn.ProgIII.repository;

import com.utn.ProgIII.model.Credential.Credential;
import com.utn.ProgIII.model.Credential.Role;
import com.utn.ProgIII.model.User.User;
import com.utn.ProgIII.model.User.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Clase que se encarga de interactuar con el ORM y el servicio de usuarios
 */
public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByDni(String dni);

    List<User> findAllByStatus(UserStatus status, Pageable pageable);

    boolean existsByCredentialRole(Role credentialRole);

    List<User> findByCredential_Role(Role credentialRole, Pageable pageable);

    List<User> findByCredential_RoleAndStatus(Role role, UserStatus userStatus, Pageable pageable);

    User findByCredential(Credential credential);

    Optional<User> findByCredential_Username(String credentialUsername);

}
