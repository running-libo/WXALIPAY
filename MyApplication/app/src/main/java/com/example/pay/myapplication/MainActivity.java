package com.example.pay.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import com.alipay.sdk.app.PayTask;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    protected Context context;
    private static final int SDK_PAY_FLAG = 1;
    private RadioButton rbWxPay,rbAlipay;
    private final String ALIPAY = "alipay";
    private final String WXPAY = "wechat";
    private String payType = ALIPAY;
    //微信请求参数，应该从后台请求而来
    private String payInfo = "{\"data\":\"alipay_sdk=lokielse%2Fomnipay-alipay&app_id=2017053107389459&biz_content=%7B%22subject%22%3A%22%5Cu91d1%5Cu5e01%5Cu5145%5Cu503c%22%2C%22out_trade_no%22%3A%22201710291326384447%22%2C%22total_amount%22%3A0.01%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%7D&charset=UTF-8&format=JSON&method=alipay.trade.app.pay&notify_url=http%3A%2F%2Fwww.goobird.com%2Falipay%2Freturn&sign_type=RSA2&timestamp=2017-10-29+13%3A26%3A38&version=1.0&sign=kHmyzkbQF3arKQB2OdTURyAC0grwyI5ZcqfhSNWjK0kzVoLAGsd%2B%2Bi0MbWgREAJc7iTYr93sKELpb9%2FnGcKuUFyFgmqoQgSA%2FM8QDcCW9RpuXjpIFxWSqNUXNxNRKmWv7hi%2BwODCXSDovn6Hg65%2Fu7DU1Y%2BofsPbn2PHHndih6c04lLrNPdfh5pUNlLWWvuwZmXl27HPHGk8KI6gl2dvyTMEKxApAlKROUgaVZwoCx0trCg9Elne%2BXQm0RCbagA7QWLspIU55O1Tj%2FkW8rwpdbD4s3dRFqEhDbbZHkEYRGIrVQ%2BfYqwToiW8GbqHlf%2FlazZrbS%2FgVknEnXjOMp%2BSow%3D%3D\"}\n";
    private IWXAPI iwxapi;
    private String appId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        iwxapi = WXAPIFactory.createWXAPI(this, appId, true);
        iwxapi.registerApp(appId);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    //对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.equals(resultStatus, "6002")) {
                        Toast.makeText(context,"网络有问题",Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        Toast.makeText(context,"您已经取消支付",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,"支付失败",Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void initView() {
        LinearLayout tvWechat = (LinearLayout) findViewById(R.id.ll_wechat);
        LinearLayout tvAlipay = (LinearLayout) findViewById(R.id.ll_alipay);
        Button btnPay = (Button) findViewById(R.id.go_pay);
        rbWxPay = (RadioButton) findViewById(R.id.pay_type_wxpay);
        rbAlipay = (RadioButton) findViewById(R.id.pay_type_alipay);
        rbAlipay.setChecked(true);
        tvWechat.setOnClickListener(this);
        tvAlipay.setOnClickListener(this);
        btnPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_wechat:
                payType = WXPAY;
                setCheckedNull();
                rbWxPay.setChecked(true);
                break;
            case R.id.ll_alipay:
                payType = ALIPAY;
                setCheckedNull();
                rbAlipay.setChecked(true);
                break;
            case R.id.go_pay:
                //requestOrderNo();
                if(payType.equals(ALIPAY)){
                    requestForAli(payInfo);
                }else if(payType.equals(WXPAY)){
                    sendWxPayRes(new JSONObject());
                }
                break;
        }
    }

    private void setCheckedNull(){
        rbWxPay.setChecked(false);
        rbAlipay.setChecked(false);
    }

    private void requestForAli(final String payInfo){
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask((Activity) context);
                // 调用支付接口，获取支付结果
                Map<String, String> result = alipay.payV2(payInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 发送微信支付请求
     */
    private void sendWxPayRes(JSONObject prepayObj) {
        if (!iwxapi.isWXAppInstalled()) {
            Toast.makeText(getApplicationContext(),"您还未安装微信客户端",Toast.LENGTH_SHORT).show();
            return;
        }

        PayReq req = new PayReq();
        try {
            req.appId = prepayObj.getString("appid");
            req.partnerId = prepayObj.getString("partnerid");
            req.prepayId = prepayObj.getString("prepayid");
            req.nonceStr = prepayObj.getString("noncestr");
            req.timeStamp = prepayObj.getString("timestamp");
            req.packageValue = prepayObj.getString("package");
            req.sign = prepayObj.getString("sign");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        iwxapi.sendReq(req);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
