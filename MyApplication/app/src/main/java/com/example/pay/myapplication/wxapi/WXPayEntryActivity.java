package com.example.pay.myapplication.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;
	private String appId = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	api = WXAPIFactory.createWXAPI(this, appId);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {

		switch (resp.errCode){
			case -1:
				Toast.makeText(getApplicationContext(),"支付失败", Toast.LENGTH_SHORT).show();
			case 0:
				Toast.makeText(getApplicationContext(),"支付成功", Toast.LENGTH_SHORT).show();
				break;
			case -2:
				Toast.makeText(getApplicationContext(),"支付取消", Toast.LENGTH_SHORT).show();
				break;
		}
	}

}