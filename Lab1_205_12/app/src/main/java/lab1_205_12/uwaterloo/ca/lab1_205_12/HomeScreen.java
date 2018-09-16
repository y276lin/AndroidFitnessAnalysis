package lab1_205_12.uwaterloo.ca.lab1_205_12;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;

public class HomeScreen extends AppCompatActivity {
    protected static float[] accelerometer_max = new float[]{0, 0, 0};
    protected static float[] magnetic_max = new float[]{0, 0, 0};
    protected static float[] rotation_max = new float[]{0, 0, 0};
    LineGraphView graphing;
    Button clear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //Set up LinearLayout
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        layout.setOrientation(LinearLayout.VERTICAL);

        //Declare TextViews, graph and button
        TextView tv_title = (TextView) findViewById(R.id.label1);
        tv_title.setText("List of Sensors:");
        TextView tv_light = new TextView(getApplicationContext());
        tv_light.setText("Light Sensor");
        TextView tv_accelerometer = new TextView(getApplicationContext());
        tv_accelerometer.setText("Accelerometer Sensor");
        TextView tv_magnetic = new TextView(getApplicationContext());
        tv_magnetic.setText("Magnetic Sensor");
        TextView tv_rotation = new TextView(getApplicationContext());
        tv_rotation.setText("Rotation Sensor");
        graphing = new LineGraphView(getApplicationContext(),100, Arrays.asList("x", "y", "z"));
        clear = new Button(getApplication());
        clear.setText("Reset");

        //Add views to layout in display order
        layout.addView(tv_light);
        layout.addView(graphing);
        layout.addView(tv_accelerometer);
        layout.addView(tv_magnetic);
        layout.addView(tv_rotation);
        layout.addView(clear);
        graphing.setVisibility(View.VISIBLE);

        //Declare sensors
        SensorManager sensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Declare event listeners
        SensorEventListener l = new AllSensorEventListener(tv_light,"---Light---    ",graphing);
        SensorEventListener a = new AllSensorEventListener(tv_accelerometer,"---Accelerometer---",graphing);
        SensorEventListener m = new AllSensorEventListener(tv_magnetic,"---Magnetic Field---",graphing);
        SensorEventListener r = new AllSensorEventListener(tv_rotation,"---Rotation Vector---",graphing);

        //Registering sensors with listeners
        sensorManager.registerListener(l, lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(a, accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(m, magneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(r, rotationSensor,SensorManager.SENSOR_DELAY_NORMAL);

        //Reset max values on clicking event
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View screen) {
                for (int x = 0; x < 3; x++) {
                    accelerometer_max[x] = 0f;magnetic_max[x] = 0f;rotation_max[x] = 0f;
                }
            }
        });
    }
}

class AllSensorEventListener extends HomeScreen implements SensorEventListener{
    TextView output;
    String title;
    LineGraphView graph;
    public AllSensorEventListener(TextView outputView, String prefix,LineGraphView visual) {
        output = outputView;
        title = prefix;
        graph = visual;
    }
    public void onAccuracyChanged(Sensor s, int i) {}
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LIGHT) {
            output.setText(title + Float.toString(se.values[0]));
        }
        if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER || se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            for(int x = 0;x < 3;x++){
                if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER ){
                    if(Math.abs(se.values[x]) > Math.abs(accelerometer_max[x])) accelerometer_max[x] = se.values[x];
                    if(x == 2)output(se.values,accelerometer_max);
                        if(x == 2)graph.addPoint(se.values);
                }
                if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD ){
                    if(Math.abs(se.values[x]) > Math.abs(magnetic_max[x]))magnetic_max[x] = se.values[x];
                    if(x == 2)output(se.values,magnetic_max);
                }
                if(se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ){
                    if(Math.abs(se.values[x]) > Math.abs(rotation_max[x]))rotation_max[x] = se.values[x];
                    if(x == 2)output(se.values,rotation_max);
                }
            }
        }
    }
    private void output(float[] value,float[] max){
        output.setText(title +
                "\nX:   " + rounding(value[0]) + "  Y:   " + rounding(value[1]) + "  z:   " + rounding(value[2]) +
                "\nMax X:   " + rounding(max[0]) + "  Max Y:   " + rounding(max[1]) + "  Max z:   " + rounding(max[2]));
    }
    private float rounding(float number){
        return (Math.round(number * 1000.0f) / 1000.0f);
    }
}
