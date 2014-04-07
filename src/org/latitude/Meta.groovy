package org.latitude

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * The main annotation in Latitude.
 * 
 * Marks a field which will be used to generate a meta-property
 * class.
 * 
 * Must be applied on top of a field. The ASTx will generate a 
 * class with meta references to the properties of the type class 
 * and a static getter. Only properties and public fields are 
 * considered when creating the meta references.
 * 
 * Example:
 * 
 * <pre>
 * class Foo {
 *   @Meta Foo meta
 *   
 *   String bar
 * }
 * </pre>
 * 
 * Will generate the following class:
 * 
 * <pre>
 * class Foo {
 *   static Foo$Meta getMeta() { new Foo$Meta() }
 *   String bar
 *   static class Foo$Meta {
 *     MetaProperty getBar() {
 *       Foo.metaClass.properties.find { MetaProperty m -> m.name == 'bar' }
 *     }
 *   }
 * }
 * </pre>
 * 
 * @author will_lp
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@GroovyASTTransformationClass("org.latitude.ast.MetaAST")
@interface Meta {
}
