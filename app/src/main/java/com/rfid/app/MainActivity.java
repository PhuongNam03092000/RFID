package com.rfid.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    ListView deliveryListView;
    public static final String IPv4 = "192.168.1.10";
    public static final String http  = "http://";
    public static final String Url_Delivery = "/PHP/GetAllOrder.php";
    public static final String URL_GetOrderDetail = "/PHP/GetOrderDetail.php?id=";

    ArrayList<DeliveryOrder> deliveryOrderArrayList;
    BaseAdapter itemsAdapter;
    ArrayList<DeliveryOrderDetail> details ;
    Bundle b;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitComponent();
        GetData(Url_Delivery);
        //CheckProductInOrder(URL_GetOrderDetail,"OR005");

    }
    public void InitComponent(){
        deliveryListView = findViewById(R.id.listViewDelivery);
        deliveryOrderArrayList = new ArrayList<>();
                details        = new ArrayList<>();
        deliveryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               intent = new Intent(getApplicationContext(), RFIDScanActivity.class);
                //Create a bundle object
                b = new Bundle();
                DeliveryOrder obj = (DeliveryOrder) itemsAdapter.getItem(position);
                b.putString("id",obj.getDelivery_order_id());
                GetProductInOrder(URL_GetOrderDetail, obj.getDelivery_order_id(), new ResponseListener() {
                    @Override
                    public void onResponseReceived(ArrayList<DeliveryOrderDetail> orderDetails) {

                    }
                });





            }
        });
    }

    private void GetData(String url){
        url = http+IPv4+url;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response){

                        for(int i =0;i<response.length();i++){
                            try {
                                JSONObject object = response.getJSONObject(i);
                                DeliveryOrder deliveryOrder = new DeliveryOrder(object.getString("DeliveryOrderId"),object.getString("DeliveryOrderDate"),object.getString("OrderStatus"));
                                deliveryOrderArrayList.add(deliveryOrder);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        PassDataToListView();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
                        System.out.println(error.toString());
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }


    public void PassDataToListView(){
        itemsAdapter =
                new OrderAdapter(this,deliveryOrderArrayList);
        deliveryListView.setAdapter(itemsAdapter);
    }


    private void GetProductInOrder(String url,String id,final  ResponseListener responseListener){
        url = http+IPv4+url;
        url +=id;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        ArrayList<DeliveryOrderDetail> hello = new ArrayList<>();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response){
                        b.putString("array",response.toString());
                        //Add the bundle to the intent.
                        intent.putExtras(b);

                        //start the DisplayActivity
                        startActivity(intent);
                    }

                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                        System.out.println(error.toString());
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }

}