package cz.xtf.radanalytics.oshinko.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OshinkoOptions {
	private String as = "--as=";
	private String certificateAuthority = "--certificate-authority=";
	private String clientCertificate = "--client-certificate=";
	private String clientKey = "--client-key=";
	private String cluster = "--cluster=";
	private String config = "--config=";
	private String context = "--context=";
	private String insecureSkipTlsVerify = "--insecure-skip-tls-verify=";
	private String logFlushFrequency = "--log-flush-frequency=";
	private String logLevel = "--loglevel=";
	private String logspec = "--logspec=";
	private String matchServerVersion = "--match-server-version=";
	private String namespace = "--namespace=";
	private String requestTimeout = "--request-timeout=";
	private String server = "--server=";
	private String token = "--token=";
	private String user = "--user=";

	@Builder
	public OshinkoOptions(String as,
				String certificateAuthority,
				String clientCertificate,
				String clientKey,
				String cluster,
				String config,
				String context,
				boolean insecureSkipTlsVerify,
				String logFlushFrequency,
				int logLevel,
				String logspec,
				boolean matchServerVersion,
				String namespace,
				String requestTimeout,
				String server,
				String token,
				String user) {
		this.as = "--as=" + as;
		this.certificateAuthority = "--certificate-authority=" + certificateAuthority;
		this.clientCertificate = "--client-certificate=" + clientCertificate;
		this.clientKey = "--client-key=" + clientKey;
		this.cluster = "--cluster=" + cluster;
		this.config = "--config=" + config;
		this.context = "--context=" + context;
		this.insecureSkipTlsVerify = "--insecure-skip-tls-verify=" + insecureSkipTlsVerify;
		this.logFlushFrequency = "--log-flush-frequency=" + logFlushFrequency;
		this.logLevel = "--loglevel=" + logLevel;
		this.logspec = "--logspec=" + logspec;
		this.matchServerVersion = "--match-server-version=" + matchServerVersion;
		this.namespace = "--namespace=" + namespace;
		this.requestTimeout = "--request-timeout=" + requestTimeout;
		this.server = "--server=" + server;
		this.token = "--token=" + token;
		this.user = "--user=" + user;
	}

	public void setToken(String token) {
		this.token = this.token + token;
	}

	public void setAs(String as) {
		this.as = this.as + as;
	}

	public void setCertificateAuthority(String certificateAuthority) {
		this.certificateAuthority = this.certificateAuthority + certificateAuthority;
	}

	public void setClientCertificate(String clientCertificate) {
		this.clientCertificate = this.clientCertificate + clientCertificate;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = this.clientKey + clientKey;
	}

	public void setCluster(String cluster) {
		this.cluster = this.cluster + cluster;
	}

	public void setConfig(String config) {
		this.config = this.config + config;
	}

	public void setContext(String context) {
		this.context = this.context + context;
	}

	public void setInsecureSkipTlsVerify(String insecureSkipTlsVerify) {
		this.insecureSkipTlsVerify = this.insecureSkipTlsVerify + insecureSkipTlsVerify;
	}

	public void setLogFlushFrequency(String logFlushFrequency) {
		this.logFlushFrequency = this.logFlushFrequency + logFlushFrequency;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = this.logLevel = logLevel;
	}

	public void setLogspec(String logspec) {
		this.logspec = this.logspec = logspec;
	}

	public void setMatchServerVersion(String matchServerVersion) {
		this.matchServerVersion = this.matchServerVersion + matchServerVersion;
	}

	public void setNamespace(String namespace) {
		this.namespace = this.namespace + namespace;
	}

	public void setRequestTimeout(String requestTimeout) {
		this.requestTimeout = this.requestTimeout = requestTimeout;
	}

	public void setServer(String server) {
		this.server = this.server + server;
	}

	public void setUser(String user) {
		this.user = this.user + user;
	}
}
