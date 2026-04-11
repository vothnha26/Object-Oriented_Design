package com.alotra.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alotra.entity.Category;
import com.alotra.entity.Product;
import com.alotra.entity.enums.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    interface BestSellerProjection {
        Integer getId();
        String getName();
        BigDecimal getPrice();
        String getImageUrl();
        Long getSoldQty();
    }

    @Query(value = "SELECT sp.id AS id, sp.name AS name, sp.image_url AS imageUrl, " +
            " MIN(b.price) AS price, COALESCE(SUM(CASE WHEN p.status='PAID' THEN ct.quantity ELSE 0 END),0) AS soldQty " +
            "FROM products sp " +
            "LEFT JOIN product_variants b ON b.product_id = sp.id " +
            "LEFT JOIN order_items ct ON ct.variant_id = b.id " +
            "LEFT JOIN orders dh ON dh.id = ct.order_id " +
            "LEFT JOIN payments p ON p.order_id = dh.id " +
            "WHERE sp.status = 'ACTIVE' " +
            "GROUP BY sp.id, sp.name, sp.image_url " +
            "ORDER BY soldQty DESC, sp.id DESC LIMIT 20", nativeQuery = true)
    List<BestSellerProjection> findBestSellersNative();

    @Query(value = "SELECT sp.id AS id, sp.name AS name, sp.image_url AS imageUrl, MIN(b.price) AS price " +
            "FROM products sp LEFT JOIN product_variants b ON b.product_id = sp.id " +
            "WHERE sp.status = 'ACTIVE' " +
            "AND (:categoryId IS NULL OR sp.category_id = :categoryId) " +
            "GROUP BY sp.id, sp.name, sp.image_url " +
            "ORDER BY sp.id DESC", nativeQuery = true)
    List<BestSellerProjection> findListByCategoryNative(@Param("categoryId") Integer categoryId);

    long countByCategory(Category category);

    List<Product> findByCategory_Id(Integer categoryId);

    @Query(value = "SELECT sp.id AS id, sp.name AS name, sp.image_url AS imageUrl, MIN(b.price) AS price " +
            "FROM products sp " +
            "LEFT JOIN product_variants b ON b.product_id = sp.id " +
            "LEFT JOIN categories dm ON dm.id = sp.category_id " +
            "WHERE sp.status = 'ACTIVE' " +
            "AND (LOWER(sp.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(dm.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "GROUP BY sp.id, sp.name, sp.image_url " +
            "ORDER BY sp.id DESC LIMIT 20", nativeQuery = true)
    List<BestSellerProjection> searchByKeywordNative(@Param("kw") String keyword);

    @Query(value = "SELECT sp.id AS id, sp.name AS name, sp.image_url AS imageUrl, MIN(b.price) AS price " +
            "FROM products sp " +
            "LEFT JOIN product_variants b ON b.product_id = sp.id " +
            "LEFT JOIN categories dm ON dm.id = sp.category_id " +
            "WHERE sp.status = 'ACTIVE' " +
            "AND (:categoryId IS NULL OR sp.category_id = :categoryId) " +
            "AND (LOWER(sp.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(dm.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "GROUP BY sp.id, sp.name, sp.image_url " +
            "ORDER BY sp.id DESC LIMIT 20", nativeQuery = true)
    List<BestSellerProjection> searchByCategoryAndKeywordNative(@Param("categoryId") Integer categoryId, @Param("kw") String keyword);

    Product findByNameIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE 1=1 " +
            "AND (:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.id DESC")
    List<Product> adminSearch(@Param("kw") String kw,
                              @Param("categoryId") Integer categoryId,
                              @Param("status") ProductStatus status);
}
