package com.sails.hkiademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.sails.engine.SAILS;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;

import java.lang.ref.WeakReference;


/**
 * Created by Rocky on 2013/10/17.
 */
public class WelcomeActivity extends Activity {

    // Splash screen timer
    private static int WIFI_SETTING_OPEN = 4000;
    private SAILS msails;
    private SingalTurnOnHandler mHandler;
    private final static String KEY_READ_CLAIM = "read_claim";
    String NAMESPACE="http://tempuri.org/";
    String URL="http://eip.ecs.com.tw/Android/WebService.asmx";
    String METHOD_NAME="GetTabletInfoByMAC";
    String SOAP_ACTION="http://tempuri.org/"+METHOD_NAME;
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            gotoMainActivityThenFinish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActionBar().hide();
        msails= new SAILS(this);



//        if (!read) {
//            AlertDialog dialog = new AlertDialog.Builder(WelcomeActivity.this)
//                    .setCancelable(false)
//                    .setTitle(R.string.terms_service_disclaimer)
//                    .setMessage("「MyMap」服務條款\n" +
//                            "上次修改日期：2014 年 1 月 25 日\n" +
//                            "您若下載、安裝或使用 「MyMap」系列軟體、存取或使用 「MyMap」服務(以下統稱「產品」和「 服務」)，或是存取或使用「產品」所提供的任何內容，即表示您同意接受本服務條款(以下簡稱「 條款」)約束：請先詳閱本文件，再繼續下一步；此文件構成您與 SAILS 之間的約束協議。\n" +
//                            "您若下載、存取或使用「產品」，即表示您亦同意本條款。\n" +
//                            "1. 「產品」的使用。SAILS 授予您非專屬且不得轉讓的授權，讓您存取 「MyMap」軟體與服務，並可依據「條款」規定存取「產品」的「內容」(定義如下)。\n" +
//                            "2. 使用限制。除非您事先獲得 SAILS 的書面授權 (在適用情況下，可改由特定「內容」提供者授權)，否則不得從事下列行為：(a) 複製、翻譯、修改「內容」或其任何部分，或製作相關衍生作品；(b) 重新散佈、轉授權、出租、發佈、銷售、轉讓、以合約出租、推銷、轉移，或以其他任何方式提供第三方使用「產品」或「內容」；(c) 對「服務」或其任何部分進行逆向工程、反譯或嘗試擷取原始程式碼 (除非經適用法律的明確許可或要求)；(d) 使用「產品」時，讓您或任何人得以大量下載或大量提供任何「內容」，包括但不限於數字經緯度座標、圖像及可顯示的地圖資料；(e) 刪除、隱藏或以任何方式修改任何出現在「產品」或「內容」中的警告或連結；或 (f) 將「服務」或「內容」配搭任何下列裝置專用或相關的產品、系統或應用程式使用：(i) 即時導航或路線導引，包括但不限於與使用者感應裝置進行同步定位的路口導航提示；或 (g) 使用「產品」建立地方資訊或其他本地商家資訊的資料庫。\n" +
//                            "3. 適當行為；遵守法律與 SAILS 政策。您同意對「產品」使用期間的個人行為和內容負責，並承擔所有相關後果。您同意只將「產品」用於合法及正當目的，並遵守「條款」及任何 SAILS 提供的適用政策或指南。舉例來說，您同意不會在「服務」使用期間從事下列行為 (下列範例並未涵蓋所有違反規定的使用方式)：(a) 誹謗、濫用、騷擾、跟蹤、威脅或以其他方式違反他人法律權利 (例如隱私權和公開權)；(b) 上傳、張貼、以電子郵件寄送、散佈，或以任何其他方式提供任何不當、誹謗、猥褻或非法內容；(c) 上傳、張貼、散佈或以任何其他方式提供侵害任何一方之專利權、商標、版權、商業機密或其他專屬權利的內容 (除非貴用戶為此權利之擁有者、已向相關擁有者取得許可，或有其他法律依據可使用此類內容)；(d) 上傳、張貼、以電子郵件寄送、散佈或以任何其他方式宣傳層壓式行銷、提供連鎖信或破壞性的商業訊息或廣告；(e) 上傳、張貼、以電子郵件寄送、散佈或以任何其他方式提供任何適用法律、「條款」或任何適用之「產品」政策或指引禁止的其他內容、訊息或通訊。(f) 下載貴用戶知悉或理應知悉為他人非法散佈之檔案；(g) 假冒他人或實體，或是竄改或刪除「內容」、軟體或其他資料出處或來源的作者資訊、專屬設計或標籤；(h) 限制或阻止其他使用者使用及享有「產品」或 SAILS 服務；(i) 使用任何漫遊器、自動尋檢程式、網站搜尋/擷取應用程式或其他裝置，對 SAILS 服務或「內容」之任何部分進行擷取或建立索引，或蒐集使用者資訊並用於任何未經授權的用途；(j) 在提交內容中誤導或暗示 SAILS 贊助或認可該等內容；(k) 以自動方式或以偽裝或詐欺方法建立使用者帳戶；(l) 宣傳非法活動，或提供相關指引；(m) 鼓勵針對任何團體或個人的人身傷害；或 (n) 散佈病毒、網路蠕蟲、瑕疵、木馬程式或任何具破壞力的項目。\n" +
//                            "4. 「產品」的內容。 「MyMap」和可讓您存取及查看各種內容，包括但不限於攝影圖像、地圖、商家資訊、評論、交通，以及由 SAILS、其授權者和使用者所提供的其他相關資訊 (以下統稱「內容」)。此外，您可以選擇存取其他第三方透過 SAILS 服務 (例如 SAILS 小工具) 在「產品」中所提供的內容。您瞭解並同意以下規定：(a) 地圖資料、路線和相關「內容」只供計劃之用。天候狀況、施工區域、道路封閉或其他事件，可能導致道路狀況或路線與地圖查詢結果不同，使用此「內容」時請自行作出判斷。\n" +
//                            "6. 擔保免責聲明與責任限制。(a) SAILS 及其授權者對任何內容或「產品」之正確性或完整性並不提供聲明或擔保。我們會以商業上合理的技術與注意程度提供「服務」，希望您會盡情使用，但SAILS 或其供應商或經銷商均不對「服務」做出任何特定保證。\n" +
//                            "例如，我們不會就「服務」中的內容、「服務」之特定功能及其可靠性、可用性和符合您的需求的能力，做出任何承諾。我們僅以「現狀」提供「服務」。部分司法管轄區會規定應提供特定擔保，例如對適銷性、特殊用途適用性及未侵權之默示擔保。凡法律准許時，我們均排除一切擔保責任。\n" +
//                            " (b) SAILS聲明不提供任何與「內容」及「產品」相關的擔保，對使用「內容」或「產品」導致的任何損害或損失，概不承擔任何責任。\n")
//                    .setNegativeButton(R.string.agree,
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialoginterface, int i) {
//                                    sp1.edit().putBoolean(KEY_READ_CLAIM, true).commit();
//                                        gotoMainActivityThenFinish();   // AD Mode : 直接前往
//                                }
//                            }
//                    )
//                    .setPositiveButton(R.string.disagree,
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialoginterface, int i) {
//                                    finish();
//                                }
//                            }
//                    ).show();
//
//            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
//            textView.setTextSize(15);
//        } else {
//            gotoMainActivityThenFinish();   // AD Mode : 直接前往
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);

    }
    @Override
    protected void onResume() {
        super.onResume();
        final long time=System.currentTimeMillis();
        mHandler = new SingalTurnOnHandler(this);
        final SharedPreferences sp1 = getPreferences(MODE_PRIVATE);
        boolean read = sp1.getBoolean(KEY_READ_CLAIM, false);

        new Thread(new Runnable() {
            int count=0;
            @Override
            public void run() {
                while(true) {
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wInfo = wifiManager.getConnectionInfo();
                    String mac = wInfo.getMacAddress();
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    request.addProperty("strMAC",mac);
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.dotNet=true;
                    envelope.setOutputSoapObject(request);
                    AndroidHttpTransport androidHttpTransport = new AndroidHttpTransport(URL);
                    SoapPrimitive resultsRequestSOAP = null;
                    try {
                        androidHttpTransport.call(SOAP_ACTION, envelope);
                        Log.d("soap",request.toString());
                        //Since we know the result is of type primitive then, cast it to SoapPrimitive
                        resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();
                        final String res=resultsRequestSOAP.toString();
                        showWelcome(res);

                        break;
                    } catch (Exception e) {
                        if(count>3) {
                            showWelcome("Welcome Dear Guest");
                            break;
                        }
                        count++;
                        Log.e("Error", e.toString());
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                new AlertDialog.Builder(WelcomeActivity.this).setTitle(R.string.network_error)
//                                        .setMessage(R.string.network_error_msg).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        finish();
//                                    }
//                                }).show();
//
//                            }
//                        });
                    }

                }

            }
        }).start();
    }
    void showWelcome(final String welcome) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(welcome.toLowerCase().contains("welcome")) {
                    MainActivity.LANGUAGE="en";
                } else {
                    MainActivity.LANGUAGE="zh_TW";
                    ((TextView)findViewById(R.id.tvEnter)).setText("進入");
                }
                findViewById(R.id.tvWelcome).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.tvWelcome)).setText(welcome);
                findViewById(R.id.tvEnter).setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).duration(1500).playOn(findViewById(R.id.tvWelcome));
                YoYo.with(Techniques.FadeInUp).duration(1500).playOn(findViewById(R.id.tvEnter));
                final SharedPreferences sharedPreferences = getSharedPreferences("ecs",Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("welcome_msg", welcome).apply();
                findViewById(R.id.tvEnter).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handler.postDelayed(runnable, 0);
                    }
                });
