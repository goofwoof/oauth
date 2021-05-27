package com.li.oauth.service.impl;

import com.github.dozermapper.core.Mapper;
import com.li.oauth.domain.Exception.AlreadyExistsException;
import com.li.oauth.domain.Exception.EntityNotFoundException;
import com.li.oauth.domain.JsonObjects;
import com.li.oauth.domain.OauthClient;
import com.li.oauth.domain.RoleEnum;
import com.li.oauth.persistence.entity.OauthClientEntity;
import com.li.oauth.persistence.entity.UserAccountEntity;
import com.li.oauth.persistence.repository.OauthClientRepository;
import com.li.oauth.persistence.repository.UserAccountRepository;
import com.li.oauth.service.OauthClientService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OauthClientServiceImpl implements OauthClientService {

    @Autowired
    OauthClientRepository oauthClientRepository;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    Mapper dozerMapper;

    @Override
    public OauthClient findByClientId(String clientId) {
        OauthClientEntity oauthClientEntity = oauthClientRepository.findByClientId(clientId);
        if (oauthClientEntity != null) {
            return dozerMapper.map(oauthClientEntity, OauthClient.class);
        } else {
            return null;
        }
    }

    @Override
    public JsonObjects<OauthClient> list(Authentication authentication, Pageable pageable) {
        JsonObjects<OauthClient> jsonObjects = new JsonObjects<>();
        Page<OauthClientEntity> page;
        if (authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(RoleEnum.ROLE_DEVELOPER.name()))) {
            UserAccountEntity user = userAccountRepository.findByUsername(authentication.getName());
            page = oauthClientRepository.findAllByUserId(user.getId(),pageable);
        } else {
            page = oauthClientRepository.findAll(pageable);
        }

        if (page.getContent().size() > 0) {
            jsonObjects.setRecordsTotal(page.getTotalElements());
            jsonObjects.setRecordsFiltered(page.getTotalElements());
            page.getContent().forEach(u -> jsonObjects.getData().add(dozerMapper.map(u, OauthClient.class)));
        }
        return jsonObjects;
    }

    @Override
    public OauthClient create(OauthClient oauthClient) throws AlreadyExistsException {
        OauthClientEntity exist = oauthClientRepository.findByClientId(oauthClient.getClientId());
        if (exist != null) {
            throw new AlreadyExistsException(oauthClient.getClientId() + " already exists!");
        }
        OauthClientEntity oauthClientEntity = dozerMapper.map(oauthClient, OauthClientEntity.class);
        oauthClientRepository.save(oauthClientEntity);
        return dozerMapper.map(oauthClientEntity, OauthClient.class);
    }

    @Override
    public OauthClient retrieveById(long id) throws EntityNotFoundException {
        Optional<OauthClientEntity> entityOptional = oauthClientRepository.findById(id);
        return dozerMapper.map(entityOptional.orElseThrow(EntityNotFoundException::new), OauthClient.class);
    }

    @Override
    public OauthClient updateById(OauthClient oauthClient) throws EntityNotFoundException {
        Optional<OauthClientEntity> entityOptional = oauthClientRepository.findById(Long.parseLong(oauthClient.getId()));
        OauthClientEntity e = entityOptional.orElseThrow(EntityNotFoundException::new);
        if (StringUtils.isNotEmpty(oauthClient.getClientSecret())) {
            e.setClientSecret(oauthClient.getClientSecret());
        }
        e.setAuthorities(oauthClient.getAuthorities());
        e.setScope(oauthClient.getScope());
        e.setAuthorizedGrantTypes(oauthClient.getAuthorizedGrantTypes());
        e.setWebServerRedirectUri(oauthClient.getWebServerRedirectUri());

        if (StringUtils.isNotEmpty(oauthClient.getRemarks())) {
            e.setRemarks(oauthClient.getRemarks());
        }

        oauthClientRepository.save(e);
        return dozerMapper.map(e, OauthClient.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecordStatus(long id, int recordStatus) {
        Optional<OauthClientEntity> entityOptional = oauthClientRepository.findById(id);
        OauthClientEntity e = entityOptional.orElseThrow(EntityNotFoundException::new);
        e.setRecordStatus(recordStatus);
        oauthClientRepository.save(e);
    }
}
