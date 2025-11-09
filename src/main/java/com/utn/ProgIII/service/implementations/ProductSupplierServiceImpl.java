package com.utn.ProgIII.service.implementations;

import com.utn.ProgIII.csv.CsvReader;
import com.utn.ProgIII.dto.*;
import com.utn.ProgIII.exceptions.*;
import com.utn.ProgIII.mapper.ProductSupplierMapper;
import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.Product.ProductStatus;
import com.utn.ProgIII.model.ProductSupplier.*;
import com.utn.ProgIII.model.Supplier.Supplier;
import com.utn.ProgIII.repository.ProductRepository;
import com.utn.ProgIII.repository.ProductSupplierRepository;
import com.utn.ProgIII.repository.SupplierRepository;
import com.utn.ProgIII.service.interfaces.AuthService;
import com.utn.ProgIII.service.interfaces.MiscService;
import com.utn.ProgIII.service.interfaces.ProductSupplierService;
import com.utn.ProgIII.validations.ProductSupplierValidations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
/*
 * Un servicio que se encarga de hacer funciones para la relación entre productos y proveedores
 */
public class ProductSupplierServiceImpl implements ProductSupplierService {

    private final ProductSupplierRepository productSupplierRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSupplierMapper mapper;
    private final ProductSupplierValidations productSupplierValidations;
    private final CsvReader csvReader;
    private final AuthService authService;
    private final MiscService miscService;

    public ProductSupplierServiceImpl(ProductSupplierRepository productSupplierRepository,
                                      ProductRepository productRepository,
                                      SupplierRepository supplierRepository,
                                      ProductSupplierMapper mapper,
                                      ProductSupplierValidations productSupplierValidations,
                                      CsvReader csvReader,
                                      AuthService authService,
                                      MiscService miscService
    ){

        this.productSupplierRepository = productSupplierRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
        this.productSupplierValidations = productSupplierValidations;
        this.csvReader = csvReader;
        this.authService = authService;
        this.miscService = miscService;
    }


    /**
     * Se crea una nueva relación entre un producto y proveedor, con su precio y margen de ganancia
     * @param createProductSupplierDTO Un DTO que se usa crear una relación
     * @return Un DTO de la relación entre el producto y proveedor creada
     */
    @Override
    public ResponseProductSupplierDTO createProductSupplier(CreateProductSupplierDTO createProductSupplierDTO) {

        Supplier supplier = supplierRepository.findById(createProductSupplierDTO.idSupplier())
                .orElseThrow(() -> new SupplierNotFoundException("No existe el proveedor con ese ID"));

        Product product = productRepository.findById(createProductSupplierDTO.idProduct())
                .orElseThrow( ()-> new ProductNotFoundException("No existe producto con ese ID"));

        if(product.getStatus() == ProductStatus.DISABLED)
        {
            throw new ProductNotFoundException("Ese producto está desactivado, activarlo y intente de nuevo");
        }

        ProductSupplier productSupplier = new ProductSupplier(
                supplier,
                product,
                createProductSupplierDTO.cost(),
                createProductSupplierDTO.profitMargin()
        );

        productSupplierValidations.validateRelationship(productSupplier);

        productSupplierRepository.save(productSupplier);

        return mapper.fromEntityToDto(productSupplier);
    }

    @Override
    public void deleteProductSupplier(Long idProductSupplier) {
        if (productSupplierRepository.existsById(idProductSupplier)) {
            productSupplierRepository.deleteById(idProductSupplier);
        } else {
            throw new ProductSupplierNotExistException("Esa relación no existe");
        }
    }

    /**
     * Se actualiza una relación existente, con sus datos
     * @param updateProductSupplierDTO Un objeto con los datos para modificar la relación
     * @param id El ID para la relación a editar
     * @return Un DTO con los datos modificados
     */
    @Override
    public ResponseProductSupplierDTO updateProductSupplier(UpdateProductSupplierDTO updateProductSupplierDTO, Long id) {

        ProductSupplier productSupplier = productSupplierRepository.findById(id)
                .orElseThrow(() -> new ProductSupplierNotExistException("La relación que quiere editar no se encuentra"));

        BigDecimal newCost = updateProductSupplierDTO.cost();
        BigDecimal newProfitMargin = updateProductSupplierDTO.profitMargin();

        productSupplier.setCost(newCost);
        productSupplier.setProfitMargin(newProfitMargin);
        productSupplier.setPrice(newCost.add(newCost.multiply(newProfitMargin).divide(BigDecimal.valueOf(100),2,RoundingMode.CEILING)));

        productSupplierRepository.save(productSupplier);

        return mapper.fromEntityToDto(productSupplier);
    }

