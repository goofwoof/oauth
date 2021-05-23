package com.li.oauth.service;


import com.li.oauth.domain.AlreadyExistsException;
import com.li.oauth.domain.EntityNotFoundException;
import com.li.oauth.domain.JsonObjects;
import com.li.oauth.domain.NotImplementException;

import java.util.List;

public interface CommonServiceInterface<T> {


    default JsonObjects<T> list(int pageNum,
                                int pageSize,
                                String sortField,
                                String sortOrder) {
        throw new NotImplementException();
    }

    default T create(T t) throws AlreadyExistsException {
        throw new NotImplementException();
    }

    default T retrieveById(long id) throws EntityNotFoundException {
        throw new NotImplementException();
    }

    default T updateById(T t) throws EntityNotFoundException {
        throw new NotImplementException();
    }

    default void deleteById(long id) {
        throw new NotImplementException();
    }

    default void updateRecordStatus(long id, int recordStatus) {
        throw new NotImplementException();
    }

    default List<T> findAll() {
        throw new NotImplementException();
    }
}
