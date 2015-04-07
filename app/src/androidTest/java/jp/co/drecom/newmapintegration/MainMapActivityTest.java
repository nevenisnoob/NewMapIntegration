package jp.co.drecom.newmapintegration;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.View;
import android.widget.ListView;

import java.util.Map;

/**
 * Created by huang_liangjin on 2015/04/03.
 */
//for MainMapActivity Test
public class MainMapActivityTest extends ActivityInstrumentationTestCase2<MainMapActivity> {

    private MainMapActivity mainMapActivity;


    private NavigationDrawerFragment navigationDrawerFragment;

    private NewMapFragment newMapFragment;
    private DatePickingFragment datePickingFragment;
    private FriendListFragment friendListFragment;
    private MapSettingFragment mapSettingFragment;


    public MainMapActivityTest(Class<MainMapActivity> activityClass) {
        super(activityClass);
    }

    public MainMapActivityTest () {
        super(MainMapActivity.class);
    }

    //add variable declarations in setUp() function.
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mainMapActivity = getActivity();
        datePickingFragment = (DatePickingFragment)mainMapActivity.getSupportFragmentManager()
                .findFragmentByTag(DatePickingFragment.FRAGMENT_TAG);
        friendListFragment = (FriendListFragment) mainMapActivity.getSupportFragmentManager()
                .findFragmentByTag(FriendListFragment.FRAGMENT_TAG);
        mapSettingFragment = (MapSettingFragment) mainMapActivity.getSupportFragmentManager()
                .findFragmentByTag(MapSettingFragment.FRAGMENT_TAG);
        newMapFragment = (NewMapFragment) mainMapActivity.getFragmentManager()
                .findFragmentById(R.id.map);
        navigationDrawerFragment = (NavigationDrawerFragment) mainMapActivity
                .getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
    }

    public void testPreconditions() {
        assertNotNull("newMapFragment is null", newMapFragment);
        assertNotNull("navigationDrawerFragment is null", navigationDrawerFragment);
//
//        assertNotNull("friendListFragment is null", friendListFragment);
//        assertNotNull("mapSettingFragment is null", mapSettingFragment);
    }

    public void test() {
//        TouchUtils.tapView();
        String[] menuArray = {getActivity().getString(R.string.title_section1),
                getActivity().getString(R.string.title_section2),
                getActivity().getString(R.string.title_section3),
                getActivity().getString(R.string.title_section4),
                getActivity().getString(R.string.title_section5)};
        ListView navigationList = (ListView)
        navigationDrawerFragment.getView().findViewById(R.id.navigation_drawer_list);
        assertNotNull("navigation listview is null", navigationList);
//        TouchUtils.tapView(this, (View)navigationList.getAdapter().getItem(1));
        int slideMenuCount = navigationList.getAdapter().getCount();

        for (int i =0; i < slideMenuCount; i++) {
            assertEquals("not same", menuArray[i], navigationList.getAdapter().getItem(i).toString());
        }

//        assertNotNull("datePickingFragment is null", datePickingFragment);

        /*
        TouchUtils.clickView(this, button);
         */


    }

}
