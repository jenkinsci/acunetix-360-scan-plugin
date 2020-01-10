package com.acunetix.plugin;

import com.acunetix.model.*;
import com.acunetix.utility.AppCommon;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.jenkinsci.Symbol;
import org.json.simple.parser.ParseException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class NCScanBuilder extends Builder implements SimpleBuildStep {

    private String ncScanType;
    private String ncWebsiteId;
    private String ncProfileId;
    private Secret ncApiToken;
    private String ncServerURL;

    @DataBoundConstructor
    public NCScanBuilder(String ncScanType, String ncWebsiteId) {
        this.ncScanType = ncScanType == null ? "" : ncScanType;
        this.ncWebsiteId = ncWebsiteId == null ? "" : ncWebsiteId;
    }

    public String getNcScanType() {
        return ncScanType;
    }

    public void setNcScanType(String ncScanType) {
        this.ncScanType = ncScanType;
    }

    public String getNcWebsiteId() {
        return ncWebsiteId;
    }

    public void setNcWebsiteId(String ncTargetURL) {
        this.ncWebsiteId = ncTargetURL;
    }

    public String getNcProfileId() {
        return ncProfileId;
    }

    @DataBoundSetter
    public void setNcProfileId(String ncProfileId) {
        this.ncProfileId = ncProfileId;
    }

    public String getNcServerURL() {
        return ncServerURL;
    }

    @DataBoundSetter
    public void setNcServerURL(String ncServerURL) {
        this.ncServerURL = ncServerURL;
    }

    public Secret getNcApiToken() {
        if (ncApiToken == null) {
            ncApiToken = getDescriptor().getNcApiToken();
        }
        return ncApiToken;
    }

    @DataBoundSetter
    public void setNcApiToken(String ncApiToken) {
        this.ncApiToken = Secret.fromString(ncApiToken);
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        logInfo("Scan step created...", listener);

        NCScanSCMAction scmAction = build.getAction(NCScanSCMAction.class);
        VCSCommit commit = scmAction == null ? VCSCommit.empty(build) : scmAction.getVcsCommit();

        try {
            ScanRequestHandler(build, commit, listener);
        } catch (Exception e) {
            build.addAction(new NCScanResultAction(ScanRequestResult.errorResult("Scan Request Failed:: " + e.getMessage())));

            throw new IOException(e);
        }
    }

    private void ScanRequestHandler(Run<?, ?> build, VCSCommit commit, TaskListener listener) throws Exception {
        DescriptorImpl descriptor = getDescriptor();
        String ncServerURL = StringUtils.isBlank(getNcServerURL()) ? descriptor.getNcServerURL() : getNcServerURL();

        Secret ncApiToken = getNcApiToken() != null && StringUtils.isBlank(getNcApiToken().getPlainText()) ?
                descriptor.getNcApiToken() :
                getNcApiToken();

        commit.setRootURL(descriptor.getRootURL());

        ScanRequest scanRequest = new ScanRequest(
                ncServerURL, ncApiToken, ncScanType, ncWebsiteId, ncProfileId, commit);

        logInfo("Requesting scan...", listener);
        org.apache.http.HttpResponse scanRequestResponse = scanRequest.scanRequest();
        logInfo("Response status code: " + scanRequestResponse.getStatusLine().getStatusCode(), listener);

        ScanRequestResult scanRequestResult = new ScanRequestResult(scanRequestResponse, ncServerURL, ncApiToken);
        build.replaceAction(new NCScanResultAction(scanRequestResult));

        // HTTP status code 201 refers to created. This means our request added to queue. Otherwise it is failed.
        if (scanRequestResult.getHttpStatusCode() == 201 && !scanRequestResult.isError()) {
            ScanRequestSuccessHandler(scanRequestResult, listener);
        } else {
            ScanRequestFailureHandler(scanRequestResult, listener);
        }
    }

    private void ScanRequestSuccessHandler(ScanRequestResult scanRequestResult, TaskListener listener) throws IOException {
        logInfo("Scan requested successfully.", listener);
    }

    private void ScanRequestFailureHandler(ScanRequestResult scanRequestResult, TaskListener listener) throws Exception {
        logError("Scan request failed. Error Message: " + scanRequestResult.getErrorMessage(), listener);

        throw new Exception("Acunetix 360 Plugin: Failed to start the scan. Response status code: " + scanRequestResult.getHttpStatusCode());
    }

    private void logInfo(String message, TaskListener listener) {
        listener.getLogger().println("> Acunetix 360 Plugin: " + message);
    }

    private void logError(String message, TaskListener listener) {
        listener.error("> Acunetix 360 Plugin: " + message);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("NCScanBuilder")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private long lastEditorId = 0;
        private ArrayList<WebsiteModel> websiteModels = new ArrayList<>();

        private String ncServerURL;
        private Secret ncApiToken;
        private String rootURL;

        public DescriptorImpl() {
            super(NCScanBuilder.class);
            load();
        }

        public String getNcServerURL() {
            return ncServerURL;
        }

        public void setNcServerURL(String ncServerURL) {
            this.ncServerURL = ncServerURL;
        }

        public Secret getNcApiToken() {
            return ncApiToken;
        }

        public void setNcApiToken(String ncApiToken) {
            this.ncApiToken = Secret.fromString(ncApiToken);
        }

        public String getRootURL() {
            return rootURL;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData)
                throws FormException {
            req.bindParameters(this);
            this.ncServerURL = formData.getString("ncServerURL");
            this.ncApiToken = Secret.fromString(formData.getString("ncApiToken"));
            this.rootURL = Jenkins.getInstance().getRootUrl();
            save();
            return super.configure(req, formData);
        }

        @Override
        public String getConfigPage() {
            try {
                updateWebsiteModels(ncServerURL, ncApiToken);
            } catch (Exception e) {
            }

            return super.getConfigPage();
        }

        @JavaScriptMethod
        public synchronized String createEditorId() {
            return String.valueOf(lastEditorId++);
        }

        public ListBoxModel doFillNcScanTypeItems() {
            ListBoxModel model = new ListBoxModel();
            model.add("-- Please select a scan type --", "");
            model.add("Incremental", "Incremental");
            model.add("Full (With primary profile)", "FullWithPrimaryProfile");
            model.add("Full (With selected profile)", "FullWithSelectedProfile");

            return model;
        }

        public ListBoxModel doFillNcWebsiteIdItems() {
            ListBoxModel model = new ListBoxModel();
            model.add("-- Please select a website --", "");
            for (WebsiteModel websiteModel : websiteModels) {
                model.add(websiteModel.getDisplayName(), websiteModel.getId());
            }

            return model;
        }

        public ListBoxModel doFillNcProfileIdItems(@QueryParameter String ncWebsiteId) {
            WebsiteModel websiteModel = new WebsiteModel();
            for (WebsiteModel wm : websiteModels) {
                if (ncWebsiteId != null && wm.getId().equals(ncWebsiteId)) {
                    websiteModel = wm;
                    break;
                }
            }

            String placeholderText;
            final ArrayList<WebsiteProfileModel> websiteProfileModels = websiteModel.getProfiles();
            if (websiteProfileModels.isEmpty()) {
                placeholderText = "-- No profile found --";
            } else {
                placeholderText = "-- Please select a profile name --";
            }

            ListBoxModel model = new ListBoxModel();
            model.add(placeholderText, "");

            for (WebsiteProfileModel websiteProfileModel : websiteProfileModels) {
                model.add(websiteProfileModel.getName(), websiteProfileModel.getId());
            }

            return model;
        }

        private int updateWebsiteModels(final String ncServerURL, final Secret ncApiToken) throws IOException, URISyntaxException, ParseException {
            WebsiteModelRequest websiteModelRequest = new WebsiteModelRequest(ncServerURL, ncApiToken);
            final HttpResponse response = websiteModelRequest.getPluginWebSiteModels();
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                websiteModels = new ArrayList<>();
                websiteModels.addAll(websiteModelRequest.getWebsiteModels());
            }

            return statusCode;
        }

        @POST
        public FormValidation doValidateAPI(@QueryParameter final String ncServerURL,
                                            @QueryParameter final Secret ncApiToken) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            try {
                int statusCode = updateWebsiteModels(ncServerURL, ncApiToken);
                if (statusCode == 200) {
                    return FormValidation.ok("Successfully connected to the Acunetix 360.");
                } else {
                    return FormValidation.error("Acunetix 360 rejected the request. HTTP status code: " + statusCode);
                }
            } catch (Exception e) {
                return FormValidation.error("Failed to connect to the Acunetix 360. : " + e.toString());
            }
        }

        public FormValidation doCheckNcServerURL(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_missingApiURL());
            } else if (!AppCommon.isUrlValid(value)) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidApiURL());
            }

            return FormValidation.ok();
        }

        @POST
        public FormValidation doCheckNcApiToken(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_missingApiToken());
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckNcScanType(@QueryParameter String value) {
            try {
                ScanType.valueOf(value);
            } catch (Exception ex) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidScanType());
            }

            return FormValidation.ok();
        }


        public FormValidation doCheckNcWebsiteID(@QueryParameter String value) {

            if (!AppCommon.isGUIDValid(value)) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidWebsiteId());
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckNcProfileId(@QueryParameter String value, @QueryParameter String ncScanType) {

            boolean isRequired;

            try {
                ScanType type = ScanType.valueOf(ncScanType);
                isRequired = type != ScanType.FullWithPrimaryProfile;
            } catch (Exception ex) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidProfileId());
            }

            if (isRequired && !AppCommon.isGUIDValid(value)) {
                return FormValidation.error(Messages.NCScanBuilder_DescriptorImpl_errors_invalidProfileId());
            }

            return FormValidation.ok();
        }
    }
}
