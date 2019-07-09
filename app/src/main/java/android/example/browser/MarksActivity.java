package android.example.browser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MarksActivity extends AppCompatActivity {

    ImageButton btnCancel;
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);
        //TODO: обработка удаления закладки

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        View childView;
        Context mContext = getApplicationContext();
        LinearLayout parentMarks = findViewById(R.id.parentMarks);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //childView = inflater.inflate(R.layout.mark, null);
        //btnCancel = childView.findViewById(R.id.btnCancel);

        Cursor c = db.query("linkTable", null,null, null,
                null, null, null);

        if(c.moveToFirst()) {
            c.moveToNext();//id = 0 its home
            do{
                childView = inflater.inflate(R.layout.mark, null);
                TextView tvMarkLink = childView.findViewById(R.id.tvLinkMark);
                btnCancel = childView.findViewById(R.id.btnCancel);
                //TODO: добавить слушателя для кнопки отмены
                tvMarkLink.setText(c.getString(c.getColumnIndex("link")));
                parentMarks.addView(childView);
            }while(c.moveToNext());

        }

    }
}
