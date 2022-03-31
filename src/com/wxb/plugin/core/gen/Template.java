package com.wxb.plugin.core.gen;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

/**
 * <p>
 *
 * </p>
 *
 * @author weixianbing
 * @create 2022/3/1 16:50
 */
public class Template {
    Header header1;
    Header header2;
    Header header3;
    Table table;

    public Template() {
    }

    public Template(Header header1, Header header2, Header header3, Table table) {
        this.header1 = header1;
        this.header2 = header2;
        this.header3 = header3;
        this.table = table;
    }

    static class Header {
        String name;
        // 字符串格式
        CTRPr rstyle;
        // 段落格式
        CTPPr gstyle;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public CTRPr getRstyle() {
            return rstyle;
        }

        public void setRstyle(CTRPr rstyle) {
            this.rstyle = rstyle;
        }

        public CTPPr getGstyle() {
            return gstyle;
        }

        public void setGstyle(CTPPr gstyle) {
            this.gstyle = gstyle;
        }
    }

    static class Table {
        // 表样式
        CTTbl tableStyle;
        // 行样式
        CTTrPr row;
        // 单元格样式
        CTTcPr cell;

        public CTTbl getTableStyle() {
            return tableStyle;
        }

        public void setTableStyle(CTTbl tableStyle) {
            this.tableStyle = tableStyle;
        }

        public CTTrPr getRow() {
            return row;
        }

        public void setRow(CTTrPr row) {
            this.row = row;
        }

        public CTTcPr getCell() {
            return cell;
        }

        public void setCell(CTTcPr cell) {
            this.cell = cell;
        }
    }

    public Header getHeader1() {
        return header1;
    }

    public void setHeader1(Header header1) {
        this.header1 = header1;
    }

    public Header getHeader2() {
        return header2;
    }

    public void setHeader2(Header header2) {
        this.header2 = header2;
    }

    public Header getHeader3() {
        return header3;
    }

    public void setHeader3(Header header3) {
        this.header3 = header3;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}



