package com.example.kiran.be_you;

/**
 * Created by Kiran on 8/21/2017.
 */

public class chat_message  {
    Boolean seen ;
    long timestamp;
    public chat_message(){

    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public chat_message(Boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}
