package org.iot.v.jt809.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author haye
 * @date 4/7/26 4:09 PM
 * <p>
 * Keep It Simple, Stupid
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JT809OctetString {
    int length();
}
