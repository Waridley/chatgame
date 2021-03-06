/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.ttv;

import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;

import java.util.*;

public interface TtvStorageInterface {
	
	TwitchHelix helix();
	String helixAccessToken();
	
	default TtvUser findOrCreateTtvUserFromId(String ttvUserId) {
		return findOrCreateTtvUser(getHelixUsersFromIds(Collections.singletonList(ttvUserId)).get(0));
	}
	default TtvUser findOrCreateTtvUserFromLogin(String ttvLogin) {
		return findOrCreateTtvUser(getHelixUsersFromLogins(Collections.singletonList(ttvLogin)).get(0));
	}
	TtvUser findOrCreateTtvUser(User user);
	
	default Optional<TtvUser> findTtvUserById(String ttvUserId) {
		return findTtvUserByLogin(getHelixUsersFromIds(Collections.singletonList(ttvUserId)).get(0));
	}
	default Optional<TtvUser> findTtvUserByLogin(String ttvLogin) {
		return findTtvUserByLogin(getHelixUsersFromLogins(Collections.singletonList(ttvLogin)).get(0));
	}
	Optional<TtvUser> findTtvUserByLogin(User user);
	
	List<TtvUser> findTtvUsers(List<User> helixUsers);
	List<TtvUser> findTtvUsersByIds(List<String> userIds);
	
	
	default List<User> getHelixUsersFromIds(List<String> ids) {
		List<User> result = new ArrayList<>(ids.size());
		int divSize = 100;
		List<List<String>> idLists = new ArrayList<>((ids.size() / divSize) + 1);
		for(int i = 0; i < ids.size(); i += divSize) {
			int to = i + divSize;
			if(to >= ids.size()) to = ids.size();
			idLists.add(ids.subList(i, to));
		}
		for(List<String> l : idLists) {
			UserList userList = helix().getUsers(
					helixAccessToken(),
					l,
					null
			).execute();
			result.addAll(userList.getUsers());
		}
		return result;
	}
	
	default List<User> getHelixUsersFromLogins(List<String> logins) {
		List<User> result = new Vector<>(logins.size());
		int divSize = 100;
		List<List<String>> loginLists = new Vector<>((logins.size() / divSize) + 1);
		for(int i = 0; i < logins.size(); i += divSize) {
			int to = i + divSize;
			if(to > logins.size()) to = logins.size();
			loginLists.add(logins.subList(i, to));
		}
		for(List<String> l : loginLists) {
			UserList userList = helix().getUsers(
					helixAccessToken(),
					null,
					l
			).execute();
			result.addAll(userList.getUsers());
		}
		
		return result;
	}
	
	TtvUser logMinutes(TtvUser user, long minutes, boolean online);
	TtvUser logGuestMinutes(TtvUser user, long minutes, String guestName);
}
