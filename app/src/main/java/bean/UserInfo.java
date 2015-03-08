/**
 * wechatgaotong
 */
package bean;

import java.io.Serializable;

import tools.AppException;
import tools.Logger;

import com.google.gson.Gson;

/**
 * wechat
 *
 * @author gaotong
 *
 */
public class UserInfo implements Serializable {
    public String userId;
    public String email;
	public String description;
	public String lLang;
	public String mLang;
	public String userHead;
	public String nickName;
    public String password;
    
    public String myid;
    
    
	/**
	 * @param string
	 * @return
	 * @throws AppException 
	 */
	public static UserInfo parse(String string) throws AppException {
		UserInfo data = null;
		try {
			Gson gson = new Gson();
			data = gson.fromJson(string, UserInfo.class);
		} catch (Exception e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                ", lLang='" + lLang + '\'' +
                ", mLang='" + mLang + '\'' +
                ", userHead='" + userHead + '\'' +
                ", nickName='" + nickName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
