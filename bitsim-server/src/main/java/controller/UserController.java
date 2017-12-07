package controller;

import com.alibaba.fastjson.JSONObject;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/imInit")
    public String imInit() {
        JSONObject jsonObject = userService.imInit();
        return jsonObject.toJSONString();
    }

    @RequestMapping("/login")
    @ResponseBody
    public User getUser(@RequestParam String username, @RequestParam String password) {
        return userService.login(username, password);
    }
}