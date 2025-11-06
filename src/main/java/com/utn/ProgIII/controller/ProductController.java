package com.utn.ProgIII.controller;

import com.utn.ProgIII.dto.ProductDTO;
import com.utn.ProgIII.service.interfaces.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Clase que maneja requests sobre productos
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Productos", description = "Operaciones relacionadas con productos")
@ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
public class ProductController {

    private final ProductService productService;


    public ProductController (ProductService productService) {
        this.productService = productService;
    }

    //crea un producto nuevo
    @PostMapping
    @Operation(summary = "Se agrega un producto", description = "Se agrega un producto con los datos del usuario")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400",description = "Error en datos introducidos", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "(un mensaje con todos los errores del usuario)")
    ))
    @ApiResponse(responseCode = "409", description = "Producto ya existente", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Ese producto ya existe")
    ))
    public ResponseEntity<ProductDTO> addProduct (
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "El producto para crear")
            @RequestBody ProductDTO productDTO
    ){

        ProductDTO response = productService.createProductDto(productDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //muestra 1 solo producto

    @GetMapping ("/{id}")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Proveedor no encontrado", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Proveedor no encontrado")
    ))
    @Operation(summary = "Se muestra un producto por ID", description = "Se muestra el producto con todos sus datos")
    public ResponseEntity<ProductDTO> getProductById (@PathVariable @Parameter(description = "El ID del producto", example = "1") Long id) {
        ProductDTO response = productService.getProductById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Una página que contiene los datos de productos.
     * <p>Se puede definir el tamaño con ?size=?</p>
     * <p>Se puede definir el número de página con ?page=?</p>
     * <p>Se puede ordenar según parámetro de objeto con ?sort=?</p>
     * @param paginacion Una página con su contenido e información
     * @return Una página con contenido e información
     */
    //muestra la lista de todos los productos
    @GetMapping("/page")
    @Operation(summary = "Devuelve todos los productos", description = "Devuelve todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista devuelta correctamente", content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class))
    ))
    @ApiResponse(responseCode = "404", description = "Productos no encontrados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No hay resultados")
    ))
    public ResponseEntity<Page<ProductDTO>> getAllProduct (@ParameterObject @PageableDefault(size = 10) Pageable paginacion){
        Page<ProductDTO> response = productService.getAllProduct(paginacion);

        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    @Operation(summary = "Devuelve todos los productos en una lista", description = "Devuelve una lista de todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista devuelta correctamente", content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = List.class)
    )))
    @ApiResponse(responseCode = "404", description = "Productos no encontrados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No hay resultados")
    ))
    public ResponseEntity<List<ProductDTO>> getAllProductsList (){
        return ResponseEntity.ok(productService.listProductNames());
    }


    //muestra la lista de todos los productos por estado
    @GetMapping("/search/status/{status}")
    @ApiResponse(responseCode = "200", description = "Lista de productos según estado devuelta correctamente", content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class))
    ))
    @ApiResponse(responseCode = "400", description = "Error de datos introducidos por el usuario", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "El estado ingresado es inválido")
    ))
    @ApiResponse(responseCode = "404", description = "Productos no encontrados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No hay resultados")
    ))
    @Operation(summary = "Se muestra una lista de productos por su estado", description = "Se muestra una lista según su estado")
    public ResponseEntity<Page<ProductDTO>> getAllProductByStatus(@PathVariable @Parameter(description = "El estado de un producto (ENABLED, DISABLED)", example = "ENABLED") String status,@ParameterObject @PageableDefault(size = 10) Pageable paginacion){

        Page<ProductDTO> response = productService.getAllProductByStatus(status,paginacion);

        return ResponseEntity.ok(response);
    }

    //muestra lista de productos (busca por nombre)
    @GetMapping("/search/name/{name}")
    @Operation(summary = "Se muestra una lista de productos por nombres", description = "Se muestra una lista de productos por nombres")
    @ApiResponse(responseCode = "200", description = "Lista de productos según nombre")
    @ApiResponse(responseCode = "404",description = "Productos no encontrados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No hay resultados")
    ))
    public ResponseEntity<Page<ProductDTO>> getProductByName(@PathVariable @Parameter(description = "El nombre de un producto", example = "Manzana") String name,@ParameterObject @PageableDefault(size = 10) Pageable paginacion){

        return ResponseEntity.ok(productService.getProductByName(name,paginacion));
    }

    @GetMapping("/active-list")
    public ResponseEntity<List<ProductDTO>> getActiveProductsInList()
    {
        return ResponseEntity.ok(productService.getAllActiveProductAsList());
    }

    /*
    @ApiResponse(
            responseCode = "200",
            description = "Encontrado",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class)))
            })
    @ApiResponse(responseCode = "400", description = "Datos erróneos", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "No property 'nam' found for type 'Product'; Did you mean 'name'")
    ))
    @ApiResponse(responseCode = "404", description = "No encontrado", content = {
            @Content(
                    mediaType = "text/plain;charset=UTF-8",
                    schema = @Schema(example = "No hay usuarios")
            )
    })
    @Operation(summary = "Busca una página de productos", description = "Lista una página de productos")
    @GetMapping("/page")
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @ParameterObject @PageableDefault(size = 10) Pageable paginacion
    )
    {
        return ResponseEntity.ok(productService.getProductPage(paginacion));
    }*/

    //modificar un producto
    @PutMapping("/{id}")
    @Operation(summary = "Se actualiza los datos de un producto")
    @ApiResponse(responseCode = "200",description = "Actualización completa")
    @ApiResponse(responseCode = "400",description = "Datos mal colocados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "(un mensaje con todos los errores del usuario)")
    ))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Producto no encontrado")
    ))

    public ProductDTO update (@PathVariable @Parameter(description = "El ID de un producto", example = "1") Long id, @RequestBody ProductDTO modifyProductDTO){

       return productService.updateProduct(id,modifyProductDTO);
    }

    //Baja lógica de un producto (modifica solo el estado)
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Eliminado correctamente", content = @Content())
    @ApiResponse(responseCode = "404", description = "Producto no existe", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Producto no encontrado")
    ))
    @Operation(summary = "Se hace una baja lógica de un producto", description = "Se hace una baja lógica según su id")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
