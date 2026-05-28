package com.example.back.service;

import com.example.back.entity.ForumTopic;
import com.example.back.entity.Material;
import com.example.back.mapper.ForumTopicMapper;
import com.example.back.mapper.MaterialMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AdminServiceTest {
    
    @Autowired
    private AdminService adminService;
    
    @MockBean
    private ForumTopicMapper forumTopicMapper;
    
    @MockBean
    private MaterialMapper materialMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 属性 1: 话题详情数据包大小限制
     * 验证: 需求 1.3
     * 对于任何话题详情查询,返回的数据包大小应该小于 4MB
     */
    @Test
    public void testTopicDetailPacketSizeLimit() throws Exception {
        // 创建测试话题
        ForumTopic topic = new ForumTopic();
        topic.setId(1L);
        topic.setTitle("测试话题");
//        topic.setContent("测试内容");
        topic.setCode("TOPIC123");
        
        // 创建素材列表(不含 fileUrl)
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Material m = new Material();
            m.setId((long) i);
            m.setTitle("素材" + i);
            m.setType("VIDEO");
            m.setDescription("描述" + i);
            // 注意: 不设置 fileUrl
            materials.add(m);
        }
        
        // Mock 数据
        when(forumTopicMapper.findByCode("TOPIC123")).thenReturn(topic);
        when(forumTopicMapper.findMaterialsByTopicId(1L)).thenReturn(materials);
        
        // 调用方法
        ForumTopic result = adminService.getTopicByCode("TOPIC123");
        
        // 序列化为 JSON
        String json = objectMapper.writeValueAsString(result);
        
        // 验证大小小于 4MB
        int sizeInBytes = json.getBytes().length;
        assertTrue(sizeInBytes < 4 * 1024 * 1024, 
            "话题详情数据包大小应该小于4MB,实际大小: " + sizeInBytes + " bytes");
    }
    
    /**
     * 属性 2: 素材元数据完整性
     * 验证: 需求 1.1
     * 对于任何话题的素材列表,每个素材应该包含必需字段但不包含 fileUrl
     */
    @Test
    public void testMaterialMetadataCompleteness() {
        // 创建测试话题
        ForumTopic topic = new ForumTopic();
        topic.setId(1L);
        topic.setTitle("测试话题");
        topic.setCode("TOPIC123");
        
        // 创建素材列表
        List<Material> materials = new ArrayList<>();
        
        Material m1 = new Material();
        m1.setId(1L);
        m1.setTitle("视频素材");
        m1.setType("VIDEO");
        m1.setDescription("视频描述");
        materials.add(m1);
        
        Material m2 = new Material();
        m2.setId(2L);
        m2.setTitle("图片素材");
        m2.setType("IMAGE");
        m2.setDescription("图片描述");
        materials.add(m2);
        
        Material m3 = new Material();
        m3.setId(3L);
        m3.setTitle("文章素材");
        m3.setType("ARTICLE");
        m3.setContent("文章内容");
        materials.add(m3);
        
        // Mock 数据
        when(forumTopicMapper.findByCode("TOPIC123")).thenReturn(topic);
        when(forumTopicMapper.findMaterialsByTopicId(1L)).thenReturn(materials);
        when(materialMapper.findByIdWithFile(3L)).thenReturn(m3);
        
        // 调用方法
        ForumTopic result = adminService.getTopicByCode("TOPIC123");
        
        // 验证素材元数据完整性
        assertNotNull(result.getMaterials());
        assertEquals(3, result.getMaterials().size());
        
        for (Material m : result.getMaterials()) {
            // 验证必需字段存在
            assertNotNull(m.getId(), "素材ID不能为空");
            assertNotNull(m.getTitle(), "素材标题不能为空");
            assertNotNull(m.getType(), "素材类型不能为空");
            
            // 验证 fileUrl 不存在(ARTICLE 除外)
            if (!"ARTICLE".equals(m.getType())) {
                assertNull(m.getFileUrl(), 
                    "非文章类型素材不应该包含 fileUrl,类型: " + m.getType());
            }
        }
    }
    
    /**
     * 属性 3: 单个素材文件查询幂等性
     * 验证: 需求 2.2
     * 对于任何素材 ID,多次调用 getMaterialFile 应该返回相同的 fileUrl
     */
    @Test
    public void testGetMaterialFileIdempotence() {
        // 创建测试素材
        Material material = new Material();
        material.setId(1L);
        material.setFileUrl("http://example.com/test.mp4");
        
        // Mock 数据
        when(materialMapper.findFileUrlById(1L)).thenReturn(material);
        
        // 多次调用
        Material result1 = adminService.getMaterialFile(1L);
        Material result2 = adminService.getMaterialFile(1L);
        Material result3 = adminService.getMaterialFile(1L);
        
        // 验证返回相同的 fileUrl
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(result1.getFileUrl(), result2.getFileUrl());
        assertEquals(result2.getFileUrl(), result3.getFileUrl());
    }
    
    /**
     * 属性 4: 批量素材文件查询数量限制
     * 验证: 需求 2.4
     * 对于任何批量素材文件查询请求,如果请求的素材数量超过 10 个,系统应该拒绝请求
     */
    @Test
    public void testGetMaterialFilesLimit() {
        // 创建超过10个的ID列表
        List<Long> ids = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
        
        // 验证抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.getMaterialFiles(ids);
        });
        
        // 验证异常消息
        assertEquals("单次最多获取10个素材文件", exception.getMessage());
    }
    
    /**
     * 测试 getMaterialFiles 处理空列表
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesEmptyList() {
        // 调用空列表
        List<Material> result = adminService.getMaterialFiles(new ArrayList<>());
        
        // 验证返回空列表
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    /**
     * 测试 getMaterialFiles 处理 null
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesNull() {
        // 调用 null
        List<Material> result = adminService.getMaterialFiles(null);
        
        // 验证返回空列表
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    /**
     * 测试 getMaterialFile 素材不存在
     * 验证: 需求 2.3
     */
    @Test
    public void testGetMaterialFileNotFound() {
        // Mock 返回 null
        when(materialMapper.findFileUrlById(999L)).thenReturn(null);
        
        // 验证抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.getMaterialFile(999L);
        });
        
        // 验证异常消息
        assertEquals("素材不存在", exception.getMessage());
    }
    
    /**
     * 测试 getMaterialFiles 正常情况
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesNormal() {
        // 创建测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        
        Material m1 = new Material();
        m1.setId(1L);
        m1.setFileUrl("http://example.com/file1.mp4");
        
        Material m2 = new Material();
        m2.setId(2L);
        m2.setFileUrl("http://example.com/file2.jpg");
        
        Material m3 = new Material();
        m3.setId(3L);
        m3.setFileUrl("http://example.com/file3.mp4");
        
        List<Material> materials = Arrays.asList(m1, m2, m3);
        
        // Mock 数据
        when(materialMapper.findFileUrlsByIdsLimited(ids)).thenReturn(materials);
        
        // 调用方法
        List<Material> result = adminService.getMaterialFiles(ids);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("http://example.com/file1.mp4", result.get(0).getFileUrl());
        assertEquals("http://example.com/file2.jpg", result.get(1).getFileUrl());
        assertEquals("http://example.com/file3.mp4", result.get(2).getFileUrl());
    }
}
