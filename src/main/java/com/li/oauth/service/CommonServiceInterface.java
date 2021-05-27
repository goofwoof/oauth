package com.li.oauth.service;


import com.li.oauth.domain.Exception.AlreadyExistsException;
import com.li.oauth.domain.Exception.EntityNotFoundException;
import com.li.oauth.domain.Exception.NotImplementException;
import com.li.oauth.domain.JsonObjects;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CommonServiceInterface<T> {

    default JsonObjects<T> list(Authentication authentication, Pageable pageable) {
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
