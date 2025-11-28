package com.example.onlinestore.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import java.util.*;

@Service
public class ProductService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fakestore.api.base}")
    private String baseUrl;

    public List<Map<String, Object>> getAllProducts() {
        String url = baseUrl + "/products";
        ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>(){});
        return resp.getBody();
    }

    public Map<String, Object> getProductById(Long id) {
        String url = baseUrl + "/products/" + id;
        return restTemplate.getForObject(url, Map.class);
    }

    public List<Map<String, Object>> getProductsByCategory(String category) {
        String url = baseUrl + "/products/category/" + category;
        ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>(){});
        return resp.getBody();
    }

    public Map<String, Object> createProduct(Map<String, Object> product) {
        String url = baseUrl + "/products";
        return restTemplate.postForObject(url, product, Map.class);
    }

    public Map<String, Object> updateProduct(Long id, Map<String, Object> product) {
        String url = baseUrl + "/products/" + id;
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(product);
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
        return resp.getBody();
    }

    public Map<String, Object> deleteProduct(Long id) {
        String url = baseUrl + "/products/" + id;
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.DELETE, null, Map.class);
        return resp.getBody();
    }
}
