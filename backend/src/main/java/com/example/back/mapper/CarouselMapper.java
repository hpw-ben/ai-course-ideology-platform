package com.example.back.mapper;

import com.example.back.entity.Carousel;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CarouselMapper {
    
    @Select("SELECT * FROM carousels ORDER BY sort_order ASC, created_at DESC")
    List<Carousel> findAll();

    @Select({"<script>",
            "SELECT * FROM carousels",
            "<where>",
            "  <if test='page != null and page != \"\"'>",
            "    (page = #{page}",
            "      <if test=\"page == 'home'\"> OR page IS NULL OR page = ''</if>",
            "    )",
            "  </if>",
            "</where>",
            "ORDER BY sort_order ASC, created_at DESC",
            "</script>"})
    List<Carousel> findAllByPage(@Param("page") String page);
    
    @Select("SELECT * FROM carousels WHERE status = 1 ORDER BY sort_order ASC, created_at DESC")
    List<Carousel> findActive();

    @Select({"<script>",
            "SELECT * FROM carousels",
            "WHERE status = 1",
            "<if test='page != null and page != \"\"'>",
            "  AND (page = #{page}",
            "    <if test=\"page == 'home'\"> OR page IS NULL OR page = ''</if>",
            "  )",
            "</if>",
            "ORDER BY sort_order ASC, created_at DESC",
            "</script>"})
    List<Carousel> findActiveByPage(@Param("page") String page);
    
    @Select("SELECT * FROM carousels WHERE id = #{id}")
    Carousel findById(Long id);
    
    @Insert("INSERT INTO carousels(title, image_url, link_url, page, sort_order, status) VALUES(#{title}, #{imageUrl}, #{linkUrl}, #{page}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Carousel carousel);
    
    @Update("UPDATE carousels SET title=#{title}, image_url=#{imageUrl}, link_url=#{linkUrl}, page=#{page}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(Carousel carousel);
    
    @Delete("DELETE FROM carousels WHERE id = #{id}")
    int delete(Long id);
}
