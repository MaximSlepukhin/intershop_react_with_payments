package com.github.maximslepukhin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table("users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    @Id
    private Long id;
    private String username;
    private String password;
    private String role;
    private Double balance;

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
