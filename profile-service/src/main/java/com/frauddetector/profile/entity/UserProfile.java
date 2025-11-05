package com.frauddetector.profile.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("UserProfile")
public class UserProfile implements Serializable {

    @Id
    private String userId;
    private int transactionCount;
    private double averageAmount;
    private String lastTransactionCountry;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public double getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(double averageAmount) {
        this.averageAmount = averageAmount;
    }

    public String getLastTransactionCountry() {
        return lastTransactionCountry;
    }

    public void setLastTransactionCountry(String lastTransactionCountry) {
        this.lastTransactionCountry = lastTransactionCountry;
    }
}