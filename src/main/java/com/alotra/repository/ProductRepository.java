package com.alotra.repository;

import com.alotra.entity.Product;
import com.alotra.entity.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    @Query("SELECT p FROM Product p WHERE 1=1 " +
           "AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "ORDER BY p.id DESC")
    List<Product> adminSearch(@Param("kw") String kw, 
                             @Param("categoryId") Integer categoryId, 
                             @Param("status") ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Integer categoryId);

    // Projections for ProductService
    public interface ProductProjection {
        Integer getId();
        String getName();
        String getImageUrl();
        BigDecimal getPrice();
    }

    @Query(value = "SELECT p.id as id, p.name as name, p.image_url as imageUrl, MIN(v.price) as price " +
           "FROM products p JOIN product_variants v ON p.id = v.product_id " +
           "GROUP BY p.id, p.name, p.image_url", nativeQuery = true)
    List<ProductProjection> findBestSellersNative();

    @Query(value = "SELECT p.id as id, p.name as name, p.image_url as imageUrl, MIN(v.price) as price " +
           "FROM products p JOIN product_variants v ON p.id = v.product_id " +
           "WHERE p.category_id = :categoryId " +
           "GROUP BY p.id, p.name, p.image_url", nativeQuery = true)
    List<ProductProjection> findListByCategoryNative(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT p.id as id, p.name as name, p.image_url as imageUrl, MIN(v.price) as price " +
           "FROM products p JOIN product_variants v ON p.id = v.product_id " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "GROUP BY p.id, p.name, p.image_url", nativeQuery = true)
    List<ProductProjection> searchByKeywordNative(@Param("kw") String kw);

    @Query(value = "SELECT p.id as id, p.name as name, p.image_url as imageUrl, MIN(v.price) as price " +
           "FROM products p JOIN product_variants v ON p.id = v.product_id " +
           "WHERE p.category_id = :categoryId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "GROUP BY p.id, p.name, p.image_url", nativeQuery = true)
    List<ProductProjection> searchByCategoryAndKeywordNative(@Param("categoryId") Integer categoryId, @Param("kw") String kw);
}
