package com.databasesandlife.util.socialnetwork;

import java.util.List;

/**
 * Represents a Runnable that can be a Future object
 *
 * @param T the future returns a list of this
 */
public interface Futureable<T> extends Runnable{

    public List<T> getResult() throws SocialNetworkUnavailableException,SocialNetworkUserException;
    
}
