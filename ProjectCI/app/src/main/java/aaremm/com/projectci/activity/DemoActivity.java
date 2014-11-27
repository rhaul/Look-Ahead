package aaremm.com.projectci.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import aaremm.com.projectci.R;
import butterknife.ButterKnife;
import butterknife.InjectView;


public class DemoActivity extends Activity implements View.OnClickListener, ToolTipView.OnToolTipViewClickedListener {

    @InjectView(R.id.b_demo_iDo)Button ok;
    @InjectView(R.id.b_demo_iDont)Button iWont;
    @InjectView(R.id.b_demo_passenger)Button mPass;
    @InjectView(R.id.b_demo_indoor)Button mIndoor;
    @InjectView(R.id.b_demo_maps)Button mMaps;
    @InjectView(R.id.activity_main_tooltipRelativeLayout)ToolTipRelativeLayout mToolTipFrameLayout;

    private ToolTipView okToolTipView;
    private ToolTipView iWontToolTipView;
    private ToolTipView mPassToolTipView;
    private ToolTipView mIndoorToolTipView;
    private ToolTipView mMapsToolTipView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        ButterKnife.inject(this);

        ok.setOnClickListener(this);
        iWont.setOnClickListener(this);
        mMaps.setOnClickListener(this);
        mIndoor.setOnClickListener(this);
        mPass.setOnClickListener(this);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addOkToolTipView();
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addIWOntToolTipView();
            }
        }, 700);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addmPassToolTipView();
            }
        }, 900);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addmIndoorToolTipView();
            }
        }, 1100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addmMapsToolTipView();
            }
        }, 1300);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void addOkToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("It would turn off your Phone Screen.")
                .withColor(getResources().getColor(R.color.emarldD));

        okToolTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.b_demo_iDo));
        okToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addIWOntToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("It would do nothing. You can carry on as you were.")
                .withColor(getResources().getColor(R.color.alizarin));

        iWontToolTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.b_demo_iDont));
        iWontToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addmPassToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Same as Indoor Button but with Passenger mode.")
                .withColor(getResources().getColor(R.color.wet));

        mPassToolTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.b_demo_passenger));
        mPassToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addmIndoorToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("It would set your current mode to Indoor. The app wont track your activity until you disable this mode by swiping off the notification.")
                .withColor(getResources().getColor(R.color.blue));

        mIndoorToolTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.b_demo_indoor));
        mIndoorToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addmMapsToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Same as Indoor Button but with Passenger mode.")
                .withColor(getResources().getColor(R.color.amethyst));

        mMapsToolTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.b_demo_maps));
        mMapsToolTipView.setOnToolTipViewClickedListener(this);
    }

    @Override
    public void onClick(final View view) {
        int id = view.getId();
        if (id == R.id.b_demo_iDo) {
            if (okToolTipView == null) {
                addOkToolTipView();
            } else {
                okToolTipView.remove();
                okToolTipView = null;
            }

            Toast.makeText(this,"It would turn off your Phone Screen.",Toast.LENGTH_LONG).show();

        } else if (id == R.id.b_demo_iDont) {
            if (iWontToolTipView == null) {
                addIWOntToolTipView();
            } else {
                iWontToolTipView.remove();
                iWontToolTipView = null;
            }
            Toast.makeText(this,"It would do nothing. You can carry on as you were.",Toast.LENGTH_LONG).show();

        } else if (id == R.id.b_demo_passenger) {
            if (mPassToolTipView == null) {
                addmPassToolTipView();
            } else {
                mPassToolTipView.remove();
                mPassToolTipView = null;
            }
            Toast.makeText(this,"Same as Indoor Button but with Passenger mode.",Toast.LENGTH_LONG).show();

        } else if (id == R.id.b_demo_indoor) {
            if (mIndoorToolTipView == null) {
                addmIndoorToolTipView();
            } else {
                mIndoorToolTipView.remove();
                mIndoorToolTipView = null;
            }
            Toast.makeText(this,"It would set your current mode to Indoor. The app wont track your activity until you disable this mode by swiping off the notification.",Toast.LENGTH_LONG).show();

        } else if (id == R.id.b_demo_maps) {
            if (mMapsToolTipView == null) {
                addmMapsToolTipView();
            } else {
                mMapsToolTipView.remove();
                mMapsToolTipView = null;
            }
            Toast.makeText(this,"Same as Indoor Button but with Passenger mode.",Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onToolTipViewClicked(final ToolTipView toolTipView) {
        if (okToolTipView == toolTipView) {
            okToolTipView = null;
        } else if (iWontToolTipView == toolTipView) {
            iWontToolTipView = null;
        } else if (mPassToolTipView == toolTipView) {
            mPassToolTipView = null;
        } else if (mIndoorToolTipView == toolTipView) {
            mIndoorToolTipView = null;
        } else if (mMapsToolTipView == toolTipView) {
            mMapsToolTipView = null;
        }
    }
}
