//package com.service.metrics;
//
//import com.netflix.discovery.converters.Auto;
//import io.micrometer.core.annotation.Incubating;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.Tag;
//import io.micrometer.core.instrument.Tags;
//import io.micrometer.core.lang.NonNullApi;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.util.function.Function;
//
//@Aspect
//@NonNullApi
//@Component
//@Incubating(since = "1.0,0")
//public class CollectMetricAspect {
//    MeterRegistry registry;
//    Function<ProceedingJoinPoint,Iterable<Tag>> tagsBasedOnJointPoint;
//    String appName;
//
//    @Autowired
//    private CollectCustomMetrics collectCustomMetrics;
//
////    public CollectMetricAspect(MeterRegistry registry, String appName){
////        this(registry,pjp->
////                Tags.of("class",pjp.get.getStaticPart().get))
////    }
//    public CollectMetricAspect(MeterRegistry registry, Function<ProceedingJoinPoint,Iterable<Tag>> tagsBasedOnJointPoint,String appName){
//        this.registry = registry;
//        this.tagsBasedOnJointPoint = tagsBasedOnJointPoint;
//        this.appName = appName;
//    }
//
//    @Around("execution (@com.service.metrics.CollectMetrics * *.*(...)")
//    public Object timedMethod(ProceedingJoinPoint pjp){
//        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
//        CollectMetrics collectMetrics = method.getAnnotation(CollectMetrics.class);
//        String metricName = appName + "-" + collectMetrics.value();
//
//    }
//
//}
