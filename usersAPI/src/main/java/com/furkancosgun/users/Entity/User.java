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
    @Column(name = "id",nullable = true)
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
