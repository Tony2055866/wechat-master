package ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.donal.wechat.R;

import bean.UserEntity;
import config.ApiClent;
import config.AppActivity;
import config.WCApplication;
import tools.AppManager;
import tools.UIHelper;
import util.DialogFactory;

/**
 * Created by Administrator on 2015/2/8.
 */
public class LoginActivity extends AppActivity implements View.OnClickListener {
    protected WCApplication appContext;

    private Button mBtnRegister;
    private Button mBtnLogin;
    private EditText mAccounts, mPassword;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = (WCApplication)getApplication();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loginpage);
        initView();
    }

    public void initView() {
        mBtnRegister = (Button) findViewById(R.id.regist_btn);
        mBtnRegister.setOnClickListener(this);
        mBtnLogin = (Button) findViewById(R.id.login_btn);
        mBtnLogin.setOnClickListener(this);
        mAccounts = (EditText) findViewById(R.id.lgoin_accounts);
        mPassword = (EditText) findViewById(R.id.login_password);

    }

    /**
     * 处理点击事件
     */
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.regist_btn:
                register();
                break;
            case R.id.login_btn:
                submit();
                break;
            default:
                break;
        }
    }

    private void register() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private ProgressDialog loadingPd;
    /**
     * 提交账号密码信息到服务器
     */
    private void submit() {
        final String accounts = mAccounts.getText().toString();
        final String password = mPassword.getText().toString();
        //accounts = "test"+accounts+"@"+FriendListActivity.SERVICE_NAME  ;
        //password = "test";
        if (accounts.length() == 0 || password.length() == 0) {
            DialogFactory.ToastDialog(this, "notice", LoginActivity.this.getString(R.string.password_not_empty));
        } else {
            try {
                loadingPd = UIHelper.showProgress(this, null, null, true);
                ApiClent.login(appContext, accounts, password, new ApiClent.ClientCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        UIHelper.dismissProgress(loadingPd);
                        UserEntity user = (UserEntity) data;
                        if (user.status == 1) {
                            appContext.saveLoginInfo(user);
                            appContext.saveLoginPassword(password);
                            saveLoginConfig(appContext.getLoginInfo());
                            Intent intent = new Intent(LoginActivity.this, Tabbar.class);
                            startActivity(intent);
                            AppManager.getAppManager().finishActivity(LoginActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        UIHelper.dismissProgress(loadingPd);
                    }

                    @Override
                    public void onError(Exception e) {
                        UIHelper.dismissProgress(loadingPd);
                    }
                });
				/*SmackAndroid.init(LoginActivity.this);
                Log.i("tong test","accounts : " + accounts + "  password:" + password);
                // 连接服务器
                
				XmppConnection.getConnection().login(accounts, password);
				// 连接服务器成功，更改在线状态
				Presence presence = new Presence(Presence.Type.available);
				XmppConnection.getConnection().sendPacket(presence);
				
				// 弹出登录成功提示
				DialogFactory.ToastDialog(this, "notice", LoginActivity.this.getString(R.string.login_success));
				// 跳转到好友列表
				Intent intent = new Intent();
				intent.putExtra("USERID", accounts);
				intent.setClass(LoginActivity.this, FriendListActivity.class);
				startActivity(intent);*/
            } catch (Exception e) {
                //XmppConnection.closeConnection();
                handler.sendEmptyMessage(2);
                e.printStackTrace();
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 2) {
                DialogFactory.ToastDialog(LoginActivity.this, "登录提示",
                        "登录失败");
            }
        };
    };
}
