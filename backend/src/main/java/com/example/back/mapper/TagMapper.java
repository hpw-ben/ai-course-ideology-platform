package com.example.back.mapper;

import com.example.back.entity.Tag;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TagMapper {
    
    @Select("SELECT * FROM tags ORDER BY id")
    List<Tag> findAll();

    @Select("SELECT * FROM tags WHERE category = #{category} ORDER BY id")
    List<Tag> findByCategory(@Param("category") String category);
    
    @Select("SELECT * FROM tags WHERE id = #{id}")
    Tag findById(Long id);
    
    @Select("SELECT * FROM tags WHERE name = #{name}")
    Tag findByName(String name);
    
    @Insert("INSERT INTO tags(name, category) VALUES(#{name}, #{category})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Tag tag);

    @Update("UPDATE tags SET category = #{category} WHERE id = #{id}")
    int updateCategory(@Param("id") Long id, @Param("category") String category);
    
    @Select("SELECT t.* FROM tags t INNER JOIN material_tags mt ON t.id = mt.tag_id WHERE mt.material_id = #{materialId}")
    List<Tag> findByMaterialId(Long materialId);
}
