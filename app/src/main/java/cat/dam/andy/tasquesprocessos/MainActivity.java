package cat.dam.andy.tasquesprocessos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private URL ImageUrl = null;
    private InputStream is = null;
    private Bitmap bitmap = null;
    private ImageView iv_async = null;
    private ProgressDialog progressDialog = null;
    private ProgressBar pb_progressBar = null;
    private TextView tv_progress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btn_colors = (Button) findViewById(R.id.btn_colors);
        final Button btn_progress = (Button) findViewById(R.id.btn_progress);
        pb_progressBar = (ProgressBar) findViewById(R.id.pb_progressBar);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        Button btn_async=findViewById(R.id.btn_asyncTask);
        iv_async=findViewById(R.id.iv_async);
        final int[] colors = {R.color.colorGreen, R.color.colorMaroon, R.color.colorFuchsia, R.color.colorNavy};
        //int[] colors = {Color.BLUE, Color.WHITE, Color.YELLOW, Color.GREEN}; //podriem utilitzar llibreria Colors
        final int ncolors = colors.length;
        final Handler handler = new Handler();

        final Runnable runnableColors = new Runnable() {
            private int i = 0;

            @Override
            public void run() {
                i++;
                i = i % ncolors;
                btn_colors.setBackgroundColor(getResources().getColor(colors[i]));
                handler.postDelayed(this, 200); // es crida a ell mateix
            }
        };

        btn_colors.setOnClickListener(new View.OnClickListener() {
            boolean actiuCanviColor = true;

            @Override
            public void onClick(View v) {
                if (actiuCanviColor) {
                    handler.removeCallbacks(runnableColors);//aturar tasca
                } else {
                    handler.post(runnableColors); //executar tasca sense retard
                }
                actiuCanviColor = !actiuCanviColor;
            }
        });
        handler.postDelayed(runnableColors, 200); // crida inicial. Retard en ms

        Runnable runnableProgress = new Runnable() {
            private Boolean running = true;

            @Override
            public void run() {
                if (running) {
                    tv_progress.setText("Updating...");
                    for (int i = 0; i <= 10; i++) {
                        final int valor = i;
                        doFakeWork();
                        pb_progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                pb_progressBar.setProgress(valor);
                            }
                        });
                    }
                    running = false;
                }
            }
        };
        btn_async.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskExemple asyncTask=new AsyncTaskExemple();
                asyncTask.execute("https://agora.xtec.cat/iespladelestany/wp-content/uploads/usu35/2015/11/P1420828.jpg");
            }
        });
    }
    //Fi oncreate

    public void startProgress(View view) {
        // tasca llarga
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 10; i++) {
                    final int valor = i;
                    doFakeWork();
                    pb_progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_progress.setText("Updating");
                            pb_progressBar.setProgress(valor);
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();
    }

    // Simulant un procés timeconsuming
    private void doFakeWork() {
        SystemClock.sleep(1000);
    }

    private class AsyncTaskExemple extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Un moment,si us plau\ns'està baixant la imatge...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                ImageUrl = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) ImageUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                //Configura el format de la imatge es que descarregarà
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bitmap = BitmapFactory.decodeStream(is, null, options);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(iv_async !=null) {
                progressDialog.hide();
                iv_async.setImageBitmap(bitmap);
            }else {
                progressDialog.show();
            }
        }
    }
}
