package com.acunetix.plugin;

import com.acunetix.model.ScanRequestResult;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.HttpResponses;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.verb.GET;

import javax.annotation.CheckForNull;

public class ACXScanResultAction implements Action, RunAction2 {

    private ScanRequestResult scanRequestResult;

    private transient Run run;

    public ACXScanResultAction(ScanRequestResult scanRequestResult) {
        this.scanRequestResult = scanRequestResult;
    }

    public ScanRequestResult getScanRequestResult() {
        return scanRequestResult;
    }

    public void setScanRequestResult(ScanRequestResult scanRequestResult) {
        this.scanRequestResult = scanRequestResult;
    }

    public String getError() {
        if (scanRequestResult.isError()) {
            return "true";
        } else {
            return "false";
        }
    }

    public String getReportGenerated() {
        if (scanRequestResult.isReportGenerated()) {
            return "true";
        } else {
            return "false";
        }
    }

    @GET
    public HttpResponse doGetContent() {
        String content = scanRequestResult.getReport().getContent();
        return HttpResponses.html(content);
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Acunetix 360 Report";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "acunetixreport";
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    public Run getRun() {
        return run;
    }
}
