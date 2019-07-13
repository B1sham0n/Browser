package android.example.browser;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ApplicationUtil extends MainActivity {
    public String getAddress(EditText etLink){
        String etOutput = etLink.getText().toString();
        String search = "";
        try {
            etOutput = URLEncoder.encode(etOutput, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(!etOutput.equals("")){
            if(etOutput.contains("http"))
                search = etOutput;
            else
                search = "https://" + etOutput;
        }
        return search;
    }
    public String getHome(DBHelper dbHelper, Context context){
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
        Toast.makeText(context, "Home page [" + linkHome + "] loaded ", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
        c.close();
        return linkHome;
    }
    public void setHome(DBHelper dbHelper, WebView wv, Context context){
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String newHome = wv.getUrl();
        if(newHome == null){
            newHome = HOME_PAGE_DEFAULT;//если строка с url пустая
        }
        cv.put("link", newHome);
        Cursor c = db.query(NAME_TABLE, null,null, null,
                null, null, null);
        //если БД пустая, то update не добавит новую строку
        if(c.getCount() != 0)
            db.update(NAME_TABLE, cv,"id = " + 1, null);
        else
            db.insert(NAME_TABLE, null, cv);
        Toast.makeText(context, "Home page[" + newHome + "] saved", Toast.LENGTH_SHORT).show();//всплывающее окно с текстом
    }
    public void openLink(String search, WebView wv, SwipeRefreshLayout swipeLayout){
        if(search != null){
            //wv.loadDataWithBaseURL(null, search, "text/html", "utf-8", null);
            wv.loadUrl(search);
            //TODO: почему-то не работает кириллица (котики.рф)
            swipeLayout.setRefreshing(true);
        }
        else
            swipeLayout.setRefreshing(false);
    }

    public void tryToOpenMark(Integer loadMark, DBHelper dbHelper, SwipeRefreshLayout swipeLayout, WebView wv){
        //при открытии закладки создается новая активити, если есть значение в интенте,
        //то берем его (это id в DB) и ищем ссылку, которую потом передаем в openLink()
        //Integer loadMark = getIntent().getIntExtra(KEY_MARK, 0);
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
                openLink(linkHome, wv, swipeLayout);
        }
    }



}
