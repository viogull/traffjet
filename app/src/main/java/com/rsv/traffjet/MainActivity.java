package com.rsv.traffjet;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.stealthcopter.networktools.ARPInfo;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.PortScan;
import com.stealthcopter.networktools.WakeOnLan;
import com.stealthcopter.networktools.ping.PingResult;

import java.io.IOException;
import java.util.ArrayList;
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



    private static final int DIA = 2;
    private static final int LIST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArrayList<Entry> trafficValues = new ArrayList<>();
        ArrayList<String> trafficNames = new ArrayList<>();
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

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab!=null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long t = (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes());
                    Snackbar.make(view, "Traffjet About " + t + " Bytes",
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Traffjet About")
                            .setMessage("Traffjet is a simple utils with the next functionality:" +
                                    "\n 1.Get information about application network usage with diagram."
                                    + "\n 2.Ping hosts at your networks."
                                    + "\n 3.Scan ports at selected host at your networks." +
                                    "\n 4. Wake-On-Lan Technology.")
                            .setCancelable(false)
                            .setIcon(R.drawable.data_usage)
                            .setNegativeButton("Continue using.",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();




                }
            });
        }
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
        private static final String ARG_SECTION_NUMBER = "section_number";
        private long dataUsageTotalLast = 0;
        private RelativeLayout relativeLayout;
        private ListView listView;
        private ArrayAdapter<TraffjetAppItem> adapterApplications;
        private Handler handler;
        private View rootView;
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
            int sectionNumber =  getArguments().getInt(ARG_SECTION_NUMBER); // detect page
            rootView = inflater.inflate(R.layout.mn_fragment, container, false);
            relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativefragmentlayout);
            listView = (ListView) rootView.findViewById(R.id.listView);
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningProcess = manager.getRunningAppProcesses();
            handler = new Handler();
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
                handler.postDelayed(runnable, 50000);
            }
        };


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

    public static class StatisticFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private PieChart pieChart;
        private static final String ARG_SECTION_NUMBER = "section_number";
        private View rootView;
        private ListView list;
        private ArrayAdapter<TraffjetAppItem> adapter;

        public StatisticFragment() {
        }
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static StatisticFragment newInstance(int sectionNumber) {
            StatisticFragment fragment = new StatisticFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber =  getArguments().getInt(ARG_SECTION_NUMBER); // detect page
                rootView = inflater.inflate(R.layout.diag, container, false);
                pieChart = (PieChart) rootView.findViewById(R.id.mainChart);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        InitAndAddDataToPieChart();
                    }
                });
                thread.start();
            long mobile_txrx = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
            Toast.makeText(getActivity().getApplicationContext(),
                    "Mobile traffic " +mobile_txrx + " Bytes Totally",
                    Toast.LENGTH_LONG).show();
            return rootView;
        }
        public void InitAndAddDataToPieChart()
        {
            final ArrayList<String> trafficNames = new ArrayList<>();
            ArrayList<Entry> trafficValues = new ArrayList<>();
            ArrayList<TraffjetAppItem> list = new ArrayList<>();

            for (ApplicationInfo app : getActivity().getBaseContext().getPackageManager().getInstalledApplications(0)) {
                TraffjetAppItem item = new TraffjetAppItem(app);
                item.setMobileTraffic(false);
                list.add(item);
            }
            pieChart.invalidate();
            float bts;
            int i = 0;
            Log.d("", "Cycle started, shown:: " + pieChart.isShown());
            for(TraffjetAppItem app : list) {
                bts = app.getTotalUsageKb();
                if(bts>0) {
                    trafficValues.add(new Entry(app.getTotalUsageKb(), i));
                    trafficNames.add(app.getApplicationLabel(getActivity().getApplicationContext().getPackageManager()));
                    Log.d("DATATAG", "name: " + app.getApplicationLabel(getActivity().getApplicationContext().getPackageManager()) +
                            " traffic: " + app.getTotalUsageKb());
                    i++;
                }
                if(trafficNames.size()>7)
                    break;
            }
            pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    if (e == null)
                        return;
                    Toast.makeText(getActivity().getApplicationContext(),
                            trafficNames.get(e.getXIndex()) + " = " + e.getVal() + " KB", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onNothingSelected() {
                }
            });





                PieDataSet set = new PieDataSet(trafficValues, "");
            ArrayList<Integer> colors = new ArrayList<Integer>();


            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);
            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);
            pieChart.setDrawSliceText(false);
            pieChart.setDescription("");
            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);
            colors.add(ColorTemplate.getHoloBlue());
                set.setColors(colors);
                PieData data = new PieData(trafficNames, set);
                data.setValueTextSize(9f);
                data.setValueTextColor(Color.BLACK);
                pieChart.setData(data);


            getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pieChart.highlightValues(null);
                        pieChart.setDrawHoleEnabled(true);
                        pieChart.setHoleRadius(7);
                        pieChart.setTransparentCircleRadius(10);
                        pieChart.setRotationAngle(0);
                        pieChart.setRotationEnabled(true);
                        pieChart.animateX(5000);
                        pieChart.animateY(5000);
                        Log.d("DATATAG SHOW", "isShown: " + pieChart.isShown());
                        pieChart.invalidate();
                    }
                });


        }

    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */









    public static class NetworkToolFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private View rootView;

        public NetworkToolFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static NetworkToolFragment newInstance(int sectionNumber) {
            NetworkToolFragment fragment = new NetworkToolFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private TextView resultText;
        private EditText editIpAdress;
        private Button wakeonlan, ping, scanport;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER); // detect page
            rootView = inflater.inflate(R.layout.tools, container, false);
            resultText = (TextView) rootView.findViewById(R.id.resultText);
            editIpAdress = (EditText) rootView.findViewById(R.id.editIpAddress);

            scanport = (Button) rootView.findViewById(R.id.portScanButton);
            wakeonlan = (Button) rootView.findViewById(R.id.wolButton);
            ping = (Button) rootView.findViewById(R.id.pingButton);

            ping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                doPing();
                            }
                            catch (Exception ex)
                            {
                                ex.printStackTrace();

                            }
                        }
                    });
                    thread.start();
                }
            });


            scanport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override public void run() {
                            try {
                                doPortScan();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });

            wakeonlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(new Runnable() {
                        @Override public void run() {
                            try {
                                doWakeOnLan();
                            }
                            catch (Exception e){
                                e.printStackTrace();

                            }
                        }
                    }).start();
                }
            });

            return rootView;
        }

        private void appendResultToText(final String text, final int text_color)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int start = resultText.getText().length();
                    resultText.append(text);
                    int end = resultText.getText().length();
                    Spannable spannableText = (Spannable) resultText.getText();
                    spannableText.setSpan(new ForegroundColorSpan(text_color), start, end, 0);
                    resultText.append("\n");                }
            });
        }

        private void doPing() throws Exception
        {
            String ipAdress = editIpAdress.getText().toString();
            if(TextUtils.isEmpty(ipAdress))
            {
                appendResultToText("Invalid IP Adress", Color.RED);
                return;
            }

            PingResult pingResult = Ping.onAddress(ipAdress).setTimeOutMillis(1000).doPing();
            appendResultToText("Pinging Adress: " + pingResult.getAddress().getHostAddress(), Color.BLUE);
            appendResultToText("Hostname: " + pingResult.getAddress().getHostName(), Color.BLUE);
            if(pingResult.getTimeTaken()==0.0f)
                appendResultToText(String.format("%.2f ms",pingResult.getTimeTaken()),Color.RED);
            else
                appendResultToText(String.format("%.2f ms",pingResult.getTimeTaken()),Color.GREEN);

            Ping.onAddress(ipAdress).setTimeOutMillis(1000).setTimes(5).doPing(new Ping.PingListener() {
                @Override
                public void onResult(PingResult pingResult) {
                    if(pingResult.getTimeTaken()==0.0f)
                        appendResultToText(String.format("%.2f ms",pingResult.getTimeTaken()),Color.RED);
                    else
                        appendResultToText(String.format("%.2f ms",pingResult.getTimeTaken()),Color.GREEN);                }

                @Override
                public void onFinished() {

                }
            });
        }


        public void doWakeOnLan() throws IllegalArgumentException
        {
            String ipAdresse = editIpAdress.getText().toString();
            if (TextUtils.isEmpty(ipAdresse)){
                appendResultToText("Invalid Ip Address", Color.RED);
                return;
            }

            appendResultToText("IP address: "+ ipAdresse, Color.BLUE);

            // Get mac address from IP (using arp cache)
            String macAddress = ARPInfo.getMACFromIPAddress(ipAdresse);

            if (macAddress == null){
                appendResultToText("Could not find MAC address, cannot send WOL packet without it.", Color.RED);
                return;
            }

            appendResultToText("MAC address: "+macAddress, Color.BLUE);
            appendResultToText("IP address2: "+ARPInfo.getIPAddressFromMAC(macAddress), Color.BLACK);

            // Send Wake on lan packed to ip/mac
            try {
                WakeOnLan.sendWakeOnLan(ipAdresse, macAddress);
                appendResultToText("WOL Packet sent", Color.BLACK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void doPortScan() throws Exception{
            String ipAddress = editIpAdress.getText().toString();

            if (TextUtils.isEmpty(ipAddress)){
                appendResultToText("Invalid Ip Address", Color.RED);
                return;
            }

            appendResultToText("PortScanning IP: "+ipAddress, Color.BLUE);
            ArrayList<Integer> openPorts = PortScan.onAddress(ipAddress).setPort(21).doScan();

            PortScan.onAddress(ipAddress).setTimeOutMillis(1000).setPortsAll().doScan(new PortScan.PortListener() {
                @Override
                public void onResult(int portNo, boolean open) {
                    if (open) appendResultToText("Open: "+portNo, Color.GREEN);
                }

                @Override
                public void onFinished(ArrayList<Integer> openPorts) {
                    appendResultToText("Open Ports: "+openPorts.size(), Color.BLACK);
                }
            });


        }

        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
         * one of the sections/tabs/pages.
         */
    }




    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0: return PlaceholderFragment.newInstance(position+1);
                case 1: return StatisticFragment.newInstance(position+1);
                case 2: return NetworkToolFragment.newInstance(position+1);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Apps usage";
                case 1:
                    return "Stats in diagrams";
                case 2:
                    return "Network Tools";

            }
            return null;
        }
    }




}
