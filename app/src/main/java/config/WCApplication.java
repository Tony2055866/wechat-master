package config;

import java.util.List;
import java.util.Properties;

import android.util.Log;
import org.apache.http.client.CookieStore;

import bean.UserEntity;
import bean.UserInfo;

import com.google.gson.Gson;
import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.utils.L;


import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import tools.AppContext;
import tools.AppException;
import tools.AppManager;
import tools.ImageCacheUtil;
import tools.Logger;
import tools.StringUtils;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * wechat
 *
 * @author gaotong
 *
 */
public class WCApplication extends AppContext {
	private static WCApplication mApplication;
	
	private NotificationManager mNotificationManager;
	
	private boolean login = false;	//登录状态
	//private String loginUid = "0";	//登录用户的id
	//private String apiKey = "0";	//登录用户的id
	private UserEntity userEntity;
	public synchronized static WCApplication getInstance() {
		return mApplication;
	}
	
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}

    
	
	public void onCreate() {
		mApplication = this;
		Logger.setDebug(true);
		Logger.getLogger().setTag("wechat");
		ImageCacheUtil.init(this);
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		L.disableLogging();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		CookieStore cookieStore = new PersistentCookieStore(this);  
		QYRestClient.getIntance().setCookieStore(cookieStore);
		
		Intent intent = new Intent();
        intent.setAction("tools.NetworkState.Service");
        startService(intent);
	}

    public void reConnAndLogin() {
        Log.i("tong test", this.getClass() + "   reConnAndLogin");
        try {
            String password = getLoginInfo().userInfo.password;
            String userId = getLoginUid();
            XMPPConnection connection = XmppConnectionManager.getInstance()
                    .getConnection();
            if(!connection.isConnected())
                connection.connect();
            if(StringUtils.notEmpty(userId) && StringUtils.notEmpty(password)){
                if(!connection.isAuthenticated()){
                    connection.login(userId, password, "android"); //
                    connection.sendPacket(new Presence(Presence.Type.available));
                    Log.i("tong test", "XMPPClient reConnAndLogin in as: " + connection.getUser() + " password:" + password);
                }
            } 
        }catch (Exception e){
            Log.e("tong test","reConnAndLogin error!", e);
        }
        
    }
	
	public void exit() {
//		XmppConnectionManager.getInstance().disconnect();
		AppManager.getAppManager().finishAllActivity();
	}
	
	/**
	 * 用户是否登录
	 * @return
	 */
	public boolean isLogin() {
		return login;
	}

	
	@SuppressWarnings("serial")
	public void saveLoginInfo(final UserEntity user) {
        if(user != null){
            //this.loginUid = user.userInfo.userId;
            //this.apiKey = user.apiKey;
            this.login = true;
            userEntity = user;
            saveAccountToLocal(user);
        }else{
            login = false;
            userEntity = null;
            //apiKey = null;
            //loginUid = null;
            clearAccountFromLocal();
        }
        
	}

    
    private void clearAccountFromLocal() {
        SharedPreferences sp = getSharedPreferences(CommonValue.SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("user");
        editor.commit();
    }

    public void saveAccountToLocal(UserEntity user) {
        SharedPreferences sp = getSharedPreferences(CommonValue.SHARED_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user", new Gson().toJson(user));
        editor.commit();
    }

    public UserEntity getAccountFromLocal(){
        SharedPreferences sp = getSharedPreferences(CommonValue.SHARED_PREFERENCE, MODE_PRIVATE);
        String jsonString = sp.getString("user","");
        if(jsonString != ""){
            return new Gson().fromJson(jsonString, UserEntity.class);
        }
        return null;
    }

    public void modifyLoginInfo(final UserInfo user) {
		UserEntity entity = getLoginInfo();
        if(entity != null){
            entity.userInfo = user;
            saveAccountToLocal(entity);
        }
	}

	/**
	 * 获取登录用户id
	 * @return
	 */
	public String getLoginUid() {
		return getLoginInfo().userInfo.userId;
	}
	
	public String getLoginApiKey() {
		return getLoginInfo().apiKey;
	}
	
	public String getLoginUserHead() {
       return getLoginInfo().userInfo.userHead;
		//return userEntity.userInfo.userHead;
	}
	
	/**
	 * 获取登录信息
	 * @return
	 */
	public UserEntity getLoginInfo() {
        if(this.userEntity == null){
            return this.userEntity = getAccountFromLocal();
        }
        return this.userEntity;
	}
	
	public String getNickname() {		
        return userEntity.userInfo.nickName;
	}
	
	/**
	 * 退出登录
	 */
	public void setUserLogout() {
		this.login = false;
        this.userEntity = null;
        
	}


    public void saveUserInfo(UserInfo userDetail) {
        userEntity.userInfo = userDetail;
    }
}
