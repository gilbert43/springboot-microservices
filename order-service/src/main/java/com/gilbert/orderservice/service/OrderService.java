package com.gilbert.orderservice.service;


import com.gilbert.orderservice.dto.OrderRequest;
import com.gilbert.orderservice.dto.OrderLineItemsDto;
import com.gilbert.orderservice.model.Order;
import com.gilbert.orderservice.model.OrderLineItems;
import com.gilbert.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;


    public void placeOrder(@RequestBody OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(OrderLineItemsDto -> mapToDto(OrderLineItemsDto)).toList();

        order.setOrderLineItemsList(orderLineItems);
        //call inventory service and check if there is product before placing an order
        Boolean result = webClient.get()
                .uri("http://localhost:8080/api/inventory")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if(result){
            orderRepository.save(order);
        }else{
            throw new IllegalArgumentException("Product is not in stock");
        }


    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {

        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;
    }

}
