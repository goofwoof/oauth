package com.li.oauth.service;

import com.li.oauth.domain.Exception.NotImplementException;
import com.li.oauth.domain.Role;

public interface RoleService extends com.li.oauth.service.CommonServiceInterface<Role> {
    default Role findByRoleName(String roleName) throws NotImplementException {
        throw new NotImplementException();
    }
}
