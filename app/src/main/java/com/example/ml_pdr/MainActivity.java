package com.example.ml_pdr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.victor.loading.rotate.RotateLoading;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_WRITE_EXTERNAL = 1;
    private static final int DEFAULT_THREAD_POOL_SIZE = 8;

    boolean isAccelData = false;
    boolean isGyroData = false;
    float x_acc, y_acc, z_acc, x_gyr, y_gyr, z_gyr;
    double[][] particles = new double[250][10];
    private RecyclerView recycler;
    private Adapter adapter;
    private Interpreter tflite;
    private SensorManager sensorManager;
    private float[][] lin_acc_1 = new float[500][3];
    private float[][][] lin_acc1 = new float[1][500][3];
    private float[][][] gyr_1 = new float[1][500][3];
    private float[][] lin_acc_2 = new float[500][3];
    private float[][][] lin_acc2 = new float[1][500][3];
    private float[][][] gyr_2 = new float[1][500][3];
    private float[][] t;
    private float[][] q;
    private String day;
    private boolean writePermission = false;
    private Button button = null;
    private int i = 0;
    private int j = 250;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private RotateLoading rotateLoading = null;
    private ArrayList<String> list = new ArrayList<>();
    private BuildingMap map;
    private double[] globalObservation = {0, 0, 0};
    private double[] globalQuaternions = {1, 0, 0, 0};
    private double[] gravity = {0, 0, 0};
    private float[] linear_acceleration = {0, 0, 0};
    private double[] rotationDevice = {0,0,0,0};
    private boolean recording = false;
    private long pastTime;
    private double diff = 0;
    private double pass = 0;
    private boolean calibration = true;
    private ExecutorService executorService;
    final SensorEventListener sensorEventListener = new SensorEventListener() {
        //On sensor changed => find the proper sensor and change corresponding values in the list
        @Override
        public void onSensorChanged(SensorEvent event) {
            final double alpha = 0.8;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                isAccelData = true;
                // We redefine the axes to have x in front of the pedestrian and y at his left
                x_acc = event.values[2] * -1;
                y_acc = event.values[0] * -1;
                z_acc = event.values[1];

                // We apply a high-pass filter to get the linear acceleration of the device

                gravity[0] = alpha * gravity[0] + (1 - alpha) * x_acc;
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y_acc;
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z_acc;
                linear_acceleration[0] = (float) (x_acc - gravity[0]);
                linear_acceleration[1] = (float) (y_acc - gravity[1]);
                linear_acceleration[2] = (float) (z_acc - gravity[2]);
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                isGyroData = true;
                // We redefine the axes to have x in front of the pedestrian and y at his left
                x_gyr = event.values[2] * -1;
                y_gyr = event.values[0] * -1;
                z_gyr = event.values[1];
            }
            if (isAccelData & isGyroData) {

                long currentTime;
                double[] globalPosition;
                if (i == 500) {

                    currentTime = System.currentTimeMillis();
                    float delta = (currentTime - pastTime) / 1000F;
                    pastTime = currentTime;

                    // Get estimations from NN
                    lin_acc1[0] = lin_acc_1;
                    List<float[][]> estimation = relativePoseEstimation(lin_acc1, gyr_1);
                    t = estimation.get(0);
                    q = estimation.get(1);

                    // Compute global position from the relative pose computed by the NN
                    List<double[]> results = WorldCoordinates.compute(globalObservation, globalQuaternions, t[0], q[0]);
                    globalObservation = results.get(0);
                    globalQuaternions = results.get(1);

                    // Generate particles given the global position as an observation
                    float[] acc1 = Utils.floatAverageByColumn(Arrays.copyOfRange(lin_acc_1,250,500));
                    double[] gyr1 = Utils.averageByColumn(gyr_1[0]);
                    String path = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    particles = Utils.makeClone(ParticleFiltering.generateNewParticles(particles, acc1, gyr1, globalObservation, globalQuaternions, delta, map,executorService,recording,path));
                    globalPosition = Utils.averageByColumn(particles);

                    list.set(5, String.format(Locale.US, "%.6f", globalPosition[0]));
                    list.set(6, String.format(Locale.US, "%.6f", globalPosition[1]));
                    list.set(7, String.format(Locale.US, "%.6f", globalPosition[2]));
                    list.set(9, String.format(Locale.US, "%.6f", globalObservation[0]));
                    list.set(10, String.format(Locale.US, "%.6f", globalObservation[1]));
                    list.set(11, String.format(Locale.US, "%.6f", globalObservation[2]));

                    if (recording) {
                        path = path + File.separator + day + ".txt";
                        String data = String.valueOf(globalPosition[0]) + ',' + globalPosition[1] + ',' + globalPosition[2] + ',' + globalObservation[0] + ',' + globalObservation[1] + ',' + globalObservation[2];
                        CsvWriter writer = new CsvWriter(path, data);
                        executorService.execute(writer);
                    }
                    i = 0;
                }
                if (j == 500) {
                    currentTime = System.currentTimeMillis();
                    float delta = (currentTime - pastTime) / 1000F;
                    pastTime = currentTime;

                    // Get estimations from NN
                    lin_acc2[0] = lin_acc_2;
                    List<float[][]> estimation = relativePoseEstimation(lin_acc2, gyr_2);
                    t = estimation.get(0);
                    q = estimation.get(1);

                    // Compute global position from the relative pose computed by the NN
                    List<double[]> results = WorldCoordinates.compute(globalObservation, globalQuaternions, t[0], q[0]);
                    globalObservation = results.get(0);
                    globalQuaternions = results.get(1);

                    // Generate particles given the global position as an observation
                    float[] acc2 = Utils.floatAverageByColumn(Arrays.copyOfRange(lin_acc_2,250,500));
                    double[] gyr2 = Utils.averageByColumn(gyr_2[0]);
                    String path = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    particles = Utils.makeClone(ParticleFiltering.generateNewParticles(particles, acc2, gyr2, globalObservation, globalQuaternions, delta, map,executorService,recording,path));
                    globalPosition = Utils.averageByColumn(particles);

                    list.set(5, String.format(Locale.US, "%.6f", globalPosition[0]));
                    list.set(6, String.format(Locale.US, "%.6f", globalPosition[1]));
                    list.set(7, String.format(Locale.US, "%.6f", globalPosition[2]));
                    list.set(9, String.format(Locale.US, "%.6f", globalObservation[0]));
                    list.set(10, String.format(Locale.US, "%.6f", globalObservation[1]));
                    list.set(11, String.format(Locale.US, "%.6f", globalObservation[2]));

                    if (recording) {
                        path = path + File.separator + day + ".txt";
                        String data = String.valueOf(globalPosition[0]) + ',' + globalPosition[1] + ',' + globalPosition[2] + ',' + globalObservation[0] + ',' + globalObservation[1] + ',' + globalObservation[2];
                        CsvWriter writer = new CsvWriter(path, data);
                        executorService.execute(writer);
                    }
                    j = 0;
                }

                gyr_1[0][i][0] = x_gyr;
                gyr_1[0][i][1] = y_gyr;
                gyr_1[0][i][2] = z_gyr;

                lin_acc_1[i][0] = linear_acceleration[0];
                lin_acc_1[i][1] = linear_acceleration[1];
                lin_acc_1[i][2] = linear_acceleration[2];

                gyr_2[0][j][0] = x_gyr;
                gyr_2[0][j][1] = y_gyr;
                gyr_2[0][j][2] = z_gyr;

                lin_acc_2[j][0] = linear_acceleration[0];
                lin_acc_2[j][1] = linear_acceleration[1];
                lin_acc_2[j][2] = linear_acceleration[2];

                isGyroData = false;
                isAccelData = false;

                i++;
                j++;

                recycler.setAdapter(adapter);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void getList() {
        list.add("");
        list.add("X");
        list.add("Y");
        list.add("Z");
        list.add("Filter");
        list.add("0.000");
        list.add("0.000");
        list.add("0.000");
        list.add("NN");
        list.add("0.000");
        list.add("0.000");
        list.add("0.000");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("PDR by ML");
        pastTime = System.currentTimeMillis();
        for (int i = 0; i < particles.length; i++) {
            particles[i][6] = 1;
        }
        // Thread pool initialization
        executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

        // Load tflite model
        try {
            tflite = new Interpreter(loadModelFile(), new Interpreter.Options());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Load the hard-coded map
        String json = null;
        try {
            InputStream inputStream = getAssets().open("data.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        map = gson.fromJson(json, BuildingMap.class);

        // RecyclerView
        recycler = findViewById(R.id.recyclerView);
        recycler.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(manager);
        getList(); //Initializing list
        adapter = new Adapter(list, this);
        recycler.setAdapter(adapter);

        //Setting-up button listener
        rotateLoading = findViewById(R.id.rotateloading);
        button = findViewById(R.id.record);
        button.setOnClickListener(view -> {
            if (rotateLoading.isStart()) {
                recording = false;
                rotateLoading.stop();
                button.setText(R.string.startRecord);
            } else {
                day = new SimpleDateFormat("yyyy-MM-dd'_'HH:mm:ss").format(Calendar.getInstance().getTime());
                recording = true;
                rotateLoading.start();
                button.setText(R.string.stopRecord);
            }
        });

        // Define the different sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        writePermission = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        recording = false;
        rotateLoading.stop();
        button.setText(R.string.startRecord);
        sensorManager.unregisterListener(sensorEventListener, accelerometer);
        sensorManager.unregisterListener(sensorEventListener, gyroscope);
    }

    // Method to check if write permission has been given
    private boolean checkPermission(String permission, int request_code) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    request_code);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writePermission = true;
            }
        }
    }

    // Load the tflite model
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("2019-07-19_17_49_6dof_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Compute the estimation with the NN giving it acceleration and angular velocity
    private List<float[][]> relativePoseEstimation(float[][][] acc, float[][][] gyr) {

        Object[] inputs = {acc, gyr};
        float[][] t = new float[1][3];
        float[][] q = new float[1][4];

        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, t);
        outputs.put(1, q);

        tflite.runForMultipleInputsOutputs(inputs, outputs);
        return Arrays.asList(t, q);
    }

    //Setting-up Adapter for RecyclerView (used to display the results in real-time)
    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<String> list;
        private Context context;

        Adapter(ArrayList<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.single_unit, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textview);
            }
        }
    }
}
