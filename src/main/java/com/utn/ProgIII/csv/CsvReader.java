package com.utn.ProgIII.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.utn.ProgIII.dto.ProductInfoFromCsvDTO;
import com.utn.ProgIII.exceptions.SupplierNotFoundException;
import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.Product.ProductStatus;
import com.utn.ProgIII.model.ProductSupplier.ProductSupplier;
import com.utn.ProgIII.model.Supplier.Supplier;
import com.utn.ProgIII.repository.ProductRepository;
import com.utn.ProgIII.repository.ProductSupplierRepository;
import com.utn.ProgIII.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
/**
 * Clase que sirve para leer archivos csv para realizar operaciones sobre la base de datos
 */
public class CsvReader {
    private final SupplierRepository supplierRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final ProductRepository productRepository;

    public CsvReader(ProductRepository productRepository, SupplierRepository supplierRepository, ProductSupplierRepository productSupplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.productSupplierRepository = productSupplierRepository;
    }

    /**
     * Crea una lista desde el csv para poder realizar operaciones
     * @param path El path interno del archivo
     * @return Una lista de datos de productos en forma de dto
     * @throws IOException En caso de que falle leerse un archivo
     */
    public static List<ProductInfoFromCsvDTO> readFile(String path) throws IOException {
        File csvFile = new File(path);
        CsvSchema schema = CsvSchema.builder()
                .addColumn("id")
                .addColumn("nombre")
                .addColumn("precio")
                .setSkipFirstDataRow(true)
                .build();

        CsvMapper mapper = new CsvMapper();
        MappingIterator<ProductInfoFromCsvDTO> productIterator = mapper
                .readerFor(ProductInfoFromCsvDTO.class)
                .with(schema)
                .readValues(csvFile);

        return productIterator.readAll();
    }

    /**
     * Actualiza relaciones existentes de productos desde una lista de dtos
     * @param csvFilePath El path interno del archivo
     * @param supplierId El proveedor que se le actualizaran las relaciones
     * @return Un mensaje de error que lista los productos que no fueron actualzados
     */
    public String uploadToDatabase(String csvFilePath, Long supplierId) {
        StringBuilder message = new StringBuilder("Productos no subidos: \n");
        List<ProductInfoFromCsvDTO> uploads;
        List<ProductInfoFromCsvDTO> failedUploads = new ArrayList<>();

        try {
            uploads = readFile(csvFilePath).stream().toList();
            Supplier supplierData = supplierRepository.getReferenceById(supplierId);

            for (ProductInfoFromCsvDTO productUpdateInfo: uploads) {
                Product productData = productRepository.getByName(productUpdateInfo.name());
                ProductSupplier relationship = productSupplierRepository.getByProductAndSupplier(productData,supplierData);
                if (productData != null && productData.getStatus().equals(ProductStatus.ENABLED) && relationship != null) {
                    relationship = updateRelationshipPricing(productUpdateInfo,relationship);
                    productSupplierRepository.save(relationship);
                } else {
                    failedUploads.add(productUpdateInfo);
                }
            }
        } catch (IOException e) {
            System.out.println("Error procesando el archivo: " + e.getMessage());
        }

        failedUploads.forEach(failedUpload -> message.append(failedUpload.name()).append("\n"));
        return message.toString();
    }

    /**
     * Carga relaciones nuevas en caso de que no existan entre un producto y proveedor, cambian el costo de las relaciones existentes
     * @param csvFilePath El camino interno del archivo
     * @param supplierId El proveedor que se le actualizaran las relaciones
     * @param bulkProfitMargin El margen de ganancia con el que se cargan las nuevas relaciones
     * @return Un mensaje de error diciendo que productos no fueron cargados
     */
    public String uploadToDatabase(String csvFilePath, Long supplierId, BigDecimal bulkProfitMargin) {
        StringBuilder message = new StringBuilder("Productos no subidos: \n");
        List<ProductInfoFromCsvDTO> uploads;
        List<ProductInfoFromCsvDTO> failedUploads = new ArrayList<>();
        try {
            uploads = readFile(csvFilePath).stream().toList();
            Supplier supplierData = supplierRepository.getReferenceById(supplierId);

            for (ProductInfoFromCsvDTO productUpdateInfo: uploads) {
                Product productData = productRepository.getByName(productUpdateInfo.name());
                ProductSupplier relationship = productSupplierRepository.getByProductAndSupplier(productData,supplierData);
                if (productData != null && productData.getStatus().equals(ProductStatus.ENABLED)) {
                    if (relationship == null) {
                        relationship = new ProductSupplier(supplierData,productData,productUpdateInfo.cost(),bulkProfitMargin);
                    } else {
                        relationship = updateRelationshipPricing(productUpdateInfo, relationship);
                    }
                    productSupplierRepository.save(relationship);
                } else {
                    failedUploads.add(productUpdateInfo);
                }
            }
        } catch (IOException e) {
            System.out.println("Error procesando el archivo: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new SupplierNotFoundException("El proveedor asignado no existe");
        }

        failedUploads.forEach(failedUpload -> message.append(failedUpload.name()).append("\n"));
        return message.toString();
    }

    /**
     * Actualiza el precio de una relacion
     * @param productUpdateInfo El dto del csv
     * @param relationship La relacion existente
     * @return La relacion modificada
     */
    public ProductSupplier updateRelationshipPricing(ProductInfoFromCsvDTO productUpdateInfo, ProductSupplier relationship) {
        relationship.setCost(productUpdateInfo.cost());
        relationship.setPrice(relationship.getCost()
                .add(relationship.getCost()
                        .multiply(relationship.getProfitMargin())
                        .divide(BigDecimal.valueOf(100), RoundingMode.CEILING)));
        return relationship;
    }
}
