package com.medicine.medicine_manager_sysytem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medicine.medicine_manager_sysytem.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM t_user WHERE email = #{email}")
    User selectByEmail(String email);

    @Select("SELECT * FROM t_user WHERE phone = #{phone}")
    User selectByPhone(String phone);
}
