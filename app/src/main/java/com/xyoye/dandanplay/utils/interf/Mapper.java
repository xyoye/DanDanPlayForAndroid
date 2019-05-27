package com.xyoye.dandanplay.utils.interf;

import java.util.Collection;

/**
 *  领域模型映射
 *  T 模型类型
 *  K 领域类型
 * Modified by xyoye on 2017/6/23.
 */
public interface Mapper<T, K> {

    T transformEntity(K obj) throws Exception;

    K transformBean(T obj) throws Exception;

    Collection<T> transformEntityCollection(Collection<K> obj) throws Exception;

    Collection<K> transformBeanCollection(Collection<T> obj) throws Exception;

    Class<K> getEntityClass();

    Class<T> getBeanClass();

}
