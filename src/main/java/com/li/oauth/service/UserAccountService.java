package com.li.oauth.service;

import com.li.oauth.domain.Exception.EntityNotFoundException;
import com.li.oauth.domain.JsonObjects;
import com.li.oauth.domain.UserAccount;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserAccountService extends com.li.oauth.service.CommonServiceInterface<UserAccount> {
    JsonObjects<UserAccount> listByUsername(String username,
                                            int pageNum,
                                            int pageSize,
                                            String sortField,
                                            String sortOrder);

    UserAccount findByUsername(String username) throws EntityNotFoundException;

    boolean existsByUsername(String username);

    void loginSuccess(String username) throws EntityNotFoundException;

    void loginFailure(String username);

    List<UserAccount> findAllDevelopers(Pageable page);
}
