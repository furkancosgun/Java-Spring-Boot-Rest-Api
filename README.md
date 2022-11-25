# Java Spring Boot Rest API

1 - [Spring Boot Projemiz Için Temel Dosyalar](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.0.1-SNAPSHOT&packaging=jar&jvmVersion=17&groupId=com.furkancosgun&artifactId=users&name=users&description=Demo%20project%20for%20Spring%20Boot&packageName=com.furkancosgun.users&dependencies=native,devtools,lombok,configuration-processor,web,security,data-jpa,mysql)
2 - [Elimizde MySQLWorkbench kurulu oldugunu varsayıyorum](https://dev.mysql.com/downloads/workbench/) 
3 - [Ayrıca MySQL inde kurulu olması gerekli](https://www.mysql.com/downloads/)
4 - MySQL Workbench yardımıyla scheme oluşturup kullanıcı adı ve şifre bilgilerini unutmuyoruz

5 - Spring Projemize donelim
-    
 -    VeriTabanı Bağlantısı ve Port Seçimi
    -main/resources/application.properties
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/UsersSchm
spring.datasource.username=root
spring.datasource.password=5747Fc..
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```
- Entityleri Oluşturmak 

```java
package com.furkancosgun.users.Entity;  
  
import jakarta.persistence.*;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
import org.hibernate.annotations.CreationTimestamp;  
import java.util.Date;  
  
  
@Entity  
@Table(name = "users")  
@Data  
@NoArgsConstructor  
public class User {  

    @Id  
 @GeneratedValue(strategy = GenerationType.IDENTITY)  
    @Column(name = "id")  
    private long id;  
    
    @Column(name = "full_name")  
    private String fullName;  
    
    @Column(name = "email")  
    private String email;  
  
    @Column(name = "password")  
    private String password;  
  
    @Column(name = "system_auto_date")  
    @Temporal(TemporalType.TIMESTAMP)  
    @CreationTimestamp  
  private Date date;  
    public User(String fullName,  String email,String password) {  
        this.email = email;  
        this.fullName = fullName;  
        this.password = password;  
    }   
}
```
-    Entity katmanlarının repostiorylerini hazırlayalım
```java
package com.furkancosgun.users.Repostiory;

import com.furkancosgun.users.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepostiory extends JpaRepository<User,Long> { }
```


<br>


- Service Katmanlarını Hazırlayalım / Buradaki onemli noktalardan birisi şifreleri , kişisel bilgileri vs. API yardımıyla göstermemek ve gerekirse istekler ve cevaplar için ayrı ayrı entityler oluşturmak isteğinize göre cevaplarınızı dönebilir isteğinize göre isteklerinizi belirleyebilirsiniz.
```java
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
}}
```
 - Kendimize ait bir de exception sınıfı oluşturalım , hata mesajlarımızı ve hata kodlarımızı istegimize göre donelim    
```java
package com.furkancosgun.users.Exception;  
  
import lombok.AllArgsConstructor;  
import lombok.Builder;  
import lombok.Data;  
  
@Data  
@AllArgsConstructor  
public class NotFoundException extends Exception {  
  
    private int errorCode;  
    private String errorMessage;  
}
```
 - Son Olarakta Controller Katmanımzıı Yazalım
    
```java
package com.furkancosgun.users.Controller;

import com.furkancosgun.users.Entity.User;
import com.furkancosgun.users.Response.UserResponse;
import com.furkancosgun.users.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
  
@RestController
@CrossOrigin("http://localhost:8080")
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
```

## Projemizi Çalıştıralım Ve Postman Uzerinden Test Edelim...

GET: localhost:8080/api/users

```json
//RESPONSE
[{
"id": 1,
"fullName": "furkan cosgun",
"email": "asdsadsds@gmail.com"
},
{
"id": 2,
"fullName":"Admin Admin",
"email":"admin@admin.com",
}]
```

GET: localhost:8080/api/users/1
```json
//RESPONSE
{
"id": 1,
"fullName": "furkan cosgun",
"email": "asdsadsds@gmail.com"
}
```

POST: localhost:8080/api/users
```json
//REQUEST                         |    RESPONSE
{                                |{
"fullName":"Admin Admin",        |"id" : 2,
"email":"admin@admin.com",        |"fullName" : "Admin Admin",
"password":"admin"                |"email" : "admin@admin.com"
}                                |}
```
PUT: localhost:8080/api/users/1
```json
//REQUEST                        |//RESPONSE
{                                |{
"fullName": "cosgun furkan",    |"id" : 1,
"email": "qwea@gmail.com",         |"email" : "qwe@gmail.com",
"password":"0123456789"            |"password" : "0123456789"
}                                |
```
DELETE: localhost:8080/api/users/1
```json
//RESPONSE
{
"success": true
}
```
```JAVA
System.out.println("KATKILARA HERZAMAN AÇIĞIM");
```
