package com.crabdp.mapper;

import com.crabdp.entity.Shop;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper {

    @Select("select * from tb_shop where id = #{id}")
    Shop queryById(Long id);


    void saveShop(Shop shop);

    void update(Shop shop);

    Page<Shop> pageQueryByType(Long typeId);


    List<Shop> findByIds(List<Long> ids);

    Page<Shop> queryByName(String name);

    List<Shop> queryByTypeAndLocation(List<Long> ids);

    List<Shop> list();
}