//                            long time1=2000-(System.currentTimeMillis()-time);
//                            if(time1<0)
//                                time1=0;
//                            handler.postDelayed(runnable, time1);

            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        msails = null;
    }

    // 前往 MainActivity後，關閉 WelcomeActivity
    private void gotoMainActivityThenFinish() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkWiFiTurnOn() {
        if (!msails.isWiFiTurnOn()) {
            new AlertDialog.Builder(WelcomeActivity.this)
                    .setCancelable(false)
                    .setTitle(R.string.imap_welcome_title)
                    .setMessage(R.string.imap_check_wifi_msg)
                    .setNegativeButton(R.string.settings,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    mHandler.enableHandler();
                                    // 改為 Bluetooth 設定
                                    startActivity(new Intent(
                                            android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
                                }
                            }
                    )
                    .setPositiveButton(R.string.skip,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    gotoMainActivityThenFinish();
                                }
                            }
                    ).show();

        } else {
            gotoMainActivityThenFinish();
        }
    }

    class SingalTurnOnHandler extends Handler {

        WeakReference<WelcomeActivity> pointer;

        boolean enable = false;

        SingalTurnOnHandler(WelcomeActivity input) {
            pointer = new WeakReference<WelcomeActivity>(input);
        }

        @Override
        public void handleMessage(Message msg) {

            if (enable) {
                if (pointer.get() != null) {
                    if (!pointer.get().msails.isWiFiTurnOn()) {
                        this.sleep(100);
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                enable = false;
                                gotoMainActivityThenFinish();
                            }
                        }, WIFI_SETTING_OPEN);
                    }
                }
            }
        }

        public void enableHandler() {
            enable = true;
            this.sleep(0);
        }


        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
