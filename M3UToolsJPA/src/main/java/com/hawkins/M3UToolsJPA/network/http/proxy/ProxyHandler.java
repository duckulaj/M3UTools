package com.hawkins.M3UToolsJPA.network.http.proxy;

import java.net.URL;

public abstract interface ProxyHandler {
	public abstract boolean isSupported(int paramInt);

	public abstract boolean isProxyCacheSupported();

	public abstract void init(BrowserProxyInfo paramBrowserProxyInfo)
			throws Exception;

	public abstract ProxyInfo[] getProxyInfo(URL paramURL) throws Exception;
}
