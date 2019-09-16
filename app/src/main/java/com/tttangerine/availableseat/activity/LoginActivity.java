package com.tttangerine.availableseat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.tttangerine.availableseat.R;
import com.tttangerine.availableseat.db.User;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.tttangerine.availableseat.db.User.USER_NONE;
import static com.tttangerine.availableseat.db.User.USER_USING;

public class LoginActivity extends Activity implements View.OnClickListener {

    //布局内的控件
    private EditText et_name;
    private EditText et_password;
    private Button mLoginBtn;
    private ImageView iv_see_password;
    private LinearLayout mLinearLayout;

    User mCurrentUseruser = BmobUser.getCurrentUser(User.class);

    //修改昵称或密码标识
    private int changeType;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setupEvents();
        //如果用户已登录，则改为修改信息页面
        if (BmobUser.isLogin())
            changeInfo();
    }

    private void initViews() {
        mLoginBtn = findViewById(R.id.btn_login);
        et_name = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        iv_see_password = findViewById(R.id.iv_see_password);
        mLinearLayout = findViewById(R.id.login_layout_bg);
    }

    private void changeInfo() {

        //接受intent传递的信息，根据修改信息的类型显示布局
        changeType = getIntent().getIntExtra("change_type", 2);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏显示
        mLinearLayout = findViewById(R.id.login_layout_bg);
        mLinearLayout.setBackground(getDrawable(R.drawable.bg_easter_egg));  //背景
        mLoginBtn.setVisibility(View.GONE);
        Button changeInfoBtn = findViewById(R.id.btn_change);
        changeInfoBtn.setOnClickListener(this);
        changeInfoBtn.setVisibility(View.VISIBLE);

        //更改登陆页面标题，隐藏提示文字
        TextView title = findViewById(R.id.login_title);
        title.setText(getResources().getString(R.string.login_change_info));
        TextView hint = findViewById(R.id.login_hint);
        hint.setVisibility(View.GONE);

        //根据修改信息类型更改输入框提示
        if (changeType == 0){
            et_name.setHint("新昵称");
            RelativeLayout pw = findViewById(R.id.edit_layout_pw);
            pw.setVisibility(View.GONE);
        } else if (changeType == 1) {
            ImageView img_account = findViewById(R.id.img_account);
            img_account.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock));
            et_name.setHint("旧密码");
            et_password.setHint("新密码");
        }
    }

    private void setupEvents() {
        //注册点击事件
        mLoginBtn.setOnClickListener(this);
        iv_see_password.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();  //登陆
                break;

            case R.id.btn_change:
                change();  //修改信息
                break;

            case R.id.iv_see_password:
                setPasswordVisibility();  //改变图片并设置输入框的文本可见或不可见
                break;
        }
    }

    private void login() {

        //先做一些基本的判断，比如输入的用户命为空，密码为空，网络不可用等情况，都不需要去链接服务器了，而是直接返回提示错误
        if (getAccount().isEmpty()){
            showToast("您输入的账号为空");
            return;
        }

        if (getPassword().isEmpty()){
            showToast("您输入的密码为空");
            return;
        }

        ConnectivityManager mConnectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo == null){
            showToast("网络不可用");
            return;
        }

        //登录
        final User user = new User();
        user.setUsername(getAccount());
        user.setPassword(getPassword());
        user.login(new SaveListener<User>() {
            @Override
            public void done(User bmobUser, BmobException e) {
                if (e == null) {
                    if (BmobUser.getCurrentUser(User.class).USER_STATE == USER_NONE) {
                        Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                        startActivity(intent);
                        showToast("登录成功");
                        finish();
                    } else if (BmobUser.getCurrentUser(User.class).USER_STATE == USER_USING) {
                        Intent intent = new Intent(LoginActivity.this, TimerActivity.class);
                        startActivity(intent);
                        showToast("登录成功");
                        finish();
                    }
                } else {
                    //若登录出错，则查询用户名是否存在
                    BmobQuery<User> bmobQuery = new BmobQuery<>();
                    bmobQuery.addWhereEqualTo("username",getAccount());
                    final BmobException e1 = e;
                    bmobQuery.findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> object, BmobException e) {
                            if (object.size() != 0) {
                                //若用户名存在则报错
                                showToast("登陆失败: " + e1.getMessage());
                            } else {
                                //若用户名不存在则自动创建
                                signUp();
                            }
                        }
                    });
                }
            }
        });
    }

    private void change(){

        //先做一些基本的判断，比如输入的用户命为空，密码为空，网络不可用等情况，都不需要去链接服务器了，而是直接返回提示错误
        //然后根据修改信息类型更新相应的用户信息
        if (changeType == 0){

            if (getAccount().isEmpty()){
                showToast("您输入的账号为空");
                return;
            }

            ConnectivityManager mConnectivityManager = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo == null){
                showToast("网络不可用");
                return;
            }

            mCurrentUseruser.setUsername(getAccount());
            mCurrentUseruser.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {

                    if (e == null)
                        showToast("昵称修改成功");
                    else
                        showToast(e.getMessage());

                    finish();

                    HomepageActivity.instance.refreshUser();
                }
            });
        } else if (changeType == 1){

            //做一些基本的判断，比如输入的用户命为空，密码为空，网络不可用等情况，都不需要去链接服务器了，而是直接返回提示错误
            if (getAccount().isEmpty()){
                showToast("您输入的旧密码为空");
                return;
            }

            if (getPassword().isEmpty()){
                showToast("您输入的新密码为空");
                return;
            }

            ConnectivityManager mConnectivityManager = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo == null) {
                showToast("网络不可用");
                return;
            }

            BmobUser.updateCurrentUserPassword(getAccount(), getPassword(), new UpdateListener() {
                @Override
                public void done(BmobException e) {

                    if (e == null)
                        showToast("密码修改成功");
                    else
                        showToast(e.getMessage());

                    finish();
                }
            });
        }
    }

    //账号密码注册
    private void signUp() {
        final User user = new User();
        user.setUsername(getAccount());// + System.currentTimeMillis());
        user.setPassword(getPassword());// + System.currentTimeMillis());
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                    startActivity(intent);
                    showToast("登录成功");
                    finish();
                } else {
                    showToast("登录失败: " + e.getMessage());
                }
            }
        });
    }

    //设置密码可见和不可见的相互转换
    private void setPasswordVisibility() {
        if (iv_see_password.isSelected()) {
            iv_see_password.setSelected(false);
            //密码不可见
            et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        } else {
            iv_see_password.setSelected(true);
            //密码可见
            et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }

    }

    //获取账号
    public String getAccount() {
        return et_name.getText().toString().trim();//去掉空格
    }

    //获取密码
    public String getPassword() {
        return et_password.getText().toString().trim();//去掉空格
    }

    //显示通知
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //监听返回键
    @Override
    public void onBackPressed() {
        finish();
    }

}
