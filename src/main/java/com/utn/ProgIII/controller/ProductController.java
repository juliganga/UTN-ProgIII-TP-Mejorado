package com.utn.ProgIII.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utn.ProgIII.dto.CreateProductDTO;
import com.utn.ProgIII.dto.ProductDTO;
import com.utn.ProgIII.exceptions.BadRequestException;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))
    @ApiResponse(responseCode = "409", description = "Producto ya existente", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))
    public ResponseEntity<ProductDTO> addProduct (
            @RequestPart(required = false) MultipartFile file,
            @RequestPart String productData
            ){

        CreateProductDTO productDTO = transformDataToCreateProductDTO(productData);
        ProductDTO response = productService.createProductDto(productDTO, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private CreateProductDTO transformDataToCreateProductDTO(String productDto)
    {
        ObjectMapper objectMapper = new ObjectMapper();

        CreateProductDTO productDTO;
        try {
            productDTO = objectMapper.readValue(productDto, CreateProductDTO.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Hay datos faltantes!");
        }

        return productDTO;
    }

    //muestra 1 solo producto

    @GetMapping ("/{id}")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Proveedor no encontrado", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
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
    /*
    @ApiResponse(responseCode = "404", description = "Productos no encontrados", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))*/
    public ResponseEntity<Page<ProductDTO>> getAllProduct (
            @ParameterObject @PageableDefault(size = 5) @SortDefault(sort = "name", direction = Sort.Direction.ASC) Pageable paginacion,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> category,
            @RequestParam(required = false) Integer rem_stock,
            @RequestParam(required = false) Long id
    ){
        Page<ProductDTO> response = productService.getProductsPage(paginacion, name, status, category, id, rem_stock);

        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    @Operation(summary = "Devuelve todos los productos en una lista", description = "Devuelve una lista de todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista devuelta correctamente", content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ProductDTO.class)
    )))
    @ApiResponse(responseCode = "404", description = "Productos no encontrados", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))
    public ResponseEntity<List<ProductDTO>> getAllProductsAsList(){
        return ResponseEntity.ok(productService.getAllProductsAsList());
    }

    @GetMapping("/active-list")
    public ResponseEntity<List<ProductDTO>> getActiveProductsInList()
    {
        return ResponseEntity.ok(productService.getAllActiveProductAsList());
    }

    //modificar un producto
    @PutMapping("/{id}")
    @Operation(summary = "Se actualiza los datos de un producto")
    @ApiResponse(responseCode = "200",description = "Actualización completa")
    @ApiResponse(responseCode = "400",description = "Datos mal colocados", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))

    public ProductDTO update (
            @PathVariable @Parameter(description = "El ID de un producto", example = "1") Long id,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart String productData)
    {
        CreateProductDTO productDTO = transformDataToCreateProductDTO(productData);
        return productService.updateProduct(id,productDTO, file);
    }

    //Baja lógica de un producto (modifica solo el estado)
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Eliminado correctamente", content = @Content())
    @ApiResponse(responseCode = "404", description = "Producto no existe", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class)
    ))
    @Operation(summary = "Se hace una baja lógica de un producto", description = "Se hace una baja lógica según su id")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
