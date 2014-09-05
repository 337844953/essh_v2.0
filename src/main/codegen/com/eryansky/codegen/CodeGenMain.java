package com.eryansky.codegen;

import com.eryansky.codegen.db.DataSource;
import com.eryansky.codegen.db.DbFactory;
import com.eryansky.codegen.vo.DbConfig;
import com.eryansky.codegen.vo.Table;
import com.eryansky.common.utils.mapper.JsonMapper;

import java.util.List;

public class CodeGenMain {

    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String URL = "jdbc:mysql://localhost:3306/essh_v2.0?useUnicode=true&characterEncoding=UTF-8"; // 数据库访问串
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    public static final String SCHEMA = "";


    public static void main(String[] args) {

        DbConfig dbConfig = new DbConfig(DRIVER,URL,USERNAME,PASSWORD);
        List<Table> tables = null;
        Builder builder = null;
        DataSource db = null;
        String t = "T_SYS_%";//表 通配"%"
        Table table = null;
        try {
            db = DbFactory.create(dbConfig);
            tables = db.getTables(t);
            System.out.println(JsonMapper.getInstance().toJson(tables));
            builder = new Builder(db,tables);
            builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
