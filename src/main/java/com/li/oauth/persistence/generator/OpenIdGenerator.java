package com.li.oauth.persistence.generator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;
import org.springframework.data.mapping.MappingException;

import java.io.Serializable;

public class OpenIdGenerator extends IdentityGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws MappingException {
        return SnowflakeIdWorker.generateId();
    }
}
