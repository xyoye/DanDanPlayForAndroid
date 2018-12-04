package com.xyoye.dandanplay.utils.interf;

import java.util.List;

/**
 * 本地数据库事务操作接口
 * Created by xyy on 2017/6/23.
 */
public interface Dao<T, Integer> {

    void insert(T t);

    void delete(Integer id);

    void update(T t, Integer id);

    T get(Integer id);

    List<T> getList();
}
