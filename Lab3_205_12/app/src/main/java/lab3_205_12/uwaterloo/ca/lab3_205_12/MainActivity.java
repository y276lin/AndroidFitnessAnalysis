package lab3_205_12.uwaterloo.ca.lab3_205_12;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Arrays;
import mapper.*;

public class MainActivity extends AppCompatActivity {
    float previous = 0f;
    SensorEventListener acceleration;
    protected static int current_state = 0;
    protected static int steps = 0;
    protected static double displaceNS = 0;
    protected static double displaceWE = 0;
    protected static boolean isN = false;
    protected static boolean isE = false;
    protected static boolean isW = false;
    protected static boolean isS = false;
    protected static float[] orientation = new float[3];
    protected static float accelerometer_record[] = new float[3];
    static LineGraphView graphing;
    MapView mv;
    Button clear;
    TextView tv_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        final Sensor linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        final Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        final Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Declare event listeners
        acceleration = new customSensorEventListener(tv_title,tv_sensor,"---Linear Acceleration---",graphing);
        SensorEventListener magnetic = new customSensorEventListener(tv_title,tv_sensor,"---Magnetic Sensor---",graphing);
        SensorEventListener accelerometer = new customSensorEventListener(tv_title,tv_sensor,"---Accelerometer Sensor---",graphing);

        //Registering sensors with listeners
        sensorManager.registerListener(acceleration, linearAccelerationSensor,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magnetic, magneticSensor,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(accelerometer, accelerometerSensor,SensorManager.SENSOR_DELAY_FASTEST);

        //Mapper
        mv = new MapView(getApplicationContext(), 900, 900, 50, 50);
        NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null), "Lab-room-peninsula.svg");
        mv.setMap(map);
        layout.addView(mv);
        registerForContextMenu(mv);

        //Reset max values on clicking event
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View screen) {
                steps = current_state = 0;
                displaceNS = displaceWE = 0d;
                tv_title.setText("Counting: " + steps);
            }
        });
    }
    public  void  onCreateContextMenu(ContextMenu menu , View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu , v, menuInfo);
        mv.onCreateContextMenu(menu , v, menuInfo);
    }
    public  boolean  onContextItemSelected(MenuItem item) {
        return  super.onContextItemSelected(item) ||  mv.onContextItemSelected(item);
    }
}


class customSensorEventListener extends MainActivity implements SensorEventListener{
    TextView sensor_title;
    TextView steps_title;
    String prefix;
    LineGraphView graph;
    public customSensorEventListener(TextView steps_title, TextView sensor_title, String prefix,LineGraphView graph) {
        this.sensor_title = sensor_title;
        this.steps_title = steps_title;
        this.prefix = prefix;
        this.graph = graph;
    }
    public void onAccuracyChanged(Sensor s, int i) {}
    public void onSensorChanged(SensorEvent se) {
        if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION ){
            se.values[1] = -1 * se.values[1];
            float smooth;
            final float upper_bound = 2f;
            final float lower_bound = 1f;
            final int axis = 1;
            smooth = previous + (se.values[axis] - previous)/10;
            finiteStateMachine(lower_bound,upper_bound,smooth);
            //final Output + graphing
            graph.addPoint(new float[]{upper_bound,lower_bound,smooth});
            steps_title.setText("Total Steps Taken: " + steps
                    + "\nDisplacement NS: " + rounding(displaceNS)
                    + "\nDisplacement WE: " + rounding(displaceWE));
            previous = smooth;
        }else if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            for(int x = 0;x<3;x++)accelerometer_record[x] = se.values[x];
        }else if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD ){
            float[] R = new float[9];
            float[] I = new float[9];

            SensorManager.getRotationMatrix(R, I, accelerometer_record, se.values);
            SensorManager.getOrientation(R, orientation);
            for(int x = 0;x < 3;x++){
                orientation[x] = Math.round(orientation[x] / 3.14f * 180f);
                if(orientation[x] < 0) orientation[x] += 360f;
            }
            String output = "Magnetic Sensor Value:\n" + "\nCompass Value is: " + orientation[0];
            if(orientation[0] <= 45 || orientation[0] >= 315){resetDirections(); isN = true; output += "\n Direction: North";}
            else if(orientation[0] >= 135 && orientation[0] <= 225){resetDirections(); isS = true; output += "\n Direction: South";}
            else if(orientation[0] >= 45 && orientation[0] <= 135){resetDirections(); isE = true; output += "\n Direction: East";}
            else if(orientation[0] >= 225 && orientation[0] <= 315){resetDirections(); isW = true; output += "\n Direction: West";}

            sensor_title.setText(output);
        }
    }
    public void resetDirections(){
        isN = isE = isW = isS = false;
    }
    private float rounding(double input){return Math.round(input * 10f) / 10f;}
    private void finiteStateMachine(float lower_walking,float upper_walking,float acceleration){
        if(acceleration <= lower_walking){
            if(current_state == 1){
                if(acceleration < lower_walking){
                    steps++;
//                    if(orientation[0] <=90 && orientation[0] >= 0){
//                        displaceNS += Math.cos(Math.toRadians(orientation[0]));
//                        displaceWE += Math.sin(Math.toRadians(orientation[0]));
//                    }
//                    else if(orientation[0] >= 90 && orientation[0] <= 180){
//                        displaceNS -= Math.sin(Math.toRadians((orientation[0] - 90f)));
//                        displaceWE += Math.cos(Math.toRadians((orientation[0] - 90f)));
//                    }
//                    else if(orientation[0] >= 180 && orientation[0] <= 270){
//                        displaceNS -= Math.cos(Math.toRadians((orientation[0] - 180f)));
//                        displaceWE -= Math.sin(Math.toRadians((orientation[0] - 180f)));
//                    }
//                    else if(orientation[0] >= 270 && orientation[0] <= 360){
//                        displaceNS += Math.sin(Math.toRadians((orientation[0] - 270f)));
//                        displaceWE -= Math.cos(Math.toRadians((orientation[0] - 270f)));
//                    }
                    if(isN == true) displaceNS++;
                    else if(isS == true) displaceNS--;
                    else if(isE == true) displaceWE++;
                    else if(isW == true) displaceWE--;

                }
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
}
