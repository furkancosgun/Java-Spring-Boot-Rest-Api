package com.furkancosgun.users.Service;

import com.furkancosgun.users.Entity.User;
import com.furkancosgun.users.Repostiory.UserRepostiory;
import com.furkancosgun.users.Response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import com.furkancosgun.users.Exception.NotFoundException ;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepostiory userRepostiory;

    public List<UserResponse> getAllUsers(){

        return userRepostiory.findAll()
                .stream()
                .map(user -> userToUserResponse(user))
                .collect(Collectors.toList());
    }

    public ResponseEntity<UserResponse> getUserById(long id) throws NotFoundException {
        return ResponseEntity.ok(userToUserResponse(userRepostiory.findById(id).orElseThrow(()-> new NotFoundException(404,"User Not Found"))));
    }

    public UserResponse saveUser(User user){
        return userToUserResponse(userRepostiory.save(user));
    }

    public ResponseEntity<UserResponse> updateUser(long id, User user) throws NotFoundException {
        User newUser = userRepostiory.findById(id).orElseThrow(()-> new NotFoundException(404,"User Not Found"));
        newUser.setFullName(user.getFullName());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        return ResponseEntity.ok(userToUserResponse(userRepostiory.save(newUser)));
    }

    public Map<String, Boolean> deleteUserById(long id){

        Map<String,Boolean> map = new HashMap<>();
        Optional<User> user = userRepostiory.findById(id);
        userRepostiory.deleteById(id);
        if (user.isPresent())
            map.put("success",Boolean.TRUE);
        else
            map.put("success",Boolean.FALSE);
        return map;
    }

    private UserResponse userToUserResponse(User user){
        return new UserResponse(user.getId(),user.getFullName(),user.getEmail());
    }


}
