package com.bin.david.form.core;

import com.bin.david.form.data.Column;
import com.bin.david.form.data.TableData;
import com.bin.david.form.data.TableInfo;
import com.bin.david.form.exception.TableException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by huang on 2017/10/31.
 * 表格解析器
 */

public class TableParser<T> {



    /**
     * 解析数据
     */
    public List<Column> parse(TableData<T> tableData, TableConfig config){
        sort(tableData);
        tableData.getChildColumns().clear();
        tableData.getColumnInfos().clear();
        int maxLevel = getChildColumn(tableData);
        TableInfo tableInfo =  tableData.getTableInfo();
        tableInfo.setColumnSize(tableData.getChildColumns().size());
        tableInfo.setMaxLevel(maxLevel);
        try {
            List<T> dataList = tableData.getT();
            for (Column column : tableData.getChildColumns()) {
                column.getValues().clear();
                column.getDatas().clear();
                column.fillData(dataList,tableInfo,config);
            }
        }catch (NoSuchFieldException e){
            throw new TableException(
                    "NoSuchFieldException :Please check whether field name is correct!");
        }catch (IllegalAccessException e){
            throw new TableException(
                    "IllegalAccessException :Please make sure that access objects are allowed!");
        }
       return tableData.getColumns();
    }


    /**
     * 排序
     * @param tableData 表格数据
     * @return
     */
    public List<Column> sort(TableData<T> tableData){

        final Column sortColumn = tableData.getSortColumn();
        if(sortColumn !=null) {
            List<T> dataList = tableData.getT();
            Collections.sort(dataList, new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {

                    try {
                        if(o1 == null){
                            return sortColumn.isReverseSort() ?1:-1;
                        }
                        if(o2 == null){
                            return sortColumn.isReverseSort() ?-1:1;
                        }
                        Object data = sortColumn.getData(o1);
                        Object compareData = sortColumn.getData(o2);
                        if(data == null){
                            return sortColumn.isReverseSort() ?1:-1;
                        }
                        if(compareData == null){
                            return sortColumn.isReverseSort() ?-1:1;
                        }
                        int compare;
                        if(sortColumn.getComparator() != null){
                            compare=  sortColumn.getComparator().compare(data,compareData);
                            return sortColumn.isReverseSort()?-compare:compare;
                        }else {
                            if(data instanceof Comparable){
                                compare= ((Comparable) data).compareTo(compareData);
                                return sortColumn.isReverseSort()?-compare:compare;
                            }
                            return 0;
                        }
                    } catch (NoSuchFieldException e){
                        throw new TableException(
                                "NoSuchFieldException :Please check whether field name is correct!");
                    }catch (IllegalAccessException e){
                        throw new TableException(
                                "IllegalAccessException :Please make sure that access objects are allowed!");
                    }
                }
            });
        }
        return tableData.getColumns();
    }


    private int  getChildColumn(TableData<T> tableData){
        int maxLevel = 0;
        for (Column column : tableData.getColumns()) {
                int level = getColumnLevel(tableData,column,0);
                if(level >maxLevel){
                    maxLevel = level;
                }
        }
        return maxLevel;
    }

    /**
     * 得到列的层级
     * @param tableData 表格数据
     * @param column 列
     * @param level 层级
     * @return
     */
    private int getColumnLevel(TableData<T> tableData,Column column,int level){
        level++;
        if(column.isParent()){
            List<Column> children = column.getChildren();
            int maxLevel =0;
            for(Column child :children){
                int childLevel = getColumnLevel(tableData,child,level);
                if(maxLevel < childLevel){
                    maxLevel = childLevel;
                    column.setLevel(maxLevel);
                }
            }
            level = maxLevel;
            return level;
        }else{
           tableData.getChildColumns().add(column);
           return level;
        }
    }



}
