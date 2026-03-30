package com.alotra.controller.account;

import com.alotra.command.CartHistoryManager;
import com.alotra.command.UpdateToppingsCommand;
import com.alotra.entity.CartItem;
import com.alotra.entity.Customer;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.CartService;
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
    private final CartService cartService;
    private final CartHistoryManager historyManager;

    public CartController(CartService cartService, CartHistoryManager historyManager) {
        this.cartService = cartService;
        this.historyManager = historyManager;
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
        
        // Data for Undo/Redo UI
        model.addAttribute("canUndo", historyManager.canUndo());
        model.addAttribute("canRedo", historyManager.canRedo());
        model.addAttribute("lastAction", historyManager.getLastActionDescription());
        
        Map<Integer, Map<Integer,Integer>> qtyMap = new HashMap<>();
        for (CartItem it : items) {
            Map<Integer,Integer> inner = new HashMap<>();
            int itemQty = it.getQuantity() > 0 ? it.getQuantity() : 1;
            cartService.getToppingsForItems(List.of(it)).get(it.getId()).forEach(st -> {
                inner.put(st.getTopping().getId(), st.getQuantity() / itemQty);
            });
            qtyMap.put(it.getId(), inner);
        }
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

    @GetMapping("/undo")
    public String undo(RedirectAttributes ra) {
        if (historyManager.canUndo()) {
            historyManager.undo();
            ra.addFlashAttribute("message", "Đã hoàn tác thao tác vừa rồi.");
        }
        return "redirect:/cart";
    }

    @GetMapping("/redo")
    public String redo(RedirectAttributes ra) {
        if (historyManager.canRedo()) {
            historyManager.redo();
            ra.addFlashAttribute("message", "Đã thực hiện lại thao tác.");
        }
        return "redirect:/cart";
    }

    @GetMapping("/clear-history")
    public String clearHistory(RedirectAttributes ra) {
        historyManager.clearHistory();
        return "redirect:/cart";
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
                                     @RequestParam MultiValueMap<String, String> allParams,
                                     RedirectAttributes ra) {
        try {
            System.out.println("[CartController] Updating toppings for item " + itemId);
            Map<Integer,Integer> toppingQtyMap = new HashMap<>();
            allParams.forEach((key, values) -> {
                if (key.startsWith("toppings[") && key.endsWith("]")) {
                    try {
                        String idPart = key.substring(9, key.length() - 1);
                        Integer tid = Integer.parseInt(idPart);
                        String value = (values != null && !values.isEmpty()) ? values.get(0) : "0";
                        Integer q = (value == null || value.isBlank()) ? 0 : Math.max(0, Integer.parseInt(value));
                        toppingQtyMap.put(tid, q);
                        System.out.println("  - Topping " + tid + ": " + q);
                    } catch (NumberFormatException ignored) {}
                }
            });
            
            // COMMAND PATTERN for Undo support
            UpdateToppingsCommand cmd = new UpdateToppingsCommand(
                cartService, principal.getCustomer(), itemId, toppingQtyMap, "Cập nhật topping"
            );
            historyManager.executeCommand(cmd);
            
            ra.addFlashAttribute("message", "Đã cập nhật topping (Hỗ trợ Undo).");
        } catch (RuntimeException ex) {
            System.err.println("[CartController] Error: " + ex.getMessage());
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
