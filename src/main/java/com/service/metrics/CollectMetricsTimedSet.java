package com.service.metrics;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollectMetricsTimedSet {
    CollectMetrics[] value();
}
