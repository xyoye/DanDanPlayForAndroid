package com.xyoye.core.interf;

import java.util.List;

/**
 * 本地数据库事务操作接口
 * Created by yzd on 2016/5/24.
 */
public interface Dao<T, Integer> {

    void insert(T t);

    void delete(Integer id);

    void update(T t, Integer id);

    T get(Integer id);

    List<T> getList();
}
