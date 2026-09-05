package com.utn.ProgIII.service.implementations;

import com.querydsl.core.BooleanBuilder;
import com.utn.ProgIII.dto.AddSupplierDTO;
import com.utn.ProgIII.dto.ProductDTO;
import com.utn.ProgIII.exceptions.SupplierNotFoundException;
import com.utn.ProgIII.mapper.SupplierMapper;
import com.utn.ProgIII.model.Address.Address;
import com.utn.ProgIII.model.ProductSupplier.ProductSupplier;
import com.utn.ProgIII.model.Supplier.QSupplier;
import com.utn.ProgIII.model.Supplier.Supplier;
import com.utn.ProgIII.dto.ViewSupplierDTO;
import com.utn.ProgIII.repository.SupplierRepository;
import com.utn.ProgIII.service.interfaces.ProductSupplierService;
import com.utn.ProgIII.service.interfaces.SupplierService;
import com.utn.ProgIII.validations.SupplierValidations;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.swing.text.View;
import java.util.Comparator;
import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierValidations supplierValidations;
    private final SupplierMapper suppliermapper;
    private final ProductSupplierService productSupplierService;

    public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierValidations supplierValidations, SupplierMapper suppliermapper, ProductSupplierService productSupplierService) {
        this.supplierRepository = supplierRepository;
        this.supplierValidations = supplierValidations;
        this.suppliermapper = suppliermapper;
        this.productSupplierService = productSupplierService;
    }

    /**
     * El proveedor es agregado una vez que se transforma a un DTO
     * @param supplierDTO El objeto de transferencia recibido desde el frontend
     * @return Un objeto DTO para mostrar los datos como una respuesta frontend
     */
    @Override
    public ViewSupplierDTO createSupplier(AddSupplierDTO supplierDTO) {
        Supplier supplier = suppliermapper.toObjectFromAddSupplierDTO(supplierDTO);

        supplierValidations.validateCompanyNameNotExists(supplierDTO.companyName());
        supplierValidations.validateSupplierByCuit(supplier.getCuit());

        supplier = supplierRepository.save(supplier);
        return suppliermapper.toViewSupplierDTO(supplier);
    }

    /**
     * Devuelve un DTO de un proveedor que sea pedido.
     * @param id El ID del proveedor.
     * @return {@link ViewSupplierDTO} un objeto para ver el resultado.
     */
    @Override
    public ViewSupplierDTO viewOneSupplier(long id) {
        Supplier sup = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado"));

        return suppliermapper.toViewSupplierDTO(sup);
    }

    @Override
    public List<ViewSupplierDTO> listAllSuppliers() {
        List<ViewSupplierDTO> list = supplierRepository.findAll().stream().map(suppliermapper::toViewSupplierDTO).sorted(Comparator.comparing(ViewSupplierDTO::companyName)).toList();

        if(list.isEmpty())
        {
            throw new SupplierNotFoundException("No hay proveedores");
        }

        return list;
    }


    /**
     * Elimina un proveedor en caso de que exista
     * @param id El ID del proveedor
     */
    @Override
    public void deleteSupplier(long id) {
        if(supplierRepository.existsById(id))
        {
            List<ProductSupplier> rels = productSupplierService.returnAllRelationshipsOfSupplier(id);
            supplierRepository.deleteById(id);
            productSupplierService.handlePostSupplierDelete(rels);
        } else {
            throw new SupplierNotFoundException("Ese proveedor no existe");
        }
    }

    /**
     * Modifica un proveedor según los datos enviados
     * @param supplierDTO Los datos para modificar
     * @return Los datos modificados del proveedor
     */
    @Override
    @Transactional
    public ViewSupplierDTO updateSupplier(AddSupplierDTO supplierDTO, Long id) {
        Supplier supplier_to_update = supplierRepository.findById(id).orElseThrow(() -> new SupplierNotFoundException("El proveedor no existe"));

        supplierValidations.validateModifiedCompanyNameNotExists(supplier_to_update.getCompanyName(),
                supplierDTO.companyName());
        supplierValidations.validateModifiedSupplierByCuit(supplier_to_update.getCuit(),supplierDTO.cuit());

        supplier_to_update.setCompanyName(supplierDTO.companyName());
        supplier_to_update.setCuit(supplierDTO.cuit());
        supplier_to_update.setPhoneNumber(supplierDTO.phoneNumber());
        supplier_to_update.setEmail(supplierDTO.email());

        Address address = supplier_to_update.getAddress();

        address.setStreet(supplierDTO.address().street());
        address.setNumber(supplierDTO.address().number());
        address.setCity(supplierDTO.address().city());

        supplier_to_update.setAddress(address);

        return suppliermapper.toViewSupplierDTO(supplierRepository.save(supplier_to_update));
    }

    @Override
    public Page<ViewSupplierDTO> listSuppliersPage(Pageable pageable, String name) {

        QSupplier supplier = QSupplier.supplier;
        BooleanBuilder builder = new BooleanBuilder().or(supplier.isNotNull());

        if(name != null && !name.isBlank())
        {
            builder.and(supplier.companyName.likeIgnoreCase('%' + name + '%'));
        }

        return supplierRepository.findAll(builder, pageable).map(suppliermapper::toViewSupplierDTO);
    }
}
