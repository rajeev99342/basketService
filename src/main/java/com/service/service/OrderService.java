package com.service.service;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import com.service.constants.enums.UserRole;
import com.service.constants.values.Constants;
import com.service.constants.values.Images;
import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.model.seller.SellerDetailModel;
import com.service.repos.*;
import com.service.service.image.ImageServiceImpl;
import com.service.utilites.Payment;
import com.service.utilites.SellerModelConvertor;
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
import static com.service.constants.enums.UserRole.SELLER;

@Service
@Slf4j
public class OrderService {

    @Autowired
    SellerModelConvertor sellerModelConvertor;
    @Autowired
    SellerDetailsRepo sellerDetailsRepo;
    @Autowired
    OrderSellerRepo orderSellerRepo;

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
        LocalDate localDate = LocalDate.now().plusDays(1);
        Date expectedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        order.setExpectedDeliveryDate(expectedDate);
        order.setUser(user);
        order.setLatitude(address.getLatitude());
        order.setLongitude(address.getLongitude());
        order.setOrderDate(new Date());
        order.setAddressLine(address.getAddressLine());
        order.setLandmark(address.getLandmark());
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
        String orderDetailMessage = "";

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
                    orderDetailMessage = orderDetailMessage + orderDetails.getProduct().getName() + "-"+orderDetails.getQuantity()+" | ";
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
            log.error("----------->> >>>>>>>>> unable to order by User : {} => due to [ OUT OF STOCK ] ", user.getPhone());
            throw new Exception("ORDERED PRODUCTS ARE OUT OF STOCKS");
        } else if (toBeOrder > totalSuccessfulPlacedOrder) {
            log.info(">>>>>>>>>>>>>> >>>>>>>>> Some products [ OUT OF STOCK ] | User ==> {}", user.getPhone());
            unsuccessfulOrderName = String.join(",", outOfStockProducts);
            sendOrderUpdateNotification(OrderStatus.PLACED, "These items are out of stock : " + unsuccessfulOrderName, null, order.getUser().getToken());
        } else {
        }
        cartDetailsRepo.deleteCartDetailsByIDs(itemIds);
        log.info(">>>>>>>>>>>>>> Order placed by user : {} " + order.getUser().getPhone());
        List<String> tokens = getTokens(ADMIN);
        if(tokens.size() > 0){
            firebasePushNotificationService.sendBulkPushMessage(prepareMap(orderDetailMessage, user.getUserName()),tokens);
        }
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName(String.valueOf(order.getId()));
        String message = unsuccessfulOrderName != null ? "Order successfully place " + "but these items are out of stocks " + unsuccessfulOrderName : "Order placed successfully";
        webSocketMessageSender.notifyNewPlacedOrder(Constants.NOTIFY_ONLY_ADMIN, webSocketMessageModel);
        return new GlobalResponse(message, HttpStatus.OK.value(), true, null);
    }

    private Map<String,String> prepareMap(String orderDetailMessage,String buyer) {
        Map<String,String> map = new HashMap<>();
        map.put("title", "New Order Arrived - "+buyer);
        map.put("body", orderDetailMessage);
        map.put("image", Images.NEW_ORDER);
        return map;
    }

    private List<String>  getTokens(UserRole role) {
        List<String> tokens = null;
        try {
            List<User> userList = userRepo.findByRolesContains(ADMIN, null);
            tokens = userList.stream().map(user -> user.getToken()).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return tokens;
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
    public GlobalResponse getOrderDetails(String token, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("order_date").descending());
            User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
            Address address = addressRepo.findAddressByUserId(user.getId());
            List<Order> orders = orderRepo.findOrderByUser(user.getId(), pageable);
            List<OrderRS> orderRsList = new ArrayList<>();
            for (Order order : orders) {
                OrderRS orderRS = new OrderRS();
                orderRS.setDeliveryAgent(order.getDeliveryAgent());
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
            return GlobalResponse.getSuccess(orderRsList);
        } catch (Exception e) {
            log.error("----------->> Failed due to {}", e.getMessage());
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
            log.error("----------->> >>> Failed to canceled order EX : {} | {} | Order ID : {}", e.getLocalizedMessage(), user.getPhone(), id);
        }

        return false;
    }

    public GlobalResponse packingOrder(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            if (updateOrderRs.getTime() != 100) {
                Date newD = Date.from(new Date().toInstant().plusSeconds(updateOrderRs.getTime() * 60 * 60));
                order.setExpectedDeliveryDate(newD);
                order.setDeliveryAgent(userRepo.findUserByPhone(updateOrderRs.getSellerPhones().get(0)));
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
            log.error("----------->> Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");

    }

    private void sendOrderUpdateNotification(OrderStatus status, String message, String image, String token) {
        try {
            firebasePushNotificationService.sendPushMessage(prepareNotificationData(status, message, image, token));
        } catch (Exception e) {
            log.error("----------->> >>> Failed to send notification EX : {}", e.getLocalizedMessage());
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
                    orderDetailsModel.setLandmark(order.getLandmark());
                    orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
                    List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
                    Double totalCost = 0.00;
                    List<SellerDetailModel> sellerModelList = getSellerDetails(order);
                    orderDetailsModel.setSellerDetailModels(sellerModelList);
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
                    orderDetailsModel.setDeliveryAgent(order.getDeliveryAgent());
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
            log.error("----------->> Failed to fetch order due to {} ", e.getMessage());
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
                log.error("----------->> Order can not be return because its {} hr old ", durationInHr);
                return new GlobalResponse("This order is too old", HttpStatus.CONFLICT.value());
            } else {
                order.setModifiedDate(new Date());
                if (order.getPaymentMode().equals(PaymentModeEnum.CASE_ON_DELIVERY)) {
                    order.setOrderStatus(OrderStatus.CANCELED);
                    message = OrderStatus.CANCELED.name();
                } else {
                    order.setOrderStatus(OrderStatus.RETURN_INITIATED);
                    message = OrderStatus.RETURN_INITIATED.name();
                }
//                orderDetailsRepository.saveAll(productDeliveries);
                orderRepo.save(order);
            }
//            notifyAdmin(order.getUser().getUserName(), "Return order request");
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(id));
//        webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return new GlobalResponse(message, HttpStatus.OK.value());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("----------->> Failed return order by user {} due to {}", order.getUser().getPhone(), e.getMessage());
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

            log.error("----------->> Unable to fetch order by ID : " + id + " ERROR : " + e);
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
            log.error("----------->> Failed to fetch order due to {} ", e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }
    }


    @Transactional
    public GlobalResponse sendToShop(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            List<User> list = userRepo.findUserByPhoneIn(updateOrderRs.getSellerPhones());
            List<OrderDetails> orderDetails = orderDetailsRepository.findProductDeliveryByOrder(order);
            String productDetailedMessage = "";
            for(OrderDetails orderDetails1 : orderDetails){
                productDetailedMessage = productDetailedMessage+ orderDetails1.getProduct().getName() + "-"+orderDetails1.getQuantity() + "  |  ";
            }
            Map<String, String> data = new HashMap<>();
            data.put("title", "New order arrived");
            data.put("body", productDetailedMessage);
            data.put("image", Images.NEW_ORDER);
            order.setOrderStatus(updateOrderRs.getStatus());
            for (String phone : updateOrderRs.getSellerPhones()) {
                OrderSeller orderShopMapped = new OrderSeller();
                orderShopMapped.setOrder(order);
                orderShopMapped.setSellerPhone(phone);
                orderShopMapped.setOrderStatus(updateOrderRs.getStatus());
                orderShopMapped.setBuyerPhone(updateOrderRs.getBuyer());
                orderSellerRepo.save(orderShopMapped);
            }

            try {
                List<String> tokens = getTokens(SELLER);
                firebasePushNotificationService.sendBulkPushMessage(prepareMap(productDetailedMessage, "Melaa"),tokens);
            } catch (Exception e) {
                log.error("----------->> Failed to send notification to sellers due to {} ", e.getLocalizedMessage());
            }
//            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
//            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
//            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            log.info(">>>>>>>>>>>>>>  Order request send to seller");
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("----------->> Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");

    }

    public GlobalResponse getAllOrderFromShops(OrderStatus orderStatusFromShop, String seller, Pageable pageable) {
        List<Order> orderList = orderRepo.findOrderFromShops(seller, orderStatusFromShop.name(), pageable);
        log.info("Order fetched by status : {} ", orderStatusFromShop);
        return getProductList(orderList);
    }

    public GlobalResponse getAllOrderFromShopsForAdmin(OrderStatus orderStatusFromShop, Pageable pageable) {
        List<Order> orderList = orderRepo.findOrderFromShopsForAdmin(orderStatusFromShop.name(), pageable);
        log.info("Order fetched by status : {} ", orderStatusFromShop);
        return getProductList(orderList);
    }


    private GlobalResponse getProductList(List<Order> orderList) {
        List<OrderRS> orderDetailsModelList = new ArrayList<>();
        for (Order order : orderList) {
            OrderRS orderDetailsModel = new OrderRS();
            orderDetailsModel.setUser(order.getUser());
            orderDetailsModel.setOrderDate(order.getOrderDate());
            orderDetailsModel.setLatitude(order.getLatitude());
            orderDetailsModel.setLongitude(order.getLongitude());
            orderDetailsModel.setAddressLine(order.getAddressLine());
            orderDetailsModel.setOrderStatus(order.getOrderStatus());
            orderDetailsModel.setLandmark(order.getLandmark());
            Address address = null;
            orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
            List<ProductOrderDetails> deliveryProductList = new ArrayList<>();
            Double totalCost = 0.00;
            List<SellerDetailModel> sellerModelList = getSellerDetails(order);
            List<OrderDetails> productDeliveries = orderDetailsRepository.findProductDeliveryByOrder(order);
            orderDetailsModel.setSellerDetailModels(sellerModelList);
            orderDetailsModel.setLandmark(order.getLandmark());
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
        return GlobalResponse.getSuccess(orderDetailsModelList);
    }


    public GlobalResponse updateOrderBySeller(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            List<User> list = userRepo.findUserByPhoneIn(updateOrderRs.getSellerPhones());
            List<String> tokens = list.stream().map(user -> user.getToken()).collect(Collectors.toList());
            Map<String, String> data = new HashMap<>();
            data.put("order_status", "New order received");
            data.put("title", "MELAA | Grocery App");
            if(updateOrderRs.getStatus().equals(OrderStatus.CONFIRMED_FROM_SELLER)){
                List<OrderSeller> orderSeller = orderSellerRepo.findAllByOrder_Id(updateOrderRs.getOrderId());
                List<OrderSeller> updatedSellerList =  orderSeller.stream().map(o->{
                    if(o.getSellerPhone().equals(updateOrderRs.getSellerPhones().get(0))){
                        o.setOrderStatus(OrderStatus.CONFIRMED_FROM_SELLER);
                    }else{
                        o.setOrderStatus(OrderStatus.CANCELED);
                    }
                    return o;
                }).collect(Collectors.toList());
                orderSellerRepo.saveAll(updatedSellerList);
            }else{
                OrderSeller orderSeller = orderSellerRepo.findBySellerPhoneByOrderId(updateOrderRs.getSellerPhones().get(0), updateOrderRs.getOrderId());
                orderSeller.setOrderStatus(updateOrderRs.getStatus());
                orderSellerRepo.save(orderSeller);
            }
            order.setOrderStatus(updateOrderRs.getStatus());

            orderRepo.save(order);


            try {
                firebasePushNotificationService.notifyAllAdmin(tokens, updateOrderRs.getStatus().name(), null);
            } catch (Exception e) {
                log.error("----------->> Failed to send notification to sellers due to {} ", e.getLocalizedMessage());
            }
//            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
//            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
//            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            log.info(">>>>>>>>>>>>>>  Order request send to seller");
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("----------->> Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");

    }

    public GlobalResponse getOrderListToDeliver(OrderStatus status, String deliveryPhone, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("order_date").descending());
        User user = userRepo.findUserByPhone(deliveryPhone);
        if(user.getRoles().contains(UserRole.DELIVERY)){
            List<Order> orderList = orderRepo.findAllOrderByDelivery(status.name(), user.getId());
            return getProductList(orderList);
        }else{
            log.error("User is not delivery agent : {} ",user.getPhone());
        }
        return null;
    }

    public GlobalResponse assignToDelivery(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            if (updateOrderRs.getTime() != 100) {
                Date newD = Date.from(new Date().toInstant().plusSeconds(updateOrderRs.getTime() * 60 * 60));
                order.setExpectedDeliveryDate(newD);
                order.setDeliveryAgent(userRepo.findUserByPhone(updateOrderRs.getDeliveryAgent()));
                order.setOrderStatus(OrderStatus.ACCEPTED);
            } else {
                order.setOrderStatus(OrderStatus.CANCELED);
            }
            order.setModifiedDate(new Date());
            orderRepo.save(order);
            Map<String,String> data = new HashMap<>();
            data.put("title", "New order arrived | Seller - "+updateOrderRs.getSellerPhones().stream().collect(Collectors.toList()));
            data.put("body", "Buyer - "+updateOrderRs.getBuyer());
            data.put("image", Images.ORDER_ACCEPTED);
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
            User deliveryAgent = userRepo.findUserByPhone(updateOrderRs.getDeliveryAgent());
            List<String> tokens = new ArrayList<>();
            tokens.add(deliveryAgent.getToken());
            firebasePushNotificationService.sendBulkPushMessage(data,tokens);
            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("----------->> Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");
    }

    private List<SellerDetailModel> getSellerDetails(Order order) {
        List<SellerDetails> sellerDetailsList = null;
        if(order.getOrderStatus().equals(OrderStatus.CONFIRMED_FROM_SELLER)){
            sellerDetailsList = sellerDetailsRepo.sellerDetailsByOrderId(order.getId());

//             sellerDetailsList = sellerDetailsRepo.sellerDetailsByOrderIdAndStatus(order.getId(),OrderStatus.CONFIRMED_FROM_SELLER.name());
        }else{
            sellerDetailsList = sellerDetailsRepo.sellerDetailsByOrderId(order.getId());
        }
        return sellerModelConvertor.CONVERT_INTO_MODEL.apply(sellerDetailsList);
    }

    public GlobalResponse markedDeliveryDoneStatus(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            order.setOrderStatus(updateOrderRs.getStatus());
            order.setModifiedDate(new Date());
            order.setOrderDeliveredAt(new Date());
            orderRepo.save(order);
            sendOrderUpdateNotification(OrderStatus.ACCEPTED, "Order accepted", null, order.getUser().getToken());
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("----------->> Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");
    }

    public GlobalResponse markedOrderOnTheWay(UpdateOrderRs updateOrderRs) {
        try {
            Order order = orderRepo.getById(updateOrderRs.getOrderId());
            order.setOrderStatus(updateOrderRs.getStatus());
            order.setModifiedDate(new Date());
            order.setOrderDeliveredAt(new Date());
            orderRepo.save(order);
            sendOrderUpdateNotification(OrderStatus.ACCEPTED, "Order accepted", null, order.getUser().getToken());
            WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
            webSocketMessageModel.setName(String.valueOf(updateOrderRs.getOrderId()));
            webSocketMessageSender.notifyUpdateOrderToUser(order.getUser().getPhone(), "/topic/order/update/", webSocketMessageModel);
            return GlobalResponse.getSuccess(true);
        } catch (Exception e) {
            log.error("----------->> Failed to update order due to {}", e.getMessage());
        }

        return GlobalResponse.getFailure("Failed");
    }
}
