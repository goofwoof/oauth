package com.li.oauth.service;

import com.li.oauth.domain.JsonObjects;
import com.li.oauth.domain.LoginHistory;

public interface LoginHistoryService extends com.li.oauth.service.CommonServiceInterface<LoginHistory> {
    JsonObjects<LoginHistory> listByUsername(String username, int pageNum,
                                             int pageSize,
                                             String sortField,
                                             String sortOrder);
    
    void asyncCreate(LoginHistory loginHistory);

}
