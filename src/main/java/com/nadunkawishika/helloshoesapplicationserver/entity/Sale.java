package com.nadunkawishika.helloshoesapplicationserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Sale {
    @Id
    @Column(length = 20)
    private String saleId;
    private LocalDate date;
    private LocalTime time;
    private String paymentDescription;
    private String cashierName;

    @ManyToOne(cascade = {CascadeType.DETACH,CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id")
    private List<SaleDetails> saleDetailsList;
}
