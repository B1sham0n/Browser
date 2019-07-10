package android.example.browser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MarksActivity extends AppCompatActivity {

    ImageButton btnCancel;
    DBHelper dbHelper;
    LinearLayout parentMarks;
    SQLiteDatabase db;
    WebView wv;
    String KEY_MARK = "currentMark";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);

        LayoutInflater layoutInflater
                = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View mainView = layoutInflater.inflate(R.layout.activity_main, null);
        wv = mainView.findViewById(R.id.webView);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        View childView;
        Context mContext = getApplicationContext();
        parentMarks = findViewById(R.id.parentMarks);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Cursor c = db.query("linkTable", null,null, null,
                null, null, null);
        if(c.moveToFirst()) {
            c.moveToNext();//id = 0 its home
            do{
                childView = inflater.inflate(R.layout.mark, null);//создается новый child
                TextView tvMarkLink = childView.findViewById(R.id.tvLinkMark);
                tvMarkLink.setOnClickListener(tvListener);
                tvMarkLink.setId(parentMarks.getChildCount());
                tvMarkLink.setTag("link_mark");//для updateIdTextView
                btnCancel = childView.findViewById(R.id.btnCancel);
                btnCancel.setTag("cancel");//для updateIdButton
                btnCancel.setOnClickListener(cancelListener);
                btnCancel.setId(parentMarks.getChildCount());

                tvMarkLink.setText(c.getString(c.getColumnIndex("link")));
                parentMarks.addView(childView);
            }while(c.moveToNext());

        }
    }
    View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Integer id = view.getId();
            System.out.println("id = " + id);
            if(id != null && id < parentMarks.getChildCount()){
                parentMarks.removeViewAt(id);
                deleteLinkFromDB(db, id);
                refreshDB(db);
                updateIdButton((ImageButton) view);
                updateIdTextView();
            }
        }
    };
    View.OnClickListener tvListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            TextView tv = (TextView) view;
            intent.putExtra(KEY_MARK, tv.getId());

            startActivity(intent);
        }
    };
    private void updateIdButton(ImageButton btn){
        View v = null;
        for(int i = 0; i < parentMarks.getChildCount(); i++){
            v = parentMarks.getChildAt(i);
            btn = v.findViewWithTag("cancel");
            if(btn != null){
                btn.setId(i);
            }
        }
    }
    private void updateIdTextView(){
        View v = null;
        TextView tv;
        for(int i = 0; i < parentMarks.getChildCount(); i++){
            v = parentMarks.getChildAt(i);
            tv = v.findViewWithTag("link_mark");
            if(tv != null){
                tv.setId(i);
            }
        }
    }
    private void deleteLinkFromDB(SQLiteDatabase db, Integer id){
        db = dbHelper.getWritableDatabase();
        id+=1;//id++, т.к. в таблице нумерация с 1
        db.delete("linkTable", "id = " + id, null);
    }
    private void refreshDB(SQLiteDatabase db){
        //потому что при удалении строки c id=3 она больше никогда не появится в бд
        //id=0 home
        ArrayList<String> tempDB = new ArrayList<String>();
        Cursor c = db.query("linkTable", null,null, null,
                null, null, null);
        if(c.moveToFirst()){
            do{
                tempDB.add(c.getString(c.getColumnIndex("link")));
            }while(c.moveToNext());
        }
        db.delete("linkTable", null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'linkTable'");
        dbHelper = new DBHelper(getApplicationContext());
        ContentValues cv = new ContentValues();
        for(int i = 0; i < tempDB.size(); i++){
            cv.put("link", tempDB.get(i));
            db.insert("linkTable",null, cv);
        }
    }
}
