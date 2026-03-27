package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewOperationsProxyTest {

    @Mock
    private ReviewOperations real;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewOperationsProxy proxy;

    @Test
    void updateIfAllowed_authorized_shouldDelegate() {
        Customer actor = customer(7);
        Review review = review(100, 7);
        when(reviewRepository.findById(100)).thenReturn(Optional.of(review));

        proxy.updateIfAllowed(actor, 100, 5, "ok");

        verify(real).updateIfAllowed(actor, 100, 5, "ok");
    }

    @Test
    void updateIfAllowed_unauthorized_shouldThrowAndNotDelegate() {
        Customer actor = customer(7);
        Review review = review(100, 8);
        when(reviewRepository.findById(100)).thenReturn(Optional.of(review));

        assertThrows(SecurityException.class, () -> proxy.updateIfAllowed(actor, 100, 5, "ok"));
        verify(real, never()).updateIfAllowed(actor, 100, 5, "ok");
    }

    @Test
    void deleteIfAllowed_authorized_shouldDelegate() {
        Customer actor = customer(11);
        Review review = review(200, 11);
        when(reviewRepository.findById(200)).thenReturn(Optional.of(review));

        proxy.deleteIfAllowed(actor, 200);

        verify(real).deleteIfAllowed(actor, 200);
    }

    @Test
    void deleteIfAllowed_unauthorized_shouldThrowAndNotDelegate() {
        Customer actor = customer(11);
        Review review = review(200, 12);
        when(reviewRepository.findById(200)).thenReturn(Optional.of(review));

        assertThrows(SecurityException.class, () -> proxy.deleteIfAllowed(actor, 200));
        verify(real, never()).deleteIfAllowed(actor, 200);
    }

    private Customer customer(int id) {
        Customer c = new Customer();
        c.setId(id);
        return c;
    }

    private Review review(int id, int ownerId) {
        Review r = new Review();
        r.setId(id);
        r.setCustomer(customer(ownerId));
        return r;
    }
}