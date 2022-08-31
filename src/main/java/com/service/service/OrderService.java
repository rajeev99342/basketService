package com.service.service;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.Role;
import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.repos.*;
import com.service.utilites.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.service.constants.enums.Role.MASTER;

@Service
public class OrderService {

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
    ProductDeliveryRepo productDeliveryRepo;

    @Autowired
    ImageService imageService;

    @Autowired
    JwtTokenUtility jwtTokenUtility;

    public List<DeliveryProductDetails> placeOrder(OrderModel orderModel) {
        List<DeliveryProductDetails> productWiseOrders = new ArrayList<>();
        User user = userRepo.findUserByPhone(orderModel.getUserPhone());
        Order order = new Order();
        order.setOrderDate(new Date(System.currentTimeMillis()));
        LocalDate localDate = LocalDate.now().plusDays(2);
        Date expectedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        order.setExpectedDeliveryDate(expectedDate);
        order.setOrderStatus(orderModel.getOrderStatus());
        order.setPaymentMode(payment.getPaymentMode(orderModel.getPaymentMode().getMethod()));
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setTotalCost(orderModel.getFinalAmount());
        orderRepo.save(order);

        for (DisplayCartProduct cartProduct : orderModel.getCartProducts()) {
            DeliveryProductDetails productWiseOrder = new DeliveryProductDetails();

            try {
                // check for product availability --> someone could have order this

                Stock stock = instockRepo.findStockByProduct(productRepo.getById(cartProduct.getId()));
                Integer inStock = stock.getInStock();
                if (inStock >= cartProduct.getSelectedCount() && cartProduct.getSelectedCount() != 0) {
                    Integer availableStockAfterThisOrder = inStock - cartProduct.getSelectedCount();
                    stock.setInStock(availableStockAfterThisOrder);
                    instockRepo.save(stock);
                    ProductDelivery productDelivery = new ProductDelivery();
                    productDelivery.setOrderStatus(OrderStatus.PLACED);
                    productDelivery.setProduct(productRepo.getById(cartProduct.getId()));
                    productDelivery.setDeliveryDate(new Date(System.currentTimeMillis() + 86400000));
                    productDelivery.setOrder(order);
                    productDelivery.setOrderedTotalCount(cartProduct.getSelectedCount());
                    productDelivery.setOrderedTotalWeight(cartProduct.getSelectedWeight());
                    Address address = addressRepo.getById(orderModel.getAddressId());
                    productDelivery.setAddress(address);
                    String completeAddress = address.getLandmark() + ", " + address.getAddressOne() + ", " + address.getArea() + ", " + address.getCity() + "-".concat(address.getPincode()) +
                            ", Mobile - " + address.getMobile();
                    productDeliveryRepo.save(productDelivery);
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setOrderStatus(OrderStatus.PLACED);
                    productWiseOrder.setPrice(cartProduct.getQuantityModel().getPrice());
                    productWiseOrder.setCompleteAddress(completeAddress);
                    productWiseOrder.setDeliveryAgentDetails("Rajeev Kumar, Mobile - 9878979798");
                } else {
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setTotalProductCount(cartProduct.getSelectedCount());
                    productWiseOrder.setOrderStatus(OrderStatus.CANCELED_DUE_TO_OUT_OF_STOCK);
                }
            } catch (Exception e) {
                productWiseOrder.setProductId(cartProduct.getId());
                productWiseOrder.setOrderStatus(OrderStatus.FAILED_DUE_TO_TECHNICAL_ISSUE);
            }

            productWiseOrders.add(productWiseOrder);

        }

        // delete from the cart
        Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(orderModel.getUserPhone()));
        List<CartDetails> cartDetails = cartDetailsRepo.findCartDetailsByCart(cart);
        List<DeliveryProductDetails> successfullyOrdredProduct = productWiseOrders.stream().
                filter(productWiseOrder -> productWiseOrder.getOrderStatus().equals(OrderStatus.PLACED)).collect(Collectors.toList());

        for (DeliveryProductDetails productWiseOrder : successfullyOrdredProduct) {
            CartDetails cartDetails1 = cartDetailsRepo.findCartDetailsByCartAndProduct(cart, productRepo.getById(productWiseOrder.getProductId()));
            cartDetailsRepo.delete(cartDetails1);
        }

        if (null != productWiseOrders && productWiseOrders.size() > 0) {
            Map<String, String> data = new HashMap<>();
            data.put("order_status", OrderStatus.PLACED.name());
            data.put("title", "BABA BASKET");
            data.put("text", "Your order confirmed");
            data.put("token", user.getToken());
            data.put("image", "https://thumbnails-photos.amazon.com/v1/thumbnail/AplBBbD9TLGIyHKBq5LOUA?viewBox=835%2C835&ownerId=A14ZH0T6C5GQSW");
            try {
                firebasePushNotificationService.sendPushMessage(data);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {

        }

        return productWiseOrders;

    }


    @Transactional
    public List<OrderDetailsModel> getOrderDetails(String token, List<OrderStatus> statusList) {
        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));

