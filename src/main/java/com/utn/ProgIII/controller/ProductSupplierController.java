package com.utn.ProgIII.controller;

import com.utn.ProgIII.dto.*;
import com.utn.ProgIII.service.interfaces.ProductSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;

/**
 * Clase que maneja requests sobre la relación de productos y proveedores
 */
@RestController
@RequestMapping("/productSupplier")
@Tag(name = "Productos y Proveedores", description = "Operaciones relacionadas con la relación de productos y proveedores")
@ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
public class ProductSupplierController {

    private final ProductSupplierService productSupplierService;

    public ProductSupplierController(ProductSupplierService productSupplierService){
        this.productSupplierService = productSupplierService;
    }

    @PostMapping
    @Operation(summary = "Crea un ProductSupplier", description = "Crea una relación entre un proveedor y producto")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Datos erróneos", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "(Un mensaje que tiene los errores del usuario)")
    ))
    @ApiResponse(responseCode = "404", description = "No encontrado o no activo", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "(Un mensaje que tiene un error de usuario (producto no activo/existente, proveedor inexistente))")
    ))
    public ResponseEntity<ResponseProductSupplierDTO> createProductSupplier(@Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Relación entre proveedor y producto para crear")
                                                                                CreateProductSupplierDTO request
    ){

        ResponseProductSupplierDTO response = productSupplierService.createProductSupplier(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PatchMapping("/{id}")
    @ApiResponse(responseCode = "200",description = "Datos modificados")
    @ApiResponse(responseCode = "404",description = "Relación no encontrada", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "La relación que quiere editar no se encuentra")
    ))
    @Operation(summary = "Modifica los datos de una relación", description = "Modifica el precio y el profit margin de una relación")
    public ResponseEntity<ResponseProductSupplierDTO> modifyProductSupplier(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Relación para modificar los precios y el profit margin")
            @Valid @RequestBody UpdateProductSupplierDTO request,
            @PathVariable @Parameter(example = "1", description = "El ID de un listado de precio") Long id){

        ResponseProductSupplierDTO response = productSupplierService.updateProductSupplier(request, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductSupplier(
            @Parameter(description = "El ID de la relación para eliminar <b>permanentemente</b>", example = "1")
            @PathVariable Long id){

        productSupplierService.deleteProductSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca todos los productos de un proveedor según su nombre", description = "Busca todos los productos de un proveedor según su nombre. Los contenidos dependen del permiso del usuario.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de precios devuelta",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SupplierProductListDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "manager-view",
                                    summary = "Vista para rol MANAGER o superior",

                                    description = "Incluye todos los precios (costo y venta) y márgenes de ganancia",
                                    value = "{\"idProduct\": 1, \"name\": \"Producto\", \"prices\": [{\"idProductSupplier\":1, \"idSupplier\": 1, \"companyName\": \"Proveedor\", \"cost\": 100.00, \"profitMargin\": 20.00, \"price\": 120.00, \"dollarPrice\": 0.1043}]}"
                            ),
                            @ExampleObject(
                                    name = "manager-view-no-dollar-api",
                                    summary = "Vista para rol MANAGER o superior (API dólar caída o cotización inexistente)",
                                    description = "Incluye todos los precios (costo y venta) y márgenes de ganancia",
                                    value = "{\"idProduct\": 1, \"name\": \"Producto\", \"prices\": [{\"idProductSupplier\":1,  \"idSupplier\": 1, \"companyName\": \"Proveedor\", \"cost\": 100.00, \"profitMargin\": 20.00, \"price\": 120.00, \"dollarPrice\": \"not available\"}]}"
                            ),
                            @ExampleObject(
                                    name = "employee-view",
                                    summary = "Vista para rol EMPLOYEE",
                                    description = "Solo muestra precios finales sin incluir márgenes de ganancia",
                                    value = "{\"idProduct\": 1, \"name\": \"Producto\", \"prices\": [{\"idSupplier\": 1, \"companyName\": \"Proveedor\", \"price\": 120.00}]}"
                            )
                    }))
    @ApiResponse(responseCode = "404",description = "Proveedor inexistente", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "El proveedor no existe")
    ))
    @GetMapping("/filter/{companyId}")
    public ResponseEntity<SupplierProductListDTO> listAllProductsBySupplier(Pageable pageable, @PathVariable @Parameter(description = "El id de una empresa") Long companyId, @RequestParam(defaultValue = "oficial",required = false) @Parameter(description = "Un tipo de cotización disponible en dolarapi.com", required = false) String exchange_rate){

        SupplierProductListDTO response = productSupplierService.listProductsBySupplier(pageable, companyId, exchange_rate);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/filter-product/{productId}")
    @Operation(summary = "Devuelve una lista de precios existentes de un producto según su ID.", description = "Devuelve una lista de precios existentes de un producto según su ID. Los contenidos dependen del rol del usuario.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de precios devuelta",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProductPricesDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "manager-view",
                                    summary = "Vista para rol MANAGER o superior",
                                    description = "Incluye todos los precios (costo y venta) y márgenes de ganancia",
                                    value = "{\"idProduct\": 1, \"name\": \"Producto\", \"prices\": [{\"idSupplier\": 1, \"companyName\": \"Proveedor\", \"cost\": 100.00, \"profitMargin\": 20.00, \"price\": 120.00, \"dollarPrice\": 0.1043}]}"
                            ),
                            @ExampleObject(
                                    name = "manager-view-no-dollar-api",
                                    summary = "Vista para rol MANAGER o superior (API dólar caída o cotización inexistente)",
                                    description = "Incluye todos los precios (costo y venta) y márgenes de ganancia",
                                    value = "{\"idProduct\": 1, \"name\": \"Producto\", \"prices\": [{\"idSupplier\": 1, \"companyName\": \"Proveedor\", \"cost\": 100.00, \"profitMargin\": 20.00, \"price\": 120.00, \"dollarPrice\": \"not available\"}]}"
                            ),
                            @ExampleObject(
                                    name = "employee-view",
                                    summary = "Vista para rol EMPLOYEE",
                                    description = "Solo muestra precios finales sin incluir márgenes de ganancia",
                                    value = "{\"idProduct\": 1, \"name\": \"Producto\", \"prices\": [{\"idSupplier\": 1, \"companyName\": \"Proveedor\", \"price\": 120.00  }]}"
                            )
                    }
            )
    )
    @ApiResponse(responseCode = "404", description = "Producto desactivado", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "El producto está desactivado, y no tendrá precios.")
    ))
    public ResponseEntity<ProductPricesDTO> listAllPricesByProduct(@ParameterObject @PageableDefault(size = 10) Pageable paginacion,@PathVariable @Parameter(description = "El ID de un producto", example = "1") Long productId, @RequestParam(defaultValue = "oficial",required = false) @Parameter(description = "Un tipo de cotización disponible en dolarapi.com", required = false) String exchange_rate){
        return ResponseEntity.ok(productSupplierService.listPricesByProduct(paginacion, productId, exchange_rate));
    }

    @Operation(summary = "Actualiza los precios de los productos de un proveedor masivamente",
            description = "Actualiza los precios de los productos de un proveedor por medio de una lista en formato .csv, " +
                    "pero solamente si la relación entre el producto y el proveedor ya existen. Finalmente, devuelve una lista con aquellos " +
                    "productos que no pudieron ser cargados")
    @ApiResponse(responseCode = "200",description = "Actualización realizada, devuelve un listado con aquellos productos que no pudieron ser cargados", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Productos no subidos:\nProduct 1\nProduct 3")
    ))
    @ApiResponse(responseCode = "404",description = "El proveedor elegido no existe", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "El proveedor asignado no existe")
    ))
    @ApiResponse(responseCode = "500",description = "El servidor no pudo procesar el archivo", content = @Content())
    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> updateWithFile(@RequestParam("file") MultipartFile file,@RequestParam @Parameter(example = "1", description = "El ID de un proveedor") Long idSupplier) {
        String filename = file.getOriginalFilename();
        String response;

        try {
            String tmpdir = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
            file.transferTo(new File(tmpdir + "\\" + filename));
            response = productSupplierService.uploadCsv(tmpdir + "\\" + filename, idSupplier);
        } catch (IOException e) {
            System.out.println("Error subiendo el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Actualiza los precios de los productos de un proveedor masivamente y vincula productos sin relación existente, ignora aquellos que estén desactivados",
            description = "Actualiza los precios de los productos de un proveedor por medio de una lista en formato .csv, " +
                    "y carga nuevas entradas con un porcentaje de ganancia definido si aún no están relacionados. Finalmente, devuelve una lista con aquellos " +
                    "productos que no pudieron ser cargados")
    @ApiResponse(responseCode = "200",description = "Actualización realizada, devuelve listado con aquellos productos que no pudieron ser cargados (productos desactivados o no existentes)", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Productos no subidos:\nProduct 1\nProduct 3")
    ))
    @ApiResponse(responseCode = "404",description = "El proveedor elegido no existe", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "El proveedor asignado no existe")
    ))
    @ApiResponse(responseCode = "500",description = "El servidor no pudo procesar el archivo", content = @Content(
            mediaType = "text/plain;charset=UTF-8",
            schema = @Schema(example = "Error subiendo el archivo: (mensaje de excepción)")
    ))
    @PostMapping(path = "/uploadNonRelatedProducts", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> updateWithFileAndProfitMargin(@RequestParam("file") MultipartFile file,
                                                                @RequestParam @Parameter(example = "1", description = "El ID de un proveedor") Long idSupplier,
                                                                @RequestParam(required = false) @Parameter(example = "20", description = "El porcentaje de margen de ganancia") BigDecimal bulkProfitMargin) {
        String filename = file.getOriginalFilename();
        String response;
        if (bulkProfitMargin == null) bulkProfitMargin = BigDecimal.valueOf(0);

        try {
            String tmpdir = Files.createTempDirectory("tmpDirPrefix").toFile().getAbsolutePath();
            file.transferTo(new File(tmpdir + "\\" + filename));
            response = productSupplierService.uploadCsv(tmpdir + "\\" + filename, idSupplier, bulkProfitMargin);
        } catch (IOException e) {
            System.out.println("Error subiendo el archivo: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(response);
    }

}
