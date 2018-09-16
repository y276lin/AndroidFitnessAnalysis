package lab4_205_12.uwaterloo.ca.lab4_205_12;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mapper.*;

public class MainActivity extends AppCompatActivity {
    private static MapView mv;
    private static NavigationalMap map;
    private static float stepLength = 0.99f;
    private static TextView tv_path;
    private static TextView tv_location;
    private static PointF magicStart = null, magicEnd = null;
    private static AlertDialog dialogCongratulations;
    private static AlertDialog dialogInvalidInput;
    private static AlertDialog dialogOffPath;
    private static AlertDialog dialogStageUp;
    private static AlertDialog dialogWall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout Setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Alert
        dialogCongratulations = new AlertDialog.Builder(MainActivity.this).create();
        dialogCongratulations.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
        dialogCongratulations.setTitle("Congratulations");
        dialogCongratulations.setMessage("You are home!");
        dialogInvalidInput = new AlertDialog.Builder(MainActivity.this).create();
        dialogInvalidInput.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
        dialogInvalidInput.setTitle("Invalid Input");
        dialogInvalidInput.setMessage("Unable to calculate this path");
        dialogOffPath = new AlertDialog.Builder(MainActivity.this).create();
        dialogOffPath.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
            updatePath(mv.getOriginPoint(), mv.getDestinationPoint());
            dialog.dismiss();
        }});
        dialogOffPath.setTitle("Off Path");
        dialogOffPath.setMessage("You are walking the wrong way");
        dialogStageUp = new AlertDialog.Builder(MainActivity.this).create();
        dialogStageUp.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {
            updatePath(mv.getOriginPoint(), mv.getDestinationPoint());
            dialog.dismiss();
        }});
        dialogStageUp.setTitle("Stage Up");
        dialogStageUp.setMessage("Staging up");
        dialogWall = new AlertDialog.Builder(MainActivity.this).create();
        dialogWall.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {dialog.dismiss();}});
        dialogWall.setTitle("Wall");
        dialogWall.setMessage("You don't have the ability to walk into the walls!");
        //Set up LinearLayout
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        layout.setOrientation(LinearLayout.VERTICAL);
        //Declare TextViews, graph and button
        TextView tv_title = new TextView(getApplicationContext());
        tv_title.setText("Counting: steps");
        tv_title.setTextColor(Color.parseColor("black"));
        TextView tv_sensor = new TextView(getApplicationContext());
        tv_sensor.setText("Accelerometer Sensor");
        tv_sensor.setTextColor(Color.parseColor("black"));
        LineGraphView graphing = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        Button clearButton = new Button(getApplication());
        clearButton.setText("Reset Steps");
        clearButton.setBackgroundColor(Color.parseColor("black"));
        Button calculate = new Button(getApplication());
        calculate.setText("Path");
        calculate.setBackgroundColor(Color.parseColor("red"));

        //Debugging
        tv_location = new TextView(getApplicationContext());
        tv_location.setText("Location Text View");
        tv_location.setTextColor(Color.parseColor("red"));

        tv_path = new TextView(getApplicationContext());
        tv_path.setText("Path Text View");
        tv_path.setTextColor(Color.parseColor("blue"));

        //Declare sensors
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        final Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        final Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Load Map Using Application Package
        mv = new MapView(getApplicationContext(), 900, 900, 35, 35);
        map = MapLoader.loadMap(getExternalFilesDir(null), "E2-3344.svg");
        mv.setMap(map);
        mv.addListener(new PositionEventHandler());
        registerForContextMenu(mv);

        //Load Compass
        String downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(downloadDir + "/up.png");
        ImageView image = new ImageView(getApplicationContext());
        image.setImageBitmap(bitmap);

        //Add views to layout in display order
        graphing.setVisibility(View.VISIBLE);
        layout.addView(tv_title);
        layout.addView(graphing);
        layout.addView(clearButton);
        layout.addView(calculate);
        layout.addView(tv_location);
        layout.addView(tv_sensor);
        layout.addView(tv_path);
        layout.addView(image);
        layout.addView(mv);

        //Declare event listeners
        SensorEventListener accelerometer = new customSensorEventListener(tv_title, tv_sensor, tv_location, graphing, mv, stepLength, map, image);
        SensorEventListener acceleration = new customSensorEventListener(tv_title, tv_sensor, tv_location, graphing, mv, stepLength, map, image);
        SensorEventListener magnetic = new customSensorEventListener(tv_title, tv_sensor, tv_location, graphing, mv, stepLength, map, image);
        new customSensorEventListener(dialogCongratulations,dialogInvalidInput, dialogOffPath, dialogStageUp, dialogWall);

        //Registering sensors with listeners
        sensorManager.registerListener(acceleration, linearAccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(accelerometer, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magnetic, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);

        //Reset max values on clicking event
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View screen) {
                new customSensorEventListener();
            }
        });

        //Reset max values on clicking event
        calculate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View screen) {
                mv.setOriginPoint(new PointF(mv.getUserPoint().x, mv.getUserPoint().y));
                updatePath(mv.getOriginPoint(), mv.getDestinationPoint());
            }
        });

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mv.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        return mv.onContextItemSelected(item);
    }

    private static void updatePath(PointF start, PointF end) {
        //clear path
        mv.setUserPath(new ArrayList<PointF>());
        //check user input
        if(start.x == 0 || start.y == 0 || end.x == 0 || end.y == 0)return;

        ArrayList<PointF> path = new ArrayList<>();
        path.add(start);

        //Straight Line Case
        if(!hasWall(start,end)){path.add(end);mv.setUserPath(path);generateInstructions(false);new customSensorEventListener(path);return;}
        //Re-direction Path
        float increment = 0.1f;
        magicStart = new PointF(start.x , start.y);
        magicEnd = new PointF(end.x, start.y);

        //Downward Search

        while (magicStart.y < 900f && magicEnd.y < 900f) {
            if (!hasWall(magicStart, magicEnd) && !hasWall(magicStart, start) && !hasWall(magicEnd, end)) {
                path.add(magicStart);
                path.add(magicEnd);
                path.add(end);
                mv.setUserPath(path);
                optimization(mv.getOriginPoint());
                return;
            }
            PointF startDestination = new PointF(magicStart.x, magicStart.y + increment);
            if (!hasWall(magicStart, startDestination)) {
                magicEnd = new PointF(magicEnd.x, magicEnd.y + increment);
                magicStart = new PointF(magicStart.x, magicStart.y + increment);
            } else break;
        }

        magicStart = new PointF(start.x, start.y);
        magicEnd = new PointF(end.x, start.y);

        //upward Search
        while (magicStart.y > 0f && magicEnd.y > 0f) {
            if (!hasWall(magicStart, magicEnd) && !hasWall(magicStart, start) && !hasWall(magicEnd, end) ) {
                path.add(magicStart);
                path.add(magicEnd);
                path.add(end);
                mv.setUserPath(path);
                tv_path.setText("upward search termination");
                optimization(mv.getOriginPoint());
                return;
            }
            //Increment Magic Line and determine if it breaks the wall
            //If it breaks the wall, then this path will lead the user outside the border
            PointF startDestination = new PointF(magicStart.x, magicStart.y - increment);
            if (!hasWall(magicStart, startDestination)) {
                magicEnd = new PointF(magicEnd.x, magicEnd.y - increment);
                magicStart = new PointF(magicStart.x, magicStart.y - increment);
            } else break;
        }
        tv_path.setText("<<<<downward search");

        path.add(magicStart);
        path.add(magicEnd);
        mv.setUserPath(path);
        dialogInvalidInput.show();
        tv_path.setText("<<<<both search");
    }

    private static boolean hasWall(PointF start, PointF end){
        return map.calculateIntersections(start,end).size() >= 1;
    }

    private static void optimization(PointF start){
        PointF end = mv.getDestinationPoint();
        //Reflect on map
        ArrayList<PointF> optimizedPath = new ArrayList<>();
        mv.setUserPath(new ArrayList<PointF>());
        optimizedPath.add(start);
        optimizedPath.add(magicStart);
        optimizedPath.add(magicEnd);
        optimizedPath.add(end);
        mv.setUserPath(optimizedPath);
        generateInstructions(true);
        new customSensorEventListener(optimizedPath);
    }

    private static void generateInstructions(boolean scenario){
        PointF start = mv.getOriginPoint();
        PointF end = mv.getDestinationPoint();
        ArrayList<PointF> path = new ArrayList<PointF>();
        String output = "";
        if(scenario == true){
            path.add(start);path.add(magicStart);path.add(magicEnd);path.add(end);
            output = "\n" + "Step 1: Walk " + getDistance(path.get(0), path.get(1)) + " @ " + getAngle(path.get(0), path.get(1))
                           +"\n" + "Step 2: Walk " + getDistance(path.get(1), path.get(2)) + " @ " + getAngle(path.get(1), path.get(2))
                          + "\n" + "Step 3: Walk " + getDistance(path.get(2), path.get(3)) + " @ " + getAngle(path.get(2), path.get(3));
        }
        else if(scenario == false) {
            path.add(start);path.add(end);
            output = "\n" + "Step 1: Walk " + getDistance(mv.getOriginPoint(), mv.getDestinationPoint()) + " @ " + getAngle(mv.getOriginPoint(), mv.getDestinationPoint());
        }
//        if(scenario == true){
//            output += "\n" + path.get(0).x + " <> " + path.get(0).y;
//            output += "\n" + path.get(1).x + " <> " + path.get(1).y;
//            output += "\n" + path.get(2).x + " <> " + path.get(2).y;
//            output += "\n" + path.get(3).x + " <> " + path.get(3).y;
//        }else if(scenario == false){
//            output += "\n" + path.get(0).x + " <> " + path.get(0).y;
//            output += "\n" + path.get(1).x + " <> " + path.get(1).y;
//        }
        tv_path.setText(output);
    }

    private static float getDistance(PointF start, PointF end){
        double x = (end.x - start.x);
        double y = (end.y - start.y);
        x = Math.pow(x, 2.0);
        y = Math.pow(y, 2.0);
        return (float)Math.sqrt( y + x );
    }
    private static float rounding(float input) {return Math.round(input * 10f) / 10f;}
    private static float getAngle(PointF start, PointF end){
        double x = rounding(end.x - start.x);
        double y = rounding(end.y - start.y);
        if( x > 0 && y > 0){
            return 90f + (float)Math.toDegrees(Math.atan(Math.abs(y)/Math.abs(x)));
        }else if(x > 0 && y < 0){
            return 90f - (float)Math.toDegrees(Math.atan(Math.abs(y)/Math.abs(x)));
        }else if(x < 0 && y > 0){
            return 270f - (float)Math.toDegrees(Math.atan(Math.abs(y)/Math.abs(x)));
        }else if(x < 0 && y < 0){
            return 270f + (float)Math.toDegrees(Math.atan(Math.abs(y)/Math.abs(x)));
        }else if(x == 0 && y > 0) return 180f;
         else if(x == 0 && y < 0) return 0f;
         else if(x > 0 && y == 0) return 90f;
         else if(x < 0 && y == 0) return 270f;
        return 0f;
    }

    private class PositionEventHandler implements PositionListener {
        @Override
        public void originChanged(MapView source, PointF p) {
            source.setOriginPoint(p);
            source.setUserPoint(p);
            updatePath(p, source.getDestinationPoint());
        }

        @Override
        public void destinationChanged(MapView source, PointF p) {
            source.setDestinationPoint(p);
            updatePath(source.getOriginPoint(), p);
        }
    }
}

