package com.sky.mapper;
import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    List<AddressBook> selectByUserId(Long userId);

    AddressBook getDefault(Long currentId);

    void insert(AddressBook addressBook);

    void deleteById(Long id);

    AddressBook selectById(Long id);

    void updateAndCleanAllDefault(Long currentId);

    void update(AddressBook addressBook);

    void setDefaultById(Long id);
}
