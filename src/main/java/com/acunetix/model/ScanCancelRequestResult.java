package com.acunetix.model;

import org.apache.http.HttpResponse;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.acunetix.utility.AppCommon;

public class ScanCancelRequestResult extends ScanRequestBase {
    public static ScanCancelRequestResult errorResult(final String errorMessage) {
        return new ScanCancelRequestResult(errorMessage);
    }

    private final int httpStatusCode;
    private String data;
    private String scanTaskID;
    private boolean isError;
    private String errorMessage;

    private ScanCancelRequestResult(final String errorMessage) {
        super();
        this.errorMessage = errorMessage;
        httpStatusCode = 0;
        isError = true;
        data = "";
    }

    public ScanCancelRequestResult(final HttpResponse response) throws MalformedURLException, URISyntaxException {
        super();
        httpStatusCode = response.getStatusLine().getStatusCode();
        isError = httpStatusCode != 200;

        if (!isError) {
            try {
                data = AppCommon.parseResponseToString(response);
                isError = !(boolean) AppCommon.parseJsonValue(data, "IsValid");
                if (!isError) {
                    scanTaskID = (String) AppCommon.parseJsonValue(data, "ScanTaskId");             
                } else {
                    errorMessage = (String) AppCommon.parseJsonValue(data, "ErrorMessage");
                }
            } catch (final ParseException ex) {
                isError = true;
                errorMessage = "Scan info request result is not parsable::: " + ex.toString();
            } catch (final IOException ex) {
                isError = true;
                errorMessage = "Scan info request result is not readable::: " + ex.toString();
            }
        }
    }

    public String getScanTaskId() {
        return scanTaskID;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return isError;
    }
}