        List<String> list = statusList.stream().map(status -> status.toString()).collect(Collectors.toList());
        List<Order> orders = orderRepo.findOrderByUser(user);
        List<OrderDetailsModel> orderDetailsModelList = new ArrayList<>();

        for (Order order : orders) {

            if (statusList.contains(order.getOrderStatus())) {
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setUser(order.getUser());
                if(statusList.contains(OrderStatus.DELIVERED)){
                    orderDetailsModel.setIsNew(false);
                }
                orderDetailsModel.setOrderDate(order.getOrderDate());
                orderDetailsModel.setOrderStatus(order.getOrderStatus());
                Address address = addressRepo.findAddressByUserAndIsDefault(order.getUser(), true);
                orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
                List<DeliveryProductDetails> deliveryProductList = new ArrayList<>();
                Double totalCost = 0.00;
                List<ProductDelivery> productDeliveries = productDeliveryRepo.findProductDeliveryByOrder(order);
                for (ProductDelivery productDelivery : productDeliveries) {
                    DeliveryProductDetails deliveryProductDetails = new DeliveryProductDetails();
                    deliveryProductDetails.setDeliveryAgentDetails("Rajeev");
                    deliveryProductDetails.setDeliveryDate(productDelivery.getDeliveryDate().toString());
                    deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                    deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
                    deliveryProductDetails.setOrderStatus(productDelivery.getOrderStatus());
                    deliveryProductDetails.setTotalProductCount(productDelivery.getOrderedTotalCount());
                    totalCost = totalCost + productDelivery.getProduct().getSellingPrice();
                    deliveryProductDetails.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                    deliveryProductDetails.setPrice(productDelivery.getProduct().getSellingPrice());
                    deliveryProductList.add(deliveryProductDetails);
                }
                orderDetailsModel.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
                orderDetailsModel.setOrderId(order.getId());
                orderDetailsModel.setDeliveryProducts(deliveryProductList);
                orderDetailsModel.setTotalCost(order.getTotalCost());
                orderDetailsModelList.add(orderDetailsModel);
            }

        }

