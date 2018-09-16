package lab2_205_12.uwaterloo.ca.lab2_205_12;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;

public class HomeScreen extends AppCompatActivity {
    float[] previous = new float[]{0, 0, 0};
    protected int current_state = 0;
    protected int vertical_current_state = 0;
    protected int horizontal_current_state = 0;
    protected static int steps = 0;
    LineGraphView graphing;
    Button clear;
    TextView tv_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //Set up LinearLayout
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        layout.setOrientation(LinearLayout.VERTICAL);

        //Declare TextViews, graph and button
        tv_title = (TextView) findViewById(R.id.label1);
        tv_title.setText("Counting: " + steps);
        TextView tv_sensor = new TextView(getApplicationContext());
        tv_sensor.setText("Accelerometer Sensor");
        tv_sensor.setTextColor(Color.parseColor("black"));
        graphing = new LineGraphView(getApplicationContext(),100, Arrays.asList("x", "y", "z"));
        clear = new Button(getApplication());
        clear.setText("Reset Steps");
        clear.setBackgroundColor(Color.parseColor("black"));

        //Add views to layout in display order
        layout.addView(graphing);
        layout.addView(clear);
        layout.addView(tv_sensor);
        graphing.setVisibility(View.VISIBLE);

        //Declare sensors
        SensorManager sensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        //Declare event listeners
        SensorEventListener a = new AccelerationSensorEventListener(tv_title,tv_sensor,"---Linear Acceleration---",graphing);

        //Registering sensors with listeners
        sensorManager.registerListener(a, accelerometerSensor,SensorManager.SENSOR_DELAY_FASTEST);

        //Reset max values on clicking event
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View screen) {
                steps = 0;
                current_state = 0;
                tv_title.setText("Counting: " + steps);
            }
        });
    }
}

class AccelerationSensorEventListener extends HomeScreen implements SensorEventListener{
    TextView sensor_title;
    TextView steps_title;
    String prefix;
    LineGraphView graph;
    public AccelerationSensorEventListener(TextView steps_title, TextView sensor_title, String prefix,LineGraphView graph) {
        this.sensor_title = sensor_title;
        this.steps_title = steps_title;
        this.prefix = prefix;
        this.graph = graph;
    }
    public void onAccuracyChanged(Sensor s, int i) {}
    public void onSensorChanged(SensorEvent se) {
        if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION ){
            se.values[1] = -1 * se.values[1];
            float[] smooth = new float[]{0, 0, 0};
            final float upper_bound = 2f;
            final float lower_bound = 1f;
            final int axis = 1;
            smooth[axis] = previous[axis] + (se.values[axis] - previous[axis])/10;
            //in Range
            determine(lower_bound,upper_bound,smooth[axis]);
            //final Output + graphing
            graph.addPoint(new float[]{upper_bound,lower_bound,smooth[axis]});
            output();
            for (int x = 0; x < 3; x++) previous[x] = smooth[x];
        }
    }
    private void determine(float lower_walking,float upper_walking,float acceleration){
        if(acceleration <= lower_walking){
            if(current_state == 1){
                if(acceleration < lower_walking) steps++;
                current_state = 0;
            }else if(current_state == 2){
                current_state =  0;
            }
        }else if(acceleration > lower_walking && acceleration < upper_walking){
            if(current_state == 0) current_state = 1;
        }else if(acceleration >= upper_walking){
            current_state = 2;
        }
    }
    private void output(){
        steps_title.setText("Total Steps Taken: " + steps + "\n");
        sensor_title.setText("Johnson Han");
    }
}
