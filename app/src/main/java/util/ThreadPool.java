package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2015/3/1.
 */
public class ThreadPool {
    private static ExecutorService es = Executors.newFixedThreadPool(3);
    static {
        
    }
    public static void submit(Runnable r){
        es.submit(r);
    }
}