        Collections.sort(orderDetailsModelList, new Comparator<OrderDetailsModel>() {
            @Override
            public int compare(OrderDetailsModel o1, OrderDetailsModel o2) {
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            }
        });
        return orderDetailsModelList;

    }


    public List<DeliveryProductDetails> getOrderListByUser(String token) {
        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
        List<Order> orders = this.orderRepo.findOrderByUser(user);
        List<DeliveryProductDetails> productWiseOrders = new ArrayList<>();

        for (Order order : orders) {
            List<ProductDelivery> productsDeliveryList = productDeliveryRepo.findProductDeliveryByOrder(order);
            for (ProductDelivery productDelivery : productsDeliveryList) {
                DeliveryProductDetails productWiseOrder = new DeliveryProductDetails();
                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                    String deliveryDate = formatter.format(productDelivery.getDeliveryDate());
                    String orderDate = formatter.format(productDelivery.getOrder().getOrderDate());
                    productWiseOrder.setDeliveryDate(deliveryDate);
                    productWiseOrder.setOrderDate(orderDate);
                } catch (Exception e) { // won't happen here
                    System.err.println("Invalid date");
                }
                productWiseOrder.setProductDeliveryId(productDelivery.getId());
                productWiseOrder.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                productWiseOrder.setProductId(productDelivery.getProduct().getId());
                productWiseOrder.setProductName(productDelivery.getProduct().getName());
                productWiseOrder.setOrderStatus(productDelivery.getOrderStatus());
                productWiseOrder.setPrice(productDelivery.getOrderedTotalCount() * productDelivery.getProduct().getSellingPrice());
                productWiseOrder.setDeliveryAgentDetails("Rajeev Kumar, Mobile - 9878979798");
                productWiseOrder.setTotalProductCount(productDelivery.getOrderedTotalCount());
                Address address = productDelivery.getAddress();
                String completeAddress = address.getLandmark() + ", " + address.getAddressOne() + ", " + address.getArea() + ", " + address.getCity() + "-".concat(address.getPincode())
                        + ", Mobile - " + address.getMobile();

                productWiseOrder.setOrderId(order.getId());
                productWiseOrder.setCompleteAddress(completeAddress);
                productWiseOrders.add(productWiseOrder);
            }
        }

        return productWiseOrders;
    }

    public Boolean cancelOrder(Long id, User user) {
        try {
            Order order = orderRepo.getById(id);
            List<ProductDelivery> productDeliveries = productDeliveryRepo.findProductDeliveryByOrder(order);
            order.setOrderStatus(OrderStatus.CANCELED);
            productDeliveries.forEach(productDelivery -> productDelivery.setOrderStatus(OrderStatus.CANCELED));
            productDeliveryRepo.saveAll(productDeliveries);
            orderRepo.save(order);
            for (ProductDelivery productDelivery : productDeliveries) {
                updateProductInventory(productDelivery.getProduct(), productDelivery.getOrderedTotalCount());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean packingOrder(Long id) {
        Order order = orderRepo.getById(id);
        List<ProductDelivery> productDeliveries = productDeliveryRepo.findProductDeliveryByOrder(order);
        productDeliveries.stream().forEach(p -> p.setOrderStatus(OrderStatus.PACKING));
        productDeliveryRepo.saveAll(productDeliveries);
        order.setOrderStatus(OrderStatus.PACKING);
        orderRepo.save(order);

        Map<String, String> data = new HashMap<>();
        data.put("order_status", OrderStatus.PLACED.name());
        data.put("title", "BABA BASKET");
        data.put("text", "Your order is getting packed");
        data.put("token", order.getUser().getToken());
        data.put("image", "https://thumbnails-photos.amazon.com/v1/thumbnail/AplBBbD9TLGIyHKBq5LOUA?viewBox=835%2C835&ownerId=A14ZH0T6C5GQSW");
        try {
            firebasePushNotificationService.sendPushMessage(data);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return true;
    }

    public Boolean markedDelivered(Long id) {
        Order order = orderRepo.getById(id);
        List<ProductDelivery> productDeliveries = productDeliveryRepo.findProductDeliveryByOrder(order);
        productDeliveries.stream().forEach(p -> p.setOrderStatus(OrderStatus.DELIVERED));
        productDeliveryRepo.saveAll(productDeliveries);
        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);
        return true;
    }

    public Boolean updateOnTheWay(Long id) {
        Order order = orderRepo.getById(id);
        List<ProductDelivery> productDeliveries = productDeliveryRepo.findProductDeliveryByOrder(order);
        productDeliveries.stream().forEach(p -> p.setOrderStatus(OrderStatus.ON_THE_WAY));
        productDeliveryRepo.saveAll(productDeliveries);
        order.setOrderStatus(OrderStatus.ON_THE_WAY);
        orderRepo.save(order);
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
    public List<OrderDetailsModel> fetchAllOrderByStatus(String token, OrderStatus status) {
        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
        List<Role> roles = user.getRoles();
        if (roles.contains(MASTER)) {
            List<Order> orders = orderRepo.findOrderByOrderStatus(status);
            List<OrderDetailsModel> orderDetailsModelList = new ArrayList<>();
            for (Order order : orders) {
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();

                orderDetailsModel.setUser(order.getUser());
                orderDetailsModel.setOrderDate(order.getOrderDate());
                orderDetailsModel.setOrderStatus(order.getOrderStatus());
                Address address = addressRepo.findAddressByUserAndIsDefault(order.getUser(), true);
                orderDetailsModel.setAddressModel(convertIntoAddressModel(address));
                List<DeliveryProductDetails> deliveryProductList = new ArrayList<>();
                Double totalCost = 0.00;
                List<ProductDelivery> productDeliveries = productDeliveryRepo.findProductDeliveryByOrderAndOrderStatus(order, status);
                for (ProductDelivery productDelivery : productDeliveries) {
                    DeliveryProductDetails deliveryProductDetails = new DeliveryProductDetails();
                    deliveryProductDetails.setDeliveryAgentDetails("Rajeev");
                    deliveryProductDetails.setDeliveryDate(productDelivery.getDeliveryDate().toString());
                    deliveryProductDetails.setProductId(productDelivery.getProduct().getId());
                    deliveryProductDetails.setProductName(productDelivery.getProduct().getName());
                    deliveryProductDetails.setOrderStatus(productDelivery.getOrderStatus());
                    deliveryProductDetails.setTotalProductCount(productDelivery.getOrderedTotalCount());
                    totalCost = totalCost + productDelivery.getProduct().getSellingPrice();
                    deliveryProductDetails.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                    deliveryProductDetails.setPrice(productDelivery.getProduct().getSellingPrice());
                    deliveryProductList.add(deliveryProductDetails);
                }
                orderDetailsModel.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
                orderDetailsModel.setOrderId(order.getId());
                orderDetailsModel.setDeliveryProducts(deliveryProductList);
                orderDetailsModel.setTotalCost(order.getTotalCost());
                orderDetailsModelList.add(orderDetailsModel);
            }
            return orderDetailsModelList;
        } else {
            return null;
        }
    }

    private AddressModel convertIntoAddressModel(Address address) {
        AddressModel addressModel = new AddressModel();
        addressModel.setAddressOne(address.getAddressOne());
        addressModel.setArea(address.getArea());
        addressModel.setCity(address.getCity());
        addressModel.setMobile(address.getMobile());
        addressModel.setId(address.getId());
        addressModel.setIsDefault(address.getIsDefault());
        addressModel.setLandmark(address.getLandmark());
        addressModel.setPincode(address.getPincode());
        addressModel.setUserPhone(address.getMobile());
        return addressModel;
    }

}
