package com.acunetix.model;

import com.google.gson.Gson;
import com.acunetix.utility.AppCommon;
import org.apache.http.HttpResponse;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ScanInfoRequestResult extends ScanRequestBase {
    public static ScanInfoRequestResult errorResult(final String errorMessage) {
        return new ScanInfoRequestResult(errorMessage);
    }

    private final int httpStatusCode;
    private String data;
    private String scanTaskID;
    private ScanTaskState scanTaskState;
    private HashMap<String, Integer> FoundedSeverityAndCounts;
    private boolean isError;
    private String errorMessage;

    private ScanInfoRequestResult(final String errorMessage) {
        super();
        this.errorMessage = errorMessage;
        httpStatusCode = 0;
        FoundedSeverityAndCounts = new HashMap<String, Integer>();
        isError = true;
        data = "";
    }

    public ScanInfoRequestResult(final HttpResponse response) throws MalformedURLException, URISyntaxException {
        super();
        httpStatusCode = response.getStatusLine().getStatusCode();
        isError = httpStatusCode != 200;

        if (!isError) {
            try {
                data = AppCommon.parseResponseToString(response);
                isError = !(boolean) AppCommon.parseJsonValue(data, "IsValid");
                if (!isError) {
                    scanTaskID = (String) AppCommon.parseJsonValue(data, "ScanTaskId");

                    final String sTaskState = (String) AppCommon.parseJsonValue(data, "State");
                    scanTaskState = ScanTaskState.valueOf(sTaskState);

                    org.json.simple.JSONObject foundedSeverityınfo = (org.json.simple.JSONObject) AppCommon
                            .parseJsonValue(data, "FoundedSeverityAndCounts");
                    FoundedSeverityAndCounts = new Gson().fromJson(foundedSeverityınfo.toString(), HashMap.class);
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

    public ScanTaskState getScanTaskState() {
        return scanTaskState;
    }

    public HashMap<String, Integer> getFoundedSeverityAndCounts() {
        return FoundedSeverityAndCounts;
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

    public boolean checkSeverity(final String ncSeverity) {
        if (isError()) {
            return false;
        } else if (ncSeverity == null) {
            return false;
        } else {
            for (Map.Entry<String, Integer> entry : this.getFoundedSeverityAndCounts().entrySet()) {
                String foundedSeverityLevel = entry.getKey();
                if (ncSeverity.contains(foundedSeverityLevel)) {
                    return true;
                }
            }

            return false;
        }
    }
}
