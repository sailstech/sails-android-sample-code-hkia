package com.sails.hkiademo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/ryg_test/log/";
    private static final String FILE_NAME = "crash";

    //log文件的后缀名
    private static final String FILE_NAME_SUFFIX = ".trace";

    private static CrashHandler sInstance = new CrashHandler();

    //系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private Context mContext;

    //构造方法私有，防止外部构造多个实例，即采用单例模式
    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    //这里主要完成初始化工作
    public void init(Context context) {
        //获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        //获取Context，方便内部使用
        mContext = context.getApplicationContext();
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug
        uploadExceptionToServer(ex);
        //打印出当前调用栈信息
        ex.printStackTrace();

        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
//            Process.killProcess(Process.myPid());
        }

    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
    }



    private void uploadExceptionToServer(Throwable ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        HashMap<String,Object> error = new HashMap<>();
        error.put("stacktrace",errors.toString());
        ParseObject parseObject= new ParseObject("error");
        parseObject.put("device_info", GeneralUsage.getMixpanel().getDeviceInfo().toString());
        parseObject.put("stack_track",error);
        try {
            parseObject.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}