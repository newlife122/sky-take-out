package com.sky.service.impl;
import com.sky.constant.IsDefaultConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author raoxin
 */
@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 根据用户查询地址
     * @return
     */
    @Override
    public List<AddressBook> listByUserId() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookMapper.selectByUserId(userId);
        return list;
    }

    /**
     * 查询默认地址
     * @return
     */
    @Override
    public AddressBook getDefault() {
        //我有个问题，这里我前端设置的是只能有一个defalut，那我查询还要按list查询吗
        AddressBook addressBook = addressBookMapper.getDefault(BaseContext.getCurrentId());
        return addressBook;
    }

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void addAddressBook(AddressBook addressBook) {
        //1.用户id  手机号  detail 还有性别 都不可以为空
        Long currentId = BaseContext.getCurrentId();
        if (addressBook == null || addressBook.getPhone()==null || addressBook.getSex()==null || addressBook.getDetail()==null){
            throw new RuntimeException("信息不完整");
        }

        //2.如果该用户没有地址，那么，这个就是默认地址
        List<AddressBook> addressBookList = addressBookMapper.selectByUserId(currentId);
        if (addressBookList.size() == 0){
            addressBook.setIsDefault(IsDefaultConstant.IS_DEFAULT);
        }else {
            addressBook.setIsDefault(IsDefaultConstant.NOT_DEFAULT);
        }
        addressBook.setUserId(currentId);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public void deleteAddressBookById(Long id) {
        addressBookMapper.deleteById(id);
    }

    /**
     * 根据id查询地址,这里并没有检查这个地址是否是属于自己，我认为这样是不可以的，因为前端已经做了检查，但是这里还是做一下检查
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.selectById(id);
        Long currentId = BaseContext.getCurrentId();
        if (addressBook != null && !addressBook.getUserId().equals(currentId)) {
            throw new RuntimeException("该地址不属于你");
        }
        return addressBook;
    }

    /**
     * 修改地址信息
     * @param addressBook
     */
    @Override
    public void updateAddressBook(AddressBook addressBook) {
        Long currentId = BaseContext.getCurrentId();
        addressBook.setUserId(currentId);

        if(addressBook == null || addressBook.getPhone()==null || addressBook.getSex()==null || addressBook.getDetail()==null){
            throw new RuntimeException("信息不完整");
        }

        //这里修改地址必定为null，因为不涉及这个字段，我们直接限制这个字段的修改，防止请求恶意篡改
        addressBook.setIsDefault(null);

        //2.更改地址
        addressBookMapper.update(addressBook);
    }

    @Transactional
    @Override
    public void updateAndCleanAllDefault(Long id) {
        Long currentId = BaseContext.getCurrentId();
        addressBookMapper.updateAndCleanAllDefault(currentId);
        addressBookMapper.setDefaultById(id);
    }
}
