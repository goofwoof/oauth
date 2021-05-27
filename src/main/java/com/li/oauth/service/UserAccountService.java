package com.li.oauth.service;

import com.li.oauth.domain.ApplyStatusEnum;
import com.li.oauth.domain.Exception.EntityNotFoundException;
import com.li.oauth.domain.JsonObjects;
import com.li.oauth.domain.RoleApply;
import com.li.oauth.domain.RoleEnum;
import com.li.oauth.domain.UserAccount;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

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

    RoleApply applyRole(String name, RoleEnum roleDeveloper);

    void reviewRole(Long applyId, Boolean review);

    List<RoleApply> queryDeveloperApplyStatus(ApplyStatusEnum applyStatus, Pageable pageable);
}
