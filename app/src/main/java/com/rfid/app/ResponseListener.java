package com.rfid.app;

import java.util.ArrayList;

public interface ResponseListener {


    default void onResponseReceived(ArrayList<DeliveryOrderDetail> orderDetails){
        getArray(orderDetails);
    }

    default ArrayList<DeliveryOrderDetail> getArray(ArrayList<DeliveryOrderDetail> orderDetails){
        return orderDetails;
    }
}
