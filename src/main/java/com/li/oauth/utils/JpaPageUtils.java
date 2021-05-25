package com.li.oauth.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class JpaPageUtils {
    /**
     * createPageable
     * <p>
     * puthlive
     *
     * @param pageNum   pageNum
     * @param pageSize  pageSize
     * @param sortField sortField
     * @param sortOrder sortOrder
     * @return org.springframework.data.domain.Pageable result
     */
    public static Pageable createPageable(int pageNum, int pageSize, String sortField, String sortOrder) {
        Sort sort;
        if (StringUtils.equalsIgnoreCase(sortOrder, "asc")) {
            sort = Sort.by(Sort.Direction.ASC, sortField);
        } else {
            sort = Sort.by(Sort.Direction.DESC, sortField);
        }
        return PageRequest.of(pageNum - 1, pageSize, sort);
    }

    /**
     * createPageableOffset
     * <p>
     * puthlive
     *
     * @param offset    offset
     * @param pageSize  pageSize
     * @param sortField sortField
     * @param sortOrder sortOrder
     * @return org.springframework.data.domain.Pageable result
     */
    public static Pageable createPageableOffset(int offset, int pageSize, String sortField, String sortOrder) {
        int pageNum = offset / pageSize + 1;
        return createPageable(pageNum, pageSize, sortField, sortOrder);
    }

}
