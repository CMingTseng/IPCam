package idv.neo.ipcam;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.lang.StringUtils;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity implements Callback {
    private String TAG = "CameraH264";

    private String mediaUrl = "http://192.168.99.103/video/ACVS-H264.cgi";
    private String userName = "";
    private String password = "";

    private SurfaceView cameraView = null;
    private URLConnection conn;
    private DataInputStream mInputStream;
    private MediaCodec decoder = null;

    private SurfaceHolder surfaceHolder = null;
    private ByteBuffer[] outputBuffers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        SurfaceHolder holder = cameraView.getHolder();
        holder.addCallback(this);

        Bundle receiveBundle = this.getIntent().getExtras();
        this.mediaUrl = receiveBundle.getString("mediaUrl");
        this.userName = receiveBundle.getString("userName");
        this.password = receiveBundle.getString("password");

        Log.d(TAG, "mediaUrl = " + mediaUrl);
        Log.d(TAG, "userName = " + userName);
        Log.d(TAG, "password = " + password);

        if (StringUtils.isBlank(mediaUrl) || StringUtils.isBlank(userName)) {
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            if (mInputStream != null) {
                mInputStream.close();
            }

            if (conn != null) {
                conn = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);

            return rootView;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        new Thread(new Runnable() {
            private URLConnection getStreammingConnection(String url,
                                                          String username, String password) {

                URLConnection conn;
                try {
                    conn = new URL(mediaUrl).openConnection();
                    conn.setDoInput(true);
                    conn.setRequestProperty("Authorization", ConnectionUtils
                            .userNamePasswordBase64(username, password));

                    conn.connect();
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();

                    return null;
                } catch (IOException e1) {
                    e1.printStackTrace();

                    return null;
                }

                return conn;
            }

            private void connectToCamera() {
                conn = this.getStreammingConnection(mediaUrl, userName,
                        password);
                if (conn == null) {
                    return;
                } else {
                    try {
                        InputStream is = conn.getInputStream();
                        mInputStream = new DataInputStream(is);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }

            private void setupCodec() {
                MediaFormat format = MediaFormat.createVideoFormat("video/avc",
                        1280, 720);

                decoder = MediaCodec.createDecoderByType("video/avc");
                decoder.configure(format, surfaceHolder.getSurface(), null, 0);
                decoder.start();
            }

            private int lengthOfSampleData(DataInputStream mInputStream) {
                ByteBuffer dlinkHeader = ByteBuffer.wrap(new byte[40]);
                dlinkHeader.order(ByteOrder.LITTLE_ENDIAN);

                try {
                    Utils.readDataToVideoBuffer(mInputStream,
                            dlinkHeader.array());

                    byte[] dataLength = new byte[4];
                    dlinkHeader.position(8);
                    dlinkHeader.get(dataLength, 0, 4);

                    return Utils.byteArray2Int(dataLength);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return -1;
            }

            private ByteBuffer getSampleData(int lengthOfData,
                                             DataInputStream sourceStream) {

                ByteBuffer dataSample = ByteBuffer.wrap(new byte[lengthOfData]);
                dataSample.order(ByteOrder.LITTLE_ENDIAN);

                try {
                    Utils.readDataToVideoBuffer(sourceStream,
                            dataSample.array());

                    return dataSample;
                } catch (IOException e) {
                    e.printStackTrace();

                    return null;
                }
            }

            private void decodeSampleData(ByteBuffer[] inputBuffers,
                                          ByteBuffer dataSample, int dataLength) {

                int inIndex = decoder.dequeueInputBuffer(10000);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];

                    // clean up
                    buffer.clear();

                    // copy dataSample into decode buffer
                    buffer.put(dataSample);

                    if (dataLength < 0) {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");

                        decoder.queueInputBuffer(inIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                    } else {
                        decoder.queueInputBuffer(inIndex, 0, dataLength, -1, 0);
                    }
                }
            }

            private void decodeToScreen(BufferInfo info) {
                int outIndex = decoder.dequeueOutputBuffer(info, 10000);

                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = decoder.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "New format " + decoder.getOutputFormat());
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;
                    default:

                        decoder.releaseOutputBuffer(outIndex, true);
                        break;
                }
            }

            @Override
            public void run() {
                try {
                    this.connectToCamera();
                    this.setupCodec();

                    ByteBuffer[] inputBuffers = decoder.getInputBuffers();
                    outputBuffers = decoder.getOutputBuffers();

                    BufferInfo info = new BufferInfo();
                    while (!Thread.interrupted()) {

                        int dataLength = this.lengthOfSampleData(mInputStream);
                        ByteBuffer dataSample = this.getSampleData(dataLength,
                                mInputStream);

                        this.decodeSampleData(inputBuffers, dataSample,
                                dataLength);
                        this.decodeToScreen(info);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}

