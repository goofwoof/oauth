package com.li.oauth.service.impl;

import com.github.dozermapper.core.Mapper;
import com.li.oauth.ErrorCodeConstant;
import com.li.oauth.domain.ApplyStatusEnum;
import com.li.oauth.domain.Exception.AlreadyExistsException;
import com.li.oauth.domain.Exception.EntityNotFoundException;
import com.li.oauth.domain.Exception.OAuth2Exception;
import com.li.oauth.domain.JsonObjects;
import com.li.oauth.domain.RoleApply;
import com.li.oauth.domain.RoleEnum;
import com.li.oauth.domain.UserAccount;
import com.li.oauth.persistence.entity.RoleApplyEntity;
import com.li.oauth.persistence.entity.RoleEntity;
import com.li.oauth.persistence.entity.UserAccountEntity;
import com.li.oauth.persistence.repository.RoleApplyRepository;
import com.li.oauth.persistence.repository.RoleRepository;
import com.li.oauth.persistence.repository.UserAccountRepository;
import com.li.oauth.service.UserAccountService;
import com.li.oauth.utils.DateUtils;
import com.li.oauth.utils.JpaPageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    Mapper dozerMapper;

    @Autowired
    RoleApplyRepository roleApplyRepository;

    @Autowired
    RoleRepository repository;

    @Value("${signin.failure.max:5}")
    private int failureMax;

    @Override
    public JsonObjects<UserAccount> listByUsername(String username, int pageNum, int pageSize, String sortField, String sortOrder) {
        JsonObjects<UserAccount> jsonObjects = new JsonObjects<>();
        Pageable pageable = JpaPageUtils.createPageable(pageNum, pageSize, sortField, sortOrder);
        Page<UserAccountEntity> page;
        if (StringUtils.isBlank(username)) {
            page = userAccountRepository.findAll(pageable);
        } else {
            page = userAccountRepository.findByUsernameLike(username + "%", pageable);
        }
        if (page.getContent().size() > 0) {
            jsonObjects.setRecordsTotal(page.getTotalElements());
            jsonObjects.setRecordsFiltered(page.getTotalElements());
            page.getContent().forEach(u -> jsonObjects.getData().add(dozerMapper.map(u, UserAccount.class)));
        }
        return jsonObjects;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAccount create(UserAccount userAccount) throws AlreadyExistsException {
        UserAccountEntity exist = userAccountRepository.findByUsername(userAccount.getUsername());
        if (exist != null) {
            throw new AlreadyExistsException(userAccount.getUsername() + " already exists!");
        }
        UserAccountEntity userAccountEntity = dozerMapper.map(userAccount, UserAccountEntity.class);
        userAccountEntity.getRoles().clear();
        if (userAccount.getRoles() != null && userAccount.getRoles().size() > 0) {
            userAccount.getRoles().forEach(e -> {
                RoleEntity roleEntity = roleRepository.findByRoleName(e.getRoleName());
                if (roleEntity != null) {
                    userAccountEntity.getRoles().add(roleEntity);
                }
            });
        }
        userAccountRepository.save(userAccountEntity);
        return dozerMapper.map(userAccountEntity, UserAccount.class);
    }

    @Override
    public UserAccount retrieveById(long id) throws EntityNotFoundException {
        Optional<UserAccountEntity> entityOptional = userAccountRepository.findById(id);
        return dozerMapper.map(entityOptional.orElseThrow(EntityNotFoundException::new), UserAccount.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAccount updateById(UserAccount userAccount) throws EntityNotFoundException {
        Optional<UserAccountEntity> entityOptional = userAccountRepository.findById(Long.parseLong(userAccount.getId()));
        UserAccountEntity e = entityOptional.orElseThrow(EntityNotFoundException::new);
        if (StringUtils.isNotEmpty(userAccount.getPassword())) {
            e.setPassword(userAccount.getPassword());
        }
        e.setNickName(userAccount.getNickName());
        e.setBirthday(userAccount.getBirthday());
        e.setMobile(userAccount.getMobile());
        e.setProvince(userAccount.getProvince());
        e.setCity(userAccount.getCity());
        e.setAddress(userAccount.getAddress());
        e.setAvatarUrl(userAccount.getAvatarUrl());
        e.setEmail(userAccount.getEmail());

        userAccountRepository.save(e);
        return dozerMapper.map(e, UserAccount.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecordStatus(long id, int recordStatus) {
        Optional<UserAccountEntity> entityOptional = userAccountRepository.findById(id);
        UserAccountEntity e = entityOptional.orElseThrow(EntityNotFoundException::new);
        e.setRecordStatus(recordStatus);
        userAccountRepository.save(e);
    }

    @Override
    public UserAccount findByUsername(String username) throws EntityNotFoundException {
        UserAccountEntity userAccountEntity = userAccountRepository.findByUsername(username);
        if (userAccountEntity != null) {
            return dozerMapper.map(userAccountEntity, UserAccount.class);
        } else {
            throw new EntityNotFoundException(username + " not found!");
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userAccountRepository.existsByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void loginSuccess(String username) throws EntityNotFoundException {
        UserAccountEntity userAccountEntity = userAccountRepository.findByUsername(username);
        if (userAccountEntity != null) {
            userAccountEntity.setFailureCount(0);
            userAccountEntity.setFailureTime(null);
            userAccountRepository.save(userAccountEntity);
        } else {
            throw new EntityNotFoundException(username + " not found!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void loginFailure(String username) {
        UserAccountEntity userAccountEntity = userAccountRepository.findByUsername(username);
        if (userAccountEntity != null) {
            if (userAccountEntity.getFailureTime() == null) {
                userAccountEntity.setFailureCount(1);
            } else {
                if (DateUtils.beforeToday(userAccountEntity.getFailureTime())) {
                    userAccountEntity.setFailureCount(0);
                } else {
                    userAccountEntity.setFailureCount(userAccountEntity.getFailureCount() + 1);
                }
            }
            userAccountEntity.setFailureTime(LocalDateTime.now());
            if (userAccountEntity.getFailureCount() >= failureMax && userAccountEntity.getRecordStatus() >= 0) {
                userAccountEntity.setRecordStatus(-1);
            }
            userAccountRepository.save(userAccountEntity);
        }
    }

    @Override
    public List<UserAccount> findAllDevelopers(Pageable page) {
        RoleEntity byRoleName = roleRepository.findByRoleName(RoleEnum.ROLE_DEVELOPER.name());
        Page<UserAccountEntity> allDevelopers = userAccountRepository.findByRolesIn(Collections.singletonList(byRoleName), page);
        return allDevelopers.stream()
            .map(userAccountEntity -> dozerMapper.map(userAccountEntity, UserAccount.class))
            .collect(Collectors.toList());
    }

    @Override
    public RoleApply applyRole(String name, RoleEnum roleDeveloper) {
        RoleApplyEntity roleApplyEntity = new RoleApplyEntity();
        UserAccountEntity user = userAccountRepository.findByUsername(name);
        if (user.getRoles().stream().anyMatch(roleEntity -> RoleEnum.ROLE_DEVELOPER.name().equals(roleEntity.getRoleName()))) {
            throw new OAuth2Exception("user is already a developer.", HttpStatus.BAD_REQUEST, ErrorCodeConstant.ROLE_APPLY_ERROR);
        }
        RoleEntity role = roleRepository.findByRoleName(roleDeveloper.name());
        List<RoleApplyEntity> applies = roleApplyRepository.findApply(user.getId(), role.getId());
        if (applies.stream().anyMatch(roleApply -> roleApply.getStatus().equals(ApplyStatusEnum.REVIEWING.name()))) {
            throw new OAuth2Exception("your apply is waiting for reviewing.", HttpStatus.BAD_REQUEST, ErrorCodeConstant.ROLE_APPLY_ERROR);
        }
        roleApplyEntity.setUser(user);
        roleApplyEntity.setStatus(ApplyStatusEnum.REVIEWING.name());
        roleApplyEntity.setRole(repository.findByRoleName(RoleEnum.ROLE_DEVELOPER.name()));
        RoleApplyEntity save = roleApplyRepository.save(roleApplyEntity);
        return dozerMapper.map(save, RoleApply.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewRole(Long applyId, Boolean review) {
        Optional<RoleApplyEntity> byId = roleApplyRepository.findById(applyId);
        if (byId.isPresent()) {
            RoleApplyEntity roleApplyEntity = byId.get();
            if (review) {
                UserAccountEntity user = roleApplyEntity.getUser();
                RoleEntity role = roleApplyEntity.getRole();
                user.getRoles().add(role);
                userAccountRepository.save(user);
                roleApplyEntity.setStatus(ApplyStatusEnum.REJECTED.name());
                roleApplyRepository.save(roleApplyEntity);
            }
        }
        throw new OAuth2Exception("Empty apply.", HttpStatus.BAD_REQUEST, ErrorCodeConstant.PARAM_INVALID);
    }

    @Override
    public List<RoleApply> queryDeveloperApplyStatus(ApplyStatusEnum applyStatus, Pageable pageable) {
        List<String> strings;
        if (Objects.isNull(applyStatus)) {
            strings = ApplyStatusEnum.names();
        } else {
            strings = Collections.singletonList(applyStatus.name());
        }
        List<RoleApplyEntity> allByRoleInAndStatusIn = roleApplyRepository
            .findALLByRoleInAndStatusIn(Collections.singletonList(
                roleRepository.findByRoleName(RoleEnum.ROLE_DEVELOPER.name())), strings, pageable);
        return allByRoleInAndStatusIn.stream()
            .map(roleApplyEntity -> dozerMapper.map(roleApplyEntity, RoleApply.class))
            .collect(Collectors.toList());
    }
}
