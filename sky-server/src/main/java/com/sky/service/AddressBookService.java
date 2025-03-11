package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

/**
 * @author raoxin
 */
public interface AddressBookService {
    List<AddressBook> listByUserId();

    AddressBook getDefault();

    void addAddressBook(AddressBook addressBook);

    void deleteAddressBookById(Long id);

    AddressBook getById(Long id);

    void updateAddressBook(AddressBook addressBook);

    void updateAndCleanAllDefault(Long id);
}
