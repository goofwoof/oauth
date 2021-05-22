package com.li.oauth.persistence.generator;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class OpenIdGenerator extends IdentityGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws MappingException {
        Object id = SnowflakeIdWorker.generateId();
        if (id != null) {
            return (Serializable) id;
        }
        return super.generate(session, object);
    }
}
