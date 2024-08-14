package com.crabdp.mapper;

import com.crabdp.entity.ShopType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface ShopTypeMapper {


    @Select("select * from tb_shop_type order by sort asc")
    List<ShopType> queryTypeList();
}
