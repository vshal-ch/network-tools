package com.example.networktools;

import java.util.concurrent.Executor;

public class ThreadExecuter implements Executor {
    Thread thread = new Thread();
    @Override
    public void execute(Runnable r){
        this.thread = new Thread(r);
        this.thread.start();
    }
    public boolean isAlive(){
        return this.thread.isAlive();
    }
}