class customSensorEventListener implements SensorEventListener {
    private static float acceleration_previous = 0f;
    private static int current_state, steps = 0;
    private static double displaceNS, displaceWE = 0;
    private static boolean isN, isE, isW, isS = false;
    private static float[] orientation = new float[3];
    private static float accelerometer_record[] = new float[3];
    private static TextView sensor_title;
    private static TextView steps_title;
    private static TextView tv_location;
    private static ImageView image;
    private static MapView mv;
    private static float angleOffSet = 0f;
    private static NavigationalMap map;
    private static float stepLength;
    private int currentPath = 0;
    private LineGraphView graph;
    private static float angle = 0f;
    private static float currentDegree = 0f;
    private static ArrayList<PointF> path = new ArrayList<PointF>();
    private static AlertDialog dialogCongratulations;
    private static AlertDialog dialogInvalidInput;
    private static AlertDialog dialogOffPath;
    private static AlertDialog dialogStageUp;
    private static AlertDialog dialogWall;
    private static PointF previous_user_point = new PointF(0f,0f);
    boolean changePermission = true;

    public customSensorEventListener() {
        steps = current_state = 0;
        displaceNS = displaceWE = 0d;
        mv.setUserPath(new ArrayList<PointF>());
        mv.setUserPoint(mv.getOriginPoint());
        this.path = new ArrayList<PointF>();
        angleOffSet = orientation[0];
    }

