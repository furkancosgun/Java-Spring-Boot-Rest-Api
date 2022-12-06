# Java Spring Boot Rest API

1 - [Spring Boot Projemiz Için Temel Dosyalar](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.0.1-SNAPSHOT&packaging=jar&jvmVersion=17&groupId=com.furkancosgun&artifactId=users&name=users&description=Demo%20project%20for%20Spring%20Boot&packageName=com.furkancosgun.users&dependencies=native,devtools,lombok,configuration-processor,web,security,data-jpa,mysql)
<br>
2 - [Elimizde MySQLWorkbench kurulu oldugunu varsayıyorum](https://dev.mysql.com/downloads/workbench/) 
<br>
3 - [Ayrıca MySQL inde kurulu olması gerekli](https://www.mysql.com/downloads/)
<br>
4 - MySQL Workbench yardımıyla scheme oluşturup kullanıcı adı ve şifre bilgilerini unutmuyoruz

5 - Spring Projemize donelim
-	
 -	VeriTabanı Bağlantısı ve Port Seçimi
	-main/resources/application.properties
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/UsersSchme #UsersSchme Şema adımız oluyor
spring.datasource.username=root #MySQL kullanıcı adı
spring.datasource.password=123456  #Şifresi
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver. #sabit
spring.jpa.hibernate.ddl-auto=update   #Okuma ve Yazma tam izin sabit
server.port=8080 #Api için port 
```
- Entityleri Oluşturmak 

```java
package com.furkancosgun.users.Entity;  
  
import jakarta.persistence.*;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
import org.hibernate.annotations.CreationTimestamp;  
import java.util.Date;  
  
  
@Entity  //Bu classın db de maplenecegini soyleriz
@Table(name = "users")  //db tablo adı
@Data   //her property için getter ve setter
@NoArgsConstructor   //argumansız constructor
public class User {  

    @Id  //id olcak
    @GeneratedValue(strategy = GenerationType.IDENTITY) //otomatik artan sayı 
    @Column(name = "id")  //kolon adı
    private long id;  
    
    @Column(name = "full_name")  
    private String fullName;  
    
    @Column(name = "email")  
    private String email;  
  
    @Column(name = "password")  
    private String password;  
  
    @Column(name = "system_auto_date")  
    @Temporal(TemporalType.TIMESTAMP)  //sisteme kayıt edildigi saat ve tarih
    @CreationTimestamp
    private Date date;  
    
    //Constructor
    public User(String fullName,  String email,String password) {  
        this.email = email;  
        this.fullName = fullName;  
        this.password = password;  
    }   
}
```
-	Entity katmanlarının repostiorylerini hazırlayalım
```java
package com.furkancosgun.users.Repostiory;

import com.furkancosgun.users.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
Jpa Tarafından bize sql tarafı için birçok hazır yapı gelmekte
msla..; select yapıları = find... şeklinde

findUserByName() şeklinde bir fonk oluşturursanız da jpa bunu sizin için doldurcaktır 
ve isme gore arama yapabilceksiniz artık
*/

@Repository
public interface UserRepostiory extends JpaRepository<User,Long> { }
```


<br>


- Service Katmanlarını Hazırlayalım / Buradaki onemli noktalardan birisi de şifreleri , kişisel bilgileri vs. Göstermemek ve gerekirse ,istek ve cevaplar için ayrı ayrı entityler oluşturmak ,isteğinize göre cevaplarınızı dönebilir, isteğinize göre isteklerinizi belirleyebilirsiniz.
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


@Service//Her classın aşagı yukarı farklı gorevi oldugu için bu tur anatosyanları unutmanız durumunda hatalar alabilirsiniz
public class UserService {

@Autowired //Constructor kullanmaya gerek kalmadan deger ataması saglar
private UserRepostiory userRepostiory;


//Butun kullanıcıları getircek olan fonk
public List<UserResponse> getAllUsers(){
//user classımızda passwrod vs. bilgiler oldugu için bu şekilde mapleme işlemi yaparak tip değişimi sagladık ve return ettik
return userRepostiory.findAll()
.stream()
.map(user -> userToUserResponse(user))
.collect(Collectors.toList());
}

  
//secilen id li kullancıyı return etcek
//bulamadıgı durumlarda oluşturdugumz exception classına takılıp hatayı return edecektir
public ResponseEntity<UserResponse> getUserById(long id) throws NotFoundException {
return ResponseEntity.ok(userToUserResponse(userRepostiory.findById(id).orElseThrow(()-> new NotFoundException(404,"User Not Found"))));
}

//Kullanıcı kayıt
public UserResponse saveUser(User user){
return userToUserResponse(userRepostiory.save(user));
}
  
/*
Kullanıcı guncelleme
once o kullanıcı varmı bakarız varsa,
onceki degerlerinin üzerine yeni degerlerini de verip 
tekrar save işlemi yaptgımızıda bu bize update olarak yanısyacaktır
yoksa exceptiondan hata doneriz
*/
public ResponseEntity<UserResponse> updateUser(long id, User user) throws NotFoundException {
User newUser = userRepostiory.findById(id).orElseThrow(()-> new NotFoundException(404,"User Not Found"));
newUser.setFullName(user.getFullName());
newUser.setEmail(user.getEmail());
newUser.setPassword(user.getPassword());

return ResponseEntity.ok(userToUserResponse(userRepostiory.save(newUser)));
}

/*
Kullanıcı silme
Kullanıcı varsa sistemde başarı durumunu true doner,
kullanıcı yoksa başarı durumunu false doneriz
*/
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

//Mapleme işlemini hızlandırmak için yapılmış bir method
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
@CrossOrigin("http://localhost:8080")//Ana Url
@RequestMapping("/api")//Ana Yol
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
//REQUEST
{                                
"fullName":"Admin Admin",        
"email":"admin@admin.com",        
"password":"admin"                
}
// RESPONSE
{
"id" : 2,
"fullName" : "Admin Admin",
"email" : "admin@admin.com"
}
```
PUT: localhost:8080/api/users/1
```json
//REQUEST
{                                
"fullName": "cosgun furkan",    
"email": "qwea@gmail.com",         
"password":"0123456789"            
}                
//RESPONSE
{
"id" : 1,
"email" : "qwe@gmail.com",
"password" : "0123456789"
}
```
DELETE: localhost:8080/api/users/1
```json
//RESPONSE
{
"success": true
}
```

