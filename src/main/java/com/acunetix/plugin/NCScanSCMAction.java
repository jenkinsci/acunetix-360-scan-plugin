package com.acunetix.plugin;

import com.acunetix.model.VCSCommit;
import hudson.model.Action;

import javax.annotation.CheckForNull;

public class NCScanSCMAction implements Action {

	private final VCSCommit vcsCommit;

	public NCScanSCMAction(VCSCommit vcsCommit) {
		this.vcsCommit = vcsCommit;
	}

	public VCSCommit getVcsCommit() {
		return vcsCommit;
	}

	@CheckForNull
	@Override
	public String getIconFileName() {
		return null;
	}

	@CheckForNull
	@Override
	public String getDisplayName() {
		return "NCScanSCMAction";
	}

	@CheckForNull
	@Override
	public String getUrlName() {
		return null;
	}
}
