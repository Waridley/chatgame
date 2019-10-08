package com.waridley.ttv;

import com.github.twitch4j.helix.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class TtvUser {
	
	private long id = -1L;
	
	private User helixUser = null;
	
	private long offlineMinutes = 0L;
	
	private long onlineMinutes = 0L;
	
	private long guestMinutes = 0L;
	
	public TtvUser(User helixUser) {
		setHelixUser(helixUser);
		setId(helixUser.getId());
	}
	
	public long channelMinutes() { return onlineMinutes + offlineMinutes; }
	public long totalMinutes() { return onlineMinutes + offlineMinutes + guestMinutes; }
	
	private Map<String, Object> properties ;
	
	private Object getProperty(String key) {
		return properties.get(key);
	}
	
	private void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
}
