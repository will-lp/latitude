package org.latitude.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.MixinNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation


@GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
class MetaAST implements ASTTransformation {

	static final META_INNER_CLASS_SUFFIX = '$Meta'
	
	ClassNode outerClass
	ClassNode innerClass
	
	void visit(ASTNode[] nodes, SourceUnit unit) {
		
		if (nodes == null || nodes[1] == null) return
		
		def node = (FieldNode) nodes[1]
		
		outerClass = node.declaringClass
		
		innerClass = createInnerClass()
		
		outerClass.addMethod(createGetMetaPropertyMethod(node))
		
		outerClass.module.addClass(innerClass)
		
		println "done!"
	}
	
	
	def createInnerClass() {
		
		def innerClass = new InnerClassNode(
			outerClass, 
			outerClass.name + META_INNER_CLASS_SUFFIX, 
			ClassNode.ACC_PUBLIC | ClassNode.ACC_STATIC, 
			new ClassNode(Object),
			[new ClassNode(GroovyObject)] as ClassNode[],
			[] as MixinNode[])
		
		createPropertyGetters(outerClass).each { innerClass.addMethod(it) }
		
		innerClass
	}
	
	
	/**
	 * 
	 * @param node the field with @Meta
	 * @return
	 */
	def createGetMetaPropertyMethod(FieldNode node) {
		def block = new BlockStatement(
			[
				new ExpressionStatement(
					new ConstructorCallExpression(innerClass, [] as ArgumentListExpression)
				)
			], 
			null
		)
		
		def method = new MethodNode(
			getMethodName(node), 
			MethodNode.ACC_PUBLIC | MethodNode.ACC_STATIC, 
			new ClassNode(innerClass), 
			[] as Parameter[], 
			[] as ClassNode[],
			block
		)
		
		method
	}
	
	
	def createPropertyGetters(clazz) {
		getProperties(clazz).collect { property ->
			
			def text = """
				${outerClass.name}.metaClass.properties.find {
					MetaProperty m ->
					println  "searching for ${property.name}" 
					m.name == "${property.name}"
				}
			"""
			
			def propertyFinder = new AstBuilder()
				.buildFromString(text)
				.head()
			
			new MethodNode(
				getMethodName(property), 
				MethodNode.ACC_PUBLIC, 
				new ClassNode(MetaProperty),
				[] as Parameter[],
				[] as ClassNode[],
				propertyFinder
			)
			
		}
	}
	
	
	def buildFindProperty(property) {
		
		def metaClass
		
		def properties = new PropertyExpression(
			new ClassExpression(outerClass), 
			new ConstantExpression('properties'))
		
		def closure = new ClosureExpression(
			new Parameter(new ClassNode(MetaProperty), 'm'), 
			new BlockStatement(
				new ReturnStatement()
			), null)
		
		new BlockStatement([
			new ReturnStatement(
				new MethodCallExpression(
					properties,
					'find', 
					closure)
			)
		], null)
	}
	
	
	def getMethodName(property) {
		"get"+property.name.capitalize()
	}
	
	
	def getProperties(ClassNode clazz) {
		def props = clazz.properties + 
			clazz.fields.findAll { FieldNode f -> 
				f.modifiers == FieldNode.ACC_PUBLIC 
			}
		
		props
	}
	
}
