package com.utn.ProgIII.service.implementations;

import com.utn.ProgIII.dto.CreateCredentialDTO;
import com.utn.ProgIII.dto.CreateUserDTO;
import com.utn.ProgIII.dto.UserWithCredentialDTO;
import com.utn.ProgIII.dto.ViewSupplierDTO;
import com.utn.ProgIII.exceptions.*;
import com.utn.ProgIII.mapper.UserMapper;
import com.utn.ProgIII.model.Credential.Role;
import com.utn.ProgIII.model.User.User;
import com.utn.ProgIII.model.User.UserStatus;
import com.utn.ProgIII.repository.UserRepository;
import com.utn.ProgIII.validations.CredentialValidations;
import com.utn.ProgIII.validations.UserValidations;
import com.utn.ProgIII.service.interfaces.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que se encarga de la lógica entre el repositorio y el mapper
 */
@Service
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidations userValidations;
    private final CredentialValidations credentialValidations;

    /**
     * Se crea un administrador en caso de que no exista
     */
    @PostConstruct
    public void init() {
        if(!userRepository.existsByCredentialRole(Role.ADMIN)) {
            CreateCredentialDTO testCreds = CreateCredentialDTO.builder()
                    .username("admin")
                    .password("admin1234")
                    .role("ADMIN")
                    .build();

            UserWithCredentialDTO testUser = createUserWithCredential(CreateUserDTO.builder()
                    .firstname("The")
                    .lastname("Administrator")
                    .dni("0000000")
                    .status("ENABLED")
                    .credential(testCreds)
                    .build());

            System.out.println("No se han encontrado administradores, se ha creado uno por defecto");
            System.out.println("El usuario es 'admin' y la contraseña es 'admin1234'");
        }
    }


    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, UserValidations userValidations,
                           CredentialValidations credentialValidations, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userValidations = userValidations;
        this.credentialValidations = credentialValidations;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Se crea un nuevo usuario y credenciales en el sistema, a partir de la información recibida en la request
     * @param dto El objeto de transferencia recibido desde la request
     * @return Un DTO para mostrar los datos cargados y su id correspondiente, como una respuesta
     */
    @Override
    @Transactional
    public UserWithCredentialDTO createUserWithCredential(CreateUserDTO dto) {
        User user = userMapper.toEntity(dto);

        userValidations.validateUserByDni(dto.dni());
        credentialValidations.validateUsernameNotExists(dto.credential().username());

        user.getCredential().setPassword(passwordEncoder.encode(user.getCredential().getPassword()));

        user = userRepository.save(user);

        return userMapper.toUserWithCredentialDTO(user);
    }

    /**
     * Muestra los datos del usuario con el ID solicitado por parámetro
     * @param id El ID correspondiente al usuario que se solicitó ver sus datos
     * @return Un DTO para mostrar la información de tal usuario
     * @throws UserNotFoundException Si el usuario no existe
     */
    @Override
    public UserWithCredentialDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        return userMapper.toUserWithCredentialDTO(user);
    }

    /**
     * Una página que contiene los datos de usuarios.
     * <p>Se puede definir el tamaño con ?size=?</p>
     * <p>Se puede definir el número de página con ?page=?</p>
     * <p>Se puede ordenar según parámetro de objeto con ?sort=?</p>
     * @param paginacion Una página con contenido e información
     * @return Una página con contenido e información
     */
    public Page<UserWithCredentialDTO> getUsersPage(Pageable paginacion)
    {
        Page<UserWithCredentialDTO> page = userRepository.findAll(paginacion).map(userMapper::toUserWithCredentialDTO);

        if(page.getNumberOfElements() == 0)
        {
            throw new UserNotFoundException("No hay usuarios");
        }

        return page;
    }

    /**
     * Muestra los datos de todos los usuarios presentes en el sistema, según el rol o estado
     * @param role El rol de los usuarios que se desea ver
     * @param status El estado de los usuarios que se desea ver
     * @return Una lista con los DTO de cada usuario existente en el sistema
     * @throws InvalidRequestException Si alguno de los parámetros tiene valores erróneos
     * <p>
     */
    @Override
    public Page<UserWithCredentialDTO> filterUsers(String role, String status,Pageable pageable) {
        List<User> users;

        if(role!= null && !EnumUtils.isValidEnum(Role.class, role.toUpperCase())) throw new InvalidRequestException("Ese rol no está presente");
        if(status != null && !EnumUtils.isValidEnum(UserStatus.class, status.toUpperCase())) throw new InvalidRequestException("Ese estado no está presente");

        if (role == null && status == null) {
            users = userRepository.findAll();
        } else if (status == null) {
            users = userRepository.findByCredential_Role(Role.valueOf(role.toUpperCase()));
        } else if (role == null) {
            users = userRepository.findAllByStatus(UserStatus.valueOf(status.toUpperCase()));
        } else {
            users = userRepository.findByCredential_RoleAndStatus(Role.valueOf(role.toUpperCase()),UserStatus.valueOf(status.toUpperCase()));
        }

        return new PageImpl<UserWithCredentialDTO>(users.stream().map(userMapper::toUserWithCredentialDTO).toList());
    }

    /**
     * Obtiene los datos del usuario solicitado por parámetro y los reemplaza por los enviados en la request.
     * @param id El ID correspondiente al usuario que se solicitó modificar sus datos
     * @param dto El objeto de transferencia con los nuevos datos recibidos desde la request
     * @return Un DTO para mostrar los nuevos datos cargados, como una respuesta
     * @throws UserNotFoundException Si el usuario no existe
     */
    @Override
    @Transactional
    public UserWithCredentialDTO updateUser(Long id, CreateUserDTO dto) {
        User currentUserData = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        User newUserData = userMapper.toEntity(dto);

        userValidations.validateModifiedUserByDni(currentUserData.getDni(), newUserData.getDni());
        credentialValidations.validateModifiedUsernameNotExists(currentUserData.getCredential().getUsername(),
                newUserData.getCredential().getUsername());

        if(userValidations.checkifRequestedUserIsTheSame(newUserData) && newUserData.getStatus() == UserStatus.DISABLED)
        {
            throw new ForbiddenModificationException("No puede desactivarse el mismo usuario que ejecuta la operación");
        }

        if(userValidations.checkifRequestedUserIsTheSame(newUserData) && newUserData.getCredential().getRole() != Role.ADMIN)
        {
            throw new ForbiddenModificationException("Administradores no pueden cambiar a ese rol");
        }

        newUserData.setIdUser(currentUserData.getIdUser());
        newUserData.getCredential().setIdCredential(currentUserData.getCredential().getIdCredential());
        newUserData.getCredential().setPassword(passwordEncoder.encode(newUserData.getCredential().getPassword()));

        currentUserData = userRepository.save(newUserData);

        return userMapper.toUserWithCredentialDTO(currentUserData);
    }

    /**
     * Un método que elimina un usuario de manera lógica o física (permanente)
     * @param id El ID del usuario
     * @param method El método de eliminación (hard o soft)
     */
    @Override
    public void deleteOrRemoveUser(Long id, String method) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if(userValidations.checkifRequestedUserIsTheSame(user))
        {
            throw new SelfDeleteUserException("No puede eliminarse el mismo usuario que ejecuta la operación");
        }

        switch (method.toUpperCase())
        {
            case "HARD":
                deleteUserHard(user);
                break;
            case "SOFT":
                deleteUserSoft(user);
                break;
            default:
                throw new InvalidRequestException("La opción de eliminación no es correcta");
        }

    }

    /**
     * Hace baja lógica del sistema al usuario con el ID solicitado por parámetro
     * @param user El usuario que se le dará baja temporal
     */
    @Transactional
    private void deleteUserSoft(User user) {
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
    }

    /**
     * Elimina físicamente del sistema al usuario con el ID solicitado por parámetro
     * @param user El usuario que se eliminará
     */
    @Transactional
    private void deleteUserHard(User user) {
            userRepository.delete(user);
    }

}
