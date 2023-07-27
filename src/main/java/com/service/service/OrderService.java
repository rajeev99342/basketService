package com.service.service;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import com.service.constants.enums.UserRole;
import com.service.constants.values.Constants;
import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.repos.*;
import com.service.service.image.ImageServiceImpl;
import com.service.utilites.Payment;
import com.service.websocket.WebSocketMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    ImageServiceImpl imageService;

    @Autowired
    JwtTokenUtility jwtTokenUtility;

    @Autowired
    QuantityRepo quantityRepo;


    @Transactional
    public GlobalResponse placeOrder(OrderModel orderModel) throws Exception {
        Integer toBeOrder = orderModel.getCartProducts().size();
        List<ProductOrderDetails> productWiseOrders = new ArrayList<>();
        User user = userRepo.findUserByPhone(orderModel.getUserPhone());
        Address address = addressRepo.findAddressByUserId(user.getId());
        Order order = new Order();
        LocalDate localDate = LocalDate.now().plusDays(2);
        Date expectedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        order.setExpectedDeliveryDate(expectedDate);
        order.setUser(user);
        order.setLatitude(address.getLatitude());
        order.setLongitude(address.getLongitude());
        order.setOrderDate(new Date());
        order.setAddressLine(address.getAddressLine());
        order.setModifiedDate(new Date());
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentMode(PaymentModeEnum.CASE_ON_DELIVERY);
        order.setTotalCost(orderModel.getFinalAmount());
        List<Long> itemIds = new ArrayList<>();
        Integer totalSuccessfulPlacedOrder = 0;
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderRepo.save(order);
        List<String> outOfStockProducts = new ArrayList<>();
        List<Quantity> quantityList = new ArrayList<>();
        for (DisplayCartProduct cartProduct : orderModel.getCartProducts()) {
            ProductOrderDetails productWiseOrder = new ProductOrderDetails();
            try {
                // check for product availability --> someone could have order this
                Quantity quantity = quantityRepo.findById(cartProduct.getQuantityModel().getId()).get();
                if (quantity.getInStock() >= cartProduct.getSelectedCount()) {
                    OrderDetails orderDetails = new OrderDetails();
                    orderDetails.setProduct(productRepo.findById(cartProduct.getId()).get());
                    orderDetails.setOrder(order);
                    orderDetails.setItemPrice(cartProduct.getQuantityModel().getPrice());
                    orderDetails.setQuantity(cartProduct.getSelectedCount());
                    orderDetailsList.add(orderDetails);
                    orderDetails.setOrderStatus(OrderStatus.PLACED);
                    orderDetailsRepository.save(orderDetails);
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setPrice(cartProduct.getQuantityModel().getPrice());
                    quantity.setInStock(quantity.getInStock() - cartProduct.getSelectedCount());
                    quantityRepo.save(quantity);
                    quantityList.add(quantity);
                    itemIds.add(cartProduct.getCartDetailsId());
                    totalSuccessfulPlacedOrder++;
                } else {
                    LOG.error("Unable to process order by user " + order.getUser().getPhone() + " " + "[OUT OF STOCK]");
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setTotalProductCount(cartProduct.getSelectedCount());
                }

            } catch (Exception e) {
                LOG.error("Unable to process order by user " + order.getUser().getPhone(), e);
                productWiseOrder.setProductId(cartProduct.getId());
                throw new Exception(OrderStatus.FAILED_DUE_TO_TECHNICAL_ISSUE.name());
            }
            productWiseOrders.add(productWiseOrder);
        }


        if (totalSuccessfulPlacedOrder == 0) {
            orderRepo.delete(order);
            throw new Exception("OUT OF STOCK");
        }

        if (orderDetailsList.size() == 0) {
            orderRepo.delete(order);
            throw new Exception("NO PRODUCT SELECTED");
        }
        String unsuccessfulOrderName = null;
        if (totalSuccessfulPlacedOrder == 0) {
            log.error(">>>>>>>>> unable to order by User : {} => due to [ OUT OF STOCK ] ", user.getPhone());
            throw new Exception("ORDERED PRODUCTS ARE OUT OF STOCKS");
        } else if (toBeOrder > totalSuccessfulPlacedOrder) {
            log.info(">>>>>>>>> Some products [ OUT OF STOCK ] | User ==> {}", user.getPhone());
            unsuccessfulOrderName = String.join(",", outOfStockProducts);
            sendOrderUpdateNotification(OrderStatus.PLACED, "These items are out of stock : " + unsuccessfulOrderName, null, order.getUser().getToken());
        } else {
            log.error(">>>>>>>>> all products are [ OUT OF STOCK ] ", user.getPhone());
        }
        String orderDetailMessage = null;
        cartDetailsRepo.deleteCartDetailsByIDs(itemIds);
        log.info("Order placed by user : {} " + order.getUser().getPhone());
        notifyAdmin(order.getUser().getUserName(), "New order arrived " + orderDetailMessage);
        sendOrderUpdateNotification(OrderStatus.PLACED, "Order placed", null, order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(order.getId()));
        String message = unsuccessfulOrderName != null ? "Order successfully place " + "but these items are out of stocks " + unsuccessfulOrderName : "Order placed successfully";
        webSocketMessageSender.notifyNewPlacedOrder(Constants.NOTIFY_ONLY_ADMIN, webSocketMessageModel);
        return new GlobalResponse(message, HttpStatus.OK.value(), true, null);
    }

    private void notifyAdmin(String buyerName, String message) {
        try {
            List<String> adminUserList = userRepo.findToken(ADMIN.name());
            Map<String, String> map = new HashMap<>();
            map.put("buyer", buyerName);
            map.put("message", message);
            firebasePushNotificationService.sendBulkPushMessage(map, adminUserList);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }


    private String getSellerToken(Long sellerId) {
        if (null != sellerId) {
            User user = userRepo.getById(sellerId);
            return user.getToken();
        } else {
            return null;
        }

    }


    @Transactional
    public GlobalResponse getOrderDetails(String token, List<OrderStatus> statusList, String days, int page, int size) {
        try {
            Pageable pageable =
                    PageRequest.of(page, size, Sort.by("order_date").descending());

            int olderDays = findDays(days);
            User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
            Address address = addressRepo.findAddressByUserId(user.getId());
            List<Order> orders = orderRepo.findOrderByUser(user.getId(), pageable);
            List<OrderRS> orderRsList = new ArrayList<>();
            for (Order order : orders) {
                if (statusList.contains(order.getOrderStatus())) {
                    OrderRS orderRS = new OrderRS();
                    orderRS.setDeliveryAgent(order.getDeliveryAgent());
                    if (statusList.contains(OrderStatus.DELIVERED)) {
                        orderRS.setIsNew(false);
                    }
                    orderRS.setOrderDate(order.getOrderDate());
                    orderRS.setOrderStatus(order.getOrderStatus());
                    orderRS.setAddressModel(convertIntoAddressModel(address));
                    List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
                    List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder_Id(order.getId());
                    for (OrderDetails productDelivery : productDeliveries) {
                        ProductOrderDetails deliveryProductDetails = new ProductOrderDetails();
                        deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                        deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
                        deliveryProductDetails.setPrice(productDelivery.getItemPrice());
                        deliveryProductDetails.setTotalProductCount(productDelivery.getQuantity());
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

            return GlobalResponse.getSuccess(orderRsList);
        } catch (Exception e) {
            log.error("Failed due to {}", e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }
    }


    public Boolean cancelOrder(Long id, User user) {
        try {
            Order order = orderRepo.getById(id);
            List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
            order.setOrderStatus(OrderStatus.CANCELED);
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
            log.error(">>> Failed to canceled order EX : {} | {} | Order ID : {}", e.getLocalizedMessage(), user.getPhone(), id);
        }

        return false;
    }

    public GlobalResponse packingOrder(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            if (updateOrderRs.getTime() != 100) {
                Date newD = Date.from(new Date().toInstant().plusSeconds(updateOrderRs.getTime() * 60 * 60));
                order.setExpectedDeliveryDate(newD);
                order.setDeliveryAgent(userRepo.findUserByPhone(updateOrderRs.getAgent()));
                order.setOrderStatus(OrderStatus.ACCEPTED);
            } else {
                order.setOrderStatus(OrderStatus.CANCELED);
            }
            List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
            orderDetailsRepository.saveAll(productDeliveries);
            order.setOrderDeliveredAt(new Date());
            order.setModifiedDate(new Date());
            orderRepo.save(order);
            sendOrderUpdateNotification(OrderStatus.ACCEPTED, "Order accepted", null, order.getUser().getToken());
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");

    }

    private void sendOrderUpdateNotification(OrderStatus status, String message, String image, String token) {
        try {
            firebasePushNotificationService.sendPushMessage(prepareNotificationData(status, message, image, token));
        } catch (Exception e) {
            log.error(">>> Failed to send notification EX : {}", e.getLocalizedMessage());
        }


    }

    private Map<String, String> prepareNotificationData(OrderStatus status, String message, String image, String token) {
        Map<String, String> data = new HashMap<>();
        data.put("order_status", status.name());
        data.put("title", "MELAA | Grocery App");
        data.put("text", message);
        data.put("token", token);
        data.put("image", "image");
        return data;
    }

    public GlobalResponse markedDelivered(Long id) {
        Order order = orderRepo.getById(id);
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        orderDetailsRepository.saveAll(productDeliveries);
        order.setOrderDeliveredAt(new Date());
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setModifiedDate(new Date());
        orderRepo.save(order);
        sendOrderUpdateNotification(OrderStatus.DELIVERED, "Order is delivered on time", null, order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(id));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
        return GlobalResponse.getSuccess(true);
    }

    public GlobalResponse updateOnTheWay(Long id) {
        Order order = orderRepo.getById(id);
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        orderDetailsRepository.saveAll(productDeliveries);
        order.setOrderStatus(OrderStatus.ON_THE_WAY);
        order.setModifiedDate(new Date());
        orderRepo.save(order);
        sendOrderUpdateNotification(OrderStatus.ON_THE_WAY, "Order is on the way", null, order.getUser().getToken());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        String name = "ON_THE_WAY-" + id;
        webSocketMessageModel.setName(String.valueOf(id));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
        return GlobalResponse.getSuccess(true);

    }

    public void updateProductInventory(Product product, Integer restoreCount) {
        Stock stock = instockRepo.findStockByProduct(product);
        Integer inStock = stock.getInStock() + restoreCount;
        stock.setInStock(inStock);
        instockRepo.save(stock);
    }

    // for admin
    @Transactional
    public GlobalResponse fetchAllOrderByStatus(String token, OrderStatus status, Pageable pageable) {
        try {
            User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
            List<UserRole> roles = user.getRoles();
            if (roles.contains(ADMIN)) {
                List<Order> orders = orderRepo.findOrderByOrderStatus(status, pageable);
                List<OrderRS> orderDetailsModelList = new ArrayList<>();
                for (Order order : orders) {
                    OrderRS orderDetailsModel = new OrderRS();
                    orderDetailsModel.setUser(order.getUser());
                    orderDetailsModel.setOrderDate(order.getOrderDate());
                    orderDetailsModel.setLatitude(order.getLatitude());
                    orderDetailsModel.setLongitude(order.getLongitude());
                    orderDetailsModel.setAddressLine(order.getAddressLine());
                    orderDetailsModel.setOrderStatus(order.getOrderStatus());
                    Address address = null;
                    orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
                    List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
                    Double totalCost = 0.00;
                    List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
                    for (OrderDetails productDelivery : productDeliveries) {
                        ProductOrderDetails deliveryProductDetails = new ProductOrderDetails();
                        deliveryProductDetails.setTotalProductCount(productDelivery.getQuantity());
                        deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                        deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
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

//                Collections.sort(orderDetailsModelList, new Comparator<OrderRS>() {
//                    @Override
//                    public int compare(OrderRS o1, OrderRS o2) {
//                        return o2.getLastModifiedDate().compareTo(o1.getLastModifiedDate());
//                    }
//                });

                return GlobalResponse.getSuccess(orderDetailsModelList);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to fetch order due to {} ", e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
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

    private Quantity getQuantityEntity(QuantityModel model) {
        return quantityRepo.getById(model.getId());
    }

    public GlobalResponse returnRequest(Long id, User user) {
        Order order = null;
        try {
            String message = null;
            order = orderRepo.getById(id);
            Instant start = order.getOrderDate().toInstant();
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            Integer durationInHr = Math.toIntExact(timeElapsed.toMillis() / (1000 * 60 * 60));
            if (durationInHr > 2) {
                log.error("Order can not be return because its {} hr old ", durationInHr);
                return new GlobalResponse("This order is too old", HttpStatus.CONFLICT.value());
            } else {
                order.setModifiedDate(new Date());
                if(order.getPaymentMode().equals(PaymentModeEnum.CASE_ON_DELIVERY)){
                    order.setOrderStatus(OrderStatus.CANCELED);
                    message = OrderStatus.CANCELED.name();
                }else{
                    order.setOrderStatus(OrderStatus.RETURN_INITIATED);
                    message = OrderStatus.RETURN_INITIATED.name();
                }
//                orderDetailsRepository.saveAll(productDeliveries);
                orderRepo.save(order);
            }
            notifyAdmin(order.getUser().getUserName(), "Return order request");
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(id));
//        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return new GlobalResponse(message, HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed return order by user {} due to {}", order.getUser().getPhone(), e.getMessage());
            return new GlobalResponse("Failed to initiate return : " + e.getMessage(), HttpStatus.CONFLICT.value());
        }


    }

    public GlobalResponse doRefund(RefundOrder refundOrder) {
        Order order = orderRepo.getById(refundOrder.getOrderId());
        List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
        order.setRefundTxnId(refundOrder.getRefundTxnId());
        order.setOrderStatus(OrderStatus.REFUNDED);
        orderDetailsRepository.saveAll(productDeliveries);
        orderRepo.save(order);
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(refundOrder.getOrderId()));
        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
        sendOrderUpdateNotification(OrderStatus.REFUNDED, "Payment refunded by Mela", null, order.getUser().getToken());
        return new GlobalResponse("Refund completed", HttpStatus.OK.value());
    }

    public GlobalResponse getOrderById(Long id) {
        Order order = orderRepo.getById(id);
        GlobalResponse globalResponse = new GlobalResponse();
        try {
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
                deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
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
        } catch (Exception e) {

            log.error("Unable to fetch order by ID : " + id + " ERROR : " + e);
        }
        return new GlobalResponse("Failed to fetch order details", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private int findDays(String val) {
        switch (val) {
            case "today":
                return 0;
            case "1 day older":
                return 1;
            case "2 day older":
                return 2;
            case "3 day older":
                return 3;
            case "4 day older":
                return 4;
            case "5 day older":
                return 5;
            default:
                return 100;
        }
    }

    public GlobalResponse fetchAllOrderByStatus(String token, List<OrderStatus> statuses, Pageable pageable) {
        try {
            User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
            List<UserRole> roles = user.getRoles();
            if (roles.contains(ADMIN)) {
                List<Order> orders = orderRepo.findOrderByOrderStatusIn(statuses, pageable);
                List<OrderRS> orderDetailsModelList = new ArrayList<>();
                for (Order order : orders) {
                    OrderRS orderDetailsModel = new OrderRS();
                    orderDetailsModel.setUser(order.getUser());
                    orderDetailsModel.setOrderDate(order.getOrderDate());
                    orderDetailsModel.setLatitude(order.getLatitude());
                    orderDetailsModel.setLongitude(order.getLongitude());
                    orderDetailsModel.setAddressLine(order.getAddressLine());
                    orderDetailsModel.setOrderStatus(order.getOrderStatus());
                    Address address = null;
                    orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
                    List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
                    Double totalCost = 0.00;
                    List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
                    for (OrderDetails productDelivery : productDeliveries) {
                        ProductOrderDetails deliveryProductDetails = new ProductOrderDetails();
                        deliveryProductDetails.setTotalProductCount(productDelivery.getQuantity());
                        deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                        deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
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

//                Collections.sort(orderDetailsModelList, new Comparator<OrderRS>() {
//                    @Override
//                    public int compare(OrderRS o1, OrderRS o2) {
//                        return o2.getLastModifiedDate().compareTo(o1.getLastModifiedDate());
//                    }
//                });

                return GlobalResponse.getSuccess(orderDetailsModelList);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to fetch order due to {} ", e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }
    }


    public GlobalResponse sendToShop(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            if (updateOrderRs.getTime() != 100) {
                order.setDeliveryAgent(userRepo.findUserByPhone(updateOrderRs.getAgent()));
                order.setOrderStatus(OrderStatus.SEND_TO_SHOP);
            } else {
                order.setOrderStatus(OrderStatus.CANCELED);
            }
            User user = userRepo.findUserByPhone(updateOrderRs.getAgent());
            List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
            orderDetailsRepository.saveAll(productDeliveries);
            order.setOrderDeliveredAt(new Date());
            order.setModifiedDate(new Date());
            orderRepo.save(order);
            Map<String, String> data = new HashMap<>();
            data.put("order_status", "New order receive");
            data.put("title", "MELAA | Grocery App");
            data.put("token", user.getToken());
            firebasePushNotificationService.sendPushMessage(data );
//            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
//            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
//            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");

    }
}
