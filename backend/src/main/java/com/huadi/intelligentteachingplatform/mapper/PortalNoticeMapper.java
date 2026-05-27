package com.huadi.intelligentteachingplatform.mapper;

import com.huadi.intelligentteachingplatform.entity.PortalNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PortalNoticeMapper {
    List<PortalNotice> selectAllOrderByCreateTimeDesc();

    int insert(PortalNotice notice);

    PortalNotice selectById(Long id);

    int deleteById(Long id);
}
