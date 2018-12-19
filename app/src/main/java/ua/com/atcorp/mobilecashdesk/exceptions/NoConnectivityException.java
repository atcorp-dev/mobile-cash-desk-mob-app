package ua.com.atcorp.mobilecashdesk.exceptions;

import java.io.IOException;

public class NoConnectivityException extends IOException {

    @Override
    public String getMessage() {
        return "No Internet Access";
    }

}
