package jp.co.drecom.newmapintegration;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import jp.co.drecom.newmapintegration.utils.ListAdapter;
import jp.co.drecom.newmapintegration.utils.LocationDBHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendListFragment extends Fragment {

    public static String FRAGMENT_TAG = "FriendListFrgment";

    //for refresh
    private ListAdapter mFriendListAdapter;
    private LocationDBHelper mFriendDBHelper;
    private SwipeListView mFriendListView;


    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriendDBHelper = new LocationDBHelper(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);
        mFriendListView = (SwipeListView) view.findViewById(R.id.friendListView);
        mFriendDBHelper.mLocationDB = mFriendDBHelper.getReadableDatabase();
        Cursor cursor = mFriendDBHelper.getFriendList();

        mFriendListAdapter = new ListAdapter(getActivity(), cursor, false);

        mFriendListView.setAdapter(mFriendListAdapter);

        mFriendListView.setSwipeListViewListener(new BaseSwipeListViewListener(){

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                super.onStartOpen(position, action, right);
            }
        });

        mFriendDBHelper.mLocationDB.close();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO
        //save the selected item to DB
        updateFriendSelected();
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // If the drawer is open, show the global app actions in the action bar. See also
//        // showGlobalContextActionBar, which controls the top-left area of the action bar.
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.add(Menu.NONE, 1, Menu.NONE, "Add").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        return super.onOptionsItemSelected(item);
//    }

    private void updateFriendSelected() {
        //TODO
        if (mFriendListAdapter == null) {
            return;
        } else {
            int sum = mFriendListAdapter.getCount();
//            for (int i = 0; i < sum; i++) {
//                mFriendListAdapter.
//            }
//            mFriendListView.
        }
    }

    public void friendListUpdate() {
        mFriendDBHelper.mLocationDB = mFriendDBHelper.getReadableDatabase();
        Cursor cursor = mFriendDBHelper.getFriendList();

        mFriendListAdapter.changeCursor(cursor);
        mFriendListAdapter.notifyDataSetChanged();
        mFriendDBHelper.mLocationDB.close();
    }

}
