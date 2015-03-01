package ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.donal.wechat.R;

import config.CommonValue;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;

import java.util.ArrayList;
import java.util.List;

import bean.Entity;
import config.ApiClent;
import config.AppActivity;
import tools.AppManager;
import tools.StringUtils;
import tools.UIHelper;
import util.Utils;

/**
 * Created by Administrator on 2015/2/8.
 */
public class RegisterActivity extends AppActivity implements View.OnClickListener {

    private Button mBtnRegister;
    private Button mRegBack;
    private EditText mEmailEt, mNameEt, mPasswdEt, mPasswdEt2;
    private TextView mLearnLangEdit,mMotherLangEdit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.register);
        mBtnRegister = (Button) findViewById(R.id.register_btn);
        mRegBack = (Button) findViewById(R.id.reg_back_btn);
        mBtnRegister.setOnClickListener(this);
        mRegBack.setOnClickListener(this);

        mMotherLangEdit = (TextView) findViewById(R.id.reg_motherLang);
        mLearnLangEdit =  (TextView) findViewById(R.id.reg_learnLang);
        //mMotherLangEdit.setOnClickListener(this);
        //mLearnLangEdit.setOnClickListener(this);
        LinearLayout ll = (LinearLayout)findViewById(R.id.reg_motherLangLayout);
        
        mEmailEt = (EditText) findViewById(R.id.reg_email);
        mNameEt = (EditText) findViewById(R.id.reg_name);
        mPasswdEt = (EditText) findViewById(R.id.reg_password);
        mPasswdEt2 = (EditText) findViewById(R.id.reg_password2);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.reg_back_btn:
                login();
                break;
            case R.id.register_btn:
                registered();
                break;
//            case R.id.reg_motherLang:
//                showLangOptions();
//                break;
//            case R.id.reg_learnLang:
//                showLangOptions();
            default:
                break;
        }
    }


    public void showLangOptions(View v) {
        Log.i("tong test", "showLangOptions v:" + v + "  ; id:" + v.getId());
       /* final View view = v;
        final String[] items = CommonValue.ITEMS;
        final boolean ischeckds[] = CommonValue.getCheckedByString(v.getTag().toString());
        
        final List<String> selected = new ArrayList<String>(2);
        final List<Integer> selectedIndex = new ArrayList<Integer>(2);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("选择你的母语")
                .setMultiChoiceItems(items, ischeckds, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        Log.i("tong test", "showMotherLangOptins click : " + i +"  boolean:" +b);
                        if(i < items.length) ischeckds[i] = b;
                        if(b){
                            selected.add(items[i]);
                            selectedIndex.add(i);
                        }
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String res = "";
                        if (selected.size() > 0) {
                            res = Utils.join(selected.toArray(), ',');
                        }
                        
                        if (view.getId() == R.id.reg_learnLangLayout) {
                            mLearnLangEdit.setText(res);
                            mLearnLangEdit.setTag(selectedIndex);
                        } else {
                            mMotherLangEdit.setText(res);
                            mMotherLangEdit.setTag(selectedIndex);
                        }
                    }
                }).setNegativeButton("取消", null)
                .show();*/
    }


    private void registered() {

        String accounts = mNameEt.getText().toString();
        String password = mPasswdEt.getText().toString();
        String email = mEmailEt.getText().toString();
        String mMotherLang = "";
            if(mMotherLangEdit.getTag() != null)
                Utils.join( ((List)mMotherLangEdit.getTag()).toArray(), ',');
        String mLearnLang = "";
            if(mLearnLangEdit.getTag() != null)
                Utils.join( ((List)mLearnLangEdit.getTag()).toArray(), ',');
        //String mLearnLang = mLearnLangEdit.getText().toString();
        final ProgressDialog dialog = UIHelper.showProgress(this, "注册", "提交数据中......", true);
        ApiClent.registerV2(appContext, email, password, accounts, mMotherLang, mLearnLang, new ApiClent.ClientCallback() {
            @Override
            public void onSuccess(Object data) {
                UIHelper.dismissProgress(dialog);
                Entity entity = (Entity) data;
                Log.i("tong test", "ApiClent.registerV2 success:" + entity.getMessage());
                switch (entity.getError_code()) {
                    case 1:
                        showToast(entity.getMessage());
                        setResult(RESULT_OK);
                        AppManager.getAppManager().finishActivity(RegisterActivity.this);
                        break;
                    default:
                        showToast(entity.getMessage());
                        break;
                }
            }

            @Override
            public void onFailure(String message) {
                UIHelper.dismissProgress(dialog);
                showToast(message);
            }

            @Override
            public void onError(Exception e) {
                UIHelper.dismissProgress(dialog);
                Log.e("tong test", "ApiClent.registerV2 error!", e);
            }
        });
        /*Registration reg = new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(XmppConnection.getConnection().getServiceName());
        reg.setUsername(accounts);
        reg.setPassword(password);
        reg.addAttribute("name", mingcheng);
        reg.addAttribute("email", email);

        reg.addAttribute("android", "geolo_createUser_android");
        PacketFilter filter = new AndFilter(new PacketIDFilter(
                reg.getPacketID()), new PacketTypeFilter(
                IQ.class));
        PacketCollector collector = XmppConnection.getConnection().
                createPacketCollector(filter);
        XmppConnection.getConnection().sendPacket(reg);
        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();// 停止请求results（是否成功的结果）
        if (result == null) {
            Toast.makeText(getApplicationContext(), "no response from server", Toast.LENGTH_SHORT).show();
        } else if (result.getType() == IQ.Type.ERROR) {
            if (result.getError().toString()
                    .equalsIgnoreCase("conflict(409)")) {
                Toast.makeText(getApplicationContext(), "this account exits", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "注册失败",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (result.getType() == IQ.Type.RESULT) {
            
        }
*/
    }

    private void login() {
        Intent intent = new Intent();
        intent.setClass(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
    
}
