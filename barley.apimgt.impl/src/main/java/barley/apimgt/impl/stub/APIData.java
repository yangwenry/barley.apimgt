package barley.apimgt.impl.stub;

public class APIData {
	
	private String name;
	
	private String host;
	
	private int port = -1;
	
	private String context;
	
	private String fileName;

	private String artifactContainerName;

	private boolean isEdited;
	
	private ResourceData[] resources;

	private boolean isStatisticsEnable;

	private boolean isTracingEnable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public ResourceData[] getResources() {
		return resources;
	}

	public void setResources(ResourceData[] resources) {
		this.resources = resources;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Get the artifactContainer of the endpoint which deployed from
	 * @return endpoint description
	 */
	public String getArtifactContainerName() {
		return artifactContainerName;
	}

	/**
	 * Set the endpoint artifact container name as a metadata
	 * @param artifactContainerName endpoint name
	 */
	public void setArtifactContainerName(String artifactContainerName) {
		this.artifactContainerName = artifactContainerName;
	}

	/**
	 * Get the edit state of the endpoint
	 * @return endpoint description
	 */
	public boolean getIsEdited() {
		return isEdited;
	}

	/**
	 * Set the edit state of the endpoint as a metadata
	 * @param isEdited endpoint name
	 */
	public void setIsEdited(boolean isEdited) {
		this.isEdited = isEdited;
	}

	public void setStatisticsEnable(boolean enableStatistics) {
		this.isStatisticsEnable = enableStatistics;
	}

	public boolean getStatisticsEnable() {
		return isStatisticsEnable;
	}

	public boolean getTracingEnable() {
		return isTracingEnable;
	}

	public void setTracingEnable(boolean tracingEnable) {
		isTracingEnable = tracingEnable;
	}
}
