package com.example.back.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NewsMaterialMapper {

    @Delete("DELETE FROM news_materials WHERE news_id = #{newsId}")
    int deleteByNewsId(Long newsId);

    @Insert("INSERT INTO news_materials(news_id, material_id) VALUES(#{newsId}, #{materialId})")
    int insert(@Param("newsId") Long newsId, @Param("materialId") Long materialId);

    @Select("SELECT material_id FROM news_materials WHERE news_id = #{newsId} ORDER BY id ASC")
    List<Long> findMaterialIdsByNewsId(Long newsId);
}
