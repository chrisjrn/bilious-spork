package com.chrisjrn.httpmajigger;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.squareup.okhttp.Address;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Connection;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;


public class MainActivity extends ActionBarActivity implements OnClickListener {

    private View button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //httpNoKeepAlives();

        //    public void doHttpBenchmark(int numConcurrentConnections, boolean doKeepAlives, boolean secure, Protocol protocol, int numRequests, int numBytes) {

        button = findViewById(R.id.button);

        button.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        if (view == button) {
            onButtonClick();
        }
    }

    protected void onButtonClick() {
        button.setEnabled(false);
        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                doConcurrencyBenchmarkSuite();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                button.setEnabled(true);
            }
        }.execute();
    }

    public void doLatencyBenchmarkSuite() {
        Log.e("MainActivity", "Sequential Tests");
        Log.e("MainActivity", ", HTTP1 - no keepalives - seq");
        doHttpBenchmark(1, false, false, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTP1 - with keepalives - seq");
        doHttpBenchmark(1, true, false, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - no keepalives - seq");
        doHttpBenchmark(1, false, true, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - with keepalives - seq");
        doHttpBenchmark(1, true, true, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - seq");
        doHttpBenchmark(1, true, true, Protocol.HTTP_2, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", "Concurrent HTTP1");
        Log.e("MainActivity", ", HTTP1 - with keepalives - x10");
        doHttpBenchmark(10, true, false, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTP1 - with keepalives - x25");
        doHttpBenchmark(25, true, false, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTP1 - with keepalives - x50");
        doHttpBenchmark(50, true, false, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTP1 - with keepalives - x100");
        doHttpBenchmark(100, true, false, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");


        Log.e("MainActivity", "Concurrent HTTPS");
        Log.e("MainActivity", ", HTTPS1 - with keepalives - x10");
        doHttpBenchmark(10, true, true, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - with keepalives - x25");
        doHttpBenchmark(25, true, true, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - with keepalives - x50");
        doHttpBenchmark(50, true, true, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - with keepalives - x100");
        doHttpBenchmark(100, true, true, Protocol.HTTP_1_1, 100, 0);
        Log.e("--", "--");

        Log.e("MainActivity", "Concurrent HTTP2");
        Log.e("MainActivity", ", HTTPS2 - with keepalives - x10");
        doHttpBenchmark(10, true, true, Protocol.HTTP_2, 100, 0);
        Log.e("--","--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - x25");
        doHttpBenchmark(25, true, true, Protocol.HTTP_2, 100, 0);
        Log.e("--","--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - x50");
        doHttpBenchmark(50, true, true, Protocol.HTTP_2, 100, 0);
        Log.e("--","--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - x100");
        doHttpBenchmark(100, true, true, Protocol.HTTP_2, 100, 0);
        Log.e("--","--");

    }

    public void doConcurrencyBenchmarkSuite() {

        /*Log.e("MainActivity", "Concurrent HTTPS");
        Log.e("MainActivity", ", HTTPS1 - with keepalives - x4");
        doHttpBenchmark(4, true, false, Protocol.HTTP_1_1, 100, 50 * 1024);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - with keepalives - x8");
        doHttpBenchmark(8, true, false, Protocol.HTTP_1_1, 100, 50 * 1024);
        Log.e("--", "--");*/

        /*Log.e("MainActivity", ", HTTPS1 - with keepalives - x16");
        doHttpBenchmark(16, true, false, Protocol.HTTP_1_1, 100, 50 * 1024);
        Log.e("--", "--");

        Log.e("MainActivity", ", HTTPS1 - with keepalives - x32");
        doHttpBenchmark(32, true, false, Protocol.HTTP_1_1, 100, 50 * 1024);
        Log.e("--", "--");*/

        Log.e("MainActivity", "Concurrent HTTP2");
        Log.e("MainActivity", ", HTTPS2 - with keepalives - x4");
        doHttpBenchmark(4, true, true, Protocol.HTTP_2, 100, 50 * 1024);
        Log.e("--","--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - x8");
        doHttpBenchmark(8, true, true, Protocol.HTTP_2, 100, 50 * 1024);
        Log.e("--","--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - x16");
        doHttpBenchmark(16, true, true, Protocol.HTTP_2, 100, 50 * 1024);
        Log.e("--","--");

        Log.e("MainActivity", ", HTTPS2 - with keepalives - x32");
        doHttpBenchmark(32, true, true, Protocol.HTTP_2, 100, 50 * 1024);
        Log.e("--","--");

    }



    public void doHttpBenchmark(int numConcurrentConnections, boolean doKeepAlives, boolean secure, Protocol protocol, int numRequests, int numBytes) {
        //String httpEndpoint = "http://nghttp2.org/httpbin/bytes";
        String httpEndpoint = "http://httpbin.org/bytes";
        String httpsEndpoint = "https://httpbin.org/bytes";

        String http2Endpoint = "https://nghttp2.org/httpbin/bytes";



        List<Protocol> protocols = new ArrayList<Protocol>();

        // Client Parameters
        int numIdleConnections = doKeepAlives ? numConcurrentConnections : 0;
        int keepAliveDuration =  doKeepAlives ? 10000 : 0;
        protocols.add(protocol);

        ExecutorService service = Executors.newFixedThreadPool(numConcurrentConnections);
        Dispatcher dispatcher = new Dispatcher(service);
        dispatcher.setMaxRequests(numConcurrentConnections);
        dispatcher.setMaxRequestsPerHost(numConcurrentConnections);

        ConnectionPool pool = new ConnectionPool( numIdleConnections, keepAliveDuration);

        OkHttpClient client = new OkHttpClient();
        client.setConnectionPool(pool);
        protocols.add(Protocol.HTTP_1_1);
        client.setProtocols(protocols);
        client.setDispatcher(dispatcher);
        client.setFollowRedirects(false);
        //client.setConnectTimeout(5, TimeUnit.MINUTES);
        client.setReadTimeout(5, TimeUnit.MINUTES);
        //client.setWriteTimeout(5, TimeUnit.MINUTES);

        //client.networkInterceptors().add(new LoggingInterceptor());
        client.networkInterceptors().add(new AbortIfNoProtocolInterceptor(protocol));

        String urlPrefix = protocol == Protocol.HTTP_2 ? http2Endpoint :
                secure ? httpsEndpoint : httpEndpoint;
        String url = String.format("%s/%d", urlPrefix, numBytes);

        CountDownLatch latch = new CountDownLatch(numRequests);

        for (int i = 0; i < numRequests; i++) {
            Request.Builder builder = new Request.Builder();
            Request request = builder.get().url(url).build();

            Call call = client.newCall(request);
            call.enqueue(newCallback(i, latch));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pool.evictAll();
        service.shutdown();
    }

    protected Callback newCallback(final int seq, final CountDownLatch latch) {

        final long time = System.currentTimeMillis();

        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("MainActivity", "Request: " + seq + " -- FAILURE", e);
                latch.countDown();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                long endTime = System.currentTimeMillis() - time;
                Log.e("MainActivity", ", " + seq + ", " + endTime );
                latch.countDown();
            }
        };
    }

    class LoggingInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Log.v("LoggingInterceptor", String.format("Using socket: " + chain.connection().getSocket()));
            Response response = chain.proceed(request);

            return response;
        }
    }

    class AbortIfNoProtocolInterceptor implements Interceptor {

        Protocol prot;
        public AbortIfNoProtocolInterceptor(Protocol prot) {
            this.prot = prot;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {

            Protocol protocol = chain.connection().getProtocol();
            if (protocol != prot) {
                throw new IOException("Connection was not made with the specified protocol: expected " + prot + " got " + protocol);
            }

            return chain.proceed(chain.request());
        }
    }


}
