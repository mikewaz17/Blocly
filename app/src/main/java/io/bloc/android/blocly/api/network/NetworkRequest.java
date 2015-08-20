package io.bloc.android.blocly.api.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mike on 8/20/2015.
 */
public abstract class NetworkRequest<Result> {
    public static final int ERROR_IO = 1;
    public static final int ERROR_MALFORMED_URL = 2;
    private int errorCode;

    protected void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public abstract Result performRequest();

    protected InputStream openStream(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            setErrorCode(ERROR_MALFORMED_URL);
            return null;
        }
        InputStream inputStream = null;
        try {
            // #5
            inputStream = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            setErrorCode(ERROR_IO);
            return null;
        }
        return inputStream;
    }
}
/* #50 set up the result type under NetworkRequest, showing the specific error types for the request
 * The subclasses under NetworkRequest implement the performRequest method. InputStream represents a
 * resource for the data found at URL. The openStream method in order to create the network connection
 * data recovery at this address. The IOException is activated if it doesn't reach the server.
 * */

