package com.rfid.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rscja.deviceapi.DeviceConfiguration;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RFIDScanActivity extends Activity  {
    public static final String URL_Update = "/PHP/UpdateStatus.php";

    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    private Handler handler;
    private ArrayList<HashMap<String, String>> tagList;
    private SimpleAdapter adapter;

    private TextView tv_count;

    private RadioGroup RgInventory;
    private RadioButton RbInventorySingle;
    private RadioButton RbInventoryLoop;

    private Button BtClear;
    private Button BtImport;
    private Button BtInventory;
    private Button BtView;

    private ListView LvTags;
    private HashMap<String, String> map;
    private RFIDWithUHFUART mReader;

    private String fCurFilePath = "";
    private boolean fIsEmulator = false;
    private String delivery_id;
    public ArrayList<DeliveryOrderDetail> array = new ArrayList<>();



    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_rfid_scan);
            setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
            tagList = new ArrayList<HashMap<String, String>>();

            BtClear = (Button) findViewById(R.id.BtClear);
            BtImport = (Button) findViewById(R.id.BtImport);
            BtView = (Button) findViewById(R.id.BtView);
            tv_count = (TextView) findViewById(R.id.tv_count);
            RgInventory = (RadioGroup) findViewById(R.id.RgInventory);
            RbInventorySingle = (RadioButton) findViewById(R.id.RbInventorySingle);
            RbInventoryLoop = (RadioButton) findViewById(R.id.RbInventoryLoop);
            BtInventory = (Button) findViewById(R.id.BtInventory);
            LvTags = (ListView) findViewById(R.id.LvTags);

            adapter = new SimpleAdapter(this, tagList, R.layout.listtag_items,
                    new String[]{"tagUii", "tagLen", "tagCount"},
                    new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount});

            BtClear.setOnClickListener(new BtClearClickListener());
            BtImport.setOnClickListener(new BtImportClickListener());
            RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
            BtInventory.setOnClickListener(new BtInventoryClickListener());
            BtView.setOnClickListener(new BtViewClickListener());

            LvTags.setAdapter(adapter);
            clearData();

            handler = new Handler() {
                @SuppressLint("HandlerLeak")
                @Override
                public void handleMessage(Message msg) {
                    String result = msg.obj + "";
                    String[] strs = result.split("@");
                    if (addEPCToList(strs[0], strs[1]))
                        UIHelper.playSoundSuccess();
                }
            };
            Bundle b = getIntent().getExtras();

            delivery_id =  b.getString("id");
            String arrayString = b.getString("array");
            InsertData(arrayString);


            fIsEmulator = UIHelper.isEmulator();
            UIHelper.initSound(RFIDScanActivity.this);
            initUHF();
            //addEPCToList("000000000000000000001322","0");
         //   addEPCToList("2","0");
        } catch (Exception ex) {
            UIHelper.showExceptionError(RFIDScanActivity.this, ex);
        }
    }

    public void initUHF() {
        // temporary check this, on emulator device mReader InitTask cause crash application
        if (!fIsEmulator) {
            if (mReader == null) {
                try {
                    mReader = RFIDWithUHFUART.getInstance();
                } catch (Exception ex) {
                    UIHelper.showExceptionError(RFIDScanActivity.this, ex);
                    return;
                }

                if (mReader != null) {
                    new InitTask().execute();
                }
            }
        }
    }


    /**
     *
     * @author liuruifeng
     */
    private class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                return mReader.init();
            }
            catch (Exception ex){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                Toast.makeText(RFIDScanActivity.this, "init fail", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            try {
                super.onPreExecute();

                mypDialog = new ProgressDialog(RFIDScanActivity.this);
                mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mypDialog.setMessage("init...");
                mypDialog.setCanceledOnTouchOutside(false);
                mypDialog.show();

            } catch (Exception ex) {
                UIHelper.showExceptionError(RFIDScanActivity.this, ex);
                return;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopInventory();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (event.getRepeatCount() == 0) {
                readTag();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     *
     * @param epc
     */

    private boolean SearchInArray(String epc){
        boolean flag = false;
        for(DeliveryOrderDetail item : array){
            if(item.getProduct_instance_id().equals(epc)){
                flag= true;
            }
        }
        return flag;
    }
    private boolean addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            //checkIsExist trả về vị trí của epc.

            if(SearchInArray(epc)){
                int index = checkIsExist(epc);
                map = new HashMap<String, String>();
                map.put("tagUii", epc);
                map.put("tagCount", String.valueOf(1));
                map.put("tagRssi", rssi);

                if (index == -1) {
                    tagList.add(map);
                    LvTags.setAdapter(adapter);
                    tv_count.setText("" + adapter.getCount());
                    UpdateStatus(URL_Update,delivery_id,epc);
                } else {
                    int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;

                    map.put("tagCount", String.valueOf(tagcount));
                    tagList.set(index, map);
                }
                adapter.notifyDataSetChanged();
                if (index >= 0)
                    return false;
            }
            return true;
        }

        return false;
    }

    private class BtClearClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            clearData();
        }
    }

    private class BtImportClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (BtInventory.getText().equals(getString(R.string.btInventory))) {
                if (tagList.size() == 0) {
                    UIHelper.ToastMessage(RFIDScanActivity.this, "No data");
                    return;
                }

                // save to SQL
                // boolean re = FileImport.SaveSQL(tagList, RFIDScanActivity.this);

                try {
                    // save excel file
                    boolean reXls = FileImport.SaveFileXls(tagList, "");
                    boolean re = FileImport.SaveFileTxt(tagList, ""); // save txt file
                    if (re) {
                        fCurFilePath = FileImport.FilePathTxt;
                        UIHelper.ToastMessage(RFIDScanActivity.this, getString(R.string.uhf_msg_inventory_save_success));
                        tv_count.setText("0");
                        tagList.clear();
                        adapter.notifyDataSetChanged();
                    }
                }
                catch (Exception ex){
                    UIHelper.showExceptionError(RFIDScanActivity.this, ex);
                }
            } else {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_save_wanrning);
            }
        }
    }

    private class BtViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (BtInventory.getText().equals(getString(R.string.btInventory))) {
//                if (UIHelper.isNullOrEmpty(fCurFilePath)) {
//                    UIHelper.ToastMessage(RFIDScanActivity.this, "No file!");
//                    return;
//                }
                Intent in = new Intent(RFIDScanActivity.this, RFIDViewActivity.class);
                in.putExtra("IntentObject", fCurFilePath);
                startActivity(in);
            } else {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_save_wanrning);
            }
        }
    }

    private void clearData() {
        tv_count.setText("0");
        tagList.clear();

        adapter.notifyDataSetChanged();
    }

    public class RgInventoryCheckedListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == RbInventorySingle.getId()) {
                inventoryFlag = 0;
            } else if (checkedId == RbInventoryLoop.getId()) {
                inventoryFlag = 1;
            }
        }
    }

    public class BtInventoryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        // kiểm tra nút đó đang có giá trị là start hay không
        if (BtInventory.getText().equals(getString(R.string.btInventory)))
        {
            //kiểm tra đối tuong mReader nếu bằng null thì thông báo và dừng lại
            if (mReader == null) {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_sdk_open_fail);
                return;
            }
            //kiểm tra giá trị của inventoryFlag, giá trị mặc định là 1
            //0 là check quét single
            //1 là check tự động (check toàn bộ sản phẩm) đề tài 5 sử dụng cái này nha
            switch (inventoryFlag) {
                case 0:
                {
                    UHFTAGInfo strUII = mReader.inventorySingleTag();
                    if (strUII != null) {
                        String strEPC = strUII.getEPC();
                        addEPCToList(strEPC, strUII.getRssi());
                        UIHelper.playSoundSuccess();
                        tv_count.setText("" + adapter.getCount());
                    } else {
                        UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_fail);
                    }
                }
                break;
                case 1://  .startInventoryTag((byte) 0, (byte) 0))
                {
                    if (mReader.startInventoryTag()) {
                        BtInventory.setText(getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        setViewEnabled(false);
                        new TagThread().start();
                    } else {
                        mReader.stopInventory();
                        UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_open_fail);
                    }
                }
                break;
                default:
                    break;
            }
        } else {
            //trường hợp chọn Stop sẽ rơi vào đây

            stopInventory();
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mReader.stopInventory()) {
                BtInventory.setText(getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(RFIDScanActivity.this, R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     *
     * @param strEPC
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (strEPC == null || strEPC.length() == 0) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    private class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            UHFTAGInfo res = null;
            while (loopFlag) {
                res = mReader.readTagFromBuffer();
                if (res != null) {
                    strTid = res.getTid();
                    if (strTid.length() != 0 && !strTid.equals("0000000" + "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }

                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + res.getEPC() + "@" + res.getRssi();

                    handler.sendMessage(msg);
                }
            }
        }
    }

    public void InsertData(String string) throws JSONException {
        JSONArray response = new JSONArray(string);
        for(int i =0;i<response.length();i++){
            try {
                JSONObject object = response.getJSONObject(i);
                DeliveryOrderDetail temp = new DeliveryOrderDetail(object.getString("DeliveryOrderId"),object.getString("ProductInstanceId"),object.getInt("IsChecked"),object.getString("ProductName"),object.getString("ProductId"),object.getInt("Quantity"));
                array.add(temp);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void UpdateStatus(String url, String orderId, String productInstanceId){
        url = MainActivity.http+MainActivity.IPv4+url;
        RequestQueue requestQueue =  Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("orderId",orderId);
                params.put("rfid",productInstanceId);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}
