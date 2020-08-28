
package me.corningrey.camunda.api.util;

import me.corningrey.camunda.api.model.UnitedLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BPMNXmlUtil {

    /**
     * 获取xml操作类
     */
    private static Document getDocument(InputStream in) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(in);
        } catch (Exception e) {
            UnitedLogger.error(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return document;
    }

    /**
     * 根据绝对路径获取bpmn文件
     *
     * @param path BPMN文件的绝对路径
     * @return
     */
    public static String getBpmnByPath(String path) {
        File file = new File(path);
        try (InputStream inputStream = new FileInputStream(file)) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8.name());
            return writer.toString();
        } catch (IOException e) {
            UnitedLogger.error(e);
        }
        return null;
    }


    public static void saveXML(Document document, String filePath) {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            // 设置XML文档格式
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            // 设置XML编码方式,即是用指定的编码方式保存XML文档到字符串(String),这里也可以指定为GBK或是ISO8859-1  
            outputFormat.setEncoding("UTF-8");
            //outputFormat.setSuppressDeclaration(true);
            outputFormat.setIndent(true); //设置是否缩进
            outputFormat.setIndent("    "); //以四个空格方式实现缩进
            outputFormat.setNewlines(true); //设置是否换行

            XMLWriter xmlWriter = new XMLWriter(out, outputFormat);
            xmlWriter.write(document);
            xmlWriter.flush();
            xmlWriter.close();
        } catch (Exception e) {
            UnitedLogger.error(e);
        }
    }

    /**
     * 获取bpmn文件里面配置的参数，userTaskId为空获取全局变量，不为空获取节点变量
     *
     * @param bpmnPath   bpmn文件绝对路径
     * @param key        参数key
     * @param userTaskId bpmn文件bpmn:userTask节点id
     * @return
     */
    public static String findExtensionElements(String bpmnPath, String key, String userTaskId) {
        String result = null;
        List<Node> nodeList = null;
        Map<String, String> map = new HashMap<String, String>();
        try { //读取bpmn文件
            String xmlString = getBpmnByPath(bpmnPath);
            Document doc = DocumentHelper.parseText(xmlString);
            Element root = doc.getRootElement();
            if (StringUtils.isNotEmpty(key)) {
                //userTaskId不为空查询节点参数，为空查询全局参数
                if (StringUtils.isNotEmpty(userTaskId)) {
                    nodeList = root.selectNodes("/bpmn:definitions/bpmn:process/bpmn:userTask[@id='" + userTaskId + "']/bpmn:extensionElements/camunda:properties/camunda:property");
                } else {
                    nodeList = root.selectNodes("/bpmn:definitions/bpmn:process/bpmn:extensionElements/camunda:properties/camunda:property");
                }
                for (int i = 0; i < nodeList.size(); i++) {
                    String name = nodeList.get(i).valueOf("@name");
                    String value = nodeList.get(i).valueOf("@value");
                    map.put(name, value);
                }
                result = map.get(key);
            }
        } catch (DocumentException e) {
            UnitedLogger.error(e);
        }
        return result;
    }

    public static String parseBpmnXmlByDefinition(String definitionId, RepositoryService repositoryService) {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(definitionId);
        Definitions definitions = bpmnModelInstance.getDefinitions();
        Bpmn.validateModel(bpmnModelInstance);
        String x = Bpmn.convertToString(bpmnModelInstance);
        return x;
    }

}
