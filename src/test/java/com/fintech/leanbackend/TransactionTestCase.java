package com.fintech.leanbackend;

import com.fintech.leanbackend.entites.Transaction;
import com.fintech.leanbackend.entites.TransactionMessages;
import com.fintech.leanbackend.entites.TransactionType;
import com.fintech.leanbackend.entites.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTestCase {

    @Test
    @DisplayName("TC-TXN-01 default constructor creates transaction successfully")
    void defaultConstructor() {

        Transaction transaction = new Transaction();

        assertThat(transaction).isNotNull();
        assertThat(transaction.getAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transaction.getTransactionID())
                .isNull();
    }


    @Test
    @DisplayName("TC-TXN-02 all arguments constructor sets transaction fields")
    void allArgsConstructor() {

        BigDecimal amount = new BigDecimal("5000");

        Users sender = new Users();
        Users receiver = new Users();

        Transaction transaction = new Transaction(
                amount,
                "receiver@fintech.com",
                sender,
                receiver,
                TransactionType.TRANSFER,
                null,
                "TNX123456789ABC"
        );

        assertThat(transaction.getAmount())
                .isEqualByComparingTo(amount);

        assertThat(transaction.getEmail())
                .isEqualTo("receiver@fintech.com");

        assertThat(transaction.getSender())
                .isEqualTo(sender);

        assertThat(transaction.getReceiver())
                .isEqualTo(receiver);

        assertThat(transaction.getTransactionType())
                .isEqualTo(TransactionType.TRANSFER);

        assertThat(transaction.getTransactionID())
                .isEqualTo("TNX123456789ABC");
    }


    @Test
    @DisplayName("TC-TXN-03 amount getter and setter work correctly")
    void amountGetterSetter() {

        Transaction transaction = new Transaction();

        transaction.setAmount(new BigDecimal("2500"));

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("2500");
    }


    @Test
    @DisplayName("TC-TXN-04 email getter and setter work correctly")
    void emailGetterSetter() {

        Transaction transaction = new Transaction();

        transaction.setEmail("receiver@fintech.com");

        assertThat(transaction.getEmail())
                .isEqualTo("receiver@fintech.com");
    }


    @Test
    @DisplayName("TC-TXN-05 sender getter and setter work correctly")
    void senderGetterSetter() {

        Transaction transaction = new Transaction();

        Users sender = new Users();

        transaction.setSender(sender);

        assertThat(transaction.getSender())
                .isEqualTo(sender);
    }


    @Test
    @DisplayName("TC-TXN-06 receiver getter and setter work correctly")
    void receiverGetterSetter() {

        Transaction transaction = new Transaction();

        Users receiver = new Users();

        transaction.setReceiver(receiver);

        assertThat(transaction.getReceiver())
                .isEqualTo(receiver);
    }


    @Test
    @DisplayName("TC-TXN-07 transaction type getter and setter work correctly")
    void transactionTypeGetterSetter() {

        Transaction transaction = new Transaction();

        transaction.setTransactionType(TransactionType.TRANSFER);

        assertThat(transaction.getTransactionType())
                .isEqualTo(TransactionType.TRANSFER);
    }


    @Test
    @DisplayName("TC-TXN-08 transaction message getter and setter work correctly")
    void transactionMessagesGetterSetter() {

        Transaction transaction = new Transaction();

        /*
         * Replace the enum value below with an actual value
         * available in your TransactionMessages enum.
         *
         * Example:
         * TransactionMessages.SUCCESS
         */

        TransactionMessages message =
                TransactionMessages.values()[0];

        transaction.setTransactionMessages(message);

        assertThat(transaction.getTransactionMessages())
                .isEqualTo(message);
    }


    @Test
    @DisplayName("TC-TXN-09 transaction ID can be set and retrieved")
    void transactionIdGetterSetter() {

        Transaction transaction = new Transaction();

        transaction.setTransactionID("TNX123456789ABC");

        assertThat(transaction.getTransactionID())
                .isEqualTo("TNX123456789ABC");
    }


    @Test
    @DisplayName("TC-TXN-10 transaction ID is generated when ID is null")
    void transactionIdGeneratedWhenNull() {

        Transaction transaction = new Transaction();

        assertThat(transaction.getTransactionID())
                .isNull();

        transaction.generateTransactionID();

        assertThat(transaction.getTransactionID())
                .isNotNull();

        assertThat(transaction.getTransactionID())
                .startsWith("TNX");

        assertThat(transaction.getTransactionID())
                .hasSize(15);
    }


    @Test
    @DisplayName("TC-TXN-11 transaction ID is generated when ID is blank")
    void transactionIdGeneratedWhenBlank() {

        Transaction transaction = new Transaction();

        transaction.setTransactionID("");

        transaction.generateTransactionID();

        assertThat(transaction.getTransactionID())
                .isNotNull();

        assertThat(transaction.getTransactionID())
                .startsWith("TNX");

        assertThat(transaction.getTransactionID())
                .hasSize(15);
    }


    @Test
    @DisplayName("TC-TXN-12 existing transaction ID is not replaced")
    void existingTransactionIdIsNotReplaced() {

        Transaction transaction = new Transaction();

        String transactionId = "TNX123456789ABC";

        transaction.setTransactionID(transactionId);

        transaction.generateTransactionID();

        assertThat(transaction.getTransactionID())
                .isEqualTo(transactionId);
    }


    @Test
    @DisplayName("TC-TXN-13 generated transaction IDs are unique")
    void generatedTransactionIdsAreUnique() {

        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        transaction1.generateTransactionID();
        transaction2.generateTransactionID();

        assertThat(transaction1.getTransactionID())
                .isNotEqualTo(transaction2.getTransactionID());
    }


    @Test
    @DisplayName("TC-TXN-14 generated transaction ID follows correct format")
    void transactionIdFormat() {

        Transaction transaction = new Transaction();

        transaction.generateTransactionID();

        String transactionId = transaction.getTransactionID();

        assertThat(transactionId)
                .startsWith("TNX");

        assertThat(transactionId)
                .hasSize(15);

        assertThat(transactionId)
                .matches("TNX[A-Z0-9]{12}");
    }


    @Test
    @DisplayName("TC-TXN-15 transaction can store complete transfer information")
    void completeTransactionInformation() {

        Transaction transaction = new Transaction();

        Users sender = new Users();
        Users receiver = new Users();

        transaction.setAmount(new BigDecimal("1000"));
        transaction.setEmail("receiver@fintech.com");
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setTransactionType(TransactionType.TRANSFER);

        transaction.generateTransactionID();

        assertThat(transaction.getAmount())
                .isEqualByComparingTo("1000");

        assertThat(transaction.getEmail())
                .isEqualTo("receiver@fintech.com");

        assertThat(transaction.getSender())
                .isEqualTo(sender);

        assertThat(transaction.getReceiver())
                .isEqualTo(receiver);

        assertThat(transaction.getTransactionType())
                .isEqualTo(TransactionType.TRANSFER);

        assertThat(transaction.getTransactionID())
                .startsWith("TNX");
    }
}