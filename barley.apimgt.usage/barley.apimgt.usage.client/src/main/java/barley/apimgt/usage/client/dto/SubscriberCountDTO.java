package barley.apimgt.usage.client.dto;

public class SubscriberCountDTO {

	private String apiVersion;
	private long subscriptionCount;
	
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public long getSubscriptionCount() {
		return subscriptionCount;
	}
	public void setSubscriptionCount(long subscriptionCount) {
		this.subscriptionCount = subscriptionCount;
	}
	
	@Override
	public String toString() {
		return "SubscriberCountDTO [apiVersion=" + apiVersion + ", subscriptionCount=" + subscriptionCount + "]";
	}
	
	
}