    public customSensorEventListener(ArrayList<PointF> path) {
        this.path = new ArrayList<PointF>();
        this.path = path;
        this.path.add(mv.getDestinationPoint());
        this.path.add(mv.getDestinationPoint());
    }
    public customSensorEventListener(AlertDialog dialogCongratulations, AlertDialog dialogInvalidInput, AlertDialog dialogOffPath, AlertDialog dialogStageUp, AlertDialog dialogWall){
        this.dialogCongratulations = dialogCongratulations;
        this.dialogInvalidInput = dialogInvalidInput;
        this.dialogOffPath = dialogOffPath;
        this.dialogStageUp = dialogStageUp;
        this.dialogWall = dialogWall;
    }
    public customSensorEventListener(TextView steps_title, TextView sensor_title, TextView tv_location, LineGraphView graph, MapView mv, float stepLength, NavigationalMap map, ImageView image){
        this.sensor_title = sensor_title;
        this.steps_title = steps_title;
        this.tv_location = tv_location;
        this.stepLength = stepLength;
        this.map = map;
        this.graph = graph;
        this.image = image;
        this.mv = mv;
    }

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if(mv.getUserPoint().x == 0f) return;
            se.values[1] *= -1;
            final float upper_bound = 2f, lower_bound = 1f;
            final int axis = 1;
            float smooth = acceleration_previous + (se.values[axis] - acceleration_previous) / 10;
            finiteStateMachine(lower_bound, upper_bound, smooth);
            //final Output + graphing
            graph.addPoint(new float[]{upper_bound, lower_bound, smooth});
            steps_title.setText("Total Steps Taken: " + steps + "\nDisplacement NS: " + rounding(displaceNS) + "\nDisplacement WE: " + rounding(displaceWE));
            acceleration_previous = smooth;
        } else if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometer_record = se.values;
        } else if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float[] R = new float[9];
            float[] I = new float[9];
            SensorManager.getRotationMatrix(R, I, accelerometer_record, se.values);
            SensorManager.getOrientation(R, orientation);
            // Data Manipulation
            orientation[0] = Math.round(orientation[0] / 3.14f * 180f);
            angle = orientation[0];
            angle -= angleOffSet;
            while(angle < 0f) angle += 360f;
            while(angle > 360f) angle -= 360f;
            // Data Output
            String outputMessage = "Compass Value is: " + angle;
            if (angle <= 45 || angle >= 315) {      resetDirections();isN = true;outputMessage += "\n Direction: North";}
            else if (angle >= 135 && angle <= 225) {resetDirections();isS = true;outputMessage += "\n Direction: South";}
            else if (angle >= 45 && angle <= 135) { resetDirections();isE = true;outputMessage += "\n Direction: East";}
            else if (angle >= 225 && angle <= 315) {resetDirections();isW = true;outputMessage += "\n Direction: West";}
            sensor_title.setText(outputMessage);

            //compass algorithm - calculate angle to end point
            final RotateAnimation rotateAnim = new RotateAnimation(currentDegree, angle, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setDuration(0);
            rotateAnim.setFillAfter(true);
            image.startAnimation(rotateAnim);
            currentDegree = angle;
        }
    }

    private float rounding(double input) {return Math.round(input * 10f) / 10f;}
    public void resetDirections() {isN = isE = isW = isS = false;}
    public void onAccuracyChanged(Sensor s, int i) {}
    private static float getDistance(PointF start, PointF end){
        double x = (end.x - start.x);
        double y = (end.y - start.y);
        x = Math.pow(x, 2.0);
        y = Math.pow(y, 2.0);
        return (float)Math.sqrt( y + x );
    }
    private void finiteStateMachine(float lower_walking, float upper_walking, float acceleration) {
        if (acceleration <= lower_walking) {
            if (current_state == 1) {
                if (acceleration < lower_walking) {

                    //Incrementing Step
                    double NS = -1d * Math.cos(Math.toRadians(angle));
                    double WE = Math.sin(Math.toRadians(angle));
                    PointF currentLocation = mv.getUserPoint();
                    float newX = currentLocation.x + stepLength * (float)WE;
                    float newY = currentLocation.y + stepLength * (float)NS;
                    PointF destinationLocation = new PointF(newX,newY);
                    //Valid step
                    if((map.calculateIntersections(currentLocation, destinationLocation)).size() < 1){
                        steps++;
                        tv_location.setText("X: " + destinationLocation.x + "     Y: " + destinationLocation.y);
                        displaceNS += NS;
                        displaceWE += WE;
                        mv.setUserPoint(destinationLocation);
                        //Alert
                        //If path set
                        if(mv.getOriginPoint().x != 0 && mv.getOriginPoint().y != 0 && mv.getDestinationPoint().x != 0 && mv.getDestinationPoint().y != 0){
                            float offSetX = Math.abs(currentLocation.x - mv.getDestinationPoint().x);
                            float offSetY = Math.abs(currentLocation.y - mv.getDestinationPoint().y);
                            if(offSetX < (2 * stepLength) && offSetY < (2 * stepLength)) {
                                dialogCongratulations.show();
                                new customSensorEventListener();
                                new customSensorEventListener(new ArrayList<PointF>());
                                mv.setUserPoint(new PointF(0f, 0f));
                                return;
                            }
                            //Siri
                            //Stage Up
                            if(path.size() != 0 && getDistance(path.get(currentPath + 1),new PointF(mv.getUserPoint().x,mv.getUserPoint().y)) < 1) {
                                if((currentPath + 1) != path.size()){
                                    currentPath++;
                                    dialogStageUp.setMessage("current path: " + currentPath + "  path.size(): " + path.size());
                                    dialogStageUp.show();
                                    return;
                                }else{
                                    dialogCongratulations.show();
                                    new customSensorEventListener();
                                    new customSensorEventListener(new ArrayList<PointF>());
                                    mv.setUserPoint(new PointF(0f, 0f));
                                    return;
                                }
                            }
                            tv_location.setText("user point.x: " + mv.getUserPoint().x + "   start.y: " + mv.getUserPoint().y
                                        + "\npreivous" + previous_user_point.x + "   " + previous_user_point.y);
                            //Current stage veering off
                            if(path.size() != 0){
//                            if(path.size() == 999){
//                                dialogOffPath.setMessage("current x: " + mv.getUserPoint().x
//                                        +"\n current y: " + mv.getUserPoint().y
//                                        +"\n origin x: " + mv.getOriginPoint().x
//                                        +"\n origin y: " + mv.getOriginPoint().y
//                                        +"\n destionation x: " + destinationLocation.x
//                                        +"\n destionation y: " + destinationLocation.y
//                                );
                                float current = getDistance(new PointF(mv.getUserPoint().x,mv.getUserPoint().y), path.get(currentPath));
                                float max = getDistance(path.get(currentPath), path.get(currentPath + 1));
                                if(current > max){
                                    changePermission = false;
                                    mv.setOriginPoint(new PointF(previous_user_point.x, previous_user_point.y));
                                    mv.setUserPoint(new PointF(previous_user_point.x, previous_user_point.y));
                                    dialogOffPath.show();
                                }
                            }
                        }
                        if(changePermission == true)    previous_user_point = new PointF(mv.getUserPoint().x, mv.getUserPoint().y);
                        else changePermission = false;
                    }

                }
                current_state = 0;
            } else if (current_state == 2) {
                current_state = 0;
            }
        } else if (acceleration > lower_walking && acceleration < upper_walking) {
            if (current_state == 0) current_state = 1;
        } else if (acceleration >= upper_walking) {
            current_state = 2;
        }
    }
}

