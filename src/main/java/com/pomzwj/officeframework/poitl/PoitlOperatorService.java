package com.pomzwj.officeframework.poitl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.DocxRenderData;
import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.pomzwj.constant.TemplateFileConstants;
import com.pomzwj.domain.DbColumnInfo;
import com.pomzwj.domain.DbTable;
import com.pomzwj.domain.SegmentData;
import com.pomzwj.domain.TempData;
import com.pomzwj.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;


/**
 * 类说明:POI-TL操作服务
 *
 * @author zhaowenjie<1513041820@qq.com>
 * @date 2018/10/29/0029.
 */
@Service
public class PoitlOperatorService {
    @Value("${export.template-copy-path}")
    private String templateCopyPath;

    /**
     * 生成word，带自定义路径
     * @param tableMessage
     */
    public XWPFTemplate makeDoc(List<DbTable> tableMessage) {
        List<TempData>tempDataList=new ArrayList<>();
        for (DbTable dbTable : tableMessage) {
            List<DbColumnInfo> data =  dbTable.getTabsColumn();
            TempData tempData = new TempData();
            tempData.setTableComment(dbTable.getTableComments());
            tempData.setTableName(dbTable.getTableName());

            List<RowRenderData> rowRenderDataList = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {

                DbColumnInfo dbColumnInfo = data.get(i);
                //列名
                String column_name = dbColumnInfo.getColumnName();
                //数据类型
                String data_type = dbColumnInfo.getDataType();
                //数据长度
                String data_length = dbColumnInfo.getDataLength();
                //是否可空
                String null_able = dbColumnInfo.getNullAble();
                //数据缺省值
                String data_default = dbColumnInfo.getDefaultVal();
                //字段注释
                String comments = dbColumnInfo.getComments();
                Boolean autoIncrement = dbColumnInfo.getAutoIncrement();
                String auto_increment = Objects.nonNull(autoIncrement)&&autoIncrement?"是":"";

                Boolean primary = dbColumnInfo.getPrimary();
                String is_primary = Objects.nonNull(primary)&&primary?"是":"";

                RowRenderData labor = RowRenderData.build( column_name, data_type,null_able,is_primary,auto_increment,data_default,comments);
                rowRenderDataList.add(labor);
            }
            tempData.setData(rowRenderDataList);
            tempDataList.add(tempData);

        }
        Map<String,Object>tempMap = new HashMap<>();
        List segmentDataList = new ArrayList();
        for(int i=0;i<tempDataList.size();i++){
            TempData tempData = tempDataList.get(i);
            RowRenderData header = RowRenderData.build("列名", "数据类型","是否为空","主键","是否自增", "默认值", "备注");
            SegmentData segmentData = new SegmentData();
            segmentData.setTable(new MiniTableRenderData(header,tempData.getData()));
            segmentData.setTableName(tempData.getTableName());
            segmentData.setTableComments(tempData.getTableComment());
            segmentDataList.add(segmentData);
        }
        File subModelWordFile = new File(templateCopyPath+"/"+ TemplateFileConstants.SUB_MODEL_TEMPLATE);
        tempMap.put("seg",new DocxRenderData(subModelWordFile, segmentDataList));


        File importWordFile = new File(templateCopyPath+"/"+ TemplateFileConstants.IMPORT_TEMPLATE);
        /*1.根据模板生成文档*/
        XWPFTemplate template = XWPFTemplate.compile(importWordFile).render(tempMap);
        return template;
    }
}
