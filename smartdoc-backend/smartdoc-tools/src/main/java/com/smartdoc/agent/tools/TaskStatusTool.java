package com.smartdoc.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 任务状态查询工具
 */
@Component
public class TaskStatusTool {

    @Tool("查询任务或业务的办理状态，如审批进度、许可证申请等")
    public String queryTaskStatus(@P("业务编号，如：GH-2024-001") String businessId) {
        return """
                {
                  "businessId": "%s",
                  "businessType": "建设用地规划许可证",
                  "status": "审核中",
                  "currentStep": "规划审查",
                  "totalSteps": 5,
                  "completedSteps": 3,
                  "applicant": "XX开发有限公司",
                  "submitDate": "2024-03-15",
                  "estimatedCompletionDate": "2024-05-01",
                  "nextStep": "公示阶段",
                  "remarks": "已通过专家评审，待公示"
                }
                """.formatted(businessId);
    }
}
