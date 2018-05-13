package edu.etu.vkpi;

import com.vk.api.sdk.client.AbstractQueryBuilder;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;

import java.util.Date;

public class QueryWithDelay<T, R> {

    private static long lastCall = 0;

    private AbstractQueryBuilder<T,R> abstractQueryBuilder;
    private int delay;

    public QueryWithDelay(AbstractQueryBuilder<T, R> abstractQueryBuilder, int delay){
        this.abstractQueryBuilder = abstractQueryBuilder;
        this.delay = delay;
    }

    public R execute() throws ClientException, ApiException, InterruptedException {
        long currentTime = new Date().getTime();
        long diff = currentTime - lastCall;
        if(delay > diff){
            Thread.sleep(delay - diff);
            lastCall = currentTime + delay - diff;
        }
        else lastCall = currentTime;
        return abstractQueryBuilder.execute();
    }
}
