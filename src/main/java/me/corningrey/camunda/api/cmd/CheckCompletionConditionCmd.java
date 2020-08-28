package me.corningrey.camunda.api.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class CheckCompletionConditionCmd implements Command<Boolean> {
    // 待办ID
    protected String taskId;

    public CheckCompletionConditionCmd(String taskId) {
        this.taskId = taskId;
    }


    @Override
    public Boolean execute(CommandContext commandContext) {
        TaskEntity taskEntity = commandContext.getTaskManager().findTaskById(taskId);
        if (taskEntity == null) {
            throw new RuntimeException("当前任务不存在或者已完成！");
        }
        String processDefinitionId = taskEntity.getProcessDefinitionId();
        String taskDefinitionKey = taskEntity.getTaskDefinitionKey();
        ExecutionEntity executionEntity = taskEntity.getExecution();

        // 解析bpmn文件并获取当前节点的完成条件
        RepositoryService repositoryService = commandContext.getProcessEngineConfiguration().getRepositoryService();
        StringWriter writer = new StringWriter();
        Document document;
        String processXml;
        try {
            IOUtils.copy(repositoryService.getProcessModel(processDefinitionId), writer, StandardCharsets.UTF_8.name());
            processXml = writer.toString();
            document = DocumentHelper.parseText(processXml);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("IO异常");
        }
        String format = "/bpmn:definitions/bpmn:process/*[@id='%s']/bpmn:multiInstanceLoopCharacteristics/bpmn:completionCondition/text()";
        List<Node> singleNode = document.selectNodes(String.format(format, taskDefinitionKey));
        String completionCondition = singleNode.stream().map(Node::getStringValue).collect(Collectors.joining());
        Boolean result = true;
        if (StringUtils.isNotBlank(completionCondition)) {
            // 完成待办会更新的内置流程变量有：nrOfCompletedInstances,此方法在completeTask之前执行，故手动递增1，方法执行完成后还原
            Integer nrOfCompletedInstances = (Integer) taskEntity.getVariable("nrOfCompletedInstances");
            if (nrOfCompletedInstances != null) {
                taskEntity.setVariable("nrOfCompletedInstances", nrOfCompletedInstances + 1);
            }
            // 校验表达式是否成立
            ExpressionManager expressionManager = commandContext.getProcessEngineConfiguration().getExpressionManager();
            Expression UELExpression = expressionManager.createExpression(completionCondition);
            result = (Boolean) UELExpression.getValue(executionEntity);
            taskEntity.setVariable("nrOfCompletedInstances", nrOfCompletedInstances);
        }
        return result;
    }


}
