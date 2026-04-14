package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.Payment;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public void processPayment(Order order, String method) {
        // Khởi tạo đối tượng Payment nếu đơn hàng chưa có
        if (order.getPayment() == null) {
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStatus(PaymentStatus.UNPAID);
            payment.setAmount(order.getTotalAmount());
            order.setPayment(payment);
        }

        if (method != null) {
            try {
                // Ánh xạ phương thức thanh toán từ chuỗi đầu vào
                order.getPayment().setMethod(PaymentMethod.valueOf(method.toUpperCase()));
            } catch (Exception e) {
                // Mặc định là TIỀN MẶT nếu phương thức không hợp lệ
                order.getPayment().setMethod(PaymentMethod.CASH);
            }
        }
        
        // Cập nhật số tiền thanh toán cuối cùng khớp với tổng tiền đơn hàng
        order.getPayment().setAmount(order.getTotalAmount());
        
        System.out.println("Processing payment via: " + order.getPayment().getMethod());
    }
}
