package com.smartdoc.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 知识搜索工具
 * 当前返回模拟数据。生产环境中应替换为真实的知识库检索调用。
 */
@Component
public class KnowledgeSearchTool {

    @Tool("根据关键词搜索知识库中的政策法规和文档，返回匹配的文档标题和摘要")
    public String searchKnowledge(@P("搜索关键词，如：国土空间规划、建设用地、生态红线") String keyword) {
        return """
                {
                  "total": 2,
                  "documents": [
                    {
                      "title": "国土空间规划编制技术指南",
                      "docNumber": "自然资发〔2024〕XX号",
                      "summary": "详细规定了国土空间规划的编制要求和技术标准。与「%s」高度相关。",
                      "publishDate": "2024-01-15"
                    },
                    {
                      "title": "建设用地审批管理办法",
                      "docNumber": "自然资发〔2023〕XX号",
                      "summary": "规范建设用地的申请、审查、批准程序。包含「%s」相关条款。",
                      "publishDate": "2023-06-01"
                    }
                  ]
                }
                """.formatted(keyword, keyword);
    }
}
