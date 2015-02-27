package config;

import java.util.List;
import java.util.Properties;

import org.apache.http.client.CookieStore;

import bean.UserEntity;
import bean.UserInfo;

import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.utils.L;


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
	private String loginUid = "0";	//登录用户的id
	private String apiKey = "0";	//登录用户的id
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
            this.loginUid = user.userInfo.userId;
            this.apiKey = user.apiKey;
            this.login = true;
            userEntity = user;
        }else{
            login = false;
            userEntity = null;
            apiKey = null;
            loginUid = null;
        }
                
		
	}
	
	public void modifyLoginInfo(final UserInfo user) {
		setProperties(new Properties(){
			{
				if (StringUtils.notEmpty(user.nickName)) {
                    userEntity.userInfo.nickName = user.nickName;
				}
				if (StringUtils.notEmpty(user.userHead)) {
                    userEntity.userInfo.userHead = user.userHead;
				}
				if (StringUtils.notEmpty(user.description)) {
                    userEntity.userInfo.description = user.description;
				}
				
			}
		});		
	}

	/**
	 * 获取登录用户id
	 * @return
	 */
	public String getLoginUid() {
		return loginUid;
	}
	
	public String getLoginApiKey() {
		return apiKey;
	}
	
	public String getLoginUserHead() {
		return userEntity.userInfo.userHead;
	}
	
	/**
	 * 获取登录信息
	 * @return
	 */
	public UserEntity getLoginInfo() {		
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
