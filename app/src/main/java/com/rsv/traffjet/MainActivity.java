package com.rsv.traffjet;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;

import java.util.Comparator;
import java.util.List;

public  class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    private static final int MOBILENETW = 2;
    private static final int WIFINETW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Traffjet Mobile Data Control Activating", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }
        });



    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private ScrollView scrollView;
        private PieChart pieChart;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private long dataUsageTotalLast = 0;
        private RelativeLayout relativeLayout;
        private ListView listView;
        private ArrayAdapter<TraffjetAppItem> adapterApplications;
        private Handler handler;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */




        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mn_fragment, container, false);

            relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativefragmentlayout);
           // pieChart = (PieChart) rootView.findViewById(R.id.mainChart);
            listView = (ListView) rootView.findViewById(R.id.listView);
            listView.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            setListViewHeightBasedOnChildren(listView);
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningProcess = manager.getRunningAppProcesses();
            handler = new Handler();


            int sectionNumber =  getArguments().getInt(ARG_SECTION_NUMBER); // detect page

            if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED && TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED) {
                handler.postDelayed(runnable, 0);


                initAdapter(getActivity().getApplicationContext(), sectionNumber);
                listView = (ListView) rootView.findViewById(R.id.listView);
                listView.setAdapter(adapterApplications);
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "UNSUPPORTED", Toast.LENGTH_LONG).show();

            }
            if(runningProcess != null && runningProcess.size() > 0)
            {
            }
            else
                Toast.makeText(getActivity().getApplicationContext(), "No application is running", Toast.LENGTH_LONG).show();






            return rootView;
        }


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long mobile = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
                long total = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
//                            tvDataUsageWiFi.setText("" + (total - mobile) / 1024 + " Kb");
//                            tvDataUsageMobile.setText("" + mobile / 1024 + " Kb");
//                            tvDataUsageTotal.setText("" + total / 1024 + " Kb");
                if (dataUsageTotalLast != total) {
                    dataUsageTotalLast = total;
                    updateAdapter();
                }
                handler.postDelayed(runnable, 5000);
            }
        };



        public static void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null)
                return;

            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            int totalHeight = 0;
            View view = null;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                view = listAdapter.getView(i, view, listView);
                if (i == 0)
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewPager.LayoutParams.WRAP_CONTENT));

                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += view.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }





        public void initAdapter(final Context context, final int page_depend) {

            adapterApplications = new ArrayAdapter<TraffjetAppItem>(context, R.layout.item_app) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TraffjetAppItem app = getItem(position);

                    final View result;
                    if (convertView == null) {
                        result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
                    } else {
                        result = convertView;
                    }

                    TextView tvAppName = (TextView) result.findViewById(R.id.tvAppName);
                    TextView tvAppTraffic = (TextView) result.findViewById(R.id.tvAppTraffic);

                    final int iconSize = Math.round(32 * getResources().getDisplayMetrics().density);
                    tvAppName.setCompoundDrawablesWithIntrinsicBounds(
                            //app.icon,
                            new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                                    ((BitmapDrawable) app.getIcon(context.getPackageManager())).getBitmap(), iconSize, iconSize, true)
                            ),
                            null, null, null
                    );
                    tvAppName.setText(app.getApplicationLabel(context.getPackageManager()));
                    tvAppTraffic.setText(Integer.toString(app.getTotalUsageKb()) + " Kb");
//                    if(page_depend==WIFINETW)
//                        tvAppTraffic.setText(Integer.toString(app.getWifiKb()) + " Kb");
//                    else
//                        tvAppTraffic.setText(Integer.toString(app.getMobileKb()) + " Kb");
                    return result;
                }
                @Override
                public int getCount() {
                    return super.getCount();
                }

                @Override
                public Filter getFilter() {
                    return super.getFilter();
                }
            };

// TODO: resize icon once
            for (ApplicationInfo app : context.getPackageManager().getInstalledApplications(0)) {
                TraffjetAppItem item = TraffjetAppItem.create(app);
                if(item != null) {
                    adapterApplications.add(item);
                }
            }
        }

        public void updateAdapter() {
            for (int i = 0, l = adapterApplications.getCount(); i < l; i++) {
                TraffjetAppItem app = adapterApplications.getItem(i);
                app.updateStats();
            }

            adapterApplications.sort(new Comparator<TraffjetAppItem>() {
                @Override
                public int compare(TraffjetAppItem lhs, TraffjetAppItem rhs) {
                    return (int)(rhs.getTotalUsageKb() - lhs.getTotalUsageKb());
                }
            });
            adapterApplications.notifyDataSetChanged();
        }




































    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Wi-Fi";
                case 1:
                    return "Mobile Networks";

            }
            return null;
        }
    }


}