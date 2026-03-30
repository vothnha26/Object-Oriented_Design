package com.alotra.service.command;

import com.alotra.entity.Product;
import com.alotra.entity.enums.ProductStatus;
import com.alotra.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Command Pattern Unit Tests")
class AdminCommandTest {

    @Mock private ProductRepository productRepository;
    @Mock private JdbcTemplate jdbc;
    
    private AdminCommandInvoker invoker;

    @BeforeEach
    void setUp() {
        invoker = new AdminCommandInvoker();
    }

    @Test
    @DisplayName("Invoker should execute and undo commands")
    void testInvokerExecutionAndUndo() {
        AdminCommand mockCommand = mock(AdminCommand.class);
        
        invoker.execute(mockCommand);
        verify(mockCommand).execute();
        
        boolean undoResult = invoker.undo();
        assertTrue(undoResult);
        verify(mockCommand).undo();
        
        // Second undo should fail (history empty)
        assertFalse(invoker.undo());
    }

    @Test
    @DisplayName("SoftDeleteProductCommand should update status to INACTIVE and support undo")
    void testSoftDeleteProductCommand() {
        Product product = new Product();
        product.setId(1);
        product.setStatus(ProductStatus.ACTIVE);
        product.setDeletedAt(null);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        AdminCommand cmd = new SoftDeleteProductCommand(productRepository, 1);
        
        // Execute
        cmd.execute();
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        assertNotNull(product.getDeletedAt());
        verify(productRepository).save(product);

        // Undo
        cmd.undo();
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertNull(product.getDeletedAt());
        verify(productRepository, times(2)).save(product);
    }

    @Test
    @DisplayName("UpdateOrderStatusCommand should update DB and support undo")
    void testUpdateOrderStatusCommand() {
        when(jdbc.queryForObject(anyString(), eq(String.class), eq(123)))
                .thenReturn("PENDING");

        AdminCommand cmd = new UpdateOrderStatusCommand(jdbc, 123, "DELIVERED");
        
        // Execute
        cmd.execute();
        verify(jdbc).update(contains("UPDATE Orders SET TrangThaiDonHang = ?"), eq("DELIVERED"), eq(123));

        // Undo
        cmd.undo();
        verify(jdbc).update(contains("UPDATE Orders SET TrangThaiDonHang = ?"), eq("PENDING"), eq(123));
    }
}
