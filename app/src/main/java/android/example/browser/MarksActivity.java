package android.example.browser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MarksActivity extends AppCompatActivity {

    ImageButton btnCancel;
    DBHelper dbHelper;
    LinearLayout parentMarks;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        View childView;
        Context mContext = getApplicationContext();
        parentMarks = findViewById(R.id.parentMarks);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Cursor c = db.query("linkTable", null,null, null,
                null, null, null);
        //TODO: обработка нажатия на закладку

        //TODO: отображать закладки красиво
        if(c.moveToFirst()) {
            c.moveToNext();//id = 0 its home
            do{
                childView = inflater.inflate(R.layout.mark, null);//создается новый child
                TextView tvMarkLink = childView.findViewById(R.id.tvLinkMark);
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
            }
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
