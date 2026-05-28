package com.example.back.controller;

import com.example.back.entity.Material;
import com.example.back.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicController.class)
public class PublicControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AdminService adminService;
    
    /**
     * 测试单个素材文件接口 - 正常情况
     * 验证: 需求 2.1
     */
    @Test
    public void testGetMaterialFileSuccess() throws Exception {
        // 创建测试数据
        Material material = new Material();
        material.setId(1L);
        material.setFileUrl("http://example.com/test.mp4");
        
        // Mock 服务
        when(adminService.getMaterialFile(1L)).thenReturn(material);
        
        // 执行请求
        mockMvc.perform(get("/api/public/material/1/file"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.fileUrl").value("http://example.com/test.mp4"));
    }
    
    /**
     * 测试单个素材文件接口 - 素材不存在
     * 验证: 需求 2.3
     */
    @Test
    public void testGetMaterialFileNotFound() throws Exception {
        // Mock 服务抛出异常
        when(adminService.getMaterialFile(999L))
                .thenThrow(new RuntimeException("素材不存在"));
        
        // 执行请求
        mockMvc.perform(get("/api/public/material/999/file"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("素材不存在"));
    }
    
    /**
     * 测试批量素材文件接口 - 正常情况
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesSuccess() throws Exception {
        // 创建测试数据
        Material m1 = new Material();
        m1.setId(1L);
        m1.setFileUrl("http://example.com/file1.mp4");
        
        Material m2 = new Material();
        m2.setId(2L);
        m2.setFileUrl("http://example.com/file2.jpg");
        
        List<Material> materials = Arrays.asList(m1, m2);
        
        // Mock 服务
        when(adminService.getMaterialFiles(any())).thenReturn(materials);
        
        // 准备请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1L, 2L));
        
        // 执行请求
        mockMvc.perform(post("/api/public/material/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].fileUrl").value("http://example.com/file1.mp4"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].fileUrl").value("http://example.com/file2.jpg"));
    }
    
    /**
     * 测试批量素材文件接口 - 超过数量限制
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesExceedLimit() throws Exception {
        // Mock 服务抛出异常
        when(adminService.getMaterialFiles(any()))
                .thenThrow(new RuntimeException("单次最多获取10个素材文件"));
        
        // 准备请求参数(11个ID)
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));
        
        // 执行请求
        mockMvc.perform(post("/api/public/material/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("单次最多获取10个素材文件"));
    }
    
    /**
     * 测试批量素材文件接口 - 参数为空
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesNullParams() throws Exception {
        // 准备空参数
        Map<String, Object> params = new HashMap<>();
        
        // 执行请求
        mockMvc.perform(post("/api/public/material/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数错误: ids 不能为空"));
    }
    
    /**
     * 测试批量素材文件接口 - 空列表
     * 验证: 需求 2.4
     */
    @Test
    public void testGetMaterialFilesEmptyList() throws Exception {
        // Mock 服务返回空列表
        when(adminService.getMaterialFiles(any())).thenReturn(Arrays.asList());
        
        // 准备请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList());
        
        // 执行请求
        mockMvc.perform(post("/api/public/material/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
