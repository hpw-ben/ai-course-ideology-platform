package com.example.back.mapper;

import com.example.back.entity.Material;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MaterialMapper {
    
    // 列表查询不返回file_url，避免大数据量传输导致MySQL packet过大
    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials WHERE teacher_id = #{teacherId} ORDER BY created_at DESC")
    List<Material> findByTeacherId(Long teacherId);
    
    // 单个素材查询返回完整数据（包括file_url）- 仅用于需要文件的场景
    @Select("SELECT * FROM materials WHERE id = #{id}")
    Material findById(Long id);
    
    // 查询素材详情（包含file_url和标签）- 用于详情展示
    @Select("SELECT * FROM materials WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "tags", column = "id", 
                many = @Many(select = "com.example.back.mapper.TagMapper.findByMaterialId"))
    })
    Material findByIdWithFile(Long id);
    
    // 单个素材查询不返回file_url - 用于列表展示
    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials WHERE id = #{id}")
    Material findByIdWithoutFile(Long id);
    
    // 列表查询不返回file_url
    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials WHERE teacher_id = #{teacherId} ORDER BY created_at DESC")
    List<Material> findByTeacherIdWithoutFile(Long teacherId);
    
    // 列表查询不返回file_url
    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials ORDER BY created_at DESC")
    List<Material> findAll();
    
    @Insert("INSERT INTO materials(title, type, content, file_url, description, teacher_id, status) " +
            "VALUES(#{title}, #{type}, #{content}, #{fileUrl}, #{description}, #{teacherId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Material material);
    
    @Update("UPDATE materials SET title=#{title}, content=#{content}, file_url=#{fileUrl}, " +
            "description=#{description} WHERE id=#{id}")
    int update(Material material);
    
    @Delete("DELETE FROM materials WHERE id = #{id}")
    int delete(Long id);
    
    @Insert("INSERT INTO material_tags(material_id, tag_id) VALUES(#{materialId}, #{tagId})")
    int insertMaterialTag(@Param("materialId") Long materialId, @Param("tagId") Long tagId);
    
    @Delete("DELETE FROM material_tags WHERE material_id = #{materialId}")
    int deleteMaterialTags(Long materialId);
    
    // 单独获取文件URL（用于下载/查看）- 返回 Material 对象(只含 id 和 file_url)
    @Select("SELECT id, file_url FROM materials WHERE id = #{id}")
    Material findFileUrlById(Long id);

    @Select("SELECT file_url FROM materials WHERE id = #{id}")
    String findFileUrlStringById(Long id);

    @Select("SELECT LENGTH(file_url) FROM materials WHERE id = #{id}")
    Long findFileUrlLength(Long id);

    @Select("SELECT SUBSTRING(file_url, #{offset}, #{len}) FROM materials WHERE id = #{id}")
    String findFileUrlChunk(@Param("id") Long id, @Param("offset") Long offset, @Param("len") Integer len);

    @Update("UPDATE materials SET file_url = #{fileUrl} WHERE id = #{id}")
    int updateFileUrlById(@Param("id") Long id, @Param("fileUrl") String fileUrl);
    
    // 批量获取文件URL（用于显示多个素材）
    @Select("<script>" +
            "SELECT id, file_url FROM materials WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Material> findFileUrlsByIds(@Param("ids") List<Long> ids);
    
    // 批量获取文件URL（限制最多10个）- 用于按需加载
    @Select("<script>" +
            "SELECT id, file_url FROM materials WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " LIMIT 10" +
            "</script>")
    List<Material> findFileUrlsByIdsLimited(@Param("ids") List<Long> ids);
    
    // 按状态查询（管理员审核用）
    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials WHERE status = #{status} ORDER BY created_at DESC")
    List<Material> findByStatus(String status);

    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials WHERE status = 'APPROVED' AND shelf_status = 'ON' ORDER BY created_at DESC")
    List<Material> findApprovedOnShelf();

    @Select("SELECT id, title, type, description, file_url, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials WHERE status = 'APPROVED' AND shelf_status = 'ON' AND type = 'ARTICLE' ORDER BY created_at DESC")
    List<Material> findApprovedArticlesForLeader();

    @Select("SELECT m.id, m.title, m.type, m.description, m.file_url, m.teacher_id, m.status, m.view_count as viewCount, m.created_at " +
            "FROM materials m " +
            "INNER JOIN material_tags mt ON m.id = mt.material_id " +
            "INNER JOIN tags t ON t.id = mt.tag_id " +
            "WHERE m.status = 'APPROVED' AND m.shelf_status = 'ON' AND m.type = 'ARTICLE' AND t.name = #{tagName} " +
            "ORDER BY m.created_at DESC")
    List<Material> findApprovedArticlesForLeaderByTagName(@Param("tagName") String tagName);

    @Select("SELECT * FROM materials WHERE id = #{id} AND status = 'APPROVED' AND shelf_status = 'ON' AND type = 'ARTICLE'")
    Material findApprovedArticleDetailById(Long id);
    
    // 管理员查看所有素材（不含file_url）
    @Select("SELECT id, title, type, content, description, teacher_id, status, shelf_status as shelfStatus, category, view_count as viewCount, created_at FROM materials ORDER BY created_at DESC")
    List<Material> findAllForAdmin();
    
    // 更新素材状态
    @Update("UPDATE materials SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE materials SET shelf_status = #{shelfStatus} WHERE id = #{id}")
    int updateShelfStatus(@Param("id") Long id, @Param("shelfStatus") String shelfStatus);

    @Update("UPDATE materials SET category = #{category} WHERE id = #{id}")
    int updateCategory(@Param("id") Long id, @Param("category") String category);

    @Update("UPDATE materials SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM materials")
    Long sumViewCount();
}
