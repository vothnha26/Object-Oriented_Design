package com.alotra.controller.account;

import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.proxy.CartOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartOperations cartService;

    public CartController(CartOperations cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomerUserDetails principal, Model model,
                           @RequestParam(value = "msg", required = false) String msg,
                           @RequestParam(value = "error", required = false) String error) {
        Customer customer = principal.getCustomer();
        List<CartItem> items = cartService.listItems(customer);
        model.addAttribute("pageTitle", "Giỏ hàng");
        model.addAttribute("items", items);
        model.addAttribute("itemToppingsMap", cartService.getToppingsForItems(items));
        model.addAttribute("toppingsCatalog", cartService.listActiveToppings());
        
        Map<Integer, Map<Integer,Integer>> qtyMap = new HashMap<>();
        cartService.getToppingsForItems(items).forEach((itemId, list) -> {
            Map<Integer,Integer> inner = new HashMap<>();
            list.forEach(t -> inner.put(t.getTopping().getId(), t.getQuantity()));
            qtyMap.put(itemId, inner);
        });
        model.addAttribute("itemTopQtyMap", qtyMap);
        
        Map<Integer, List<com.alotra.entity.ProductVariant>> itemVariantsMap = new HashMap<>();
        for (CartItem it : items) {
            var product = it.getVariant() != null ? it.getVariant().getProduct() : null;
            itemVariantsMap.put(it.getId(), cartService.listVariantsForProduct(product));
        }
        model.addAttribute("itemVariantsMap", itemVariantsMap);
        model.addAttribute("total", cartService.calcTotal(items));
        
        if (msg != null) model.addAttribute("message", msg);
        if (error != null) model.addAttribute("error", error);
        return "cart/cart";
    }

    @PostMapping("/update")
    public String updateQty(@AuthenticationPrincipal CustomerUserDetails principal,
                            @RequestParam("itemId") Integer itemId,
                            @RequestParam("qty") Integer qty,
                            RedirectAttributes ra) {
        try {
            cartService.updateQuantity(principal.getCustomer(), itemId, qty);
            ra.addFlashAttribute("message", "Đã cập nhật số lượng.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String remove(@AuthenticationPrincipal CustomerUserDetails principal,
                         @PathVariable Integer id,
                         RedirectAttributes ra) {
        try {
            cartService.removeItem(principal.getCustomer(), id);
            ra.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/item/{id}/toppings")
    public String updateItemToppings(@AuthenticationPrincipal CustomerUserDetails principal,
                                     @PathVariable("id") Integer itemId,
                                     @RequestParam MultiValueMap<String, String> params,
                                     RedirectAttributes ra) {
        try {
            Map<Integer,Integer> map = new HashMap<>();
            for (String key : params.keySet()) {
                if (key.startsWith("toppings[") && key.endsWith("]")) {
                    String idStr = key.substring(9, key.length() - 1);
                    try {
                        Integer tid = Integer.valueOf(idStr);
                        String raw = params.getFirst(key);
                        Integer q;
                        try { q = (raw == null || raw.isBlank()) ? 0 : Integer.valueOf(raw); }
                        catch (NumberFormatException nfe) { q = 0; }
                        map.put(tid, Math.max(0, q));
                    } catch (NumberFormatException ignored) {}
                }
            }
            cartService.updateToppings(principal.getCustomer(), itemId, map);
            ra.addFlashAttribute("message", "Đã cập nhật topping.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/item/{id}/variant")
    public String changeVariant(@AuthenticationPrincipal CustomerUserDetails principal,
                                @PathVariable("id") Integer itemId,
                                @RequestParam("variantId") Integer newVariantId,
                                RedirectAttributes ra) {
        try {
            cartService.changeVariant(principal.getCustomer(), itemId, newVariantId);
            ra.addFlashAttribute("message", "Đã cập nhật kích cỡ sản phẩm.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cart";
    }
}
