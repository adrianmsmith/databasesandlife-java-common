package com.databasesandlife.util.socialnetwork.xing;

import com.databasesandlife.util.socialnetwork.School;
import com.databasesandlife.util.socialnetwork.SocialUser;
import com.databasesandlife.util.socialnetwork.Work;

@SuppressWarnings("serial")
public class XingSocialUser extends SocialUser<XingUserId> {

    protected Work[] work;
    protected School[] education;
    
    @Override
    public Work[] getWork() {
        return work;
    }

    @Override
    public School[] getEducation() {
        return education;
    }

}
