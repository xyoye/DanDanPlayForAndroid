package com.anjiu.repository.mmkv.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */
class MMKVProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MMKVProcessor(environment.codeGenerator)
    }
}