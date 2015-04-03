package jp.co.drecom.newmapintegration.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import jp.co.drecom.newmapintegration.R;

/**
 * Created by huang_liangjin on 2015/03/31.
 */
public class ListAdapter extends CursorAdapter{
    private LayoutInflater mInflater;

    private LocationDBHelper mDBhelper;



    class ViewHolder {
        CheckBox friendSelected;
        TextView friendMail;
        Button friendDelete;
    }

    /**
     * @param context
     * @param c
     * @param autoRequery
     */
    //set autoRequery to false
    public ListAdapter(Context context, Cursor c, boolean autoRequery) {
        //must contain a column named _id
        super(context, c, autoRequery);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDBhelper = new LocationDBHelper(context);
    }


    /**
     * @inheritDoc
     */
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        //
        NewLog.logD("listAdapter.bindView");
        ViewHolder holder = (ViewHolder) view.getTag();

        //
        final int id = cursor.getInt(
                cursor.getColumnIndexOrThrow(LocationDBHelper.MY_FRIEND_ID));
        final int selected = cursor.getInt(
                cursor.getColumnIndexOrThrow(LocationDBHelper.MY_FRIEND_SELECTED));
        final String mail = cursor.getString(cursor
                .getColumnIndexOrThrow(LocationDBHelper.MY_FRIEND_MAIL));
        NewLog.logD("listAdapter.bindView whether selected" + selected + " mail " + mail);
        //
        holder.friendSelected.setChecked((selected == 1) ? true : false);
        holder.friendMail.setText(mail);

        Button deleteButton = (Button) view.findViewById(R.id.friend_item_delete);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecordWithId(id);
            }
        });

    }

    /**
     * @inheritDoc
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //
        final View view = mInflater.inflate(R.layout.list_item, null);

        ViewHolder holder = new ViewHolder();
        holder.friendSelected = (CheckBox) view.findViewById(R.id.friend_selected);
        holder.friendMail = (TextView) view.findViewById(R.id.friend_mail);
        holder.friendDelete = (Button) view.findViewById(R.id.friend_item_delete);

        view.setTag(holder);

        return view;
    }

    private Cursor deleteRecordWithId(int id) {
        mDBhelper.mLocationDB = mDBhelper.getWritableDatabase();
        mDBhelper.deleteFriend(id);
        Cursor cursor = mDBhelper.getFriendList();
        changeCursor(cursor);
        notifyDataSetChanged();
        mDBhelper.mLocationDB.close();
        return cursor;
    }

    private void updateRecordWithId(int id) {

    }

}
