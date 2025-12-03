package com.server.campushelpserver.mapper.lostfound;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.lostfound.LostFound;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 失物招领Mapper接口
 */
@Mapper
public interface LostFoundMapper extends BaseMapper<LostFound> {
    
    /**
     * 使用乐观锁更新状态
     * @param id 失物ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param version 版本号
     * @return 更新行数
     */
    @Update("UPDATE lost_found SET status = #{newStatus}, version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{oldStatus} AND version = #{version} AND delete_flag = 0")
    int updateStatusWithVersion(@Param("id") Long id, @Param("oldStatus") String oldStatus, 
                                 @Param("newStatus") String newStatus, @Param("version") Integer version);
    
    /**
     * 增加浏览次数
     * @param id 失物ID
     */
    @Update("UPDATE lost_found SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND delete_flag = 0")
    void incrementViewCount(Long id);
}

