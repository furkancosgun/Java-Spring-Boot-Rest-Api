package com.furkancosgun.users.Controller;

import com.furkancosgun.users.Entity.User;
import com.furkancosgun.users.Response.UserResponse;
import com.furkancosgun.users.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController//Rest api
@CrossOrigin("http://localhost:8080")//Host
@RequestMapping("/api")
public class UserController {

    @Autowired
    UserService userService;



    @GetMapping("/users")
    public List<UserResponse> getAllUsers(){
        return  userService.getAllUsers();
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) throws Throwable {
        return userService.getUserById(id);
    }

    @PostMapping("/users")
    public UserResponse saveUser(@RequestBody User user){
        return  userService.saveUser(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable long id, @RequestBody User user) throws Throwable {
        return userService.updateUser(id,user);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Boolean> deleteUser(@PathVariable long id) throws Throwable {
        return userService.deleteUserById(id);
    }
}
