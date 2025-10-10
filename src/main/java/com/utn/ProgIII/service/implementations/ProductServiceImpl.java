package com.utn.ProgIII.service.implementations;

import com.utn.ProgIII.dto.ProductDTO;

import com.utn.ProgIII.dto.UserWithCredentialDTO;
import com.utn.ProgIII.exceptions.InvalidProductStatusException;
import com.utn.ProgIII.exceptions.InvalidRequestException;
import com.utn.ProgIII.exceptions.ProductNotFoundException;
import com.utn.ProgIII.exceptions.UserNotFoundException;
import com.utn.ProgIII.mapper.ProductMapper;
import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.Product.ProductStatus;
import com.utn.ProgIII.repository.ProductRepository;
import com.utn.ProgIII.repository.ProductSupplierRepository;
import com.utn.ProgIII.service.interfaces.AuthService;
import com.utn.ProgIII.service.interfaces.ProductService;
import jakarta.transaction.Transactional;
import com.utn.ProgIII.validations.ProductValidations;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSupplierRepository productSupplierRepository;
    private final ProductValidations productValidations;
    private final AuthService authService;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, ProductSupplierRepository productSupplierRepository, ProductValidations productValidations, AuthService authService) {

        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productSupplierRepository = productSupplierRepository;
        this.productValidations = productValidations;
        this.authService = authService;
    }


    /**
     * Busca un producto por su ID
     * @param id ID del producto
     * @return <code>ProductDTO</code>
     */
    @Override
    public ProductDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("Producto no encontrado"));

        if(authService.isEmployee() && product.getStatus() == ProductStatus.DISABLED)
        {

            throw new ProductNotFoundException("Producto no encontrado");

        }

        return productMapper.toProductDTO(product);
    }

    /**
     * Una página que contiene los datos de productos.
     * <p>Se puede definir el tamaño con ?size=?</p>
     * <p>Se puede definir el número de página con ?page=?</p>
     * <p>Se puede ordenar según parámetro de objeto con ?sort=?</p>
     * @param paginacion Una página con contenido e información
     * @return Una página con contenido e información
     */
    @Override
    public Page<ProductDTO> getAllProduct(Pageable paginacion) {

        Page<ProductDTO> page = null;

        if(authService.isEmployee())
        {
            page = productRepository.findByStatus(ProductStatus.ENABLED,paginacion).map(productMapper::toProductDTO);
        } else {
            page = productRepository.findAll(paginacion).map(productMapper::toProductDTO);
        }

        if(page.getNumberOfElements() == 0)
        {
            throw new ProductNotFoundException("No hay productos");
        }

        return page;
    }

    /**
     * Busca productos según estado
     * @param status El estado del producto
     * @return Lista <code>ProductDto</code>
     * @see ProductStatus
     * @see ProductDTO
     *
     */
    @Override
    public Page<ProductDTO> getAllProductByStatus(String status,Pageable paginacion) {

        try {
            ProductStatus value = ProductStatus.valueOf(status.toUpperCase());

            Page<ProductDTO> products = productRepository.findByStatus(value,paginacion).map(productMapper::toProductDTO);

            if(products.isEmpty())
            {
                throw new ProductNotFoundException("No hay resultados");
            }

            return products;
        } catch (IllegalArgumentException e){
            throw new InvalidProductStatusException("El estado ingresado es inválido");
        }
    }

    /**
     * Busca un producto según nombre
     * @param name El nombre del producto, se usa un LIKE de sql
     * @return Retorna una lista de <code>ProductDto</code>
     * @see ProductDTO
     */

    @Override
    public Page<ProductDTO> getProductByName(String name,Pageable pageable) {

        List<Product> products = productRepository.findByNameContaining(name,pageable);


        if(authService.isEmployee())
        {
            products = products.stream().filter((x) -> x.getStatus() == ProductStatus.ENABLED).toList();
        }

        if(products.isEmpty())
        {
            throw new ProductNotFoundException("No hay resultados");
        }



        return new PageImpl<ProductDTO>(products.stream().map(productMapper::toProductDTO).toList());
    }


    /**
     * Crea un producto nuevo y lo guarda en la base de datos
     * @param productDto Un DTO de un producto que se creará
     * @return Un <code>ProductDto</code> del producto creado
     * @see ProductDTO
     */

    @Override
    public ProductDTO createProductDto(ProductDTO productDto) {

        Product product = productMapper.toEntity(productDto);
        productValidations.validateProductNameExists(product);
        product = productRepository.save(product);

        return productMapper.toProductDTO(product);
    }

    /**
     * Se actualiza un producto según su ID
     * @param id ID del producto que se modificará
     * @param productDto Los datos para modificar el producto
     * @return Un ProductDTO del producto modificado
     * @see ProductDTO
     */
    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDto) {

        if(!EnumUtils.isValidEnum(ProductStatus.class,productDto.status()))
        {
            throw new InvalidRequestException("El estado de producto ingresado no es válido");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

        product.setName(productDto.name());
        product.setStatus(ProductStatus.valueOf(productDto.status().toUpperCase()));

        if(product.getStatus() == ProductStatus.DISABLED)
        {
            productSupplierRepository.removeAllByProduct_IdProduct(id);
        }

        product = productRepository.save(product);
        return productMapper.toProductDTO(product);
    }

    /**
     * Se da de baja (lógica) un producto según su ID, también se eliminan las relaciones de los proveedores
     * @param id identificador único del producto
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

        product.setStatus(ProductStatus.DISABLED);
        productSupplierRepository.removeAllByProduct_IdProduct(id);

        productRepository.save(product);

    }
}
