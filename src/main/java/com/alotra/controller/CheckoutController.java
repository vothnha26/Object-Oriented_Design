package com.alotra.controller;

import com.alotra.dto.CartItemDTO;
import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.service.interaction.CartService;
import com.alotra.service.order.PriceService;
import com.alotra.service.order.OrderFactory;
import com.alotra.service.order.CheckoutFacade;
import com.alotra.security.CustomerUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderFactory orderFactory;
    private final PriceService priceService;
    private final CheckoutFacade checkoutFacade;

    private final com.alotra.service.account.AddressService addressService;

    public CheckoutController(CartService cartService, 
                              OrderFactory orderFactory,
                              PriceService priceService,
                              CheckoutFacade checkoutFacade,
                              com.alotra.service.account.AddressService addressService) {
        this.cartService = cartService;
        this.orderFactory = orderFactory;
        this.priceService = priceService;
        this.checkoutFacade = checkoutFacade;
        this.addressService = addressService;
    }

    @GetMapping
    public String checkout(HttpSession session, 
                           @AuthenticationPrincipal CustomerUserDetails principal,
                           @RequestParam(required = false) String promoCode,
                           Model model) {
        
        List<CartItemDTO> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderFactory.createOrder(
                principal != null ? principal.getCustomer() : null, 
                null, 
                cartItems, 
                ""
        );

        priceService.calculateTotal(order, promoCode);

        model.addAttribute("order", order);
        model.addAttribute("items", order.getItems());
        model.addAttribute("total", order.getSubTotal());
        model.addAttribute("promoCode", promoCode);
        model.addAttribute("discountAmount", order.getDiscountAmount());
        model.addAttribute("finalTotal", order.getTotalAmount());
        
        // Dữ liệu mẫu cho Combobox địa chỉ
        model.addAttribute("provinces", List.of("TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Bình Dương", "Đồng Nai"));
        model.addAttribute("districts", Map.of(
            "TP. Hồ Chí Minh", List.of("Quận 1", "Quận 3", "Quận 5", "Quận 7", "Quận 10", "Quận Bình Thạnh", "Quận Tân Bình", "TP. Thủ Đức", "Huyện Hóc Môn", "Huyện Củ Chi"),
            "Hà Nội", List.of("Quận Hoàn Kiếm", "Quận Ba Đình", "Quận Đống Đa", "Quận Hai Bà Trưng", "Quận Cầu Giấy", "Quận Thanh Xuân", "Quận Hà Đông", "Quận Long Biên"),
            "Đà Nẵng", List.of("Quận Hải Châu", "Quận Thanh Khê", "Quận Sơn Trà", "Quận Ngũ Hành Sơn", "Quận Liên Chiểu", "Quận Cẩm Lệ", "Huyện Hòa Vang"),
            "Bình Dương", List.of("TP. Thủ Dầu Một", "TP. Thuận An", "TP. Dĩ An", "TP. Tân Uyên", "TP. Bến Cát", "Huyện Dầu Tiếng", "Huyện Phú Giáo"),
            "Đồng Nai", List.of("TP. Biên Hòa", "TP. Long Khánh", "Huyện Long Thành", "Huyện Nhơn Trạch", "Huyện Trảng Bom", "Huyện Thống Nhất")
        ));

        if (principal != null) {
            model.addAttribute("addresses", addressService.findByCustomer(principal.getId()));
        }

        model.addAttribute("defaultShipName", principal != null ? principal.getCustomer().getFullName() : "");
        model.addAttribute("defaultShipPhone", principal != null ? principal.getCustomer().getPhone() : "");
        
        return "checkout/confirm";
    }

    @PostMapping("/place")
    public String placeOrder(HttpSession session,
                             @AuthenticationPrincipal CustomerUserDetails principal,
                             @RequestParam String paymentMethod,
                             @RequestParam String receivingMethod,
                             @RequestParam(required = false) String shipName,
                             @RequestParam(required = false) String shipPhone,
                             @RequestParam(required = false) String province,
                             @RequestParam(required = false) String district,
                             @RequestParam(required = false) String ward,
                             @RequestParam(required = false) String street,
                             @RequestParam(required = false) String note,
                             @RequestParam(required = false) String promoCode,
                             RedirectAttributes ra) {
        
        List<CartItemDTO> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        String fullAddress = "";
        if ("DELIVERY".equals(receivingMethod)) {
            fullAddress = String.format("%s, %s, %s, %s", street, ward, district, province);
        } else {
            fullAddress = "Nhận tại cửa hàng";
        }

        try {
            // Tạo request DTO để bọc dữ liệu
            com.alotra.dto.CheckoutRequest request = new com.alotra.dto.CheckoutRequest();
            request.setCartItems(cartItems);
            request.setPaymentMethod(paymentMethod);
            request.setShippingAddress(fullAddress);
            request.setNote(note);
            request.setPromotionCode(promoCode);

            // Sử dụng Facade để xử lý logic lưu đơn hàng phức tạp
            Order savedOrder = checkoutFacade.processCheckout(
                    principal != null ? principal.getCustomer() : null,
                    request
            );

            cartService.clearCart(session);
            ra.addFlashAttribute("message", "Đặt hàng thành công!");

            // Nếu thanh toán chuyển khoản, chuyển hướng đến trang transfer có mã QR
            if ("BANK_TRANSFER".equals(paymentMethod)) {
                return "redirect:/payment/" + savedOrder.getId();
            }

            return "redirect:/checkout/success?id=" + savedOrder.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            return "redirect:/checkout?promoCode=" + (promoCode != null ? promoCode : "");
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam Integer id, Model model) {
        model.addAttribute("orderId", id);
        return "checkout/success";
    }
}
