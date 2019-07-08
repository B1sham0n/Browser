package android.example.browser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class MainActivity extends AppCompatActivity {
    WebView wv;
    SwipeRefreshLayout swipeLayout;
    EditText etLink;
    Button btnSearch, btnMore;
    Button btnBefore, btnNext, btnHome, btnMarks;
    DBHelper dbHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wv = findViewById(R.id.webView);
        swipeLayout = findViewById(R.id.swipe);
        etLink = findViewById(R.id.etLink);
        btnMore = findViewById(R.id.btnMore);
        btnSearch = findViewById(R.id.btnSearch);

        //mvc ap

        swipeLayout.setOnRefreshListener(refreshListener);
        btnSearch.setOnClickListener(searchListener);
        btnMore.setOnClickListener(moreListener);

        //TODO: добавить функционал кнопок в меню

        //TODO: создать БД и сохранять home и marks

        dbHelper = new DBHelper(this);
        setHome();
    }
    private String getAddress(){
        String etOutput = etLink.getText().toString();
        String search;
        if(etOutput.contains("http"))
            search = etOutput;
        else
            search = "https://" + etOutput;
        return search;
        //TODO: поиск по умолчанию в гугл (мб добавить отдельную кнопку)
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void openLink(String search){
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.setWebViewClient(new MyWebViewClient());

        wv.loadUrl(search);

        swipeLayout.setRefreshing(true);
    }
    private String getHome(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String linkHome;
        Cursor c = db.query("linkTable", null,null, null,
                null, null, null);

        //далее в цикле создаем childView до тех пор, пока не закончится бд
        //при этом каждый раз перемещаем курсор и берем новые значения строки
        if(c.moveToFirst()) {
            linkHome = c.getString(c.getColumnIndex("link"));//id=0 - HOME
        }
        else
            linkHome = "https://ya.ru";
        c.close();
        return linkHome;
    }
    private void setHome(){
        ContentValues cv = new ContentValues();
        db = dbHelper.getWritableDatabase();
        cv.put("link", wv.getUrl());

        db.update("linkTable", cv,"id = " + 0, null);
        //dbHelper.close();//TODO:проверить на баги, мб надо убрать
    }
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            wv.reload();
            wv.loadUrl( "javascript:window.location.reload( true )" );
        }
    };
    View.OnClickListener goHome = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            String linkHome = getHome();
            wv.loadUrl(linkHome);
            //TODO: сделать, чтобы тут читался id=0 (home)
        }
    };
    View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("NEXT");
                wv.goForward();
        }
    };
    View.OnClickListener beforeListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            System.out.println("LAST");
                wv.goBack();
        }
    };
    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String link = getAddress();
            openLink(link);
        }
    };
    View.OnClickListener moreListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LayoutInflater layoutInflater
                    = (LayoutInflater) getBaseContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);

            final View popupView = layoutInflater.inflate(R.layout.popup, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true);

            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            //popupWindow.setBackgroundDrawable(new BitmapDrawable());

            View parent = view.getRootView();

            btnBefore = popupView.findViewById(R.id.btnBefore);
            btnNext = popupView.findViewById(R.id.btnNext);
            btnHome = popupView.findViewById(R.id.btnHome);
            btnMarks = popupView.findViewById(R.id.btnMarks);

            btnBefore.setOnClickListener(beforeListener);
            btnNext.setOnClickListener(nextListener);

            popupWindow.showAtLocation(parent, Gravity.RIGHT, 0, 0);

        }
    };
}
    class MyWebViewClient extends WebViewClient {
    //без переопределения класса не будет открываться мой браузер
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    // Для старых устройств
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
class DBHelper extends SQLiteOpenHelper {

    String nameDB = "testDB";

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "testDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d(LOG_TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table linkTable ("
                + "id integer primary key autoincrement,"
                + "link text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}