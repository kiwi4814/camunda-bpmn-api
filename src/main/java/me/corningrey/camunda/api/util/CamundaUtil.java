
package me.corningrey.camunda.api.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import me.corningrey.camunda.api.enums.ResultEnum;
import me.corningrey.camunda.api.model.FormVariable;
import me.corningrey.camunda.api.model.TaskNode;
import me.corningrey.camunda.api.model.UnitedException;
import me.corningrey.camunda.api.model.UnitedLogger;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.UserTask;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CamundaUtil {

    // 数据类型下拉
    public final static String NUMBER = "number";
    public final static String TEXT = "text";
    public final static String BOOLEAN = "boolean";
    public final static String DATE = "date";
    public final static String LIST = "list";

    /**
     * 获取可用的resourceName
     *
     * @param resourceName 资源名称
     * @param filePath     资源路径
     * @return String
     */
    public static String operResourceName(String resourceName, String filePath) {
        // 处理resourceName使之符合规范
        if (StringUtils.isBlank(resourceName) && checkResourceSuffix(filePath)) {
            resourceName = filePath;
        } else {
            if (!checkResourceSuffix(resourceName)) {
                resourceName += "." + BpmnDeployer.BPMN_RESOURCE_SUFFIXES[1];
            }
        }
        return resourceName;
    }


    /**
     * 判断字符串是否符合resourceName后缀
     *
     * @param str 字符串
     * @return Boolean
     */
    public static Boolean checkResourceSuffix(String str) {
        for (String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
            if (StringUtils.endsWith(str, suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(String commaString, String string) {
        boolean flag = false;
        if (StringUtils.isBlank(commaString) || StringUtils.isBlank(string)) {
            return flag;
        }
        List<String> stringArray = Arrays.asList(StringUtils.split(commaString, ","));
        if (stringArray.contains(string)) {
            flag = true;
        }
        return flag;
    }

    public static Map<String, Object> convertJsonStrToMap(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return new HashMap<>(16);
        }
        try {
            jsonStr = CommonUtil.isJSONValid(jsonStr) ? jsonStr : URLDecoder.decode(jsonStr, "UTF-8");
            if (CommonUtil.isJSONValid(jsonStr)) {
                return JSON.parseObject(jsonStr, new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
        }
        return new HashMap<>(16);
    }

    public static String toString(Object object) {
        return object == null ? "" : object.toString();
    }

    /**
     * 简单分页
     *
     * @param list
     * @param pageNum
     * @param pageLimit
     * @param <T>
     * @return
     */
    public static <T> Map<String, Object> getSimpleParamer(List<T> list, Integer pageNum, Integer pageLimit) {
        Map<String, Object> resultMap = new HashMap<>();
        if (pageNum != null && pageLimit != null) { // 需要分页
            // 每页条数处理
            if (pageLimit <= 0) {
                pageLimit = 5; // 默认每页为5条数据
            }

            // 获取总数
            int total = list.size();
            // 计算页数
            int pageCount = total / pageLimit + (total % pageLimit == 0 ? 0 : 1);
            // 页码处理
            pageNum = pageNum < pageCount ? pageNum : pageCount;
            if (pageNum <= 0) {
                pageNum = 1;
            }

            // 计算从第几条开始
            int start = pageLimit * (pageNum - 1);
            // 计算要到第几条
            int end = (start + pageLimit) < total ? start + pageLimit : total;

            List<T> resultList = new ArrayList<>();
            for (int i = start; i < end; i++) {
                resultList.add(list.get(i));
            }

            resultMap.put("pageNum", pageNum);
            resultMap.put("pageLimit", pageLimit);
            resultMap.put("total", total);
            resultMap.put("pageCount", pageCount);
            resultMap.put("list", resultList);
        } else { // 不需要分页
            resultMap.put("list", list);
        }
        return resultMap;
    }

    /**
     * 封装camunda查询分页
     *
     * @param pageNum
     * @param pageLimit
     * @param callback
     * @param <T>
     * @return
     */
    public static <T> Map<String, Object> getCamundaParamer(Integer pageNum, Integer pageLimit, Query callback) {
        Map<String, Object> resultMap = CamundaUtil.getSimplePageParam(pageNum, pageLimit, (int) callback.count());
        List<T> resultList = null;
        if (MapUtil.isEmpty(resultMap)) { // 不需要分页
            resultList = callback.list();
        } else { // 需要分页
            resultList = callback.listPage((int) resultMap.get("start"), pageLimit); // offset limit
        }
        resultMap.put("list", resultList);
        return resultMap;
    }

    /**
     * 获取经过计算后的分页参数
     *
     * @param pageNum
     * @param pageLimit
     * @param total
     * @return
     */
    public static Map<String, Object> getSimplePageParam(Integer pageNum, Integer pageLimit, int total) {
        Map<String, Object> resultMap = new HashMap<>();
        if (pageNum != null && pageLimit != null) { // 需要分页
            // 每页条数处理
            if (pageLimit <= 0) {
                pageLimit = 5; // 默认每页为5条数据
            }
            // 计算页数
            int pageCount = total / pageLimit + (total % pageLimit == 0 ? 0 : 1);
            // 页码处理
            pageNum = pageNum < pageCount ? pageNum : pageCount;
            if (pageNum <= 0) {
                pageNum = 1;
            }

            // 计算从第几条开始
            int start = pageLimit * (pageNum - 1);
            // 计算要到第几条
            // int end = (start + pageLimit) < total ? start + pageLimit : total;

            resultMap.put("pageNum", pageNum);
            resultMap.put("pageLimit", pageLimit);
            resultMap.put("total", total);
            resultMap.put("pageCount", pageCount);
            resultMap.put("start", start);
            // resultMap.put("end", end);
        }
        return resultMap;
    }

    // 获取嵌套异常中的最终信息
    public static String getRealMessage(Throwable e) {
        // 如果e不为空，则去掉外层的异常包装
        while (e != null) {
            Throwable cause = e.getCause();
            if (cause == null) {
                return e.getMessage();
            }
            e = cause;
        }
        return "";
    }

    public static String generateRandomId() {
        IdGenerator idGenerator = Context.getCommandContext().getProcessEngineConfiguration().getIdGenerator();
        return idGenerator.getNextId();
    }


    /**
     * 获取流程图上某个activity之后的所有用户任务
     *
     * @param sequenceFlow  线
     * @param taskNodes     已筛选的用户任务节点
     * @param sequenceFlows 记录已循环过的sequenceFlow的ID
     */
    public static void getUserTaskAfter(SequenceFlow sequenceFlow, List<TaskNode> taskNodes, List<String> sequenceFlows) {
        if (!sequenceFlows.contains(sequenceFlow.getId())) {
            sequenceFlows.add(sequenceFlow.getId());
            FlowNode flowNode = sequenceFlow.getTarget();
            if (flowNode instanceof UserTask) {
                TaskNode taskNode = TaskNode.builder().id(flowNode.getId()).name(flowNode.getName())
                        .incoming(Optional.ofNullable(flowNode.getIncoming()).map(t -> t.iterator().next().getId()).orElse(""))
                        .build();
                taskNodes.add(taskNode);
            }
            flowNode.getOutgoing().forEach(t -> getUserTaskAfter(t, taskNodes, sequenceFlows));
        }
    }

    /**
     * 按照流程变量后缀过滤大运河系统内置的不需要传给客户端的流程变量，如审批结果、意见等
     *
     * @param paramMap        需要过滤的流程变量Map
     * @param filterVariables 从配置文件中获取的需要过滤的流程变量，多个为逗号拼接
     * @return 过滤后的流程变量Map
     */
    public static Map<String, String> filterVariablesMap(Map<String, Object> paramMap, String filterVariables) {
        Map<String, String> resultMap = new HashMap<>();
        if (paramMap != null && paramMap.size() > 0) {
            Predicate<Map.Entry<String, Object>> predicate = p -> p.getValue() instanceof String || p.getValue() instanceof Short || p.getValue() instanceof Integer
                    || p.getValue() instanceof Long || p.getValue() instanceof Double || p.getValue() instanceof Boolean;
            if (StringUtils.isNotBlank(filterVariables)) {
                String[] filters = filterVariables.split(",");
                for (String filter : filters) {
                    predicate = predicate.and(p -> !p.getKey().endsWith(filter.trim()));
                }
            }
            resultMap = paramMap.entrySet().stream().filter(predicate)
                    .collect(Collectors.toMap(Map.Entry::getKey, map -> toString(map.getValue()), (v1, v2) -> v2));
        }
        return resultMap;
    }

    /**
     * 校验返回结果并且返回Result对象中的Object对象data
     *
     * @param responseText  HttpClient调用接口的返回值
     * @param interfaceName 调用接口的名称
     * @param interfaceUrl  调用接口的地址
     * @return Result对象中的Object对象data
     */
    public static JSONObject checkResponseText(String responseText, String interfaceName, String interfaceUrl) throws UnitedException {
        JSONObject jsonObject;
        StringBuilder message = new StringBuilder();
        if (StringUtils.isBlank(responseText)) {
            message.append("接口【").append(interfaceName).append("】调用失败，无法请求到该地址：【").append(interfaceUrl).append("】");
            throw new UnitedException(message.toString());
        } else if (CommonUtil.isJSONValid(responseText)) {
            jsonObject = JSON.parseObject(responseText);
            message.append(jsonObject.getString("message"));
            if (StringUtils.isBlank(message.toString())) {
                message.append("接口【").append(interfaceName).append("】调用失败且未返回任何信息！");
            }
            // 判断resultCode或者status
            String resultCode = jsonObject.getString("resultCode");
            if (StringUtils.isBlank(resultCode) || StringUtils.equalsIgnoreCase("null", resultCode)) {
                String status = jsonObject.getString("status");
                if (StringUtils.isNotBlank(status) && !StringUtils.equals("200", status)) {
                    throw new UnitedException(message.toString());
                }
            } else if (!ResultEnum.SUCCESS.getValue().equals(resultCode)) {
                throw new UnitedException(message.toString());
            }
        } else {
            throw new UnitedException("接口【" + interfaceName + "】返回值不是标准的JSON格式，请检查返回值：【" + responseText + "】");
        }
        return jsonObject.getJSONObject("data");
    }

    public static String decode(String str, String enc) {
        if (StringUtils.isBlank(str) || StringUtils.isBlank(enc)) {
            return str;
        }
        str = str.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        str = str.replaceAll("\\+", "%2B");
        try {
            str = URLDecoder.decode(str, enc);
        } catch (UnsupportedEncodingException e) {
            UnitedLogger.error(e);
            return str;
        }
        return str;
    }


    /**
     * 发起流程时根据表单中设置的流程变量的属性，重新设置流程变量的类型
     * 支持的变量类型为：数字、布尔、日期、文本
     *
     * @param variables     流程变量列表
     * @param formVariables 表单变量列表
     */
    public static void resetVarType(Map<String, Object> variables, List<FormVariable> formVariables) {
        if (formVariables != null && !formVariables.isEmpty()) {
            formVariables.forEach(t -> {
                String key = t.getKey();
                if (StrUtil.isNotBlank(key)) {
                    if (variables.containsKey(key)) {
                        Object value = getRealTypeValue(t.getVarType(), variables.get(key));
                        variables.put(key, value);
                    } else if (StringUtils.isNotBlank(t.getDefaultValue())) {
                        Object value = getRealTypeValue(t.getVarType(), t.getDefaultValue());
                        variables.put(key, value);
                    }
                }
            });
        }
    }

    public static void resetVarTypeAuto(Map<String, Object> variableMap) {
        variableMap.forEach((k, v) -> {
            if (v != null) {
                variableMap.put(k, getRealTypeValue(v.toString()));
            }
        });
    }

    /**
     * 根据数据类型转换值的类型
     *
     * @param varType 数据类型，目前支持数字、文本、日期、布尔
     * @param value   值
     */
    public static Object getRealTypeValue(String varType, Object value) {
        switch (varType) {
            // 数值类型
            case NUMBER:
                return Convert.toNumber(value);
            // 布尔型
            case BOOLEAN:
                return Convert.toBool(value);
            //日期类
            case DATE:
                return Convert.toDate(value);
            //日期类
            case LIST:
                return Convert.toList(value);
            //其他类型默认为文本
            case TEXT:
            default:
                break;
        }
        return value;
    }

    /**
     * 根据值自动转换值的类型（number，text，boolean，date）
     *
     * @param value 值
     */
    public static Object getRealTypeValue(String value) {
        Object typeValue;
        // 转为日期格式(yyyy-mm-dd hh:ss)
        typeValue = Convert.toDate(value);
        if (typeValue == null) {
            // 只有严格按照boolean格式写法的才会被强制转换为布尔（不支持0和1）
            if (StrUtil.equalsAny(value, "true", "false")) {
                return Convert.toBool(value);
            }
            // 转换为数字
            if (NumberUtil.isNumber(value)) {
                return Convert.toNumber(value);
            }
            return value;
        }
        return typeValue;
    }

    /**
     * Hutool合并多个List
     *
     * @param coll1      list1
     * @param coll2      list2
     * @param otherColls 其他list
     * @param <T>
     * @return
     */
    public static <T> List<T> union(Collection<T> coll1, Collection<T> coll2, Collection<T>... otherColls) {
        List<T> union = union(coll1, coll2);
        Collection[] var4 = otherColls;
        int var5 = otherColls.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Collection<T> coll = var4[var6];
            union = union(union, coll);
        }

        return union;
    }

    /**
     * Hutool合并两个list
     *
     * @param coll1 list1
     * @param coll2 list2
     */
    public static <T> List<T> union(Collection<T> coll1, Collection<T> coll2) {
        ArrayList<T> list = new ArrayList();
        if (CollUtil.isEmpty(coll1) && CollUtil.isEmpty(coll2)) {
            return list;
        }
        if (CollUtil.isEmpty(coll1)) {
            list.addAll(coll2);
        } else if (CollUtil.isEmpty(coll2)) {
            list.addAll(coll1);
        } else {
            Map<T, Integer> map1 = CollUtil.countMap(coll1);
            Map<T, Integer> map2 = CollUtil.countMap(coll2);
            Set<T> elts = CollUtil.newHashSet(coll2);
            elts.addAll(coll1);
            Iterator var7 = elts.iterator();
            while (var7.hasNext()) {
                T t = (T) var7.next();
                int m = Math.max(Convert.toInt(map1.get(t), 0), Convert.toInt(map2.get(t), 0));

                for (int i = 0; i < m; ++i) {
                    list.add(t);
                }
            }
        }
        return list;
    }


}
