/**
 * wechatgaotong
 */
package ui;

import android.app.ActionBar;
import android.util.Log;
import android.widget.*;
import im.Chating;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.XMPPConnection;

import tools.AppManager;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import ui.adapter.FieldAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import bean.KeyValue;
import bean.UserEntity;
import bean.UserInfo;

import com.donal.wechat.R;

import config.ApiClent;
import config.ApiClent.ClientCallback;
import config.AppActivity;
import config.CommonValue;
import config.FriendManager;
import config.MessageManager;
import config.NoticeManager;
import config.XmppConnectionManager;
import util.Utils;

/**
 * wechat
 *
 * @author gaotong
 *
 */
public class Me extends AppActivity{
	private ImageView avatarView;
	private TextView nameTV;
	private ListView iphoneTreeView;
	private List<KeyValue> datas = new ArrayList<KeyValue>();
	private FieldAdapter fieldAdapter;
    private UserEntity user = null;;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        user = appContext.getLoginInfo();
		setContentView(R.layout.wechat);
		initUI();
	}
	
	private void initUI() {
		initTextEditDialog();
		initdesDialog();
		LayoutInflater inflater = LayoutInflater.from(this);
		iphoneTreeView = (ListView) findViewById(R.id.xlistview);
		View header = inflater.inflate(R.layout.more_headerview, null);
		avatarView = (ImageView) header.findViewById(R.id.avatar);
		nameTV = (TextView) header.findViewById(R.id.title);
        if(StringUtils.notEmpty(user.userInfo.userHead))
		    imageLoader.displayImage(CommonValue.BASE_URL+user.userInfo.userHead, avatarView, CommonValue.DisplayOptions.avatar_options);
		iphoneTreeView.addHeaderView(header);
		View footer = inflater.inflate(R.layout.me_footer, null);
		iphoneTreeView.addFooterView(footer);
		fieldAdapter = new FieldAdapter(datas, this);
		iphoneTreeView.setAdapter(fieldAdapter);
		setInfo();
		iphoneTreeView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if (position == 1) {
					textDialog.show();
				}
				else if (position == 2) {
					text1Dialog.show();
				}else if(position == 3 || position == 4){
                   showLangOptions(position);
                }
			}
		});
	}
	
	private void setInfo() {
		datas.add(new KeyValue("名字", user.userInfo.nickName));
		datas.add(new KeyValue("个性签名", user.userInfo.description));
        datas.add(new KeyValue("母语", CommonValue.getLangStrings(user.userInfo.mLang)));
        datas.add(new KeyValue("学习语言", CommonValue.getLangStrings(user.userInfo.lLang)));
		fieldAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onBackPressed() {
		isExit();
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.exit:
			XMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
			if (connection.isConnected()) {
				connection.disconnect();
			}
			stopService();
			MessageManager.destroy();
			NoticeManager.destroy();
			FriendManager.destroy();
			appContext.setUserLogout();
			AppManager.getAppManager().finishAllActivity();
			startActivity(new Intent(this, LoginActivity.class));
			break;

		default:
			PhotoChooseOption();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		String newPhotoPath;
		switch (requestCode) {
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (StringUtils.notEmpty(theLarge)) {
				File file = new File(theLarge);
				File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
				try {
					ExifInterface sourceExif = new ExifInterface(theLarge);
					String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 200), 80);
					ExifInterface exif = new ExifInterface(imagePathAfterCompass);
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				    exif.saveAttributes();
					newPhotoPath = imagePathAfterCompass;
					uploadPhotoService(newPhotoPath);
				} catch (IOException e) {
                    Log.e("tong test", "onActivityResult" ,e);
//					Crashlytics.logException(e);
				}
			}
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			if(data == null)  return;
			Uri thisUri = data.getData();
        	String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(thisUri);
        	if(StringUtils.empty(thePath)) {
        		newPhotoPath = ImageUtils.getAbsoluteImagePath(this,thisUri);
        	}
        	else {
        		newPhotoPath = thePath;
        	}
        	File file = new File(newPhotoPath);
			File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
			try {
				ExifInterface sourceExif = new ExifInterface(newPhotoPath);
				String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 200), 80);
				ExifInterface exif = new ExifInterface(imagePathAfterCompass);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
			    exif.saveAttributes();
				newPhotoPath = imagePathAfterCompass;
				uploadPhotoService(newPhotoPath);
			} catch (IOException e) {
//				Crashlytics.logException(e);
			}
			break;
		}
	}
	
	private String theLarge;
	private void PhotoChooseOption() {
		closeInput();
		CharSequence[] item = {"相册", "拍照"};
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star).setItems(item,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int item)
					{
						//手机选图
						if( item == 0 )
						{
							Intent intent = new Intent(Intent.ACTION_PICK,
									Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
						}
						//拍照
						else if( item == 1 )
						{	
							String savePath = "";
							//判断是否挂载了SD卡
							String storageState = Environment.getExternalStorageState();		
							if(storageState.equals(Environment.MEDIA_MOUNTED)){
								savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + ImageUtils.DCIM;//存放照片的文件夹
								File savedir = new File(savePath);
								if (!savedir.exists()) {
									savedir.mkdirs();
								}
							}
							//没有挂载SD卡，无法保存文件
							if(StringUtils.empty(savePath)){
								UIHelper.ToastMessage(Me.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
								return;
							}
							String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
							String fileName = "c_" + timeStamp + ".jpg";//照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);
							theLarge = savePath + fileName;//该照片的绝对路径
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}
					}}).create();
			 imageDialog.show();
	}
	
	private void uploadPhotoService(String file) {
		pg = UIHelper.showProgress(this, "", "上传头像", true);
		imageLoader.displayImage("file:///"+file, avatarView, CommonValue.DisplayOptions.avatar_options);
		ApiClent.uploadFile(appContext.getLoginApiKey(), file, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				UIHelper.dismissProgress(pg);
				String head = (String) data;
				modify("", head, "");
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(pg); 
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(pg);
			}
		});
	}

    private void modify(UserInfo userInfo) {
        pg = UIHelper.showProgress(this, "", "保存中", true);
        ApiClent.modifiedUserInfo(appContext, appContext.getLoginApiKey(), userInfo, new ClientCallback() {
            @Override
            public void onSuccess(Object data) {
                UIHelper.dismissProgress(pg);
                showToast((String)data);
            }
            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(pg);
            }

            @Override
            public void onError(Exception e) {
                UIHelper.dismissProgress(pg);
            }
        });
    }
	private void modify(String nickname, String head, String des) {
		pg = UIHelper.showProgress(this, "", "保存中", true);
		ApiClent.modifiedUser(appContext, appContext.getLoginApiKey(), nickname, head, des, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				UIHelper.dismissProgress(pg);
				showToast((String)data);
			}
			
			@Override
			public void onFailure(String message) {
				UIHelper.dismissProgress(pg);
			}
			
			@Override
			public void onError(Exception e) {
				UIHelper.dismissProgress(pg);
			}
		});
	}
	
	private AlertDialog textDialog;
	private void initTextEditDialog() {
		LayoutInflater inFlater = LayoutInflater.from(this);  
		View textDialogView = inFlater.inflate(R.layout.lovecode_edit_edittext_dialog, null);
		final EditText ed = (EditText) textDialogView.findViewById(R.id.text);
        ed.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("编辑姓名");
		builder.setView(textDialogView);
		builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (StringUtils.empty(ed.getText().toString())) {
					return;
				}
				datas.set(0, new KeyValue("昵称", ed.getText().toString()));
				fieldAdapter.notifyDataSetChanged();
				modify(ed.getText().toString(), "", "");
			}
		});
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		textDialog = builder.create();
	}
	
	private AlertDialog text1Dialog;
	private void initdesDialog() {
		LayoutInflater inFlater = LayoutInflater.from(this);  
		View textDialogView = inFlater.inflate(R.layout.lovecode_edit_edittext_dialog, null);
		final EditText ed = (EditText) textDialogView.findViewById(R.id.text);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("编辑个性签名");
		builder.setView(textDialogView);
		builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (StringUtils.empty(ed.getText().toString())) {
					return;
				}
				datas.set(1, new KeyValue("个性签名", ed.getText().toString()));
				fieldAdapter.notifyDataSetChanged();
				modify("", "", ed.getText().toString());
			}
		});
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		text1Dialog = builder.create();
	}

    public void showLangOptions(final int position) {
        final String[] items = CommonValue.ITEMS;
        
        final List<String> selected = new ArrayList<String>(2);
        final List<Integer> selectedIndex = new ArrayList<Integer>(2);
        
        
        String currentSelected = null;
        if(position == 3){
            currentSelected = user.userInfo.mLang;
        }else{
            currentSelected = user.userInfo.lLang;
        }
        final boolean ischeckds[] = CommonValue.getCheckedByString(currentSelected);
        
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("选择你的母语")
                .setMultiChoiceItems(items, ischeckds, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        Log.i("tong test", "showMotherLangOptins click : " + i +"  boolean:" +b);
                        if(i < items.length) ischeckds[i] = b;
//                        if(b){
//                            selected.add(items[i]);
//                            selectedIndex.add(i);
//                        }
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(int k=0; k<ischeckds.length; k++){
                            if( ischeckds[k]){
                                selectedIndex.add(k);
                                selected.add(items[k]);
                            }
                            
                        }
                        
                        String selectedStrings = "";
                        if (selected.size() > 0) {
                            selectedStrings = Utils.join(selected.toArray(), ',');
                        }

                        String selectedIndexs = "";
                        if (selectedIndex.size() > 0) {
                            selectedIndexs = Utils.join(selectedIndex.toArray(), ',');
                        }
                        
                        if (position == 3) { //选择我的母语
                            user.userInfo.mLang = selectedIndexs;
                        } else {
                            user.userInfo.lLang = selectedIndexs;
                        }
                        Log.i("tong test", "datas.get(position-1).value :" + datas.get(position-1).value + " selectedStrings");
                        datas.get(position-1).value = selectedStrings;
                        fieldAdapter.notifyDataSetChanged();
                        
                        modify(user.userInfo);
                    }
                }).setNegativeButton("取消", null)
                .show();
    }
}
