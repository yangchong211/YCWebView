package com.ycbjie.webviewlib.tls;

import android.net.SSLCertificateSocketFactory;
import android.os.Build;

import com.ycbjie.webviewlib.utils.X5LogUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * <pre>
 *     @author  yangchong
 *     email  : yangchong211@163.com
 *     blog   : https://github.com/yangchong211
 *     time   : 2020/04/27
 *     desc   : 自定义SSLSocketFactory
 *     revise:
 * </pre>
 */
public class WebTlsSniSocketFactory extends SSLSocketFactory {

    private final String TAG = WebTlsSniSocketFactory.class.getSimpleName();
    private HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    private HttpsURLConnection conn;

    public WebTlsSniSocketFactory(HttpsURLConnection conn) {
        this.conn = conn;
    }

    @Override
    public Socket createSocket() throws IOException {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return null;
    }

    // TLS layer
    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    @Override
    public Socket createSocket(Socket plainSocket, String host, int port, boolean autoClose)
            throws IOException {
        String peerHost = this.conn.getRequestProperty("Host");
        if (peerHost == null){
            peerHost = host;
        }
        X5LogUtils.i("customized createSocket. host: " + peerHost);
        InetAddress address = plainSocket.getInetAddress();
        if (autoClose) {
            // we don't need the plainSocket
            plainSocket.close();
        }
        // create and connect SSL socket, but don't do hostname/certificate verification yet
        SSLCertificateSocketFactory sslSocketFactory = (SSLCertificateSocketFactory)
                SSLCertificateSocketFactory.getDefault(0);
        SSLSocket ssl = (SSLSocket) sslSocketFactory.createSocket(address, port);

        // enable TLSv1.1/1.2 if available
        ssl.setEnabledProtocols(ssl.getSupportedProtocols());

        // set up SNI before the handshake
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            X5LogUtils.i("Setting SNI hostname");
            sslSocketFactory.setHostname(ssl, peerHost);
        } else {
            X5LogUtils.d("No documented SNI support on Android <4.2, trying with reflection");
            try {
                java.lang.reflect.Method setHostnameMethod =
                        ssl.getClass().getMethod("setHostname", String.class);
                setHostnameMethod.invoke(ssl, peerHost);
            } catch (Exception e) {
                X5LogUtils.e("SNI not useable", e);
            }
        }
        // verify hostname and certificate
        SSLSession session = ssl.getSession();
        if (!hostnameVerifier.verify(peerHost, session)){
            throw new SSLPeerUnverifiedException("Cannot verify hostname: " + peerHost);
        }
        X5LogUtils.i("Established " + session.getProtocol() + " connection with " +
                session.getPeerHost() + " using " + session.getCipherSuite());
        return ssl;
    }

}
