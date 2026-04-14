package com.alotra.service.pricing;

import com.alotra.entity.Order;

/**
 * Interface cho các trạm xử lý trong đường ống tính giá (Pipeline).
 * Áp dụng Chain of Responsibility Pattern.
 */
public interface OrderPriceProcessor {
    /**
     * Thứ tự thực thi của Processor. Số càng nhỏ chạy trước.
     */
    int getOrder();

    /**
     * Xử lý và bọc thêm Decorator vào chuỗi tính giá.
     * @param chain Chuỗi tính giá hiện tại
     * @param order Đối tượng đơn hàng
     * @param promoCode Mã khuyến mãi (nếu có)
     * @return Chuỗi tính giá sau khi đã được bọc thêm
     */
    PriceComponent process(PriceComponent chain, Order order, String promoCode);
}
