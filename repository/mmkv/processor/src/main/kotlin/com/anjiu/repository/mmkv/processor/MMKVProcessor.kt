package com.anjiu.repository.mmkv.processor

import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled
import com.anjiu.repository.mmkv.processor.generator.MMKVFileGenerator
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */
class MMKVProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(MMKVClass::class.qualifiedName.orEmpty())
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(MMKVClassBuilder(), Unit) }

        return symbols.filter { !it.validate() }.toList()
    }

    inner class MMKVClassBuilder : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // 使用了类注解的类应该是一个公开的类
            if (classDeclaration.isPrivate()) {
                throw RuntimeException("使用的MMKVClass注解的类应该是一个公开的类($classDeclaration)")
            }

            // 获取类注解实例
            val mmkvClass = classDeclaration.getAnnotationsByType(MMKVClass::class).firstOrNull()
                ?: throw RuntimeException("无法获取到类注解($classDeclaration)")

            // 获取生成类的类名
            val className = mmkvClass.className
            if (className.isEmpty()) {
                throw RuntimeException("生成类的类名不能为空($classDeclaration)")
            }

            // 获取类中带MMKVField注解的变量
            val properties = classDeclaration.getDeclaredProperties().mapNotNull {
                it.getAnnotationsByType(MMKVFiled::class)
                    .firstOrNull()
                    ?.run { it to this }
            }.toList()

            if (properties.isEmpty()) {
                throw RuntimeException("类中没有MMKVFiled的变量($classDeclaration)")
            }

            // 生成kotlin规格的文件
            val fileSpec = MMKVFileGenerator.buildFileSpec(
                className,
                classDeclaration,
                mmkvClass,
                properties
            )

            // 生成文件
            codeGenerator.createNewFile(
                Dependencies(false, classDeclaration.containingFile!!),
                fileSpec.packageName,
                fileSpec.name
            )
                .writer()
                .use { fileSpec.writeTo(it) }
        }
    }
}