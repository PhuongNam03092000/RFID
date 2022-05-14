package com.rfid.app;

public class ProductLine {
    private String product_line_id;
    private String name;
    private long price;

    public String getProduct_line_id() {
        return product_line_id;
    }

    public void setProduct_line_id(String product_line_id) {
        this.product_line_id = product_line_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public ProductLine(String product_line_id, String name, long price) {
        this.product_line_id = product_line_id;
        this.name = name;
        this.price = price;
    }
}
