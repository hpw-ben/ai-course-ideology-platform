package com.example.back.mapper;

import com.example.back.entity.ScienceItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScienceItemMapper {

    // 列表查询排除 link_url（视频类型该字段可能存放 base64 视频，数据量极大）
    @Select({"<script>",
            "SELECT id, module, content_type as contentType, title, description, cover_image as coverImage, sort_order, status, created_at, updated_at FROM science_items",
            "<where>",
            "  <if test='module != null and module != \"\"'>",
            "    module = #{module}",
            "  </if>",
            "</where>",
            "ORDER BY sort_order ASC, created_at DESC",
            "</script>"})
    List<ScienceItem> findAllLite(@Param("module") String module);

    @Select({"<script>",
            "SELECT * FROM science_items",
            "<where>",
            "  <if test='module != null and module != \"\"'>",
            "    module = #{module}",
            "  </if>",
            "</where>",
            "ORDER BY sort_order ASC, created_at DESC",
            "</script>"})
    List<ScienceItem> findAll(@Param("module") String module);

    // 列表查询排除 link_url（视频类型该字段可能存放 base64 视频，数据量极大）
    @Select({"<script>",
            "SELECT id, module, content_type as contentType, title, description, cover_image as coverImage, sort_order, status, created_at, updated_at FROM science_items",
            "WHERE status = 1",
            "<if test='module != null and module != \"\"'>",
            "  AND module = #{module}",
            "</if>",
            "ORDER BY sort_order ASC, created_at DESC",
            "</script>"})
    List<ScienceItem> findActiveLite(@Param("module") String module);

    @Select({"<script>",
            "SELECT * FROM science_items",
            "WHERE status = 1",
            "<if test='module != null and module != \"\"'>",
            "  AND module = #{module}",
            "</if>",
            "ORDER BY sort_order ASC, created_at DESC",
            "</script>"})
    List<ScienceItem> findActive(@Param("module") String module);

    @Select("SELECT * FROM science_items WHERE id = #{id} AND status = 1")
    ScienceItem findActiveById(Long id);

    @Select("SELECT * FROM science_items WHERE id = #{id}")
    ScienceItem findById(Long id);

    @Insert("INSERT INTO science_items(module, content_type, title, description, cover_image, article_content, link_url, sort_order, status) VALUES(#{module}, #{contentType}, #{title}, #{description}, #{coverImage}, #{articleContent}, #{linkUrl}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ScienceItem item);

    @Update("UPDATE science_items SET module=#{module}, content_type=#{contentType}, title=#{title}, description=#{description}, cover_image=#{coverImage}, article_content=#{articleContent}, link_url=#{linkUrl}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(ScienceItem item);

    @Update("UPDATE science_items SET cover_image = #{coverImage} WHERE id = #{id}")
    int updateCoverImageById(@Param("id") Long id, @Param("coverImage") String coverImage);

    // 按需加载 link_url（视频 URL 或历史 base64），供详情页/迁移使用
    @Select("SELECT link_url FROM science_items WHERE id = #{id}")
    String findLinkUrlStringById(@Param("id") Long id);

    @Update("UPDATE science_items SET link_url = #{linkUrl} WHERE id = #{id}")
    int updateLinkUrlById(@Param("id") Long id, @Param("linkUrl") String linkUrl);

    @Delete("DELETE FROM science_items WHERE id = #{id}")
    int delete(Long id);
}
