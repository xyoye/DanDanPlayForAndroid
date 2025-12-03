package com.anjiu.repository.mmkv.processor.generator

import com.anjiu.repository.mmkv.annotation.MMKVClass
import com.anjiu.repository.mmkv.annotation.MMKVFiled
import com.anjiu.repository.mmkv.processor.extension.generateGetFunction
import com.anjiu.repository.mmkv.processor.extension.generateSetFunction
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */
object MMKVFileGenerator {

    fun buildFileSpec(
        className: String,
        classDeclaration: KSClassDeclaration,
        mmkvClass: MMKVClass,
        properties: List<Pair<KSPropertyDeclaration, MMKVFiled>>
    ): FileSpec {
        // 获取类的构造器
        val clazzBuilder = getKotlinClazzBuilder(className, mmkvClass.customMMKV)

        // 遍历带@MMKVFiled注解的属性
        for ((property, mmkvFiled) in properties) {

            // 生成get方法
            val getFunSpec = property.generateGetFunction(mmkvFiled, classDeclaration)
            clazzBuilder.addFunction(getFunSpec)

            // 生成set方法
            val setFunSpec = property.generateSetFunction(mmkvFiled, classDeclaration)
            clazzBuilder.addFunction(setFunSpec)
        }

        // 生成类文件
        return FileSpec.builder(classDeclaration.packageName.asString(), "$className.kt")
            .addType(clazzBuilder.build())
            .build()
    }


    /**
     * 生成类的构造器
     */
    private fun getKotlinClazzBuilder(className: String, customMMKV: Boolean): TypeSpec.Builder {
        val typeSpecBuilder = TypeSpec.objectBuilder(className)

        //引入MMKV类
        val mmkvBundle = ClassName("com.tencent.mmkv", "MMKV")
        //属性构造器
        val propertySpec = PropertySpec.builder("mmkv", mmkvBundle)

        //是否自定义mmkv的初始化
        if (customMMKV) {
            //私有、延迟实例化、可变
            propertySpec.addModifiers(KModifier.PRIVATE, KModifier.LATEINIT).mutable()

            //添加mmkv初始化方法
            typeSpecBuilder.addFunction(
                FunSpec.builder("initMMKV")
                    .addParameter("initMMKV", mmkvBundle)
                    .addStatement("mmkv = initMMKV")
                    .build()
            )

        } else {
            //添加mmkv默认初始化方法
            propertySpec.initializer("MMKV.defaultMMKV()")
                .addModifiers(KModifier.PRIVATE)
        }

        return typeSpecBuilder.addProperty(propertySpec.build())
    }
}