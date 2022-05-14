package com.rfid.app;

import java.io.Serializable;

public class DeliveryOrderDetail {
    private String delivery_order_id;
    private String product_instance_id;
    private int is_checked;
    private String product_name;
    private String product_line_id;
    private int quantity;

    public String getDelivery_order_id() {
        return delivery_order_id;
    }

    public void setDelivery_order_id(String delivery_order_id) {
        this.delivery_order_id = delivery_order_id;
    }

    public String getProduct_instance_id() {
        return product_instance_id;
    }

    public void setProduct_instance_id(String product_instance_id) {
        this.product_instance_id = product_instance_id;
    }

    public int getIs_checked() {
        return is_checked;
    }

    public void setIs_checked(int is_checked) {
        this.is_checked = is_checked;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_line_id() {
        return product_line_id;
    }

    public void setProduct_line_id(String product_line_id) {
        this.product_line_id = product_line_id;
    }

    public int getQuanlity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public DeliveryOrderDetail(String delivery_order_id, String product_instance_id, int is_checked, String product_name, String product_line_id, int quantity) {
        this.delivery_order_id = delivery_order_id;
        this.product_instance_id = product_instance_id;
        this.is_checked = is_checked;
        this.product_name = product_name;
        this.product_line_id = product_line_id;
        this.quantity = quantity;
    }


}
