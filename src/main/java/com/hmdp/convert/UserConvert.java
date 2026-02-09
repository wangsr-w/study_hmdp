package com.hmdp.convert;

import java.util.HashMap;
import java.util.Map;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;

public class UserConvert {

    public static UserDTO convertStudent(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setNickName(user.getNickName());
        userDTO.setIcon(user.getIcon());
        return userDTO;
    }

    public static User convertStudent(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setNickName(userDTO.getNickName());
        user.setIcon(userDTO.getIcon());
        return user;
    }

    // UserDto转成hashmap保存
    public static Map<String, String> converttoMap(UserDTO userDTO) {
        Map<String, String> userDtoMap = new HashMap<String, String>();
        userDtoMap.put("id", userDTO.getId().toString());
        userDtoMap.put("icon", userDTO.getIcon());
        userDtoMap.put("nickname", userDTO.getNickName());

        return userDtoMap;
    }

    // hashmap转化成UserDto
    public static UserDTO converttoUserDTO(Map<String, String> userDtoMap) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(Long.valueOf(userDtoMap.get("id")));
        userDTO.setIcon((String) userDtoMap.get("icon"));
        userDTO.setNickName((String) userDtoMap.get("nickname"));

        return userDTO;
    }

}
