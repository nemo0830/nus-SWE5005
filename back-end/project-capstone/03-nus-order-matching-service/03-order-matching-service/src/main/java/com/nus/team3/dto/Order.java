package com.nus.team3.dto;

import com.nus.team3.constants.TradeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order implements Comparable<Order>{
    private String buyOrSell;
    private String transactionId;
    private String transactionIdAfterMatch;
    private String stockTicker;
    private String user;
    private long timestamp;
    private float price;
    private int quantity;
    private String matchStatus;

    public Order(String buyOrSell, String transactionId, String stockTicker, String user, long timestamp, float price, int quantity) {
        this.buyOrSell = buyOrSell;
        this.transactionId = transactionId;
        this.stockTicker = stockTicker;
        this.user = user;
        this.timestamp = timestamp;
        this.price = price;
        this.quantity = quantity;
        this.matchStatus = TradeEnum.STATUS.UNMATCHED.name();
        this.transactionIdAfterMatch = "";
    }

    public void print(){
        System.out.print(this.toString());
    }

    public String toString(){
        return this.buyOrSell + '#' +
                this.transactionId + '#' +
                this.stockTicker + '#' +
                this.user + '#' +
                this.quantity+ '#' +
                this.price + '#' +
                this.matchStatus + '#' +
                this.transactionIdAfterMatch;
    }

    @Override
    public int compareTo(Order o){
        if (this.getBuyOrSell().equalsIgnoreCase(TradeEnum.SIDE.BUY.name())){
            return o.getPrice() - this.getPrice() != 0.00 ? (o.getPrice() - this.getPrice() > 0 ? 1 : -1) : (int)(this.getTimestamp() - o.getTimestamp());
        }else{
            return this.getPrice() - o.getPrice() != 0.00 ? (this.getPrice() - o.getPrice() > 0 ? 1 : -1): (int)(this.getTimestamp() - o.getTimestamp());
        }
    }

    public String getBuyOrSell() {
        return buyOrSell;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStockTicker() {
        return stockTicker;
    }

    public String getUser() {
        return user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }


    public String getTransactionIdAfterMatch() {
        return transactionIdAfterMatch;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setTransactionIdAfterMatch(String transactionIdAfterMatch) {
        this.transactionIdAfterMatch = transactionIdAfterMatch;
    }

    public void setMatchStatus(String status){
        this.matchStatus = status;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Id
    @Column(name="id",unique=true,nullable=false)
    private Long id;
}
