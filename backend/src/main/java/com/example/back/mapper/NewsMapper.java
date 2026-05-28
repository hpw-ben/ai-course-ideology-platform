package com.example.back.mapper;

import com.example.back.entity.News;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NewsMapper {
    
    @Select("SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt FROM news ORDER BY created_at DESC")
    List<News> findAll();

    @Select({
            "<script>",
            "SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt",
            "FROM news",
            "<where>",
            "  1=1",
            "  <if test='category != null and category != \"\"'> AND category = #{category}</if>",
            "</where>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    List<News> findAllByCategory(@Param("category") String category);
    
    @Select("SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt FROM news WHERE status = 1 ORDER BY created_at DESC")
    List<News> findPublished();

    @Select({
            "<script>",
            "SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt",
            "FROM news",
            "<where>",
            "  status = 1",
            "  <if test='category != null and category != \"\"'> AND category = #{category}</if>",
            "</where>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    List<News> findPublishedByCategory(@Param("category") String category);
    
    @Select("SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt FROM news WHERE status = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<News> findPublishedLimit(int limit);

    @Select({
            "<script>",
            "SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt",
            "FROM news",
            "<where>",
            "  status = 1",
            "  <if test='category != null and category != \"\"'> AND category = #{category}</if>",
            "</where>",
            "ORDER BY created_at DESC LIMIT #{limit}",
            "</script>"
    })
    List<News> findPublishedLimitByCategory(@Param("limit") int limit, @Param("category") String category);
    
    @Select("SELECT * FROM news WHERE id = #{id}")
    News findById(Long id);

    @Select({
            "<script>",
            "SELECT id, title, summary, cover_image as coverImage, author, status, view_count as viewCount, category, created_at as createdAt, updated_at as updatedAt",
            "FROM news",
            "WHERE status = 1",
            "<if test='ids != null and ids.size() > 0'>",
            "  AND id IN",
            "  <foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "</if>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    List<News> findPublishedByIds(@Param("ids") List<Long> ids);
    
    @Insert("INSERT INTO news(title, summary, content, cover_image, author, status, category) VALUES(#{title}, #{summary}, #{content}, #{coverImage}, #{author}, #{status}, #{category})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(News news);
    
    @Update("UPDATE news SET title=#{title}, summary=#{summary}, content=#{content}, cover_image=#{coverImage}, author=#{author}, status=#{status}, category=#{category} WHERE id=#{id}")
    int update(News news);

    @Update("UPDATE news SET cover_image=#{coverImage} WHERE id=#{id}")
    int updateCoverImageById(@Param("id") Long id, @Param("coverImage") String coverImage);
    
    @Delete("DELETE FROM news WHERE id = #{id}")
    int delete(Long id);
    
    @Update("UPDATE news SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
}
