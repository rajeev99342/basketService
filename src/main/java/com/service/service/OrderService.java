package com.service.service;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.UserRole;
import com.service.constants.values.Constants;
import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.repos.*;
import com.service.utilites.Payment;
import com.service.websocket.WebSocketMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.service.constants.enums.UserRole.ADMIN;

@Service
@Slf4j
public class OrderService {

    @Autowired
    WebSocketMessageSender webSocketMessageSender;
    private static Logger LOG = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    FirebasePushNotificationService firebasePushNotificationService;
    @Autowired
    CartRepo cartRepo;
    @Autowired
    CartDetailsRepo cartDetailsRepo;

    @Autowired
    InstockRepo instockRepo;
    @Autowired
    Payment payment;

    @Autowired
    UserRepo userRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    OrderDetailsRepository orderDetailsRepository;

    @Autowired
    ImageService imageService;

    @Autowired
    JwtTokenUtility jwtTokenUtility;

    @Autowired
    QuantityRepo quantityRepo;

    public GlobalResponse placeOrder(OrderModel orderModel) {
        String sellerToken = null;
        List<ProductOrderDetails> productWiseOrders = new ArrayList<>();
        User user = userRepo.findUserByPhone(orderModel.getUserPhone());
        Order order = new Order();
        order.setOrderDate(new Date(System.currentTimeMillis()));
        LocalDate localDate = LocalDate.now().plusDays(2);
        Date expectedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        order.setExpectedDeliveryDate(expectedDate);
        order.setUser(user);
        order.setModifiedDate(new Date());
        order.setOrderStatus(OrderStatus.PLACED);
        order.setTotalCost(orderModel.getFinalAmount());
        orderRepo.save(order);
        List<String> outOfStockProducts = new ArrayList<>();
        for (DisplayCartProduct cartProduct : orderModel.getCartProducts()) {
            ProductOrderDetails productWiseOrder = new ProductOrderDetails();
            try {
                // check for product availability --> someone could have order this

                Stock stock = instockRepo.findStockByProduct(productRepo.findById(cartProduct.getId()).get());
                Integer inStock = stock.getInStock();
                if (inStock >= cartProduct.getSelectedCount() && cartProduct.getSelectedCount() != 0) {
                    Integer availableStockAfterThisOrder = inStock - cartProduct.getSelectedCount();
                    stock.setInStock(availableStockAfterThisOrder);
                    instockRepo.save(stock);
                    OrderDetails productDelivery = new OrderDetails();
                    productDelivery.setOrderStatus(OrderStatus.PLACED);
                    productDelivery.setProduct(productRepo.findById(cartProduct.getId()).get());
                    productDelivery.setOrder(order);
                    productDelivery.setItemPrice(cartProduct.getQuantityModel().getPrice()*cartProduct.getSelectedCount());
                    productDelivery.setQuantity(cartProduct.getSelectedCount());
                    sellerToken = getSellerToken(cartProduct.getModel().getSellerId());
                    Address address = addressRepo.findAddressByUser(user);
                    orderDetailsRepository.save(productDelivery);
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setOrderStatus(OrderStatus.PLACED);
                    productWiseOrder.setPrice(cartProduct.getQuantityModel().getPrice());
//                    productWiseOrder.setCompleteAddress(completeAddress);
                    productWiseOrder.setDeliveryAgentDetails("Rajeev Kumar, Mobile - 9878979798");
                } else {
                    LOG.error("Unable to process order by user "+order.getUser().getPhone()+" "+ "[OUT OF STOCK]");
                    productWiseOrder.setProductId(cartProduct.getId());
                    outOfStockProducts.add(cartProduct.getModel().getName());
                    productWiseOrder.setTotalProductCount(cartProduct.getSelectedCount());
                    productWiseOrder.setOrderStatus(OrderStatus.CANCELED_DUE_TO_OUT_OF_STOCK);
                }
            } catch (Exception e) {
                LOG.error("Unable to process order by user "+order.getUser().getPhone(),e);
                productWiseOrder.setProductId(cartProduct.getId());
                productWiseOrder.setOrderStatus(OrderStatus.FAILED_DUE_TO_TECHNICAL_ISSUE);
            }

            productWiseOrders.add(productWiseOrder);

        }

        // delete from the cart
        Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(orderModel.getUserPhone()));
        List<CartDetails> cartDetails = cartDetailsRepo.findCartDetailsByCart(cart);
        List<ProductOrderDetails> successfullyOrdredProduct = productWiseOrders.stream().
                filter(productWiseOrder -> productWiseOrder.getOrderStatus().equals(OrderStatus.PLACED)).collect(Collectors.toList());
        String unsuccessfulOrderName = null ;
        if(successfullyOrdredProduct.size() == 0){
//            orderRepo.delete(order);
            return new GlobalResponse("ORDERED PRODUCTS ARE OUT OF STOCKS", HttpStatus.FORBIDDEN.value());
        }else if(outOfStockProducts.size() > 0){
            unsuccessfulOrderName =  String.join(",", outOfStockProducts);
            sendOrderUpdateNotification(OrderStatus.PLACED,"These items are out of stock : "+unsuccessfulOrderName,null,order.getUser().getToken());
        }
        for (ProductOrderDetails productWiseOrder : successfullyOrdredProduct) {
            CartDetails cartDetails1 = cartDetailsRepo.findCartDetailsByCartAndProduct(cart, productRepo.findById(productWiseOrder.getProductId()).get());
            cartDetailsRepo.delete(cartDetails1);
        }
        log.info("Order placed by user : {} "+order.getUser().getPhone());

