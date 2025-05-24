package com.utn.ProgIII.service.implementations;

import com.utn.ProgIII.exceptions.ProductNotFoundException;
import com.utn.ProgIII.exceptions.ProductSupplierNotExistException;
import com.utn.ProgIII.exceptions.SupplierNotFoundException;
import com.utn.ProgIII.mapper.ProductSupplierMapper;
import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.ProductSupplier.*;
import com.utn.ProgIII.model.ProductSupplier.dto.*;
import com.utn.ProgIII.model.Supplier.Supplier;
import com.utn.ProgIII.repository.ProductRepository;
import com.utn.ProgIII.repository.ProductSupplierRepository;
import com.utn.ProgIII.repository.SupplierRepository;
import com.utn.ProgIII.service.interfaces.ProductSupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSupplierServiceImpl implements ProductSupplierService {

    private final ProductSupplierRepository productSupplierRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSupplierMapper mapper;

    public ProductSupplierServiceImpl(ProductSupplierRepository productSupplierRepository,
                                      ProductRepository productRepository,
                                      SupplierRepository supplierRepository,
                                      ProductSupplierMapper mapper){
        this.productSupplierRepository = productSupplierRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }


    @Override
    public ResponseProductSupplierDTO createProductSupplier(CreateProductSupplierDTO createProductSupplierDTO) {

        Supplier supplier = supplierRepository.findById(createProductSupplierDTO.idSupplier())
                .orElseThrow(() -> new SupplierNotFoundException("No existe el proveedor con ese ID"));

        Product product = productRepository.findById(createProductSupplierDTO.idProduct())
                .orElseThrow( ()-> new ProductNotFoundException("No existe producto con ese ID"));

        ProductSupplier productSupplier = new ProductSupplier(
                supplier,
                product,
                createProductSupplierDTO.cost(),
                createProductSupplierDTO.profitMargin()
        );

        productSupplierRepository.save(productSupplier);

        return mapper.fromEntityToDto(productSupplier);
    }

    @Override
    public ResponseProductSupplierDTO updateProductSupplier(UpdateProductSupplierDTO updateProductSupplierDTO, Long id) {

        ProductSupplier productSupplier = productSupplierRepository.findById(id)
                .orElseThrow(() -> new ProductSupplierNotExistException("El relación que quiere editar no se encuentra"));

        productSupplier.setCost(updateProductSupplierDTO.cost());
        productSupplier.setProfitMargin(updateProductSupplierDTO.profitMargin());
        productSupplier.calculatePrice();

        return mapper.fromEntityToDto(productSupplier);
    }

    @Override
    public SupplierProductListDTO listProductsBySupplier(String companyName) {

        Supplier supplier = supplierRepository.findByCompanyName(companyName)
                .orElseThrow(() -> new SupplierNotFoundException("El proveedor no existe"));

        List<ExtendedProductDTO> extendedProductDTOList = productSupplierRepository.productsBySupplier(supplier.getIdSupplier());

        return new SupplierProductListDTO(
                supplier.getIdSupplier(),
                supplier.getCompanyName(),
                extendedProductDTOList
        );

    }
}
