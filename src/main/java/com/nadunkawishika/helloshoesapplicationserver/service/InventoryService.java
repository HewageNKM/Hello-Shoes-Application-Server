package com.nadunkawishika.helloshoesapplicationserver.service;

import com.nadunkawishika.helloshoesapplicationserver.dto.ItemDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface InventoryService {
    List<ItemDTO>getAllItems();
    void addItem(String dto, MultipartFile image) throws IOException;
    void updateItem(String id, String itemDTO, MultipartFile image) throws IOException;
    void deleteItem(String id);
    List<ItemDTO> filterItems(String pattern);
    ItemDTO getItem(String id);
}
