package com.fintech.leanbackend.repository;

import com.fintech.leanbackend.dto.BankAdminStatsProjection;
import com.fintech.leanbackend.dto.BankStats;
import com.fintech.leanbackend.dto.UserLoginResponse;
import com.fintech.leanbackend.dto.UserLoginResponseDetail;
import com.fintech.leanbackend.entites.Status;
import com.fintech.leanbackend.entites.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Users> findByMi(String mi);
    @Query("""
    SELECT new com.fintech.leanbackend.dto.UserLoginResponseDetail(
        u.id,
        u.email,
        u.name,
        u.verification,
        r.name
    )
    FROM Users u
    LEFT JOIN u.roles r
    WHERE u.id = :id
""")
    UserLoginResponseDetail findUserLoginDetails(@Param("id") Long id);
    @Modifying
    @Transactional
    @Query("""
    UPDATE Users u
    SET u.statuses = :status
    WHERE u.email = :email
""")
    int updateStatusByEmail(
            @Param("email") String email,
            @Param("status") Status status
    );
    @Query(value = """
    SELECT
        (SELECT COALESCE(SUM(a.balance), 0)
         FROM Applicat.account a) AS totalBalance,

        (SELECT COUNT(*)
         FROM Applicat.users) AS totalUsers,

        (SELECT COUNT(*)
         FROM Applicat.users
         WHERE statuses = 'SUSPENDED') AS totalSuspendedUsers,

        (SELECT COUNT(*)
         FROM Applicat.users
         WHERE verification = 'UN_VERIFIED') AS totalUnverifiedUsers,

        (SELECT COUNT(*)
         FROM Applicat.users
         WHERE verification = 'VERIFIED') AS totalVerifiedUsers
    """, nativeQuery = true)
    BankAdminStatsProjection getAdminankStats();

    @Query("""
    SELECT new com.fintech.leanbackend.dto.BankStats(
        (SELECT COALESCE(SUM(a.balance), 0)
         FROM Account a),

        COUNT(u),

        SUM(CASE
            WHEN u.statuses = com.fintech.leanbackend.entites.Status.SUSPENDED
            THEN 1 ELSE 0
        END),

        SUM(CASE
            WHEN u.verification = com.fintech.leanbackend.entites.Verification.UN_VERIFIED
            THEN 1 ELSE 0
        END),

        SUM(CASE
            WHEN u.verification = com.fintech.leanbackend.entites.Verification.VERIFIED
            THEN 1 ELSE 0
        END)
    )
    FROM Users u
""")
    BankStats getBankStats();
}
