package com.jwebmp.guicedpersistence.db.annotations;

import java.lang.annotation.*;

/**
 * Transactional annotation for JPA to use specific entity manager annotation as registered in the abstract module
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SuppressWarnings("unused")
public @interface Transactional
{
	/**
	 * Entity manager annotation as registered in the abstract module
	 *
	 * @return The class type for the annotation that identifies the entity manager to use
	 */
	Class<? extends Annotation> entityManagerAnnotation();

	/**
	 * A list of exceptions to rollback on, if thrown by the transactional method. These exceptions
	 * are propagated correctly after a rollback.
	 */
	Class<? extends Exception>[] rollbackOn() default RuntimeException.class;

	/**
	 * A list of exceptions to <b>not<b> rollback on. A caveat to the rollbackOn clause. The
	 * disjunction of rollbackOn and ignore represents the list of exceptions that will trigger a
	 * rollback. The complement of rollbackOn and the universal set plus any exceptions in the ignore
	 * set represents the list of exceptions that will trigger a commit. Note that ignore exceptions
	 * take precedence over rollbackOn, but with subtype granularity.
	 */
	Class<? extends Exception>[] ignore() default {};
}
