package com.acunetix.model;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The ProxyBlock class corresponds to an "optionalBlock" global.jelly element that
 * represents proxy configuration data.
 *
 */
public class ProxyBlock {

    private final Boolean _useProxy;
    private final String _pHost;
    private final String _pPort;
    private final String _pUser;
    private final String _pPassword;

    /**
     * Corresponds to the {@code useProxy} identifier referenced in a global.jelly file.
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getUseProxy() {
        return this._useProxy;
    }

    /**
     * Corresponds to the {@code pHost} identifier referenced in a global.jelly file.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getpHost() {
        return this._pHost;
    }

    /**
     * Corresponds to the {@code pPort} identifier referenced in a global.jelly file.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getpPort() {
        return this._pPort;
    }

    /**
     * Corresponds to the {@code pUser} identifier referenced in a global.jelly file.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getpUser() {
        return this._pUser;
    }

    /**
     * Corresponds to the {@code pPassword} identifier referenced in a global.jelly file.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getpPassword() {
        return this._pPassword;
    }

    
    /**
     * Called by Jenkins with form data.
     *
     * @param useProxy  a {@link java.lang.Boolean} object.
     * @param pHost     a {@link java.lang.String} object.
     * @param pPort     a {@link java.lang.String} object.
     * @param pUser     a {@link java.lang.String} object.
     * @param pPassword a {@link java.lang.String} object.
     */
    @DataBoundConstructor
    public ProxyBlock(Boolean useProxy, String pHost, String pPort, String pUser, String pPassword) {
        this._useProxy = useProxy;
        this._pHost = pHost;
        this._pPort = pPort;
        this._pUser = pUser;
        this._pPassword = pPassword;
    }
}
