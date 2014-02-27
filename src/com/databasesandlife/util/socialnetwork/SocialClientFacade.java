package com.databasesandlife.util.socialnetwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.databasesandlife.util.CompositeIterable;


public class SocialClientFacade {

	SocialNetworkToken[] tokens;
	Iterable<SocialFriend<?>>[] threads;
	
    @SuppressWarnings("unchecked")
    public SocialClientFacade(SocialNetworkToken... tokens){
		this.tokens = tokens;
		threads = new Iterable[tokens.length];
	}
    
    public SocialClientFacade(List<SocialNetworkToken> tokens){
    	this(tokens.toArray(new SocialNetworkToken[0]));
    }
	
    /**
     * Social network errors are silently ignored; friends from those networks are not returned.
     * This is because, when displaying a feed, it is more important to display some data and not to crash, 
     * than to display the right friends always.
     */
	public Iterable<SocialFriend<?>> getAllFriendsIgnoringErrors(SocialClientFactory fac) {
		for(int i = 0;i<tokens.length;i++){
			threads[i] = getFriendsIgnoringErrors(fac, tokens[i]);
		}
		List<Iterable<SocialFriend<?>>> iterators = new ArrayList<Iterable<SocialFriend<?>>>();
		for(Iterable<SocialFriend<?>> i : threads){
			iterators.add(i);
		}
		return new CompositeIterable<SocialFriend<?>>(iterators);
	}
	
	private Iterable<SocialFriend<?>> getFriendsIgnoringErrors(SocialClientFactory fac, final SocialNetworkToken t) {
		try{
			final SocialClientThreadRunnable runnable = new SocialClientThreadRunnable(t.getClient(fac),t.accessToken);
			final Thread socialFriendsFetcher = new Thread(runnable, "GetSocialFriends-" + t.getClient(fac).getClass().getSimpleName());
			socialFriendsFetcher.start();
			return new Iterable<SocialFriend<?>>(){
				public Iterator<SocialFriend<?>> iterator() {
					try{
						socialFriendsFetcher.join();
						return runnable.getResult().iterator();
					}catch(Exception e){
			            Logger.getLogger(getClass()).warn("Failed fetching friends for network " + t.getSocialNetwork().name(), e);
			            return new ArrayList<SocialFriend<?>>().iterator();
					}
				} 
			};
		}catch(Exception e){ 
		    Logger.getLogger(getClass()).warn("Failed fetching friends for network " + t.getSocialNetwork().name(), e);
		    return new ArrayList<SocialFriend<?>>();
		}
	}
}
