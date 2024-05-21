package com.nadunkawishika.helloshoesapplicationserver.service.impl;

import com.nadunkawishika.helloshoesapplicationserver.dto.RefundDTO;
import com.nadunkawishika.helloshoesapplicationserver.dto.SaleDTO;
import com.nadunkawishika.helloshoesapplicationserver.dto.SaleDetailDTO;
import com.nadunkawishika.helloshoesapplicationserver.entity.*;
import com.nadunkawishika.helloshoesapplicationserver.enums.Level;
import com.nadunkawishika.helloshoesapplicationserver.exception.customExceptions.NotFoundException;
import com.nadunkawishika.helloshoesapplicationserver.exception.customExceptions.RefundNotAvailableException;
import com.nadunkawishika.helloshoesapplicationserver.repository.*;
import com.nadunkawishika.helloshoesapplicationserver.service.SaleService;
import com.nadunkawishika.helloshoesapplicationserver.util.GenerateId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final SaleDetailsRepository saleDetailsRepository;
    private final CustomerRepository customerRepository;
    private final StocksRepository stocksRepository;
    private final InventoryRepository inventoryRepository;
    private final DecimalFormat df = new DecimalFormat("0.00");
    private final Logger LOGGER = LoggerFactory.getLogger(SaleServiceImpl.class);


    @Override
    public void addSale(SaleDTO dto) {
        LOGGER.info("Sale request received");
        Optional<Customer> customer = Optional.empty();
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId());
        }
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SaleDetailDTO> saleDetailsList = dto.getSaleDetailsList();
        saleDetailsList.forEach(saleDTO -> {
            Item item = inventoryRepository.findById(saleDTO.getItemId().toLowerCase()).orElseThrow(() -> new NotFoundException("Inventory not found " + saleDTO.getItemId()));
            Stock stock = stocksRepository.findByItemId(saleDTO.getItemId().toLowerCase()).orElseThrow(() -> new NotFoundException("Stock not found " + saleDTO.getItemId()));
            if (saleDTO.getSize().equalsIgnoreCase("40")) {
                item.setQuantity(item.getQuantity() - saleDTO.getQuantity());
                stock.setSize40(stock.getSize40() - saleDTO.getQuantity());
            } else if (saleDTO.getSize().equalsIgnoreCase("41")) {
                item.setQuantity(item.getQuantity() - saleDTO.getQuantity());
                stock.setSize41(stock.getSize41() - saleDTO.getQuantity());
            } else if (saleDTO.getSize().equalsIgnoreCase("42")) {
                item.setQuantity(item.getQuantity() - saleDTO.getQuantity());
                stock.setSize42(stock.getSize42() - saleDTO.getQuantity());
            } else if (saleDTO.getSize().equalsIgnoreCase("43")) {
                item.setQuantity(item.getQuantity() - saleDTO.getQuantity());
                stock.setSize43(stock.getSize43() - saleDTO.getQuantity());
            } else if (saleDTO.getSize().equalsIgnoreCase("44")) {
                item.setQuantity(item.getQuantity() - saleDTO.getQuantity());
                stock.setSize44(stock.getSize44() - saleDTO.getQuantity());
            } else if (saleDTO.getSize().equalsIgnoreCase("45")) {
                item.setQuantity(item.getQuantity() - saleDTO.getQuantity());
                stock.setSize45(stock.getSize45() - saleDTO.getQuantity());
            }
            inventoryRepository.save(item);
            stocksRepository.save(stock);
        });

        Sale sale = Sale.builder().saleId(GenerateId.getId("SAL").toLowerCase()).date(LocalDate.now()).total(dto.getTotal()).paymentDescription(dto.getPaymentDescription()).time(LocalTime.now()).customer(customer.orElse(null)).cashierName(userName).build();
        saleRepository.save(sale);

        saleDetailsList.forEach(saleDTO -> saleDetailsRepository
                .save
                        (
                                SaleDetails
                                        .builder()
                                        .sale(sale)
                                        .price(saleDTO.getPrice())
                                        .item(inventoryRepository.findById(saleDTO.getItemId().toLowerCase()).orElseThrow(() -> new NotFoundException("Inventory not found " + saleDTO.getItemId())))
                                        .qty(saleDTO.getQuantity())
                                        .saleDetailsId(GenerateId.getId("SALD").toLowerCase())
                                        .total(saleDTO.getTotal())
                                        .name(saleDTO.getDescription().toLowerCase())
                                        .size(saleDTO.getSize().toLowerCase())
                                        .build()
                        ));

        customer.ifPresent(cus -> {
            cus.setRecentPurchaseDateAndTime(LocalDateTime.now());
            Double totalPoints = dto.getTotal() / 1000.0;
            totalPoints = Double.valueOf(df.format(totalPoints));
            cus.setTotalPoints(cus.getTotalPoints() + totalPoints);

            if (cus.getTotalPoints() < 50) {
                cus.setLevel(Level.New);
            } else if (cus.getTotalPoints() >= 50 && cus.getTotalPoints() < 100) {
                cus.setLevel(Level.Bronze);
            } else if (cus.getTotalPoints() >= 100 && cus.getTotalPoints() < 200) {
                cus.setLevel(Level.Silver);
            } else if (cus.getTotalPoints() >= 200) {
                cus.setLevel(Level.Gold);
            }
            System.out.print(cus);
            customerRepository.save(cus);
        });
        LOGGER.info("Sale request completed "+sale.getSaleId());
    }

    @Override
    public SaleDTO getSale(String id) {
        Sale sale = saleRepository.findById(id).orElseThrow(() -> new NotFoundException("Sale not found " + id));
        LocalDate date = sale.getDate();
        long between = ChronoUnit.DAYS.between(date, LocalDate.now());
        if (between >= 3) {
            throw new RefundNotAvailableException("Refund Not Available for " + id);
        }
        return SaleDTO.builder().saleId(sale.getSaleId()).paymentDescription(sale.getPaymentDescription()).total(sale.getTotal()).customerId(sale.getCustomer()!=null?sale.getCustomer().getCustomerId():"No Customer").build();
    }

    @Override
    public SaleDetailDTO getSaleItem(String orderId, String itemId) {
        Sale sale = saleRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Sale Not Found " + orderId));
        List<SaleDetails> saleDetailsList = sale.getSaleDetailsList();
        for(SaleDetails saleDetails : saleDetailsList) {
            if (saleDetails.getItem().getItemId().equalsIgnoreCase(itemId)) {
                return SaleDetailDTO.builder().description(saleDetails.getName()).price(saleDetails.getPrice()).quantity(saleDetails.getQty()).size(saleDetails.getSize()).total(saleDetails.getTotal()).build();
            }
        }
        throw new NotFoundException("Item not found in the sale");
    }

    @Override
    public void refundSaleItem(RefundDTO dto) {
        LOGGER.info("Refund sale item request received");
        Stock stock = stocksRepository.findByItemId(dto.getItemId().toLowerCase()).orElseThrow(() -> new NotFoundException("Stock not found " + dto.getItemId()));
        Item item = inventoryRepository.findById(dto.getItemId().toLowerCase()).orElseThrow(() -> new NotFoundException("Inventory not found " + dto.getItemId()));
        item.setQuantity(item.getQuantity() + Integer.parseInt(dto.getQty()));
        switch (dto.getSize()) {
            case "40":
                stock.setSize40(stock.getSize40() + Integer.parseInt(dto.getQty()));
                break;
            case "41":
                stock.setSize41(stock.getSize41() + Integer.parseInt(dto.getQty()));
                break;
            case "42":
                stock.setSize42(stock.getSize42() + Integer.parseInt(dto.getQty()));
                break;
            case "43":
                stock.setSize43(stock.getSize43() + Integer.parseInt(dto.getQty()));
                break;
            case "44":
                stock.setSize44(stock.getSize44() + Integer.parseInt(dto.getQty()));
                break;
            case "45":
                stock.setSize45(stock.getSize45() + Integer.parseInt(dto.getQty()));
                break;
        }

        inventoryRepository.save(item);
        stocksRepository.save(stock);
    }
}