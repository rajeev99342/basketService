package com.service.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CollectCustomMetrics {
    String env = "prod";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    public void simulateMetrics(String metricName, double value, String tag){
        CompletableFuture.runAsync(()->{
                ProcessBuilder processBuilder = new ProcessBuilder();
                Date date = new Date();
                long time = date.getTime()/1000;
                String shellCommand = "echo \""+
                        time +" metrics." + metricName +".value "+
                        decimalFormat.format(value)+
                        " tag="+ tag+
                        " env="+env + "\" | /usr/bin/cosmos";
        });
    }

}
