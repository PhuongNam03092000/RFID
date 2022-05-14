package com.rfid.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OrderAdapter extends BaseAdapter {
    Context context;
    ArrayList<DeliveryOrder> listItem;
    public OrderAdapter(Context context, ArrayList<DeliveryOrder> listItem){
        this.context = context;
        this.listItem = listItem;
    }
    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public DeliveryOrder getItem(int i) {
        return listItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_order, viewGroup, false);
        TextView tvId = (TextView)rowView.findViewById(R.id.orderId);
        TextView tvInfo = (TextView)rowView.findViewById(R.id.orderInfo);

        tvId.setText("Order Id :"+listItem.get(i).getDelivery_order_id());
        tvInfo.setText("Date :"+listItem.get(i).getDelivery_order_date());
        return rowView;
    }


}
