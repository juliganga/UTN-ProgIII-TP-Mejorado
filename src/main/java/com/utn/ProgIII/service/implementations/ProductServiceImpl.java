package com.utn.ProgIII.service.implementations;

import com.querydsl.core.BooleanBuilder;
import com.utn.ProgIII.dto.CreateProductDTO;

import com.utn.ProgIII.dto.ProductDTO;
import com.utn.ProgIII.exceptions.*;
import com.utn.ProgIII.mapper.ProductMapper;
import com.utn.ProgIII.model.Product.Category;
import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.Product.ProductStatus;
import com.utn.ProgIII.model.Product.QProduct;
import com.utn.ProgIII.repository.CategoryRepository;
import com.utn.ProgIII.repository.ProductRepository;
import com.utn.ProgIII.repository.ProductSupplierRepository;
import com.utn.ProgIII.service.interfaces.AuthService;
import com.utn.ProgIII.service.interfaces.PictureService;
import com.utn.ProgIII.service.interfaces.ProductService;
import jakarta.transaction.Transactional;
import com.utn.ProgIII.validations.ProductValidations;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductSupplierRepository productSupplierRepository;
    private final CategoryRepository categoryRepository;
    private final ProductValidations productValidations;
    private final AuthService authService;

    private final PictureService pictureService;


    // def can be changed, but this should do
    @Value("${app.imagesFolder}")
    private String image_product_path;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, ProductSupplierRepository productSupplierRepository, CategoryRepository categoryRepository, ProductValidations productValidations, AuthService authService, PictureService pictureService) {

        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productSupplierRepository = productSupplierRepository;
        this.categoryRepository = categoryRepository;
        this.productValidations = productValidations;
        this.authService = authService;
        this.pictureService = pictureService;
    }


    /**
     * Busca un producto por su ID
     *
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


    @Override
    public List<ProductDTO> getAllActiveProductAsList() {
        List<ProductDTO> list = productRepository.findByStatus(ProductStatus.ENABLED).stream().map(productMapper::toProductDTO).sorted(Comparator.comparing(ProductDTO::name)).toList();

        if(list.isEmpty())
        {
            throw new ProductNotFoundException("No hay productos");
        }

        return list;
    }

    @Override
    public List<ProductDTO> getAllProductsAsList() {
        List<ProductDTO> list = productRepository.findAll().stream().map(productMapper::toProductDTO).sorted(Comparator.comparing(ProductDTO::name)).toList();

        if(list.isEmpty()) {
            throw new ProductNotFoundException("No hay productos");
        }

        return list;
    }

    /**
     * Crea un producto nuevo y lo guarda en la base de datos
     *
     * @param productDto Un DTO de un producto que se creará
     * @param image
     * @return Un <code>ProductDto</code> del producto creado
     * @see CreateProductDTO
     */

    @Override
    public ProductDTO createProductDto(CreateProductDTO productDto, MultipartFile image) {

        Product product = productMapper.toEntity(productDto);
        Category category = categoryRepository.findById(productDto.idCategory()).orElseThrow(() -> new NotFoundException("Esa categoria no existe!"));
        product.setCategory(category);

        productValidations.validateProductNameExists(product);

        if(image != null && !image.isEmpty())
        {
            String file_name = pictureService.uploadPicture(image_product_path,image);
            product.setImage_url(file_name);
        }

        product = productRepository.save(product);

        return productMapper.toProductDTO(product);
    }

    /**
     * Se actualiza un producto según su ID
     *
     * @param id         ID del producto que se modificará
     * @param productDto Los datos para modificar el producto
     * @param image
     * @return Un ProductDTO del producto modificado
     * @see CreateProductDTO
     */
    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, CreateProductDTO productDto, MultipartFile image) {

        if(!EnumUtils.isValidEnum(ProductStatus.class,productDto.status()))
        {
            throw new InvalidRequestException("El estado de producto ingresado no es válido");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
        Category category = categoryRepository.findById(productDto.idCategory()).orElseThrow(() -> new NotFoundException("Esa categoria no existe!"));

        product.setName(productDto.name());
        product.setCategory(category);
        product.setStatus(ProductStatus.valueOf(productDto.status().toUpperCase()));

        if(product.getStatus() == ProductStatus.DISABLED)
        {
            productSupplierRepository.removeAllByProduct_IdProduct(id);
        }

        product.setProfitMargin(productDto.profitMargin());
        product.setStock(productDto.stock());

        if(image != null && !image.isEmpty())
        {
            pictureService.deleteFile(image_product_path,product.getImage_url());
            String new_picture = pictureService.uploadPicture(image_product_path,image);
            product.setImage_url(new_picture);
        }

        product = productRepository.save(product);
        return productMapper.toProductDTO(product);
    }

    /**
     *
     * @param pageable   Objecto de paginación
     * @param name       Nombre del producto buscado
     * @param status     Estado del producto buscado
     * @param categories Listado de categorias de productos (puede ser buscado individualmente)
     * @param id         Id del producto
     * @param rem_stock Stock restante (se usara para buscar productos menor o igual a esto)
     * @return Pagina de DTOs de productos
     */
    @Override
    public Page<ProductDTO> getProductsPage(Pageable pageable, String name, String status, List<Long> categories, Long id, Integer rem_stock) {
        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder().or(product.isNotNull());

        if(name != null && !name.isBlank())
        {
            builder.and(product.name.likeIgnoreCase('%' + name + '%'));
        }

        if(id != null)
        {
            builder.and(product.idProduct.eq(id));
        }

        if(status != null && !EnumUtils.isValidEnum(ProductStatus.class, status.toUpperCase())) {
            throw new InvalidRequestException("Ese estado no está presente");
        }

        if(rem_stock != null)
        {
            builder.and(product.stock.loe(rem_stock));
        }

        if (status != null && !authService.isEmployee())
        {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());

            builder.and(product.status.eq(productStatus));
        }

        if (authService.isEmployee())
        {
            builder.and(product.status.eq(ProductStatus.ENABLED));
        }

        if (categories != null && !categories.isEmpty())
        {
            builder.and(product.category.idCategory.in(categories));
        }

        return productRepository.findAll(builder,pageable).map(productMapper::toProductDTO);
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
        product.setPrice(null);
        productSupplierRepository.removeAllByProduct_IdProduct(id);

        productRepository.save(product);

    }
}
