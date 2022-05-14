package com.rfid.app;

public class ProuductInstance {
    private String product_instance_id;
    private String product_line_id;
    private int is_purchased;

    public String getProduct_instance_id() {
        return product_instance_id;
    }

    public void setProduct_instance_id(String product_instance_id) {
        this.product_instance_id = product_instance_id;
    }

    public String getProduct_line_id() {
        return product_line_id;
    }

    public void setProduct_line_id(String product_line_id) {
        this.product_line_id = product_line_id;
    }

    public int getIs_purchased() {
        return is_purchased;
    }

    public void setIs_purchased(int is_purchased) {
        this.is_purchased = is_purchased;
    }

    public ProuductInstance(String product_instance_id, String product_line_id, int is_purchased) {
        this.product_instance_id = product_instance_id;
        this.product_line_id = product_line_id;
        this.is_purchased = is_purchased;
    }
}
