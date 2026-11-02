package com.fintech.leanbackend.repository;

import com.fintech.leanbackend.dto.TransactionSummaryDto;
import com.fintech.leanbackend.entites.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findById(Long aLong);
    @Query(value = "SELECT " +
            "t.id AS transaction_id, " +
            "t.amount AS amount, " +
            "t.created_at AS created_at, " +
            "t.transaction_type AS transaction_type, " +
            "sender.name AS sender_name, " +
            "sender.mi AS sender_mi, " +
            "receiver.name AS receiver_name, " +
            "receiver.mi AS receiver_mi, " +
            "CASE WHEN t.sender_id = :userId THEN 'SENDER' ELSE 'RECEIVER' END AS user_role " +
            "FROM `transaction` t " +
            "LEFT JOIN `users` sender ON t.sender_id = sender.id " +
            "LEFT JOIN `users` receiver ON t.receiver_id = receiver.id " +
            "WHERE t.sender_id = :userId OR t.receiver_id = :userId " +
            "ORDER BY t.created_at DESC, t.id DESC " +
            "LIMIT 50",
            nativeQuery = true)
    List<TransactionSummaryDto> findLastFiveTransactionsByUserId(@Param("userId") Long userId);
//    @Query(value = "SELECT " +
//            "t.id AS transaction_id, " +
//            "t.amount AS amount, " +
//            "t.created_at AS created_at, " +
//            "t.transaction_type AS transaction_type, " +
//            "sender.name AS sender_name, " +
//            "sender.mi AS sender_mi, " +
//            "receiver.name AS receiver_name, " +
//            "receiver.mi AS receiver_mi, " +
//            "CASE WHEN t.sender_id = :userId THEN 'SENDER' ELSE 'RECEIVER' END AS user_role " +
//            "FROM `transaction` t " +
//            "JOIN `users` sender ON t.sender_id = sender.id " +
//            "JOIN `users` receiver ON t.receiver_id = receiver.id " +
//            "WHERE t.sender_id = :userId OR t.receiver_id = :userId " +
//            "ORDER BY t.created_at DESC, t.id DESC " +
//            "LIMIT 5",
//            nativeQuery = true)
//    List<TransactionSummaryDto> findLastFiveTransactionsByUserId(@Param("userId") Long userId);

}
