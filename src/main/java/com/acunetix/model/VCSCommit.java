package com.acunetix.model;

import com.acunetix.utility.AppCommon;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VCSCommit {

    private final String ciBuildServerVersion;
    private final String ciNcPluginVersion;
    private final String buildId;
    private final String buildConfigurationName;
    private final String buildURL;
    private final boolean buildHasChange;
    private final String versionControlName;
    private final String committer;
    private final String vcsVersion;
    private final String ciTimestamp;
    private String rootURL = "";

    public VCSCommit(Run<?, ?> build, ChangeLogSet<?> changelog) {
        buildId = String.valueOf(build.number);
        buildConfigurationName = build.getParent().getName();
        buildURL = getBuildURL(build);

        SimpleDateFormat iso8601DateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

        buildHasChange = changelog != null && !changelog.isEmptySet();
        if (buildHasChange) {
            versionControlName = changelog.getKind();

            final ChangeLogSet.Entry change = (ChangeLogSet.Entry) changelog.getItems()[0];
            long ciTimestampInMilliseconds = change.getTimestamp();
            ciTimestamp = iso8601DateTimeFormat.format(new Date(ciTimestampInMilliseconds));

            final User author = change.getAuthor();
            vcsVersion = change.getCommitId();

            String fullName = author.getFullName();
            String displayName = author.getDisplayName();
            if (AppCommon.isValidEmailAddress(fullName)) {
                committer = fullName;
            } else if (AppCommon.isValidEmailAddress(displayName)) {
                committer = displayName;
            } else {
                committer = fullName;
            }

        } else {
            versionControlName = "";
            ciTimestamp = iso8601DateTimeFormat.format(new Date());
            vcsVersion = "";
            committer = "";
        }

        VersionNumber versionNumber = Jenkins.getVersion();
        ciBuildServerVersion = versionNumber != null ? versionNumber.toString() : "Not found.";
        ciNcPluginVersion = null; // don't add plugin version number
    }

    public static VCSCommit empty(Run<?, ?> build) {
        return new VCSCommit(build, null);
    }

    public void setRootURL(String rootURL) {
        if (rootURL == null) {
            this.rootURL = "";
            return;
        }
        this.rootURL = rootURL;
    }

    public void addVcsCommitInfo(List<NameValuePair> params) {
        params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildId", buildId));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.IntegrationSystem", "Jenkins"));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildServerVersion",
                ciBuildServerVersion));
        params.add(
                new BasicNameValuePair("VcsCommitInfoModel.CiNcPluginVersion", ciNcPluginVersion));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildConfigurationName",
                buildConfigurationName));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildUrl", rootURL + buildURL));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.CiBuildHasChange",
                String.valueOf(buildHasChange)));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.CiTimestamp", ciTimestamp));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.VcsName", versionControlName));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.VcsVersion", vcsVersion));
        params.add(new BasicNameValuePair("VcsCommitInfoModel.Committer", committer));
    }

    private String getBuildURL(Run<?, ?> build) {

        try {
            return build.getUrl();
        } catch (Exception ex) {
            return "";
        }
    }
}
