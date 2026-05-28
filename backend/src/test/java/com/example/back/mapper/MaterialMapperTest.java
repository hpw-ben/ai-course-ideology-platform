package com.example.back.mapper;

import com.example.back.entity.Material;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MaterialMapperTest {
    
    @Autowired
    private MaterialMapper materialMapper;
    
    /**
     * 测试 findFileUrlById 返回正确的素材文件
     * 验证: 需求 2.1, 2.2
     */
    @Test
    public void testFindFileUrlById() {
        // 创建测试素材
        Material material = new Material();
        material.setTitle("测试素材");
        material.setType("VIDEO");
        material.setFileUrl("http://example.com/test.mp4");
        material.setDescription("测试描述");
        material.setTeacherId(1L);
        material.setStatus("APPROVED");
        
        materialMapper.insert(material);
        Long materialId = material.getId();
        
        // 查询素材文件
        Material result = materialMapper.findFileUrlById(materialId);
        
        // 验证返回正确数据
        assertNotNull(result);
        assertEquals(materialId, result.getId());
        assertEquals("http://example.com/test.mp4", result.getFileUrl());
    }
    
    /**
     * 测试 findFileUrlById 查询不存在的素材返回 null
     * 验证: 需求 2.3
     */
    @Test
    public void testFindFileUrlByIdNotFound() {
        // 查询不存在的素材
        Material result = materialMapper.findFileUrlById(99999L);
        
        // 验证返回 null
        assertNull(result);
    }
    
    /**
     * 测试 findFileUrlsByIdsLimited 限制数量为 10
     * 验证: 需求 2.4
     */
    @Test
    public void testFindFileUrlsByIdsLimited() {
        // 创建 15 个测试素材
        for (int i = 0; i < 15; i++) {
            Material material = new Material();
            material.setTitle("测试素材" + i);
            material.setType("VIDEO");
            material.setFileUrl("http://example.com/test" + i + ".mp4");
            material.setDescription("测试描述" + i);
            material.setTeacherId(1L);
            material.setStatus("APPROVED");
            materialMapper.insert(material);
        }
        
        // 查询所有素材ID
        List<Material> allMaterials = materialMapper.findAll();
        List<Long> ids = allMaterials.stream()
                .map(Material::getId)
                .limit(15)
                .toList();
        
        // 使用限制方法查询
        List<Material> results = materialMapper.findFileUrlsByIdsLimited(ids);
        
        // 验证最多返回 10 个
        assertNotNull(results);
        assertTrue(results.size() <= 10, "应该最多返回10个素材");
    }
    
    /**
     * 测试 findFileUrlsByIdsLimited 返回正确的数据
     * 验证: 需求 2.1, 2.4
     */
    @Test
    public void testFindFileUrlsByIdsLimitedCorrectData() {
        // 创建 3 个测试素材
        Material material1 = new Material();
        material1.setTitle("素材1");
        material1.setType("VIDEO");
        material1.setFileUrl("http://example.com/video1.mp4");
        material1.setDescription("描述1");
        material1.setTeacherId(1L);
        material1.setStatus("APPROVED");
        materialMapper.insert(material1);
        
        Material material2 = new Material();
        material2.setTitle("素材2");
        material2.setType("IMAGE");
        material2.setFileUrl("http://example.com/image2.jpg");
        material2.setDescription("描述2");
        material2.setTeacherId(1L);
        material2.setStatus("APPROVED");
        materialMapper.insert(material2);
        
        Material material3 = new Material();
        material3.setTitle("素材3");
        material3.setType("VIDEO");
        material3.setFileUrl("http://example.com/video3.mp4");
        material3.setDescription("描述3");
        material3.setTeacherId(1L);
        material3.setStatus("APPROVED");
        materialMapper.insert(material3);
        
        // 查询这3个素材的文件
        List<Long> ids = Arrays.asList(material1.getId(), material2.getId(), material3.getId());
        List<Material> results = materialMapper.findFileUrlsByIdsLimited(ids);
        
        // 验证返回正确数量和数据
        assertNotNull(results);
        assertEquals(3, results.size());
        
        // 验证每个素材都有 id 和 fileUrl
        for (Material m : results) {
            assertNotNull(m.getId());
            assertNotNull(m.getFileUrl());
            assertTrue(m.getFileUrl().startsWith("http://example.com/"));
        }
    }
    
    /**
     * 测试 findFileUrlsByIdsLimited 处理空列表
     * 验证: 需求 2.4
     */
    @Test
    public void testFindFileUrlsByIdsLimitedEmptyList() {
        // 查询空列表
        List<Material> results = materialMapper.findFileUrlsByIdsLimited(Arrays.asList());
        
        // 验证返回空列表
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
