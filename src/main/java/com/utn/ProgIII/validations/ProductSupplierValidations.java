package com.utn.ProgIII.validations;

import com.utn.ProgIII.exceptions.DuplicateRelationshipException;
import com.utn.ProgIII.exceptions.NonExistentRelationshipException;
import com.utn.ProgIII.model.ProductSupplier.ProductSupplier;
import com.utn.ProgIII.repository.ProductSupplierRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductSupplierValidations {

    private final ProductSupplierRepository productSupplierRepository;

    public ProductSupplierValidations(ProductSupplierRepository productSupplierRepository){
        this.productSupplierRepository = productSupplierRepository;
    }

    /**
     * Valida si la relación entre un producto y un proveedor existe
     * @param productSupplier Objeto de relación
     */
    public void validateRelationship(ProductSupplier productSupplier){
        if (productSupplierRepository.existsByProductAndSupplier(productSupplier.getProduct(),productSupplier.getSupplier())){
            throw new DuplicateRelationshipException("La relación del proveedor con el producto ya se encuentra registrada");
        }
    }

    public void productSupplierIdExist(Long id){
        if(!productSupplierRepository.existsById(id)){
            throw new NonExistentRelationshipException("La relacion que estas buscando es inexistente");
        }
    }


}
