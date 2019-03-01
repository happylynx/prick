package com.github.happylynx.prick.lib.guice;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@BindingAnnotation
public @interface PrickRoot {
}
