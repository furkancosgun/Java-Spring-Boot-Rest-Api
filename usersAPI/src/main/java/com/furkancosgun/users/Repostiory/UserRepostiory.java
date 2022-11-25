package com.furkancosgun.users.Repostiory;

import com.furkancosgun.users.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepostiory extends JpaRepository<User,Long> {

}
