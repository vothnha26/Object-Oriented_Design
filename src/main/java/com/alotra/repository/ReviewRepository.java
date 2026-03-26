package com.alotra.repository;

import com.alotra.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByCustomer_IdAndOrderLine_Id(Integer customerId, Integer orderLineId);
    List<Review> findByCustomer_IdAndOrderLine_IdIn(Integer customerId, List<Integer> orderLineIds);

    @Query("SELECT dg FROM Review dg WHERE dg.orderLine.id = :lineId")
    Review findByOrderLineId(@Param("lineId") Integer lineId);

    @Query("SELECT dg FROM Review dg WHERE dg.customer.id = :customerId AND dg.orderLine.id = :lineId")
    Review findByCustomerIdAndLineId(@Param("customerId") Integer customerId, @Param("lineId") Integer lineId);

    interface ProductRatingStats { Double getAvg(); Long getCnt(); }

    @Query(value = "SELECT AVG(CAST(dg.SoSao AS FLOAT)) AS avg, COUNT(*) AS cnt\n" +
            "FROM DanhGia dg\n" +
            "JOIN CTDonHang ct ON ct.MaCT = dg.MaCT\n" +
            "JOIN BienTheSanPham bt ON bt.MaBT = ct.MaBT\n" +
            "WHERE bt.MaSP = :productId", nativeQuery = true)
    ProductRatingStats findStatsByProductId(@Param("productId") Integer productId);

    @Query("SELECT dg FROM Review dg " +
            "JOIN dg.orderLine ol " +
            "JOIN ol.variant v " +
            "JOIN v.product p " +
            "WHERE p.id = :productId " +
            "ORDER BY dg.createdAt DESC")
    List<Review> findByProductIdOrderByCreatedAtDesc(@Param("productId") Integer productId);

    @Query("SELECT DISTINCT dg FROM Review dg " +
            "LEFT JOIN FETCH dg.customer c " +
            "LEFT JOIN FETCH dg.orderLine ol " +
            "LEFT JOIN FETCH ol.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH v.size s " +
            "ORDER BY dg.createdAt DESC")
    List<Review> findAllOrderByCreatedAtDesc();
}
