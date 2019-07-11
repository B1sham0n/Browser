package android.example.browser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    WebView wv;
    private SwipeRefreshLayout swipeLayout;
    EditText etLink;
    Button btnSearch, btnMore;
    Button btnSaveHome;
    Button btnBefore, btnNext, btnHome, btnMarks, btnSaveMark, btnRefresh, btnAddPage;
    DBHelper dbHelper;
    SQLiteDatabase db;
    String  HOME_PAGE_DEFAULT = "https://ya.ru";
    String NAME_TABLE = "linkTable";
    String KEY_MARK = "currentMark";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wv = findViewById(R.id.webView);
        swipeLayout = findViewById(R.id.swipe);
        etLink = findViewById(R.id.etLink);
        btnMore = findViewById(R.id.btnMore);
        btnSearch = findViewById(R.id.btnSearch);
        btnSaveHome = findViewById(R.id.btnSaveHome);
        //mvc ap
        //etLink.setBackground(R.drawable.custom_rectangle);

        swipeLayout.setOnRefreshListener(refreshListener);
        btnSearch.setOnClickListener(searchListener);
        btnMore.setOnClickListener(moreListener);
        btnSaveHome.setOnClickListener(saveHomeListener);

        swipeLayout.setColorSchemeResources(R.color.refresh_1, R.color.refresh_2, R.color.refresh_3);
        //TODO: погуглить про историю, вынести переменные в отдельный файл

        dbHelper = new DBHelper(this);
        //setHome();
        swipeLayout.setRefreshing(false);
        tryToOpenMark();
    }
    private String getAddress(){
        String etOutput = etLink.getText().toString();
        String search;
        if(etOutput.contains("http"))
            search = etOutput;
        else
            search = "https://" + etOutput;
        return search;
    }
    private void tryToOpenMark(){
        //при открытии закладки создается новая активити, если есть значение в интенте,
        //то берем его (это id в DB) и ищем ссылку, которую потом передаем в openLink()
        Integer loadMark = getIntent().getIntExtra(KEY_MARK, 0);
        if(loadMark != 0){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String linkHome = null;
            Cursor c = db.query(NAME_TABLE, null,null, null,
                    null, null, null);
            Integer i = 0;
            if(c.moveToFirst()){
                do{
                    if (i == loadMark)
                        linkHome = c.getString(c.getColumnIndex("link"));
                    i++;
                }while(c.moveToNext());
            }
            if(linkHome != null)
                openLink(linkHome);
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void openLink(String search){
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.setWebChromeClient(new WebChromeClient());
        wv.setWebViewClient(new MyWebViewClient());
        if(search != null){

            WebSettings settings = wv.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            //wv.loadDataWithBaseURL(null, search, "text/html", "utf-8", null);
            wv.loadUrl(search);
            //TODO: почему-то не работает кириллица (котики.рф)
            swipeLayout.setRefreshing(true);
        }
        else
            swipeLayout.setRefreshing(false);
    }
    private String getHome(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String linkHome;
        Cursor c = db.query(NAME_TABLE, null,null, null,
                null, null, null);

        //далее в цикле создаем childView до тех пор, пока не закончится бд
        //при этом каждый раз перемещаем курсор и берем новые значения строки
        if(c.moveToFirst())
            linkHome = c.getString(c.getColumnIndex("link"));//id=0 - HOME
        else
            linkHome = HOME_PAGE_DEFAULT;
        Toast.makeText(getApplicationContext(), "Home page [" + linkHome + "] loaded ", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
        c.close();
        return linkHome;
    }
    private void setHome(){
        ContentValues cv = new ContentValues();
        db = dbHelper.getWritableDatabase();
        String newHome = wv.getUrl();
        if(newHome == null){
          newHome = HOME_PAGE_DEFAULT;//если строка с url пустая
        }
        cv.put("link", newHome);
        Cursor c = db.query(NAME_TABLE, null,null, null,
                null, null, null);
        //если БД чистая, то update не добавит новую строку
        if(c.getCount() != 0)
            db.update(NAME_TABLE, cv,"id = " + 1, null);
        else
            db.insert(NAME_TABLE, null, cv);
        Toast.makeText(this, "Home page[" + newHome + "] saved", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
    }
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            wv.reload();
            wv.loadUrl( "javascript:window.location.reload( true )" );
            swipeLayout.setRefreshing(false);
        }
    };
    View.OnClickListener saveHomeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setHome();
        }
    };
    View.OnClickListener goHomeListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            String linkHome = getHome();
            Toast.makeText(getApplicationContext(), "Home page [" + linkHome + "] loaded", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
            wv.setWebViewClient(new WebViewClient());//иначе открывается стандратный браузер
            wv.loadUrl(linkHome);
        }
    };
    View.OnClickListener nextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            wv.goForward();
        }
    };
    View.OnClickListener beforeListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            wv.goBack();
        }
    };
    View.OnClickListener btnRefreshListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            wv.reload();
        }
    };
    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String link = getAddress();
            openLink(link);
        }
    };
    View.OnClickListener marksListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), MarksActivity.class);
            startActivity(intent);
        }
    };
    View.OnClickListener saveMarkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ContentValues cv = new ContentValues();
            db = dbHelper.getWritableDatabase();
            String newMark = wv.getUrl();
            if(newMark != null){
                cv.put("link", newMark);
                db.insert(NAME_TABLE, null, cv);
                Toast.makeText(getApplicationContext(), "Mark [" + newMark + "] saved", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
            }
        }
    };
    View.OnClickListener btnAddPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
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
            btnSaveMark = popupView.findViewById(R.id.btnSaveMark);
            btnRefresh = popupView.findViewById(R.id.btnRefresh);
            btnAddPage = popupView.findViewById(R.id.btnAddPage);

            btnBefore.setOnClickListener(beforeListener);
            btnNext.setOnClickListener(nextListener);
            btnHome.setOnClickListener(goHomeListener);
            btnMarks.setOnClickListener(marksListener);
            btnSaveMark.setOnClickListener(saveMarkListener);
            btnRefresh.setOnClickListener(btnRefreshListener);
            btnAddPage.setOnClickListener(btnAddPageListener);

            popupWindow.showAtLocation(parent, Gravity.RIGHT|Gravity.TOP, 0, 0);

        }
    };
    public void setURl(String url){
        System.out.println("_________________URL: " + url);
        etLink = findViewById(R.id.etLink);
        if(url != null)
            etLink.setText(url);
    }
    public EditText getET(){
        EditText et = findViewById(R.id.etLink);
        Intent intent = new Intent();
        return  et;
    }

    class MyWebViewClient extends WebViewClient{
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
        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            Toast.makeText(view.getContext(), "Url [" + url + "] loaded", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
            swipeLayout.setRefreshing(false);
            etLink.setText(wv.getUrl());
        }
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //если поиск url вернул ошибку, то ищем в гугле
            view.loadUrl("https://www.google.com/search?q=" + failingUrl.replaceAll(".*https://", "").replaceFirst(".$",""));
            swipeLayout.setRefreshing(true);
            //replace удаляет https и / в конце, чтобы выполнить поиск в гугле
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }
}
    class DBHelper extends SQLiteOpenHelper {
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