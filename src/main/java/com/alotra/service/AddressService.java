package com.alotra.service;

import com.alotra.entity.Address;
import com.alotra.entity.Customer;
import com.alotra.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> findByCustomer(Integer customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    public Address save(Address address, Customer customer) {
        address.setCustomer(customer);
        if (address.isDefault()) {
            List<Address> defaults = addressRepository.findByCustomerIdAndIsDefaultTrue(customer.getId());
            defaults.forEach(a -> {
                a.setDefault(false);
                addressRepository.save(a);
            });
        }
        return addressRepository.save(address);
    }

    public void delete(Integer id) {
        addressRepository.deleteById(id);
    }

    public Address findById(Integer id) {
        return addressRepository.findById(id).orElse(null);
    }
}