        notifyAdmin(order.getUser().getUserName(),"New order arrived");
        sendOrderUpdateNotification(OrderStatus.PLACED,"Order placed",null,order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(order.getId()));
        String message = unsuccessfulOrderName != null ? "Order successfully place "+"but these items are out of stocks "+unsuccessfulOrderName: "Order placed successfully";
        webSocketMessageSender.notifyNewPlacedOrder(Constants.NEW_ORDER_TOPIC,webSocketMessageModel);
        return new GlobalResponse(message, HttpStatus.OK.value(), true,productWiseOrders);
    }

    private void notifyAdmin(String buyerName,String message)  {
        try{
            List<String> adminUserList = userRepo.findToken(ADMIN.name());
            Map<String,String> map = new HashMap<>();
            map.put("buyer", buyerName);
            map.put("message", message);
            firebasePushNotificationService.sendBulkPushMessage(map,adminUserList);

        }catch (Exception e){
            log.error(e.getMessage());
        }

    }



    private String getSellerToken(Long sellerId) {
        if(null != sellerId){
            User user = userRepo.getById(sellerId);
            return user.getToken();
        }else{
            return null;
        }

    }


    @Transactional
    public List<OrderRS> getOrderDetails(String token, List<OrderStatus> statusList) {
         User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));

        List<String> list = statusList.stream().map(status -> status.toString()).collect(Collectors.toList());
        List<Order> orders = orderRepo.findOrderByUser(user);
        List<OrderRS> orderRsList = new ArrayList<>();

        for (Order order : orders) {

            if (statusList.contains(order.getOrderStatus())) {
                OrderRS orderRS = new OrderRS();
                orderRS.setUser(order.getUser());
                if(statusList.contains(OrderStatus.DELIVERED)){
                    orderRS.setIsNew(false);
                }
                orderRS.setOrderDate(order.getOrderDate());
                orderRS.setOrderStatus(order.getOrderStatus());
                Address address = null;
//                Address address = addressRepo.findAddressByUserAndIsDefault(order.getUser(), true);
                orderRS.setAddressModel(convertIntoAddressModel(address));
                List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
                Double totalCost = 0.00;
                List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
                for (OrderDetails productDelivery : productDeliveries) {
                    ProductOrderDetails deliveryProductDetails = new ProductOrderDetails();
                    deliveryProductDetails.setDeliveryAgentDetails("Rajeev");
                    deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                    deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
                    deliveryProductDetails.setOrderStatus(productDelivery.getOrderStatus());
                    deliveryProductDetails.setPrice(productDelivery.getItemPrice());
                    deliveryProductDetails.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                    deliveryProductList.add(deliveryProductDetails);
                }
                orderRS.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
                orderRS.setOrderId(order.getId());
                orderRS.setDeliveryProducts(deliveryProductList);
                orderRS.setTotalCost(order.getTotalCost());
                orderRS.setDeliveredAt(order.getOrderDeliveredAt());
                orderRS.setLastModifiedDate(order.getModifiedDate());
                orderRsList.add(orderRS);
            }

        }

        Collections.sort(orderRsList, new Comparator<OrderRS>() {
            @Override
            public int compare(OrderRS o1, OrderRS o2) {
                return o2.getLastModifiedDate().compareTo(o1.getLastModifiedDate());
            }
        });
        return orderRsList;

    }


    public List<ProductOrderDetails> getOrderListByUser(String token) {
        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
        List<Order> orders = this.orderRepo.findOrderByUser(user);
        List<ProductOrderDetails> productWiseOrders = new ArrayList<>();

        for (Order order : orders) {
            List<OrderDetails> productsDeliveryList = orderDetailsRepository.findProductDeliveryByOrder(order);
            for (OrderDetails productDelivery : productsDeliveryList) {
                ProductOrderDetails productWiseOrder = new ProductOrderDetails();
                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm");
//                    String deliveryDate = formatter.format(productDelivery.getDeliveryDate());
                    String orderDate = formatter.format(productDelivery.getOrder().getOrderDate());
//                    productWiseOrder.setDeliveryDate(deliveryDate);
                    productWiseOrder.setOrderDate(orderDate);
                } catch (Exception e) { // won't happen here
                    System.err.println("Invalid date");
                }
                productWiseOrder.setProductDeliveryId(productDelivery.getId());
                productWiseOrder.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                productWiseOrder.setProductId(productDelivery.getProduct().getId());
                productWiseOrder.setProductName(productDelivery.getProduct().getName());
                productWiseOrder.setOrderStatus(productDelivery.getOrderStatus());
//                productWiseOrder.setPrice(productDelivery.getOrderedTotalCount() * productDelivery.getProduct().getSellingPrice());
                productWiseOrder.setDeliveryAgentDetails("Rajeev Kumar, Mobile - 9878979798");
//                productWiseOrder.setTotalProductCount(productDelivery.getOrderedTotalCount());
//                Address address = productDelivery.getAddress();
//                String completeAddress = address.getLandmark() + ", " + address.getAddressOne() + ", " + address.getArea() + ", " + address.getCity() + "-".concat(address.getPincode())
//                        + ", Mobile - " + address.getMobile();

                productWiseOrder.setOrderId(order.getId());
//                productWiseOrder.setCompleteAddress(completeAddress);
                productWiseOrders.add(productWiseOrder);
            }
        }

        return productWiseOrders;
    }

    public Boolean cancelOrder(Long id, User user) {
        try {
            Order order = orderRepo.getById(id);
            List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
            order.setOrderStatus(OrderStatus.CANCELED);
            productDeliveries.forEach(productDelivery -> productDelivery.setOrderStatus(OrderStatus.CANCELED));
            orderDetailsRepository.saveAll(productDeliveries);
            order.setModifiedDate(new Date());
            orderRepo.save(order);
            for (OrderDetails productDelivery : productDeliveries) {
//                updateProductInventory(productDelivery.getProduct(), productDelivery.getOrderedTotalCount());
            }
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(id));
            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean packingOrder(Long id) {

        Order order = orderRepo.getById(id);
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        productDeliveries.stream().forEach(p -> p.setOrderStatus(OrderStatus.PACKING));
        orderDetailsRepository.saveAll(productDeliveries);
        order.setOrderStatus(OrderStatus.PACKING);
        order.setOrderDeliveredAt(new Date());
        order.setModifiedDate(new Date());
        orderRepo.save(order);
        sendOrderUpdateNotification(OrderStatus.PACKING,"Order accepted",null,order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(id));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);

        return true;
    }

    private void sendOrderUpdateNotification(OrderStatus status , String message, String image,String token) {
       try{
           firebasePushNotificationService.sendPushMessage(prepareNotificationData(status,message,image,token));
       }catch ( Exception e){
            e.printStackTrace();
       }


    }

    private Map<String,String> prepareNotificationData(OrderStatus status , String message, String image,String token){
        Map<String, String> data = new HashMap<>();
        data.put("order_status", status.name());
        data.put("title", "MELAA | Grocery App");
        data.put("text", message);
        data.put("token", token);
        data.put("image", "image");
        return data;
    }
    public Boolean markedDelivered(Long id) {
        Order order = orderRepo.getById(id);
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        productDeliveries.stream().forEach(p -> p.setOrderStatus(OrderStatus.DELIVERED));
        orderDetailsRepository.saveAll(productDeliveries);
        order.setOrderDeliveredAt(new Date());
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setModifiedDate(new Date());
        orderRepo.save(order);
        sendOrderUpdateNotification(OrderStatus.DELIVERED,"Order is delivered on time",null,order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(id));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);

        return true;
    }

    public Boolean updateOnTheWay(Long id) {
        Order order = orderRepo.getById(id);
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        productDeliveries.stream().forEach(p -> p.setOrderStatus(OrderStatus.ON_THE_WAY));
        orderDetailsRepository.saveAll(productDeliveries);
        order.setOrderStatus(OrderStatus.ON_THE_WAY);
        order.setModifiedDate(new Date());
        orderRepo.save(order);
        sendOrderUpdateNotification(OrderStatus.ON_THE_WAY,"Order is on the way",null,order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        String name = "ON_THE_WAY-"+id;
        webSocketMessageModel.setName(String.valueOf(id));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
        return true;
    }

    public void updateProductInventory(Product product, Integer restoreCount) {
        Stock stock = instockRepo.findStockByProduct(product);
        Integer inStock = stock.getInStock() + restoreCount;
        stock.setInStock(inStock);
        instockRepo.save(stock);
    }

    // for admin
    @Transactional
    public List<OrderRS> fetchAllOrderByStatus(String token, OrderStatus status) {
        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
        List<UserRole> roles = user.getRoles();
        if (roles.contains(ADMIN)) {
            List<Order> orders = orderRepo.findOrderByOrderStatus(status);
            List<OrderRS> orderDetailsModelList = new ArrayList<>();
            for (Order order : orders) {
                OrderRS orderDetailsModel = new OrderRS();

                orderDetailsModel.setUser(order.getUser());
                orderDetailsModel.setOrderDate(order.getOrderDate());
                orderDetailsModel.setOrderStatus(order.getOrderStatus());
                Address address = null;
//                Address address = addressRepo.findAddressByUserAndIsDefault(order.getUser(), true);
                orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
                List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
                Double totalCost = 0.00;
                List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrderAndOrderStatus(order, status);
                for (OrderDetails productDelivery : productDeliveries) {
                    ProductOrderDetails deliveryProductDetails = new ProductOrderDetails();
                    deliveryProductDetails.setDeliveryAgentDetails("Rajeev");
                    deliveryProductDetails.setTotalProductCount(productDelivery.getQuantity());
//                    deliveryProductDetails.setDeliveryDate(productDelivery.getDeliveryDate().toString());
                    deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                    deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
                    deliveryProductDetails.setOrderStatus(productDelivery.getOrderStatus());
//                    deliveryProductDetails.setTotalProductCount(productDelivery.getOrderedTotalCount());
//                    totalCost = totalCost + productDelivery.getQuantity().getPrice();
                    deliveryProductDetails.setPrice(productDelivery.getItemPrice());
                    deliveryProductDetails.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                    deliveryProductDetails.setPrice(productDelivery.getItemPrice());
                    deliveryProductList.add(deliveryProductDetails);
                }
                orderDetailsModel.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
                orderDetailsModel.setOrderId(order.getId());
                orderDetailsModel.setLastModifiedDate(order.getModifiedDate());
                orderDetailsModel.setDeliveryProducts(deliveryProductList);
                orderDetailsModel.setTotalCost(order.getTotalCost());
                orderDetailsModelList.add(orderDetailsModel);
            }

            Collections.sort(orderDetailsModelList, new Comparator<OrderRS>() {
                @Override
                public int compare(OrderRS o1, OrderRS o2) {
                    return o2.getLastModifiedDate().compareTo(o1.getLastModifiedDate());
                }
            });

            return orderDetailsModelList;
        } else {
            return null;
        }
    }

    private AddressModel convertIntoAddressModel(Address address) {
        AddressModel addressModel = new AddressModel();
//        addressModel.setAddressOne(address.getAddressOne());
//        addressModel.setArea(address.getArea());
//        addressModel.setCity(address.getCity());
//        addressModel.setMobile(address.getMobile());
//        addressModel.setId(address.getId());
//        addressModel.setIsDefault(address.getIsDefault());
//        addressModel.setLandmark(address.getLandmark());
//        addressModel.setPincode(address.getPincode());
//        addressModel.setMobile(address.getMobile());
        return addressModel;
    }

    private Quantity getQuantityEntity(QuantityModel model){
        return quantityRepo.getById(model.getId());
    }

    public GlobalResponse returnRequest(Long id, User user) {
        Order order = null;
        try {
            order = orderRepo.getById(id);
            Instant start = order.getOrderDate().toInstant();
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
            Integer durationInHr = Math.toIntExact(timeElapsed.toMillis() / (1000 * 60 * 60));
            if (durationInHr > 18) {
                log.error("Order can not be return because its {} hr old ", durationInHr);
                return new GlobalResponse("This order is too old", HttpStatus.CONFLICT.value());
            } else {
                List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
                order.setOrderStatus(OrderStatus.RETURN_INITIATED);
                order.setModifiedDate(new Date());

                productDeliveries.forEach(productDelivery -> productDelivery.setOrderStatus(OrderStatus.RETURN_INITIATED));
                orderDetailsRepository.saveAll(productDeliveries);
                orderRepo.save(order);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed return order by user {} due to {}", order.getUser().getPhone(),e.getMessage());
            return new GlobalResponse("Failed to initiate return : " + e.getMessage(), HttpStatus.CONFLICT.value());
        }

        notifyAdmin(order.getUser().getUserName(), "Return order request");
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(id));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
        return new GlobalResponse("Refund initiated", HttpStatus.CONFLICT.value());
    }

    public GlobalResponse doRefund(RefundOrder refundOrder) {
        Order order = orderRepo.getById(refundOrder.getOrderId());
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        order.setRefundTxnId(refundOrder.getRefundTxnId());
        order.setOrderStatus(OrderStatus.REFUNDED);
        productDeliveries.forEach(productDelivery -> productDelivery.setOrderStatus(OrderStatus.RETURN_INITIATED));
        orderDetailsRepository.saveAll(productDeliveries);
        orderRepo.save(order);
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(refundOrder.getOrderId()));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
        sendOrderUpdateNotification(OrderStatus.REFUNDED,"Payment refunded by Mela",null,order.getUser().getToken());
        return new GlobalResponse("Refund completed", HttpStatus.OK.value());
    }

    public GlobalResponse getOrderById(Long id) {
        Order order = orderRepo.getById(id);
        GlobalResponse globalResponse = new GlobalResponse();
       try{
           OrderRS orderDetailsModel = new OrderRS();
           orderDetailsModel.setUser(order.getUser());

           orderDetailsModel.setOrderDate(order.getOrderDate());
           orderDetailsModel.setOrderStatus(order.getOrderStatus());
           Address address = null;
//                Address address = addressRepo.findAddressByUserAndIsDefault(order.getUser(), true);
           orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
           List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
           Double totalCost = 0.00;
           List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
           for (OrderDetails productDelivery : productDeliveries) {
               ProductOrderDetails deliveryProductDetails = new ProductOrderDetails();
               deliveryProductDetails.setDeliveryAgentDetails("Rajeev");
               deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
               deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
               deliveryProductDetails.setOrderStatus(productDelivery.getOrderStatus());
               deliveryProductDetails.setPrice(productDelivery.getItemPrice());
               deliveryProductDetails.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
               deliveryProductList.add(deliveryProductDetails);
           }
           orderDetailsModel.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
           orderDetailsModel.setOrderId(order.getId());
           orderDetailsModel.setDeliveryProducts(deliveryProductList);
           orderDetailsModel.setTotalCost(order.getTotalCost());
           orderDetailsModel.setDeliveredAt(order.getOrderDeliveredAt());
           orderDetailsModel.setLastModifiedDate(order.getModifiedDate());
           globalResponse.setBody(orderDetailsModel);
           globalResponse.setMessage("Order fetched successfully");
           globalResponse.setStatus(true);
           globalResponse.setHttpStatusCode(HttpStatus.OK.value());
           return globalResponse;
       }catch (Exception e){

          log.error("Unable to fetch order by ID : "+id+" ERROR : "+e);
       }
       return new GlobalResponse("Failed to fetch order details", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }


}
