package com.fintech.leanbackend.repository;

import com.fintech.leanbackend.dto.BankStats;
import com.fintech.leanbackend.dto.UserAccountProjection;
import com.fintech.leanbackend.entites.Account;
import com.fintech.leanbackend.entites.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUserId(Long userId);
    Optional<Account> findByUserId(Long userId);
    Optional<Account> findByUser(Users user);
    Optional<Account> findByPhoneNumber(String phoneNumber);
    @Query(value = """
    SELECT 
        u.mi AS EMI,
        u.email AS email,
        a.phone_number AS phoneNumber
    FROM Applicat.users u
    INNER JOIN Applicat.account a
        ON u.id = a.user_id
    WHERE u.id = :id
    """, nativeQuery = true)
    UserAccountProjection getUserAccountDetails(@Param("id") Long id);

}