    /**
     * Lista las relaciones según el nombre de la empresa, los datos mostrados varían según el nivel de acceso del usuario
     *
     * @param companyId   Id del proveedor para buscar
     * @param exchange_rate Tipo de cotizacion de dolarapi.com
     * @return Una lista de DTO con los datos para mostrar
     */
    @Override
    public SupplierProductListDTO listProductsBySupplier(Pageable pageable, Long companyId, String exchange_rate) {

        Supplier supplier = supplierRepository.findById(companyId)
                .orElseThrow(() -> new SupplierNotFoundException("El proveedor no existe"));

        Page<?> priceList;


        if (!authService.isEmployee()) {
            try {
                BigDecimal dolar = miscService.searchDollarPrice(exchange_rate).venta();

                priceList = productSupplierRepository.productsBySupplierManager(pageable, supplier.getIdSupplier(),dolar);
            } catch (UnexpectedServerErrorException e) {
                priceList = productSupplierRepository.productsBySupplierManagerFallback(pageable, supplier.getIdSupplier());
            }
        } else {
            priceList = productSupplierRepository.productsBySupplierEmployee(pageable, supplier.getIdSupplier());
        }


        return new SupplierProductListDTO(
                supplier.getIdSupplier(),
                supplier.getCompanyName(),
                priceList
        );

    }

    /**
     * Lista las relaciones según el ID de un producto cargado, los datos mostrados varían según el nivel de acceso del usuario
     *
     * @param idProduct     El ID del producto
     * @param exchange_rate Tipo de cotizacion de dolarapi.com
     * @return Una lista de DTO para mostrar
     */
    public ProductPricesDTO listPricesByProduct(Pageable pageable, Long idProduct, String exchange_rate) {
        Product product = productRepository.findById(idProduct).orElseThrow(() -> new ProductNotFoundException("El producto no existe"));

        if (product.getStatus() == ProductStatus.DISABLED) {
            throw new ProductNotFoundException("El producto está desactivado, y no tendrá precios.");
        }

        Page<?> priceList;

      
        if(!authService.isEmployee()){
            try {
                BigDecimal dolar = miscService.searchDollarPrice(exchange_rate).venta();

                priceList = productSupplierRepository.listPricesByProductManager(pageable,idProduct,dolar);
            } catch (UnexpectedServerErrorException e) {
                priceList = productSupplierRepository.listPricesByProductManagerFallback(pageable,idProduct);
            }
        } else {
           priceList = productSupplierRepository.listPricesByProductEmployee(pageable,idProduct);
        }

        return new ProductPricesDTO(product.getIdProduct(),product.getName(),priceList);
    }

    /**
     * Se usa para modificar relaciones existentes
     * @param filepath Path interno del archivo
     * @param idSupplier El ID del proveedor para modificar sus relaciones
     */
    @Override
    public String uploadCsv(String filepath, Long idSupplier) {

        if(!supplierRepository.existsById(idSupplier))
        {
            throw new SupplierNotFoundException("El proveedor asignado no existe");
        }

        return csvReader.uploadToDatabase(filepath,idSupplier);
    }

    /**
     * Se usa para cargar relaciones nuevas y modifican el precio de las existentes
     * @param filepath Path interno del archivo
     * @param idSupplier El ID del proveedor para modificar sus relaciones
     * @param bulkProfitMargin El margen de ganancia
     * @return Un mensaje de error diciendo que productos no fueron cargados
     * @see CsvReader
     */
    @Override
    public String  uploadCsv(String filepath, Long idSupplier, BigDecimal bulkProfitMargin) {

        if(!supplierRepository.existsById(idSupplier))
        {
            throw new SupplierNotFoundException("El proveedor asignado no existe");
        }

        if (0 > bulkProfitMargin.compareTo(BigDecimal.valueOf(0))) {
            throw new InvalidRequestException("El porcentaje de ganancia no es válido");
        }

        return csvReader.uploadToDatabase(filepath,idSupplier,bulkProfitMargin);
    }

    @Override
    public ResponseProductSupplierDTO getProductSupplier(Long id) {
        this.productSupplierValidations.productSupplierIdExist(id);

        return this.mapper.fromEntityToDto(this.productSupplierRepository.getReferenceById(id)) ;
    }
}
