package com.alotra.service.order;

import com.alotra.dto.CartItemDTO;
import java.util.List;

public interface StockService {
    void validateStock(List<CartItemDTO> items);
}
