package com.li.oauth.service.impl;

import com.github.dozermapper.core.Mapper;
import com.li.oauth.domain.Exception.NotImplementException;
import com.li.oauth.domain.Role;
import com.li.oauth.persistence.entity.RoleEntity;
import com.li.oauth.persistence.repository.RoleRepository;
import com.li.oauth.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    Mapper dozerMapper;

    @Override
    public Role findByRoleName(String roleName) throws NotImplementException {
        RoleEntity roleEntity = roleRepository.findByRoleName(roleName);
        if (roleEntity != null) {
            return dozerMapper.map(roleEntity, Role.class);
        } else {
            return null;
        }
    }

}
