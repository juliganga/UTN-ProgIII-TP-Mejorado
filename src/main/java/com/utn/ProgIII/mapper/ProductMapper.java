package com.utn.ProgIII.mapper;

import com.utn.ProgIII.dto.*;
import com.utn.ProgIII.exceptions.InvalidRequestException;
import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.Product.ProductStatus;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
/*
 * Una clase que se dedica a convertir un DTO a un objeto y viceversa
 */
public class ProductMapper {

    @Autowired
    private CategoryMapper categoryMapper;

    @Value("${app.serverAddress}")
    private String address;
    /**
     * Se encarga de convertir un objeto a un dto
     * @param product El objeto
     * @return Un dto nuevo
     */
    public ProductDTO toProductDTO (Product product) {
        Long idProduct = product.getIdProduct();
        String name = product.getName();
        String status = product.getStatus().toString();
        Double profitMargin = product.getProfitMargin();
        Integer stock = product.getStock();
        CategoryDTO category = categoryMapper.toDTO(product.getCategory());
        Double price = calculateClientPrice(product.getPrice(),profitMargin);
        String image_url = createImageURL(product.getImage_url());

        return new ProductDTO(idProduct,name,status, profitMargin, stock,price,category,image_url);
    }

    public ProductDTOOrder ProductDTOOrder(Product product)
    {
        return new ProductDTOOrder(product.getIdProduct(), product.getName());
    }

    /**
     * Se encarga de convertir un objeto a un DTO
     * @param productDTO Un dto
     * @return Un objeto nuevo
     */
    public Product toEntity (CreateProductDTO productDTO){
        Product result = new Product();

        result.setName(productDTO.name());

        if(productDTO.status() != null && !EnumUtils.isValidEnum(ProductStatus.class, productDTO.status().toUpperCase()))
        {
            throw new InvalidRequestException("El estado no es válido");
        }

        result.setStatus(productDTO.status() == null ? ProductStatus.ENABLED : ProductStatus.valueOf(productDTO.status().toUpperCase()));

        result.setProfitMargin(productDTO.profitMargin());
        result.setStock(productDTO.stock());

        return result;
    }


    public ViewProductCustomer toViewCustomerDTO(Product product)
    {
        Long idProduct = product.getIdProduct();
        String name = product.getName();
        Double profitMargin = product.getProfitMargin();
        Integer stock = product.getStock();
        CategoryDTO category = categoryMapper.toDTO(product.getCategory());
        Double price = calculateClientPrice(product.getPrice(),profitMargin);
        String image_url = createImageURL(product.getImage_url());

        return new ViewProductCustomer(idProduct,name,stock,price,category,image_url);
    }

    private Double calculateClientPrice(Double cost, Double profit_margin)
    {
        if(cost == null) return null;
        double clientPrice = cost * (1 + profit_margin/100);

        return BigDecimal.valueOf(clientPrice)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String createImageURL(String url)
    {
        if(url == null) return null;
        return address + "misc/image/" + url;
    }
}
