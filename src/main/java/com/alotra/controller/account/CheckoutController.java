package com.alotra.controller.account;

import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.proxy.CartOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final CartOperations cartService;

    public CheckoutController(CartOperations cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/confirm")
    public String confirm(@AuthenticationPrincipal CustomerUserDetails principal,
                          @RequestParam(value = "itemIds", required = false) List<Integer> itemIds,
                          @RequestParam(value = "paymentMethod", defaultValue = "CASH") String paymentMethod,
                          Model model,
                          RedirectAttributes ra) {
        if (itemIds == null || itemIds.isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng chọn ít nhất 1 sản phẩm để đặt hàng");
            return "redirect:/cart";
        }
        Customer customer = principal.getCustomer();
        List<CartItem> all = cartService.listItems(customer);
        List<CartItem> sel = all.stream().filter(it -> itemIds.contains(it.getId())).toList();
        if (sel.isEmpty()) {
            ra.addFlashAttribute("error", "Không có sản phẩm hợp lệ để đặt hàng");
            return "redirect:/cart";
        }
        model.addAttribute("pageTitle", "Xác nhận đặt hàng");
        model.addAttribute("items", sel);
        model.addAttribute("itemToppingsMap", cartService.getToppingsForItems(sel));
        model.addAttribute("total", cartService.calcTotal(sel));
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("itemIds", itemIds);
        model.addAttribute("defaultShipName", customer.getFullName());
        model.addAttribute("defaultShipPhone", customer.getPhone());
        return "checkout/confirm";
    }

    @PostMapping("/place")
    public String place(@AuthenticationPrincipal CustomerUserDetails principal,
                        @RequestParam("itemIds") List<Integer> itemIds,
                        @RequestParam("paymentMethod") String paymentMethod,
                        @RequestParam(value = "receivingMethod", defaultValue = "Ship") String receivingMethod,
                        @RequestParam(value = "note", required = false) String note,
                        @RequestParam(value = "shipName", required = false) String shipName,
                        @RequestParam(value = "shipPhone", required = false) String shipPhone,
                        @RequestParam(value = "shipAddress", required = false) String shipAddress,
                        RedirectAttributes ra) {
        try {
            var order = cartService.checkoutWithOptions(
                    principal.getCustomer(), itemIds, paymentMethod, note, receivingMethod, shipName, shipPhone, shipAddress
            );
            if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
                return "redirect:/payment/" + order.getId();
            }
            ra.addFlashAttribute("msg", "Đặt hàng thành công. Mã đơn: " + order.getId());
            return "redirect:/account/orders";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }
    }
}