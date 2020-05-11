package com.acunetix.plugin;

import com.acunetix.model.VCSCommit;
import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;

/**
 * This class registers an {@link SCMListener} with Jenkins which allows us to create the "Checkout
 * successful" event.
 */
@Extension
public class NCScanSCMListener<Entry> extends SCMListener {
    /**
     * Invoked right after the source code for the build has been checked out. It will NOT be called
     * if a checkout fails.
     *
     * @param build     - Current build
     * @param scm       - Configured SCM
     * @param listener  - Current build listener
     * @param changelog - Changelog
     * @throws Exception if an error is encountered
     */
    @Override
    public void onChangeLogParsed(Run<?, ?> build, SCM scm, TaskListener listener,
            ChangeLogSet<?> changelog) throws Exception {
        super.onChangeLogParsed(build, scm, listener, changelog);

        build.replaceAction(new NCScanSCMAction(new VCSCommit(build, changelog)));
    }
}
