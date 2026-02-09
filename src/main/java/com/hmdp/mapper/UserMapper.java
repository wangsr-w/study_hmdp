package com.hmdp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.User;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since
 */
public interface UserMapper extends BaseMapper<User> {

    // 查询电话号是否在数据库中
    @Select("select * from tb_user where phone = #{phone}")
    User selectUserByPhone(String phone);

    // 创建新用户
    @Insert("insert into tb_user values (#{id}, #{phone}, #{password}, #{nickName}, #{icon}, #{createTime}, #{updateTime})")
    int insert(User user); // 这里的放回值代表的是几条记录
}
