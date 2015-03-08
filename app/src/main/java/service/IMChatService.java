package service;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import config.*;
import im.Chating;
import im.model.IMMessage;
import im.model.Notice;

import java.util.Calendar;


import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import bean.JsonMessage;

import com.donal.wechat.R;
import com.google.gson.Gson;

import tools.DateUtil;
import tools.Logger;
import ui.Tabbar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * 聊天服务.
 * 
 */
public class IMChatService extends Service {
	private Context context;
	private NotificationManager notificationManager;
	private ChatListener cListener;
    ConnectionChangeReceiver receiver = new ConnectionChangeReceiver();
    public static boolean CHECK_CONN = true;
	@Override
	public void onCreate() {
		context = this;
		Log.d("tong test", "IMChatService onCreate");
		super.onCreate();
		initChatManager();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.i("s");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

   
	@Override
	public void onDestroy() {
        CHECK_CONN = false;
        Log.d("tong test",this.getClass() + " onDestroy !");
		XMPPConnection conn = XmppConnectionManager.getInstance()
				.getConnection();
		if (cListener != null) {
			conn.removePacketListener(cListener);
		}
        conn.disconnect();
        
        unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void initChatManager() {
		XMPPConnection conn = XmppConnectionManager.getInstance()
				.getConnection();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(cListener != null)
          conn.removePacketListener(cListener);
        cListener = new ChatListener();
		conn.addPacketListener(cListener, new MessageTypeFilter(
				Message.Type.chat));
        /*ChatManager cm = XmppConnectionManager.getInstance().getConnection().getChatManager();
        Log.i("tong test","initChatManager addChatListener : ");
        cm.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean b) {
                Log.i("tong test","chatCreated chat : " + chat);
                chat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Chat chat2, Message message) {
                        
                    }
                });
            }
        });*/
        
        
	}

	class ChatListener implements PacketListener {

		@Override
		public void processPacket(Packet arg0) {
			Message message = (Message) arg0;
			if (message != null && message.getBody() != null
					&& !message.getBody().equals("null")) {
				IMMessage msg = new IMMessage();
				// String time = (String)
				// message.getProperty(IMMessage.KEY_TIME);
				String time = (System.currentTimeMillis()/1000)+"";//DateUtil.date2Str(Calendar.getInstance(), Constant.MS_FORMART);
				msg.setTime(time);
				msg.setContent(message.getBody());
				if (Message.Type.error == message.getType()) {
					msg.setType(IMMessage.ERROR);
				} else {
					msg.setType(IMMessage.SUCCESS);
				}
				String from = message.getFrom().split("/")[0];
				msg.setFromSubJid(from);
				NoticeManager noticeManager = NoticeManager
						.getInstance(context);
				Notice notice = new Notice();
				notice.setTitle("会话信息");
				notice.setNoticeType(Notice.CHAT_MSG);
				notice.setContent(message.getBody());
				notice.setFrom(from);
				notice.setStatus(Notice.UNREAD);
				notice.setNoticeTime(time);

				IMMessage newMessage = new IMMessage();
				newMessage.setMsgType(0);
				newMessage.setFromSubJid(from);
				newMessage.setContent(message.getBody());
				newMessage.setTime(time);
                
				newMessage.setType(0); 
				MessageManager.getInstance(context).saveIMMessage(newMessage);
				long noticeId = -1;

				noticeId = noticeManager.saveNotice(notice);
				if (noticeId != -1) {
                    Log.i("tong test","get message and: new Intent(CommonValue.NEW_MESSAGE_ACTION)  newMessage:" +newMessage);
					Intent intent = new Intent(CommonValue.NEW_MESSAGE_ACTION);
					intent.putExtra(IMMessage.IMMESSAGE_KEY, msg);
					intent.putExtra("notice", notice);
					sendBroadcast(intent);
					setNotiType(R.drawable.ic_launcher,
							"新消息",
							notice.getContent(), Tabbar.class, from);

				}
			}
		}
		
	}

	private void setNotiType(int iconId, String contentTitle,
			String contentText, Class activity, String from) {
		JsonMessage msg = new JsonMessage();
		Gson gson = new Gson();
        ///////////task
		msg = gson.fromJson(contentText, JsonMessage.class);
		Intent notifyIntent = new Intent(this, activity);
		notifyIntent.putExtra("to", from);
		PendingIntent appIntent = PendingIntent.getActivity(this, 0,
				notifyIntent, 0);
		Notification myNoti = new Notification();
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		myNoti.icon = iconId;
		myNoti.tickerText = contentTitle;
		myNoti.defaults |= Notification.DEFAULT_SOUND;
		myNoti.defaults |= Notification.DEFAULT_VIBRATE;
		myNoti.setLatestEventInfo(this, contentTitle, msg.text, appIntent);
		notificationManager.notify(0, myNoti);
	}

    public class ConnectionChangeReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("tong test", "网络状态改变");
            boolean success = false;
            //获得网络连接服务
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取WIFI网络连接状态  
            NetworkInfo.State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            // 判断是否正在使用WIFI网络   
            if (NetworkInfo.State.CONNECTED == state) {
                success = true;
            }else{
                // 获取GPRS网络连接状态   
                state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                // 判断是否正在使用GPRS网络   
                if (NetworkInfo.State.CONNECTED == state) {
                    success = true;
                    return;
                }
            }
            
            if(success){
                 ((AppActivity)context).appContext.reConnAndLogin();
            }
/*
        if (!success) {
            Toast.makeText(context, "ddd", Toast.LENGTH_LONG).show();
        }*/
        }

    }
}
