package com.utn.ProgIII.controller;

import com.utn.ProgIII.dto.CreateUserDTO;
import com.utn.ProgIII.dto.UserWithCredentialDTO;
import com.utn.ProgIII.dto.ViewSupplierDTO;
import com.utn.ProgIII.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Clase que se encarga del procesamiento de las solicitudes recibidas desde el front
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con los usuarios")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Devuelve los datos del usuario con el ID enviado por parámetro
     * @param id El ID del usuario que se desea ver su información
     * @return Una respuesta en formato json para mostrar los datos del usuario deseado
     */
    @Operation(summary = "Obtener un usuario por ID", description = "Obtiene un usuario mediante ID")
    @ApiResponse(responseCode = "200",description = "Usuario encontrado", content = @Content(
            schema = @Schema(implementation = UserWithCredentialDTO.class)
    ))
    @ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
    @ApiResponse(responseCode = "404",description = "Usuario no encontrado", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Usuario no encontrado")
    ))
    @GetMapping("/{id}")
    public ResponseEntity<UserWithCredentialDTO> getUserById(@PathVariable @Parameter(description = "El ID de un usuario") Long id) {
        UserWithCredentialDTO response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Una página que contiene los datos de usuarios.
     * <p>Se puede definir el tamaño con ?size=?</p>
     * <p>Se puede definir el número de página con ?page=?</p>
     * <p>Se puede ordenar según parámetro de objeto con ?sort=?</p>
     * @param paginacion Una página con su contenido e información
     * @return Una página con contenido e información
     */
    @ApiResponse(
            responseCode = "200",
            description = "Encontrado",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserWithCredentialDTO.class)))
            })
    @ApiResponse(responseCode = "400", description = "Datos erróneos", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No property 'firstNam' found for type 'User'; Did you mean 'firstname'")
    ))
    @ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
    @ApiResponse(responseCode = "404", description = "No encontrado", content = {
            @Content(
                    mediaType = "text/plain;charset=UTF-8",
                    schema = @Schema(example = "No hay usuarios")
            )
    })
    @Operation(summary = "Busca una página de usuarios", description = "Lista una página de usuarios")
    @GetMapping("/page")
    public ResponseEntity<Page<UserWithCredentialDTO>> getUsers(
            @ParameterObject @PageableDefault(size = 10) Pageable paginacion
    )
    {
        return ResponseEntity.ok(userService.getUsersPage(paginacion));
    }

    /**
     * Muestra todos los usuarios que cumplan con los criterios solicitados o todos si no se proporciona ninguno
     * @return Una respuesta en formato json con los datos de los usuarios que cumplen los criterios solicitados
     */
    @Operation(summary = "Obtener una lista de todos los usuarios que tengan un rol o estado solicitado",
            description = "Retorna una lista de todos los usuarios que cumplan los criterios solicitados," +
                    "si no se solicita ninguno, se retornan todos los usuarios existentes")
    @ApiResponse(responseCode = "200",description = "Usuarios encontrados",content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = UserWithCredentialDTO.class))
    ))
    @ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
    @GetMapping()
    public ResponseEntity<Page<UserWithCredentialDTO>> getOrFilterUsers(@RequestParam(required = false) String role,
                                                                        @RequestParam(required = false) String status,
                                                                        @ParameterObject @PageableDefault(size = 10) Pageable paginacion) {
        return ResponseEntity.ok(userService.filterUsers(role,status,paginacion));
    }


    /**
     * Se crea un nuevo usuario y credenciales en el sistema a partir de la información recibida en el cuerpo de la request
     * @param dto El objeto de transferencia recibido desde la request
     * @return Un DTO para mostrar los datos cargados y su id correspondiente, como una respuesta
     */
    @Operation(summary = "Crea un usuario", description = "Crea un usuario con sus credenciales")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Error en datos insertados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "(Un mensaje de los errores del usuario)")
    ))
    @ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
    @ApiResponse(responseCode = "409", description = "Usuario existente por dni", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(examples = {"El nombre de usuario ya existe en la base de datos", "El dni ingresado ya se encuentra registrado"})
    ))
    @PostMapping()
    public ResponseEntity<UserWithCredentialDTO> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Los datos del usuario nuevo")
            @RequestBody CreateUserDTO dto
    ) {
        UserWithCredentialDTO response = userService.createUserWithCredential(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene los datos del usuario con el ID enviado por parámetro y los reemplaza por los provistos en el
     * cuerpo de la request
     * @param id El ID correspondiente al usuario que se solicitó modificar sus datos
     * @param dto El objeto de transferencia con los nuevos datos creado a partir del cuerpo de la request
     * @return Una respuesta en formato json para mostrar los nuevos datos del usuario modificado
     */
    @Operation(summary = "Actualiza un usuario", description = "Actualiza un usuario con los datos introducidos")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "400",description = "Error en datos insertados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(examples = {"El nombre de usuario ya existe en la base de datos", "El dni ingresado ya se encuentra registrado"})
    ))
    @ApiResponse(responseCode = "403", description = "Modificaciones no permitidas", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "(Un mensaje de error diciendo al administrador que no puede cambiar su nivel de acceso o estado de cuenta)")
    ))
    @PutMapping("/{id}")
    public ResponseEntity<UserWithCredentialDTO> updateUser(@PathVariable @Parameter(description = "El id de un usuario", example = "1") Long id,
                                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Los datos cambiados del usuario")
                                                            @RequestBody CreateUserDTO dto) {
        UserWithCredentialDTO response = userService.updateUser(id, dto);
        return ResponseEntity.ok().body(response);
    }

    /**
     * Elimina al usuario con el ID enviado por parámetro del sistema, pudiendo especificar si es por medio
     * de baja lógica o física
     * @param id El ID correspondiente al usuario que se solicitó eliminar
     * @param deletionType El tipo de eliminación (dura/física o blanda/lógica)
     * @return Una respuesta sin contenido (HTTP 204) para indicar que no hubo errores
     */
    @Operation(summary = "Elimina (hard) o hace una baja lógica (soft)", description = "Hace una baja lógica o una eliminación del usuario.")
    @ApiResponse(responseCode = "204", description = "Eliminación correcta", content = @Content())
    @ApiResponse(responseCode = "400", description = "Error en datos insertados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "La opción de eliminación no es correcta")
    ))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Usuario no encontrado")
    ))
    @ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
    @ApiResponse(responseCode = "409", description = "Intento de eliminación del usuario logueado", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No podes eliminar tu usuario")
    ))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "soft") @Parameter(description = "soft = baja lógica<br>hard = baja física") String deletionType) {

        userService.deleteOrRemoveUser(id, deletionType);
        return ResponseEntity.noContent().build();
    }
}
