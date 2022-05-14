package com.rfid.app;

public class DeliveryOrder  {
    private String delivery_order_id;
    private String delivery_order_date;
    private String order_status;

    public String getDelivery_order_id() {
        return delivery_order_id;
    }

    public void setDelivery_order_id(String delivery_order_id) {
        this.delivery_order_id = delivery_order_id;
    }

    public String getDelivery_order_date() {
        return delivery_order_date;
    }

    public void setDelivery_order_date(String delivery_order_date) {
        this.delivery_order_date = delivery_order_date;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public DeliveryOrder(String delivery_order_id, String delivery_order_date, String order_status) {
        this.delivery_order_id = delivery_order_id;
        this.delivery_order_date = delivery_order_date;
        this.order_status = order_status;
    }

    @Override
    public String toString() {
        return delivery_order_id;
    }
}
