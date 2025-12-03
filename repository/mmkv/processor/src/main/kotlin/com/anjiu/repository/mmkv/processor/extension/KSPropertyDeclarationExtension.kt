package com.anjiu.repository.mmkv.processor.extension

import com.anjiu.repository.mmkv.annotation.MMKVFiled
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName


/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */

/**
 * 生成set方法
 */
fun KSPropertyDeclaration.generateSetFunction(
    mmkvFiled: MMKVFiled,
    classDeclaration: KSClassDeclaration
): FunSpec {
    // 属性所在类的类名
    val className = classDeclaration.qualifiedName?.asString()
        ?: throw RuntimeException("无法获取类名")

    // 属性名
    val propertyName = qualifiedName?.getShortName()
        ?: throw RuntimeException("无法获取变量名")

    // 变量应该是val
    if (isMutable) {
        throw RuntimeException("变量应使用val修饰（$className.$propertyName）")
    }

    val ksType = type.resolve()
    // 变量是否支持生成方法
    val supportedClassName = SupportedClassName.get(ksType)
        ?: throw RuntimeException("变量类型不支持生成方法（$className.$propertyName），请参考MMKV支持的类型")

    // 变量不能为null
    if (supportedClassName.nullable.not() && ksType.nullability == Nullability.NULLABLE) {
        throw RuntimeException("变量类型不支持设置为可空（$className.$propertyName）")
    }

    // 需要生成的方法名
    val setFunctionName = "set${propertyName.toUpperCaseInitials()}"

    // MMKV的key值
    val key = mmkvFiled.key.ifEmpty { "key_${propertyName.toLowerCaseUnderline()}" }

    // MMKV的方法名
    val mmkvFunctionName = supportedClassName.mmkvPutFun

    // 生成方法
    val funBuilder = FunSpec.builder(setFunctionName)
        .addModifiers(KModifier.PUBLIC)
        .addParameter("value", ksType.toTypeName())
    if (mmkvFiled.commit) {
        funBuilder.addStatement("return mmkv.$mmkvFunctionName(\"$key\", value).commit()")
        //使用commit，有boolean值返回
        funBuilder.returns(Boolean::class)
    } else {
        funBuilder.addStatement("mmkv.$mmkvFunctionName(\"$key\", value).apply()")
    }
    return funBuilder.build()
}

/**
 * 生成get方法
 */
fun KSPropertyDeclaration.generateGetFunction(
    mmkvFiled: MMKVFiled,
    classDeclaration: KSClassDeclaration
): FunSpec {
    // 属性所在类的类名
    val className = classDeclaration.qualifiedName?.asString()
        ?: throw RuntimeException("无法获取类名")

    // 属性名
    val propertyName = qualifiedName?.getShortName()
        ?: throw RuntimeException("无法获取变量名")

    // 变量应该是不可变的
    if (isMutable) {
        throw RuntimeException("变量应使用val修饰（$className.$propertyName）")
    }

    val ksType = type.resolve()
    // 变量是否支持生成方法
    val supportedClassName = SupportedClassName.get(ksType)
        ?: throw RuntimeException("变量类型不支持生成方法（$className.$propertyName），请参考MMKV支持的类型")

    // 变量不能为null
    if (supportedClassName.nullable.not() && ksType.nullability == Nullability.NULLABLE) {
        throw RuntimeException("变量类型不支持设置为可空（$className.$propertyName）")
    }

    // 需要生成的方法名
    val setFunctionName = "get${propertyName.toUpperCaseInitials()}"

    // MMKV的key值
    val key = mmkvFiled.key.ifEmpty { "key_${propertyName.toLowerCaseUnderline()}" }

    // MMKV的方法名
    val mmkvFunctionName = supportedClassName.mmkvGetFun

    // MMKV的默认值
    val defaultValue = "${className}.${propertyName}"

    // 生成方法
    val builder = FunSpec.builder(setFunctionName).addModifiers(KModifier.PUBLIC)

    // 判断是否需要添加空安全逻辑
    if (supportedClassName.nullable && ksType.nullability == Nullability.NOT_NULL) {
        builder.addStatement("return mmkv.$mmkvFunctionName(\"$key\", $defaultValue\n) ?: $defaultValue")
    } else {
        builder.addStatement("return mmkv.$mmkvFunctionName(\"$key\", $defaultValue)")
    }

    return builder.returns(ksType.toTypeName()).build()
}