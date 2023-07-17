package com.service.metrics;


import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE,ElementType.METHOD})
@Repeatable(CollectMetricsTimedSet.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollectMetrics {
    String value();
    String tag() default "";

    String appMetricType() default "appMetric";

}
