package com.ardian.erclinkusb;

/**
 * Created by Ardian Iqbal Yusmartito on 03/10/22
 * Github : https://github.com/ALU-syntax
 * Twitter : https://twitter.com/mengkerebe
 * Instagram : https://www.instagram.com/ardian_iqbal_
 * LinkedIn : https://www.linkedin.com/in/ardianiqbal
 */
import android.content.Context;
import android.util.Log;

import com.facebook.stetho.server.LazySocketHandler;
import com.facebook.stetho.server.LocalSocketServer;
import com.facebook.stetho.server.ProtocolDetectingSocketHandler;
import com.facebook.stetho.server.SocketHandler;
import com.facebook.stetho.server.SocketHandlerFactory;
import com.facebook.stetho.server.SocketLike;
import com.facebook.stetho.server.SocketLikeHandler;
import com.facebook.stetho.server.http.ExactPathMatcher;
import com.facebook.stetho.server.http.HandlerRegistry;
import com.facebook.stetho.server.http.LightHttpServer;
import com.facebook.stetho.websocket.SimpleEndpoint;
import com.facebook.stetho.websocket.WebSocketHandler;

import java.io.IOException;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class LocalWebSocketServer {
    private static final String TAG = "LocalWebSocketServer";
    private final LocalSocketServer mServer;
    /**
     * Create and start a new local websocket server
     *
     * @param context the context.
     * @param address the local socket address to listen on.
     * @param endpoint the websocket endpoint.
     */
    public static LocalWebSocketServer createAndStart(Context context, String address, SimpleEndpoint endpoint) {
        return new LocalWebSocketServer(context, address, endpoint);
    }

    /**
     * Stop the server
     */
    public void stop() {
        mServer.stop();
    }

    private LocalWebSocketServer(Context context, String address, SimpleEndpoint endpoint) {
        mServer = new LocalSocketServer(address, address,
                new LazySocketHandler(new ForwardSocketHandlerFactory(context, endpoint)));

        // Start the local socket server on new thread
        new Thread(TAG + "-" + mServer.getName()) {


            @Override
            public void run() {
                try {
                    mServer.run();
                } catch (IOException ignored) {
                    Log.e(TAG, "Local socket server failed to run.");
                }
            }
        }.start();
    }


    static class ForwardSocketHandlerFactory implements SocketHandlerFactory {
        final Context context;
        final SimpleEndpoint endpoint;

        ForwardSocketHandlerFactory(Context context, SimpleEndpoint endpoint) {
            this.context = context;
            this.endpoint = endpoint;
        }

        @Override
        public SocketHandler create() {
            final ProtocolDetectingSocketHandler socketHandler =
                    new ProtocolDetectingSocketHandler(context);

            // Setup a light http server
            final HandlerRegistry registry = new HandlerRegistry();
            registry.register(
                    new ExactPathMatcher("/"),
                    new WebSocketHandler(endpoint));
            final LightHttpServer server = new LightHttpServer(registry);

            socketHandler.addHandler(
                    new ProtocolDetectingSocketHandler.AlwaysMatchMatcher(),
                    new SocketLikeHandler() {
                        @Override
                        public void onAccepted(SocketLike socket) throws IOException {
                            server.serve(socket);
                        }
                    });

            return socketHandler;
        }
    }
}
