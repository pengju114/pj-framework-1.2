/**
 * 
 */
package com.pj.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法标志注解，此注解用于唯一标志一个方法
 * @author luzhenwen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodIdentifier {
	public int methodId() default 0;
}
